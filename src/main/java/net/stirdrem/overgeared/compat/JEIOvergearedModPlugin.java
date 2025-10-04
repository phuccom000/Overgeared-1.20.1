package net.stirdrem.overgeared.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.*;
import net.stirdrem.overgeared.screen.*;
import net.stirdrem.overgeared.util.ModTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class JEIOvergearedModPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ForgingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new KnappingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FlintKnappingCategory(registration.getJeiHelpers().getGuiHelper()));
        RegistryAccess registryAccess = Minecraft.getInstance().getConnection().registryAccess();
        registration.addRecipeCategories(new StoneAnvilCategory(registration.getJeiHelpers().getGuiHelper(), registryAccess));
        registration.addRecipeCategories(new SteelAnvilCategory(registration.getJeiHelpers().getGuiHelper(), registryAccess));
        registration.addRecipeCategories(new FletchingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) return;
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<ForgingRecipe> allForgingRecipes = recipeManager.getAllRecipesFor(ForgingRecipe.Type.INSTANCE);

        // Filter stone and steel recipes
        List<ForgingRecipe> stoneTierRecipes = allForgingRecipes.stream()
                .filter(recipe -> recipe.getAnvilTier().equalsIgnoreCase(AnvilTier.STONE.getDisplayName()))
                .toList();

        List<ForgingRecipe> steelTierRecipes = allForgingRecipes.stream()
                .filter(recipe -> recipe.getAnvilTier().equalsIgnoreCase(AnvilTier.IRON.getDisplayName()))
                .toList();
        List<ForgingRecipe> ATierRecipes = allForgingRecipes.stream()
                .filter(recipe -> recipe.getAnvilTier().equalsIgnoreCase(AnvilTier.ABOVE_A.getDisplayName()))
                .toList();
        List<ForgingRecipe> BTierRecipes = allForgingRecipes.stream()
                .filter(recipe -> recipe.getAnvilTier().equalsIgnoreCase(AnvilTier.ABOVE_B.getDisplayName()))
                .toList();

        // Add only stone-tier recipes to Stone Forging JEI category
        //registration.addRecipes(ForgingRecipeCategory.FORGING_RECIPE_TYPE, stoneTierRecipes);

        // Add steel-tier AND stone-tier to Steel Forging JEI category
        List<ForgingRecipe> combinedSteelCategory = new java.util.ArrayList<>();
        combinedSteelCategory.addAll(stoneTierRecipes);
        combinedSteelCategory.addAll(steelTierRecipes);
        combinedSteelCategory.addAll(ATierRecipes);
        combinedSteelCategory.addAll(BTierRecipes);
        combinedSteelCategory.sort((a, b) -> {
            String catA = categorizeRecipe(a);
            String catB = categorizeRecipe(b);

            int priorityA = categoryPriority.getOrDefault(catA, 999);
            int priorityB = categoryPriority.getOrDefault(catB, 999);

            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB);
            }

            // Fallback: alphabetical by display name
            return a.getResultItem(null).getDisplayName().getString()
                    .compareToIgnoreCase(b.getResultItem(null).getDisplayName().getString());
        });
        registration.addRecipes(ForgingRecipeCategory.FORGING_RECIPE_TYPE, combinedSteelCategory);

        // Rock Knapping
        List<RockKnappingRecipe> knappingRecipes = recipeManager.getAllRecipesFor(RockKnappingRecipe.Type.INSTANCE);
        registration.addRecipes(KnappingRecipeCategory.KNAPPING_RECIPE_TYPE, knappingRecipes);

        registration.addRecipes(RecipeTypes.CRAFTING, BlueprintCloningRecipeMaker.createRecipes(registration.getJeiHelpers()));
        if (ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get())
            registration.addRecipes(RecipeTypes.BREWING, dragonBreathRecipe());

        List<ExplanationRecipe> recipes = List.of(
                new ExplanationRecipe(new ItemStack(ModItems.ROCK.get()))
                // Add more recipes as needed
        );
        registration.addRecipes(FlintKnappingCategory.FLINT_KNAPPING, recipes);

        List<ExplanationRecipe> StoneAnvilRecipes = List.of(
                new ExplanationRecipe(new ItemStack(ModBlocks.STONE_SMITHING_ANVIL.get()))
                // Add more recipes as needed
        );

        List<ExplanationRecipe> SteelAnvilRecipes = List.of(
                new ExplanationRecipe(new ItemStack(ModBlocks.SMITHING_ANVIL.get()))
                // Add more recipes as needed
        );
        registration.addRecipes(StoneAnvilCategory.STONE_ANVIL_GET, StoneAnvilRecipes);
        registration.addRecipes(SteelAnvilCategory.STEEL_ANVIL_GET, SteelAnvilRecipes);

        registration.addRecipes(RecipeTypes.ANVIL,
                OvergearedAnvilRecipes.getOvergearedAnvilRecipes(
                        registration.getVanillaRecipeFactory(),
                        registration.getIngredientManager()
                ).toList()
        );
        // Fletching
        if (ServerConfig.ENABLE_FLETCHING_RECIPES.get()) {
            List<FletchingRecipe> fletchingRecipes = recipeManager.getAllRecipesFor(FletchingRecipe.Type.INSTANCE);
            registration.addRecipes(FletchingCategory.FLETCHING_RECIPE_TYPE, fletchingRecipes);

            // Add dynamic "potion conversion" recipes
            List<FletchingRecipe> dynamicConversions = generatePotionConversions();
            if (ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get()) {
                registration.addRecipes(FletchingCategory.FLETCHING_RECIPE_TYPE, dynamicConversions);
            }
        }
    }

    private List<IJeiBrewingRecipe> dragonBreathRecipe() {
        List<IJeiBrewingRecipe> recipes = new ArrayList<>();

        // Create input potion (Thick Potion)
        ItemStack thickPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.THICK);

        // Create ingredient (Chorus Fruit)
        ItemStack chorusFruit = new ItemStack(Items.CHORUS_FRUIT);

        // Create output (Dragon's Breath)
        ItemStack dragonBreath = new ItemStack(Items.DRAGON_BREATH);

        ResourceLocation recipeId = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "dragon_breath_brewing");

        // Create JEI brewing recipe
        recipes.add(new JeiBetterBrewingRecipe(
                List.of(thickPotion),    // Input potions
                List.of(chorusFruit),   // Ingredients
                dragonBreath,            // Output
                recipeId
        ));

        return recipes;
    }

    private List<FletchingRecipe> generatePotionConversions() {
        List<FletchingRecipe> list = new ArrayList<>();

        // Upgradeable arrow types
        ItemStack[] arrowTypes;
        if (ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get())
            arrowTypes = new ItemStack[]{
                    new ItemStack(Items.ARROW),
                    new ItemStack(ModItems.IRON_UPGRADE_ARROW.get()),
                    new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get()),
                    new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get())
            };
        else arrowTypes = new ItemStack[]{
                new ItemStack(Items.ARROW)
        };
        int idCounter = 0;

        // --- Generate tipped conversions ---
        for (ItemStack arrow : arrowTypes) {
            for (Potion potion : ForgeRegistries.POTIONS) {
                if (potion != Potions.EMPTY) {
                    // Potion input stack
                    ItemStack potionStack = new ItemStack(Items.POTION);
                    PotionUtils.setPotion(potionStack, potion);

                    // Output stack
                    ItemStack output = arrow.is(Items.ARROW) ? new ItemStack(Items.TIPPED_ARROW) : arrow.copy();
                    PotionUtils.setPotion(output, potion);

                    list.add(new FletchingRecipe(
                            new ResourceLocation(OvergearedMod.MOD_ID, "lingering_conv_" + (idCounter++)),
                            Ingredient.EMPTY,             // shaft
                            Ingredient.of(arrow),        // tip
                            Ingredient.EMPTY,             // feather
                            Ingredient.of(potionStack),
                            output,              // normal result
                            ItemStack.EMPTY,              // tipped result
                            ItemStack.EMPTY,                       // lingering result
                            "Potion",                     // tippedTag (not used)
                            "LingeringPotion"              // lingeringTag
                    ));
                }
            }
        }

        // --- Generate lingering conversions ---
        for (ItemStack arrow : arrowTypes) {
            for (Potion potion : ForgeRegistries.POTIONS) {
                if (potion != Potions.EMPTY) {
                    // Potion input stack
                    ItemStack potionStack = new ItemStack(Items.LINGERING_POTION);
                    PotionUtils.setPotion(potionStack, potion);

                    // Output stack
                    ItemStack output;
                    if (arrow.is(Items.ARROW)) {
                        output = new ItemStack(ModItems.LINGERING_ARROW.get());
                        PotionUtils.setPotion(output, potion);
                    } else {
                        output = arrow.copy();
                        setLingeringPotion(output, potion);
                    }

                    list.add(new FletchingRecipe(
                            new ResourceLocation(OvergearedMod.MOD_ID, "lingering_conv_" + (idCounter++)),
                            Ingredient.EMPTY,             // shaft
                            Ingredient.of(arrow),        // tip
                            Ingredient.EMPTY,             // feather
                            Ingredient.of(potionStack),
                            output,              // normal result
                            ItemStack.EMPTY,              // tipped result
                            ItemStack.EMPTY,                       // lingering result
                            "Potion",                     // tippedTag (not used)
                            "LingeringPotion"              // lingeringTag
                    ));
                }
            }
        }

        return list;
    }

    public static ItemStack setLingeringPotion(ItemStack pStack, Potion pPotion) {
        ResourceLocation resourcelocation = BuiltInRegistries.POTION.getKey(pPotion);
        if (pPotion == Potions.EMPTY) {
            pStack.removeTagKey("LingeringPotion");
        } else {
            pStack.getOrCreateTag().putString("LingeringPotion", resourcelocation.toString());
        }

        return pStack;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(SteelSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(FletchingStationScreen.class, 90, 35, 22, 15,
                FletchingCategory.FLETCHING_RECIPE_TYPE);

        registration.addRecipeClickArea(TierASmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(TierBSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(StoneSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(RockKnappingScreen.class, 90, 35, 22, 15,
                KnappingRecipeCategory.KNAPPING_RECIPE_TYPE);


    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(ModItems.LINGERING_ARROW.get());
        registration.useNbtForSubtypes(ModItems.IRON_UPGRADE_ARROW.get());
        registration.useNbtForSubtypes(ModItems.STEEL_UPGRADE_ARROW.get());
        registration.useNbtForSubtypes(ModItems.DIAMOND_UPGRADE_ARROW.get());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.STONE_SMITHING_ANVIL.get()), // or your custom source block
                ForgingRecipeCategory.FORGING_RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.SMITHING_ANVIL.get()), // or your custom source block
                ForgingRecipeCategory.FORGING_RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModItems.ROCK.get()), // or your custom source block
                KnappingRecipeCategory.KNAPPING_RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.TIER_A_SMITHING_ANVIL.get()), // or your custom source block
                ForgingRecipeCategory.FORGING_RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.TIER_B_SMITHING_ANVIL.get()), // or your custom source block
                ForgingRecipeCategory.FORGING_RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(Blocks.FLETCHING_TABLE), // or your custom source block
                FletchingCategory.FLETCHING_RECIPE_TYPE
        );
    }

    Map<String, Integer> categoryPriority = Map.of(
            "tool_head", 0,
            "tools", 1,
            "armor", 2,
            "plate", 3,
            "misc", 4
    );

    private static String categorizeRecipe(ForgingRecipe recipe) {
        ItemStack output = recipe.getResultItem(null);
        if (output.is(Tags.Items.ARMORS)) return "armor";
        if (output.is(ModTags.Items.TOOL_PARTS)) return "tool_head";
        if (output.is(Tags.Items.TOOLS)) return "tools";
        if (output.is(ModItems.IRON_PLATE.get()) || output.is(ModItems.STEEL_PLATE.get()) || output.is(ModItems.COPPER_PLATE.get()))
            return "plate";
        return "misc";
    }

    private List<ItemStack> getTippedArrowPotions() {
        List<ItemStack> potions = new ArrayList<>();
        // Example: Add all potion types (you can customize this)
        for (Potion potion : ForgeRegistries.POTIONS) {
            if (potion != Potions.EMPTY) {
                potions.add(PotionUtils.setPotion(new ItemStack(Items.POTION), potion));
            }
        }
        return potions;
    }

    // Helper method to get lingering potions (for lingering arrows)
    private List<ItemStack> getLingeringPotions() {
        List<ItemStack> potions = new ArrayList<>();
        // Example: Add all lingering potion types
        for (Potion potion : ForgeRegistries.POTIONS) {
            if (potion != Potions.EMPTY) {
                potions.add(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion));
            }
        }
        return potions;
    }
}

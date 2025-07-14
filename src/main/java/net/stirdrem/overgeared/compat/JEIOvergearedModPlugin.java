package net.stirdrem.overgeared.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.Tags;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ExplanationRecipe;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;
import net.stirdrem.overgeared.screen.RockKnappingScreen;
import net.stirdrem.overgeared.screen.SteelSmithingAnvilScreen;
import net.stirdrem.overgeared.screen.StoneSmithingAnvilScreen;
import net.stirdrem.overgeared.util.ModTags;

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
                .filter(recipe -> recipe.getAnvilTier().equalsIgnoreCase(AnvilTier.STEEL.getDisplayName()))
                .toList();

        // Add only stone-tier recipes to Stone Forging JEI category
        //registration.addRecipes(ForgingRecipeCategory.FORGING_RECIPE_TYPE, stoneTierRecipes);

        // Add steel-tier AND stone-tier to Steel Forging JEI category
        List<ForgingRecipe> combinedSteelCategory = new java.util.ArrayList<>();
        combinedSteelCategory.addAll(stoneTierRecipes);
        combinedSteelCategory.addAll(steelTierRecipes);
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

        List<ExplanationRecipe> recipes = List.of(
                new ExplanationRecipe(new ItemStack(ModItems.ROCK.get()))
                // Add more recipes as needed
        );
        registration.addRecipes(FlintKnappingCategory.FLINT_KNAPPING, recipes);

        List<ExplanationRecipe> StoneAnvilRecipes = List.of(
                new ExplanationRecipe(new ItemStack(ModBlocks.STONE_SMITHING_ANVIL.get()))
                // Add more recipes as needed
        );
        registration.addRecipes(StoneAnvilCategory.STONE_ANVIL_GET, StoneAnvilRecipes);
    }


    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(SteelSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(StoneSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(RockKnappingScreen.class, 90, 35, 22, 15,
                KnappingRecipeCategory.KNAPPING_RECIPE_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.SMITHING_ANVIL.get()), // or your custom source block
                ForgingRecipeCategory.FORGING_RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModItems.ROCK.get()), // or your custom source block
                KnappingRecipeCategory.KNAPPING_RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.STONE_SMITHING_ANVIL.get()), // or your custom source block
                ForgingRecipeCategory.FORGING_RECIPE_TYPE
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

}

package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;

import dev.emi.emi.api.stack.EmiStack;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.Tags;

import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;

import java.util.*;

/**
 * EMI integration for displaying Forging recipes.
 * This class is only loaded when EMI is present (handled by @EmiEntrypoint).
 */
@EmiEntrypoint
public class OvergearedEmiPlugin implements EmiPlugin {
    
    private static final ResourceLocation TEXTURE = OvergearedMod.loc("textures/gui/smithing_anvil_jei.png");
    private static final ResourceLocation KNAPPING_TEXTURE = OvergearedMod.loc("textures/gui/rock_knapping_gui.png");
    
    public static final EmiStack WORKSTATION = EmiStack.of(ModBlocks.SMITHING_ANVIL.get());
    
    public static final EmiRecipeCategory FORGING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("forging"),
            WORKSTATION,
            new EmiTexture(TEXTURE, 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.smithing_anvil");
        }
    };
    
    public static final EmiStack KNAPPING_WORKSTATION = EmiStack.of(ModItems.ROCK.get());
    public static final EmiRecipeCategory KNAPPING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("rock_knapping"),
            KNAPPING_WORKSTATION,
            new EmiTexture(KNAPPING_TEXTURE, 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.rock_knapping");
        }
    };
    
    // Priority for sorting recipes by category
    private static final Map<String, Integer> CATEGORY_PRIORITY = Map.of(
            "tool_head", 0,
            "tools", 1,
            "armor", 2,
            "plate", 3,
            "misc", 4
    );
    
    @Override
    public void register(EmiRegistry registry) {
        OvergearedMod.LOGGER.info("Registering EMI plugin for Overgeared recipes.");
        
        // Register the forging category
        registry.addCategory(FORGING_CATEGORY);
        
        // Register all smithing anvil blocks as workstations (ordered by tier: Stone -> Iron -> A -> B)
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.STONE_SMITHING_ANVIL.get()));
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.SMITHING_ANVIL.get()));
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.TIER_A_SMITHING_ANVIL.get()));
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.TIER_B_SMITHING_ANVIL.get()));
        
        // Register Knapping
        registry.addCategory(KNAPPING_CATEGORY);
        registry.addWorkstation(KNAPPING_CATEGORY, KNAPPING_WORKSTATION);
        
        for (RecipeHolder<RockKnappingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.KNAPPING.get())) {
            registry.addRecipe(new KnappingEmiRecipe(holder));
        }
        
        // Collect and sort all forging recipes
        List<RecipeHolder<ForgingRecipe>> allRecipes = new ArrayList<>(
                registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.FORGING.get())
        );
        
        // Sort recipes by category priority, then alphabetically by output name
        allRecipes.sort((a, b) -> {
            String catA = categorizeRecipe(a.value());
            String catB = categorizeRecipe(b.value());
            
            int priorityA = CATEGORY_PRIORITY.getOrDefault(catA, 999);
            int priorityB = CATEGORY_PRIORITY.getOrDefault(catB, 999);
            
            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB);
            }
            
            // Fallback: alphabetical by display name
            return a.value().getResultItem(null).getDisplayName().getString()
                    .compareToIgnoreCase(b.value().getResultItem(null).getDisplayName().getString());
        });
        
        // Add sorted recipes
        for (RecipeHolder<ForgingRecipe> holder : allRecipes) {
            registry.addRecipe(new ForgingEmiRecipe(holder));
        }
        
        OvergearedMod.LOGGER.info("EMI plugin registered successfully.");
    }
    
    /**
     * Categorize a recipe for sorting purposes.
     */
    private static String categorizeRecipe(ForgingRecipe recipe) {
        ItemStack output = recipe.getResultItem(null);
        if (output.is(Tags.Items.ARMORS)) return "armor";
        if (output.is(ModTags.Items.TOOL_PARTS)) return "tool_head";
        if (output.is(Tags.Items.TOOLS)) return "tools";
        if (output.is(ModItems.IRON_PLATE.get()) || output.is(ModItems.STEEL_PLATE.get()) || output.is(ModItems.COPPER_PLATE.get())) {
            return "plate";
        }
        return "misc";
    }
    
}

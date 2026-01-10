package net.stirdrem.overgeared.compat.polymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.common.capability.PolymorphCapabilities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.StoneSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.TierBSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.compat.polymorph.client.PolymorphClient;
import net.stirdrem.overgeared.compat.polymorph.client.PolymorphClientHelper;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilMenu;

import java.util.List;
import java.util.Optional;

/**
 * UNSAFE: Contains actual Polymorph API integration code.
 * This class imports Polymorph classes and should only be accessed via reflection
 * from the safe {@link Polymorph} wrapper class.
 */
public class PolymorphIntegration {

    /**
     * Registers block entities and menus with Polymorph API.
     */
    public static void init() {
        OvergearedMod.LOGGER.info("Polymorph detected, enabling Overgeared compat.");
        PolymorphApi api = PolymorphApi.getInstance();
        
        // Register all smithing anvil block entity types
        List.of(
            StoneSmithingAnvilBlockEntity.class,
            SteelSmithingAnvilBlockEntity.class,
            TierASmithingAnvilBlockEntity.class,
            TierBSmithingAnvilBlockEntity.class
        ).forEach(clazz -> api.registerBlockEntity(clazz, 
            be -> be instanceof AbstractSmithingAnvilBlockEntity anvil 
                ? new SmithingAnvilRecipeData(anvil) : null));
        
        // Register menu to block entity mapping
        api.registerMenu(containerMenu -> {
            if (containerMenu instanceof AbstractSmithingAnvilMenu menu) {
                return menu.blockEntity;
            }
            return null;
        });
        
        OvergearedMod.LOGGER.info("Polymorph compat initialized successfully.");
    }
    
    /**
     * Client-side initialization for registering widgets.
     */
    public static void initClient() {
        PolymorphClient.init();
    }
    
    /**
     * Gets the recipe selected by Polymorph for the given smithing anvil.
     */
    public static Optional<RecipeHolder<ForgingRecipe>> getSelectedRecipe(
            AbstractSmithingAnvilBlockEntity blockEntity, RecipeInput recipeInput) {
        
        Level level = blockEntity.getLevel();
        if (level == null) {
            return Optional.empty();
        }
        
        IBlockEntityRecipeData recipeData = PolymorphCapabilities.getRecipeData(blockEntity);
        if (recipeData == null) {
            OvergearedMod.LOGGER.debug("Polymorph: No recipe data found for block entity");
            return Optional.empty();
        }
        
        // Find all matching recipes
        List<RecipeHolder<ForgingRecipe>> allMatchingRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.FORGING.get())
                .stream()
                .filter(holder -> holder.value().matches(recipeInput, level))
                .toList();
        
        OvergearedMod.LOGGER.debug("Polymorph: Found {} matching forging recipes", allMatchingRecipes.size());
        
        if (allMatchingRecipes.isEmpty()) {
            return Optional.empty();
        }
        
        // If only one recipe matches, no conflict - just return it
        if (allMatchingRecipes.size() == 1) {
            return Optional.of(allMatchingRecipes.get(0));
        }

        // Multiple recipes match - use Polymorph's getRecipe to handle selection
        RecipeHolder<ForgingRecipe> selected = recipeData.getRecipe(
                ModRecipeTypes.FORGING.get(),
                recipeInput,
                level,
                allMatchingRecipes
        );

        if (selected != null) {
            return Optional.of(selected);
        }
        
        // Fallback to first match if something went wrong
        return Optional.of(allMatchingRecipes.getFirst());
    }
    
    /**
     * Gets the selected output from Polymorph's client widget.
     * Delegates to PolymorphClientHelper (no reflection needed here since we're already unsafe).
     */
    public static Optional<ItemStack> getSelectedOutput() {
        return PolymorphClientHelper.getSelectedOutput();
    }
}


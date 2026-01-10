package net.stirdrem.overgeared.compat.polymorph;

import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

import java.util.List;

/**
 * Recipe data implementation for Polymorph compatibility.
 * Provides the crafting grid contents to Polymorph for recipe conflict resolution.
 * Overrides recipe lookup to find all matching forging recipes for conflict detection.
 */
public class SmithingAnvilRecipeData extends AbstractBlockEntityRecipeData<AbstractSmithingAnvilBlockEntity> {

    public SmithingAnvilRecipeData(AbstractSmithingAnvilBlockEntity owner) {
        super(owner);
    }

    @Override
    protected NonNullList<ItemStack> getInput() {
        AbstractSmithingAnvilBlockEntity blockEntity = this.getOwner();
        var itemHandler = blockEntity.getItemHandler();

        if (itemHandler != null) {
            // Create a list for the 9 crafting slots (0-8) + blueprint slot (9)
            NonNullList<ItemStack> stacks = NonNullList.withSize(10, ItemStack.EMPTY);

            for (int i = 0; i < 10; i++) {
                stacks.set(i, itemHandler.getStackInSlot(i));
            }
            return stacks;
        }
        return NonNullList.create();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends RecipeInput, T extends Recipe<I>> RecipeHolder<T> getRecipe(
            RecipeType<T> type, I recipeInput, Level level, List<RecipeHolder<T>> recipesListIn) {
        
        // Only handle forging recipes
        if (type == ModRecipeTypes.FORGING.get()) {
            // Find ALL matching forging recipes (not just the best one)
            List<RecipeHolder<ForgingRecipe>> allMatchingRecipes = level.getRecipeManager()
                    .getAllRecipesFor(ModRecipeTypes.FORGING.get())
                    .stream()
                    .filter(holder -> holder.value().matches(recipeInput, level))
                    .toList();
            
            // Pass to parent method which will handle conflict detection and selection
            return super.getRecipe(type, recipeInput, level, (List<RecipeHolder<T>>) (List<?>) allMatchingRecipes);
        }
        
        // For other recipe types, use default behavior
        return super.getRecipe(type, recipeInput, level, recipesListIn);
    }
}

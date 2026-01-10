package net.stirdrem.overgeared.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.recipe.CoolingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Utility class for item-related helper methods.
 */
public final class ItemUtils {

    private ItemUtils() {
        // Utility class - no instantiation
    }

    /**
     * Gets the cooled Item for a heated Item.
     *
     * @param heatedItem The heated item
     * @param level      The level for recipe lookup
     * @return The cooled item, or null if no recipe found
     */
    @Nullable
    public static Item getCooledItem(@Nullable Item heatedItem, @NotNull Level level) {
        if (heatedItem == null || level == null) return null;
        ItemStack result = getCooledItem(new ItemStack(heatedItem), level);
        return result.isEmpty() ? heatedItem : result.getItem();
    }

    /**
     * Converts a heated item to its cooled version using CoolingRecipe.
     *
     * @param stack The heated item stack
     * @param level The level for recipe lookup
     * @return The cooled item stack, or empty if no recipe found
     */
    @NotNull
    public static ItemStack getCooledItem(@NotNull ItemStack stack, @NotNull Level level) {
        if (stack.isEmpty() || level == null) return ItemStack.EMPTY;

        // Create SingleRecipeInput for recipe matching (1.21 API)
        SingleRecipeInput input = new SingleRecipeInput(stack.copy());

        // Find the first matching CoolingRecipe (1.21 returns RecipeHolder<T>)
        Optional<RecipeHolder<CoolingRecipe>> recipeOpt = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.COOLING_RECIPE.get())
                .stream()
                .filter(holder -> holder.value().matches(input, level))
                .findFirst();

        if (recipeOpt.isEmpty()) {
            return ItemStack.EMPTY; // no cooling recipe found
        }

        // Return the result item from the recipe
        CoolingRecipe recipe = recipeOpt.get().value();
        ItemStack result = recipe.getResultItem(level.registryAccess());
        return result.isEmpty() ? ItemStack.EMPTY : result.copy();
    }

    /**
     * Copies all data components from source to target, except heated-related components.
     * This preserves components added by other mods for better compatibility.
     *
     * @param source The source item stack to copy components from
     * @param target The target item stack to copy components to
     */
    public static void copyComponentsExceptHeated(@NotNull ItemStack source, @NotNull ItemStack target) {
        // Apply all component changes from source
        target.applyComponents(source.getComponentsPatch());
        // Remove heated-related components (we're cooling the item)
        target.remove(ModComponents.HEATED_COMPONENT);
        target.remove(ModComponents.HEATED_TIME);
    }
}

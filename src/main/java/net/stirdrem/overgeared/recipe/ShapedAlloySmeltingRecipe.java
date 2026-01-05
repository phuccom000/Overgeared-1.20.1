package net.stirdrem.overgeared.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public class ShapedAlloySmeltingRecipe implements Recipe<RecipeInput>, IAlloyRecipe {
    private final String group;
    private final CraftingBookCategory category;
    private final NonNullList<Ingredient> pattern; // size 4
    private final ItemStack output;
    private final float experience;
    private final int cookingTime;

    public ShapedAlloySmeltingRecipe(String group, CraftingBookCategory category,
                                     NonNullList<Ingredient> pattern, ItemStack output,
                                     float experience, int cookingTime) {
        if (pattern.size() != 4)
            throw new IllegalArgumentException("Pattern for 2x2 alloy smelting must have exactly 4 ingredients");
        this.group = group;
        this.category = category;
        this.pattern = pattern;
        this.output = output;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public boolean matches(RecipeInput inv, Level level) {
        if (level.isClientSide) return false;

        for (int i = 0; i < 4; i++) {
            Ingredient ingredient = pattern.get(i);
            ItemStack stack = inv.getItem(i);
            if (!ingredient.test(stack)) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w >= 2 && h >= 2;
    }


    @Override
    public List<Ingredient> getIngredientsList() {
        return pattern;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    public ItemStack getResultItem() {
        return output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SHAPED_ALLOY_SMELTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SHAPED_ALLOY_SMELTING.get();
    }

    public String getGroup() {
        return group;
    }

    public CraftingBookCategory category() {
        return category;
    }

    public float getExperience() {
        return experience;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public NonNullList<Ingredient> getPattern() {
        return pattern;
    }

    public CraftingBookCategory getCraftingBookCategory() {
        return category;
    }
}

package net.stirdrem.overgeared.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface IAlloyRecipe {
    List<Ingredient> getIngredientsList();

    ItemStack getResultItem(HolderLookup.Provider provider);

    float getExperience();
}

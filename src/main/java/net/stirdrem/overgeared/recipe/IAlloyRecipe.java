package net.stirdrem.overgeared.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface IAlloyRecipe {
    List<Ingredient> getIngredientsList();

    ItemStack getResultItem(RegistryAccess registryAccess);

    float getExperience();
}

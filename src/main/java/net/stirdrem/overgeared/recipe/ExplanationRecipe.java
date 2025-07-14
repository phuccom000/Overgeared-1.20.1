package net.stirdrem.overgeared.recipe;

import net.minecraft.world.item.ItemStack;

public class ExplanationRecipe {

    private final ItemStack result;

    public ExplanationRecipe(ItemStack result) {
        this.result = result;
    }

    public ItemStack getResultItem() {
        return result.copy();
    }
}
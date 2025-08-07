package net.stirdrem.overgeared.recipe;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class JeiBetterBrewingRecipe implements IJeiBrewingRecipe {
    private final List<ItemStack> input;
    private final List<ItemStack> ingredient;
    private final ItemStack output;
    private final @Nullable ResourceLocation uid;

    public JeiBetterBrewingRecipe(List<ItemStack> input, List<ItemStack> ingredient, ItemStack output, @Nullable ResourceLocation uid) {
        this.input = List.copyOf(input);
        this.ingredient = List.copyOf(ingredient);
        this.output = output;
        this.uid = uid;
    }

    @Override
    public @Unmodifiable List<ItemStack> getPotionInputs() {
        return this.input;
    }

    @Override
    public @Unmodifiable List<ItemStack> getIngredients() {
        return this.ingredient;
    }

    @Override
    public ItemStack getPotionOutput() {
        return this.output;
    }

    @Override
    public int getBrewingSteps() {
        return 2;
    }

    @Override
    public @Nullable ResourceLocation getUid() {
        return this.uid;
    }
}
package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CoolingRecipe implements Recipe<SingleRecipeInput> {
    private final Ingredient ingredient;
    private final ItemStack result;

    public CoolingRecipe(Ingredient ingredient, ItemStack result) {
        this.ingredient = ingredient;
        this.result = result;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return null;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.COOLING_RECIPE.get();
    }

    public static class Serializer implements RecipeSerializer<CoolingRecipe> {
        public static final MapCodec<CoolingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC.fieldOf("input").forGetter(r -> r.ingredient),
                ItemStack.CODEC.fieldOf("output").forGetter(r -> r.result)
        ).apply(i, CoolingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CoolingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
                ItemStack.STREAM_CODEC, r -> r.result,
                CoolingRecipe::new
        );

        @Override
        public MapCodec<CoolingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CoolingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

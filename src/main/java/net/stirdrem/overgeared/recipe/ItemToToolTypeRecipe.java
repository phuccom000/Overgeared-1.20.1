package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public record ItemToToolTypeRecipe(Ingredient input, String toolType) implements Recipe<SingleRecipeInput> {

    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level level) {
        return input.test(recipeInput.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput recipeInput, HolderLookup.Provider provider) {
        return ItemStack.EMPTY; // purely data-driven recipe
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ITEM_TO_TOOLTYPE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ITEM_TO_TOOLTYPE.get();
    }

    public List<ItemStack> getItems() {
        return List.of(input.getItems());
    }

    // ----------------------------------------------------
    // Serializer
    // ----------------------------------------------------
    public static class Serializer implements RecipeSerializer<ItemToToolTypeRecipe> {
        public static final MapCodec<ItemToToolTypeRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC.fieldOf("item").forGetter(r -> r.input),
                Codec.STRING.fieldOf("tooltype").forGetter(r -> r.toolType)
        ).apply(i, ItemToToolTypeRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemToToolTypeRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
                ByteBufCodecs.STRING_UTF8, r -> r.toolType,
                ItemToToolTypeRecipe::new
        );

        @Override
        public MapCodec<ItemToToolTypeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ItemToToolTypeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

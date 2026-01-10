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

public class NBTKeepingBlastingRecipe extends BlastingRecipe {

    public NBTKeepingBlastingRecipe(String group, CookingBookCategory category,
                                    Ingredient ingredient, ItemStack result,
                                    float experience, int cookingTime) {
        super(group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        ItemStack inputStack = input.getItem(0);
        ItemStack output = this.result.copy();

        // Copy all data components from input to output (replaces NBT copying)
        output.applyComponents(inputStack.getComponents());

        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NBT_BLASTING.get();
    }

    public static class Serializer implements RecipeSerializer<NBTKeepingBlastingRecipe> {

        private static final MapCodec<NBTKeepingBlastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.getGroup()),
                        CookingBookCategory.CODEC.optionalFieldOf("category", CookingBookCategory.MISC)
                                .forGetter(r -> r.category()),
                        Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
                        ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                        Codec.FLOAT.optionalFieldOf("experience", 0.0f).forGetter(r -> r.getExperience()),
                        Codec.INT.optionalFieldOf("cookingtime", 100).forGetter(r -> r.getCookingTime())
                ).apply(instance, NBTKeepingBlastingRecipe::new)
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, NBTKeepingBlastingRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            // Encode
                            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.getGroup());
                            ByteBufCodecs.fromCodec(CookingBookCategory.CODEC).encode(buf, recipe.category());
                            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient);
                            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
                            buf.writeFloat(recipe.getExperience());
                            ByteBufCodecs.VAR_INT.encode(buf, recipe.getCookingTime());
                        },
                        buf -> {
                            // Decode
                            String group = ByteBufCodecs.STRING_UTF8.decode(buf);
                            CookingBookCategory category = ByteBufCodecs.fromCodec(CookingBookCategory.CODEC).decode(buf);
                            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                            float xp = buf.readFloat();
                            int cookTime = ByteBufCodecs.VAR_INT.decode(buf);

                            return new NBTKeepingBlastingRecipe(group, category, ingredient, result, xp, cookTime);
                        }
                );

        @Override
        public MapCodec<NBTKeepingBlastingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, NBTKeepingBlastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AlloySmeltingSerializer implements RecipeSerializer<AlloySmeltingRecipe> {
  public static final AlloySmeltingSerializer INSTANCE = new AlloySmeltingSerializer();

  @Override
  public MapCodec<AlloySmeltingRecipe> codec() {
    return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("group").forGetter(AlloySmeltingRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").forGetter(AlloySmeltingRecipe::getCraftingBookCategory),
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(AlloySmeltingRecipe::getIngredientsList),
            ItemStack.CODEC.fieldOf("output").forGetter(AlloySmeltingRecipe::getResultItem),
            Codec.FLOAT.fieldOf("experience").forGetter(AlloySmeltingRecipe::getExperience),
            Codec.INT.fieldOf("cookingtime").forGetter(AlloySmeltingRecipe::getCookingTime)
    ).apply(instance, AlloySmeltingRecipe::new));
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf, AlloySmeltingRecipe> streamCodec() {
    return StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            AlloySmeltingRecipe::getGroup,
            CraftingBookCategory.STREAM_CODEC,
            AlloySmeltingRecipe::getCraftingBookCategory,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
            AlloySmeltingRecipe::getIngredientsList,
            ItemStack.STREAM_CODEC,
            AlloySmeltingRecipe::getResultItem,
            ByteBufCodecs.FLOAT,
            AlloySmeltingRecipe::getExperience,
            ByteBufCodecs.INT,
            AlloySmeltingRecipe::getCookingTime,
            AlloySmeltingRecipe::new
    );
  }
}

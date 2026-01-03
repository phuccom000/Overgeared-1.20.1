package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public class ShapedAlloySmeltingSerializer implements RecipeSerializer<ShapedAlloySmeltingRecipe> {
  public static final ShapedAlloySmeltingSerializer INSTANCE = new ShapedAlloySmeltingSerializer();

  private static final Codec<NonNullList<Ingredient>> INGREDIENTS_CODEC =
          Ingredient.CODEC.listOf()
                  .flatXmap(
                          list -> list.size() == 4
                                  ? DataResult.success(NonNullList.of(Ingredient.EMPTY, list.toArray(Ingredient[]::new)))
                                  : DataResult.error(() -> "Shaped alloy smelting requires exactly 4 ingredients (2x2)"),
                          DataResult::success
                  );

  @Override
  public MapCodec<ShapedAlloySmeltingRecipe> codec() {
    return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("group").forGetter(ShapedAlloySmeltingRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").forGetter(ShapedAlloySmeltingRecipe::getCraftingBookCategory),
            INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(ShapedAlloySmeltingRecipe::getPattern),
            ItemStack.CODEC.fieldOf("output").forGetter(ShapedAlloySmeltingRecipe::getResultItem),
            Codec.FLOAT.fieldOf("experience").forGetter(ShapedAlloySmeltingRecipe::getExperience),
            Codec.INT.fieldOf("cookingtime").forGetter(ShapedAlloySmeltingRecipe::getCookingTime)
    ).apply(instance, ShapedAlloySmeltingRecipe::new));
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf, ShapedAlloySmeltingRecipe> streamCodec() {
    return StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ShapedAlloySmeltingRecipe::getGroup,
            CraftingBookCategory.STREAM_CODEC,
            ShapedAlloySmeltingRecipe::getCraftingBookCategory,
            Ingredient.CONTENTS_STREAM_CODEC
                    .apply(ByteBufCodecs.list())
                    .map(
                            list -> NonNullList.of(Ingredient.EMPTY, list.toArray(Ingredient[]::new)),
                            List::copyOf
                    ),
            ShapedAlloySmeltingRecipe::getPattern,
            ItemStack.STREAM_CODEC,
            ShapedAlloySmeltingRecipe::getResultItem,
            ByteBufCodecs.FLOAT,
            ShapedAlloySmeltingRecipe::getExperience,
            ByteBufCodecs.INT,
            ShapedAlloySmeltingRecipe::getCookingTime,
            ShapedAlloySmeltingRecipe::new
    );
  }
}

package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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

    public static class Serializer implements RecipeSerializer<ShapedAlloySmeltingRecipe> {
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
}

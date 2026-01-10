package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
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
        @Override
        public MapCodec<ShapedAlloySmeltingRecipe> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedAlloySmeltingRecipe::getGroup),
                    CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(ShapedAlloySmeltingRecipe::getCraftingBookCategory),
                    Codec.list(Codec.STRING).fieldOf("pattern").forGetter(r -> List.of("AA", "AA")),
                    Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).fieldOf("key").forGetter(r -> java.util.Map.of()),
                    ItemStack.CODEC.fieldOf("result").forGetter(ShapedAlloySmeltingRecipe::getResultItem),
                    Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(ShapedAlloySmeltingRecipe::getExperience),
                    Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(ShapedAlloySmeltingRecipe::getCookingTime)
            ).apply(instance, (group, category, pattern, key, result, exp, time) -> {
                // Parse the pattern using the key map
                if (pattern.size() != 2) {
                    throw new IllegalArgumentException("2x2 pattern must have exactly 2 rows");
                }
                
                NonNullList<Ingredient> ingredients = NonNullList.withSize(4, Ingredient.EMPTY);
                for (int y = 0; y < 2; y++) {
                    String row = pattern.get(y);
                    if (row.length() != 2) {
                        throw new IllegalArgumentException("Each row must have exactly 2 characters");
                    }
                    for (int x = 0; x < 2; x++) {
                        char c = row.charAt(x);
                        String keyStr = String.valueOf(c);
                        ingredients.set(y * 2 + x, key.getOrDefault(keyStr, Ingredient.EMPTY));
                    }
                }
                return new ShapedAlloySmeltingRecipe(group, category, ingredients, result, exp, time);
            }));
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

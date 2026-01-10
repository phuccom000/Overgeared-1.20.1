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
import java.util.Map;

public class ShapedNetherAlloySmeltingRecipe implements Recipe<RecipeInput>, INetherAlloyRecipe {
    private final String group;
    private final CraftingBookCategory category;
    private final NonNullList<Ingredient> pattern; // size 9
    private final ItemStack output;
    private final float experience;
    private final int cookingTime;

    public ShapedNetherAlloySmeltingRecipe(String group, CraftingBookCategory category,
                                           NonNullList<Ingredient> pattern, ItemStack output,
                                           float experience, int cookingTime) {
        if (pattern.size() != 9)
            throw new IllegalArgumentException("Pattern for 3x3 alloy smelting must have exactly 9 ingredients");
        this.group = group;
        this.category = category;
        this.pattern = pattern;
        this.output = output;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level level) {
        if (level.isClientSide) return false;

        for (int i = 0; i < 9; i++) {
            Ingredient ingredient = pattern.get(i);
            ItemStack stack = recipeInput.getItem(i);
            if (!ingredient.test(stack)) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w >= 3 && h >= 3;
    }

    @Override
    public List<Ingredient> getIngredientsList() {
        return pattern;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SHAPED_NETHER_ALLOY_SMELTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SHAPED_NETHER_ALLOY_SMELTING.get();
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

    public static class Type implements RecipeType<ShapedNetherAlloySmeltingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "shaped_nether_alloy_smelting";
    }

    // -----------------------------
    // Serializer
    // -----------------------------
    public static class Serializer implements RecipeSerializer<ShapedNetherAlloySmeltingRecipe> {
        public static final MapCodec<ShapedNetherAlloySmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedNetherAlloySmeltingRecipe::getGroup),
                        CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(ShapedNetherAlloySmeltingRecipe::category),
                        Codec.list(Codec.STRING).fieldOf("pattern").forGetter(r -> List.of("AAA", "AAA", "AAA")),
                        Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).fieldOf("key").forGetter(r -> Map.of()),
                        ItemStack.CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
                        Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(ShapedNetherAlloySmeltingRecipe::getExperience),
                        Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(ShapedNetherAlloySmeltingRecipe::getCookingTime)
                ).apply(instance, (group, category, pattern, key, result, exp, time) -> {
                    // Parse the pattern using the key map
                    if (pattern.size() != 3) {
                        throw new IllegalArgumentException("3x3 pattern must have exactly 3 rows");
                    }
                    
                    NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
                    for (int y = 0; y < 3; y++) {
                        String row = pattern.get(y);
                        if (row.length() != 3) {
                            throw new IllegalArgumentException("Each row must have exactly 3 characters");
                        }
                        for (int x = 0; x < 3; x++) {
                            char c = row.charAt(x);
                            String keyStr = String.valueOf(c);
                            ingredients.set(y * 3 + x, key.getOrDefault(keyStr, Ingredient.EMPTY));
                        }
                    }
                    return new ShapedNetherAlloySmeltingRecipe(group, category, ingredients, result, exp, time);
                })
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedNetherAlloySmeltingRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            // Encode
                            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.group);
                            buf.writeEnum(recipe.category);
                            for (Ingredient ingredient : recipe.pattern) {
                                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
                            }
                            ItemStack.STREAM_CODEC.encode(buf, recipe.output);
                            ByteBufCodecs.FLOAT.encode(buf, recipe.experience);
                            ByteBufCodecs.VAR_INT.encode(buf, recipe.cookingTime);
                        },
                        buf -> {
                            // Decode
                            String group = ByteBufCodecs.STRING_UTF8.decode(buf);
                            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
                            NonNullList<Ingredient> pattern = NonNullList.withSize(9, Ingredient.EMPTY);
                            for (int i = 0; i < 9; i++) {
                                pattern.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                            }
                            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
                            float experience = ByteBufCodecs.FLOAT.decode(buf);
                            int cookingTime = ByteBufCodecs.VAR_INT.decode(buf);
                            return new ShapedNetherAlloySmeltingRecipe(group, category, pattern, output, experience, cookingTime);
                        }
                );

        @Override
        public MapCodec<ShapedNetherAlloySmeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedNetherAlloySmeltingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

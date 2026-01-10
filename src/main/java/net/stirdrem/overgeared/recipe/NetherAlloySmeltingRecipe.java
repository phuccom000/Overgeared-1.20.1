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

import java.util.ArrayList;
import java.util.List;

//TODO: make this extend AlloySmeltingRecipe to cut down on duplicate code
public class NetherAlloySmeltingRecipe implements Recipe<RecipeInput>, IAlloyRecipe {
    private final String group;
    private final CraftingBookCategory category;
    private final List<Ingredient> ingredients;
    private final ItemStack result;
    private final float experience;
    private final int cookingTime;

    public NetherAlloySmeltingRecipe(String group, CraftingBookCategory category, List<Ingredient> ingredients, ItemStack result, float experience, int cookingTime) {
        this.group = group;
        this.category = category;
        this.ingredients = ingredients;
        this.result = result;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        if (level.isClientSide) return false;

        List<Ingredient> remainingIngredients = new ArrayList<>();
        List<ItemStack> remainingItems = new ArrayList<>();

        // Collect all recipe ingredients (skip empty ingredients if any)
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.isEmpty()) {
                remainingIngredients.add(ingredient);
            }
        }

        // Collect all non-empty items from the input slots
        for (int i = 0; i < 9; i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                remainingItems.add(stack);
            }
        }

        // Must have same number of non-empty items as non-empty ingredients
        if (remainingItems.size() != remainingIngredients.size()) {
            return false;
        }

        // Try to match every stack in inventory to an ingredient
        for (ItemStack stack : remainingItems) {
            boolean matched = false;

            for (int i = 0; i < remainingIngredients.size(); i++) {
                if (remainingIngredients.get(i).test(stack)) {
                    remainingIngredients.remove(i); // consume one ingredient
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false; // found a stack that doesn't match any remaining ingredient
            }
        }

        // All ingredients matched successfully
        return remainingIngredients.isEmpty();
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public List<Ingredient> getIngredientsList() {
        return ingredients;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return result;
    }

    @Override
    public float getExperience() {
        return experience;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public CraftingBookCategory getCraftingBookCategory() {
        return category;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.NETHER_ALLOY_SMELTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.NETHER_ALLOY_SMELTING.get();
    }

    public static class Serializer implements RecipeSerializer<NetherAlloySmeltingRecipe> {
        public static final MapCodec<NetherAlloySmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(r -> r.category),
                Ingredient.CODEC.listOf(0, 9).fieldOf("ingredients").forGetter(r -> r.ingredients),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(r -> r.experience),
                Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(r -> r.cookingTime)
        ).apply(i, NetherAlloySmeltingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, NetherAlloySmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, r -> r.group,
                CraftingBookCategory.STREAM_CODEC, r -> r.category,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(9)), r -> r.ingredients,
                ItemStack.STREAM_CODEC, r -> r.result,
                ByteBufCodecs.FLOAT, r -> r.experience,
                ByteBufCodecs.INT, r -> r.cookingTime,
                NetherAlloySmeltingRecipe::new
        );

        @Override
        public MapCodec<NetherAlloySmeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, NetherAlloySmeltingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

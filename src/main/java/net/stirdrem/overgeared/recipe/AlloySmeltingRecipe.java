package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class AlloySmeltingRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final String group;
    private final CraftingBookCategory category;
    private final List<Ingredient> inputs;
    private final ItemStack output;
    private final float experience;
    private final int cookingTime;

    public AlloySmeltingRecipe(ResourceLocation id, String group, CraftingBookCategory category, List<Ingredient> inputs, ItemStack output, float experience, int cookingTime) {
        this.id = id;
        this.group = group;
        this.category = category;
        this.inputs = inputs;
        this.output = output;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public boolean matches(SimpleContainer inv, Level level) {
        // Don't bother checking on client side for performance reasons
        if (level.isClientSide) return false;

        // Create a list of non-empty ingredient-item pairs to match
        List<Ingredient> remainingIngredients = new ArrayList<>();
        List<ItemStack> remainingItems = new ArrayList<>();

        // Collect all recipe ingredients (skip empty ingredients if any)
        for (Ingredient ingredient : inputs) {
            if (!ingredient.isEmpty()) {
                remainingIngredients.add(ingredient);
            }
        }

        // Collect all non-empty items from the input slots
        for (int i = 0; i < 4; i++) {
            ItemStack stack = inv.getItem(i);
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
    public ItemStack assemble(SimpleContainer container, net.minecraft.core.RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return output;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ALLOY_SMELTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ALLOY_SMELTING.get();
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

    public List<Ingredient> getIngredientsList() {
        return inputs;
    }

    // ---------------------------------------------------------------------------------------
    // Type & Serializer
    // ---------------------------------------------------------------------------------------
    public static class Type implements RecipeType<AlloySmeltingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "alloy_smelting";
    }

    public static class Serializer implements RecipeSerializer<AlloySmeltingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public AlloySmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = json.has("group") ? json.get("group").getAsString() : "";
            CraftingBookCategory category = json.has("category")
                    ? CraftingBookCategory.CODEC.byName(json.get("category").getAsString(), CraftingBookCategory.MISC)
                    : CraftingBookCategory.MISC;

            JsonArray ingredients = json.getAsJsonArray("ingredients");
            List<Ingredient> inputList = new ArrayList<>();
            for (int i = 0; i < ingredients.size(); i++) {
                inputList.add(Ingredient.fromJson(ingredients.get(i)));
            }

            ItemStack result = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("result"));
            float experience = json.has("experience") ? json.get("experience").getAsFloat() : 0.0F;
            int cookingTime = json.has("cookingtime") ? json.get("cookingtime").getAsInt() : 200;

            return new AlloySmeltingRecipe(id, group, category, inputList, result, experience, cookingTime);
        }

        @Override
        public AlloySmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);

            int count = buf.readVarInt();
            List<Ingredient> inputs = new ArrayList<>();
            for (int i = 0; i < count; i++) inputs.add(Ingredient.fromNetwork(buf));

            ItemStack result = buf.readItem();
            float experience = buf.readFloat();
            int cookingTime = buf.readVarInt();

            return new AlloySmeltingRecipe(id, group, category, inputs, result, experience, cookingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AlloySmeltingRecipe recipe) {
            buf.writeUtf(recipe.group);
            buf.writeEnum(recipe.category);
            buf.writeVarInt(recipe.inputs.size());
            recipe.inputs.forEach(i -> i.toNetwork(buf));
            buf.writeItem(recipe.output);
            buf.writeFloat(recipe.experience);
            buf.writeVarInt(recipe.cookingTime);
        }
    }
}

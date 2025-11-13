package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapedNetherAlloySmeltingRecipe implements Recipe<SimpleContainer>, INetherAlloyRecipe {
    private final ResourceLocation id;
    private final String group;
    private final CraftingBookCategory category;
    private final NonNullList<Ingredient> pattern; // size 9
    private final ItemStack output;
    private final float experience;
    private final int cookingTime;

    public ShapedNetherAlloySmeltingRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                           NonNullList<Ingredient> pattern, ItemStack output,
                                           float experience, int cookingTime) {
        if (pattern.size() != 9)
            throw new IllegalArgumentException("Pattern for 3x3 alloy smelting must have exactly 9 ingredients");
        this.id = id;
        this.group = group;
        this.category = category;
        this.pattern = pattern;
        this.output = output;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public boolean matches(SimpleContainer inv, Level level) {
        if (level.isClientSide) return false;

        for (int i = 0; i < 9; i++) {
            Ingredient ingredient = pattern.get(i);
            ItemStack stack = inv.getItem(i);
            if (!ingredient.test(stack)) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(SimpleContainer inv, net.minecraft.core.RegistryAccess registryAccess) {
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
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SHAPED_NETHER_ALLOY_SMELTING.get();
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
        public static final ShapedNetherAlloySmeltingRecipe.Type INSTANCE = new ShapedNetherAlloySmeltingRecipe.Type();
        public static final String ID = "shaped_nether_alloy_smelting";
    }

    // -----------------------------
    // Serializer
    // -----------------------------
    public static class Serializer implements RecipeSerializer<ShapedNetherAlloySmeltingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public ShapedNetherAlloySmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = json.has("group") ? json.get("group").getAsString() : "";
            CraftingBookCategory category = json.has("category")
                    ? CraftingBookCategory.CODEC.byName(json.get("category").getAsString(), CraftingBookCategory.MISC)
                    : CraftingBookCategory.MISC;

            JsonArray patternArray = json.getAsJsonArray("pattern");
            if (patternArray.size() != 3)
                throw new JsonSyntaxException("3x3 alloy smelting requires exactly 3 rows in the pattern");

            JsonObject keyJson = json.getAsJsonObject("key");
            Map<Character, Ingredient> keyMap = new HashMap<>();
            for (var entry : keyJson.entrySet()) {
                if (entry.getKey().length() != 1) throw new JsonSyntaxException("Invalid key: " + entry.getKey());
                keyMap.put(entry.getKey().charAt(0), Ingredient.fromJson(entry.getValue()));
            }

            NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);
            for (int y = 0; y < 3; y++) {
                String row = patternArray.get(y).getAsString();
                if (row.length() != 3) throw new JsonSyntaxException("Each row in 3x3 pattern must have 3 characters");
                for (int x = 0; x < 3; x++) {
                    char c = row.charAt(x);
                    ingredients.set(y * 3 + x, keyMap.getOrDefault(c, Ingredient.EMPTY));
                }
            }

            ItemStack output = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("result"));
            float experience = json.has("experience") ? json.get("experience").getAsFloat() : 0.0F;
            int cookingTime = json.has("cookingtime") ? json.get("cookingtime").getAsInt() : 200;

            return new ShapedNetherAlloySmeltingRecipe(id, group, category, ingredients, output, experience, cookingTime);
        }

        @Override
        public ShapedNetherAlloySmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);

            NonNullList<Ingredient> pattern = NonNullList.withSize(9, Ingredient.EMPTY);
            for (int i = 0; i < 9; i++) {
                pattern.set(i, Ingredient.fromNetwork(buf));
            }

            ItemStack output = buf.readItem();
            float experience = buf.readFloat();
            int cookingTime = buf.readVarInt();

            return new ShapedNetherAlloySmeltingRecipe(id, group, category, pattern, output, experience, cookingTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ShapedNetherAlloySmeltingRecipe recipe) {
            buf.writeUtf(recipe.group);
            buf.writeEnum(recipe.category);
            recipe.pattern.forEach(i -> i.toNetwork(buf));
            buf.writeItem(recipe.output);
            buf.writeFloat(recipe.experience);
            buf.writeVarInt(recipe.cookingTime);
        }
    }
}

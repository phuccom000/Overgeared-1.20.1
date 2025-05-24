package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.HashMap;
import java.util.Map;

public class ForgingRecipe2 implements Recipe<SimpleContainer> {
    private final NonNullList<Ingredient> inputItems;
    private final ItemStack output;
    private final ResourceLocation id;
    private final int hammeringRequired;
    //private static final int hammering = 5;
    private final int patternWidth;
    private final int patternHeight;

    public ForgingRecipe2(NonNullList<Ingredient> inputItems, int hammeringRequired, ItemStack output, ResourceLocation id, int patternWidth, int patternHeight) {
        this.inputItems = inputItems;
        this.output = output;
        this.id = id;
        this.hammeringRequired = hammeringRequired;
        this.patternWidth = patternWidth;
        this.patternHeight = patternHeight;
    }


    @Override
    public boolean matches(SimpleContainer container, Level level) {
        if (level.isClientSide()) return false;

        for (int yOffset = 0; yOffset <= 3 - patternHeight; yOffset++) {
            for (int xOffset = 0; xOffset <= 3 - patternWidth; xOffset++) {
                if (matchesPattern(container, xOffset, yOffset)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesPattern(SimpleContainer container, int xOffset, int yOffset) {
        for (int row = 0; row < patternHeight; row++) {
            for (int col = 0; col < patternWidth; col++) {
                int containerIndex = (row + yOffset) * 3 + (col + xOffset);
                Ingredient ingredient = inputItems.get(row * patternWidth + col);
                if (!ingredient.test(container.getItem(containerIndex))) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output.copy();
    }


    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<ForgingRecipe2> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "forging";
    }

    public int getHammeringRequired() {
        return hammeringRequired;
    }

    public static class Serializer implements RecipeSerializer<ForgingRecipe2> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "forging");

        @Override
        public ForgingRecipe2 fromJson(ResourceLocation recipeId, JsonObject json) {
            int hammering = GsonHelper.getAsInt(json, "hammering", 1);
            JsonObject key = GsonHelper.getAsJsonObject(json, "key");
            Map<Character, Ingredient> keyMap = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : key.entrySet()) {
                if (entry.getKey().length() != 1) {
                    throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is not a single character.");
                }
                keyMap.put(entry.getKey().charAt(0), Ingredient.fromJson(entry.getValue()));
            }

            JsonArray patternArray = GsonHelper.getAsJsonArray(json, "pattern");
            int patternHeight = patternArray.size();
            int patternWidth = patternArray.get(0).getAsString().length();

            NonNullList<Ingredient> inputs = NonNullList.withSize(patternWidth * patternHeight, Ingredient.EMPTY);
            for (int row = 0; row < patternHeight; row++) {
                String line = patternArray.get(row).getAsString();
                for (int col = 0; col < patternWidth; col++) {
                    char symbol = line.charAt(col);
                    Ingredient ingredient = keyMap.getOrDefault(symbol, Ingredient.EMPTY);
                    inputs.set(row * patternWidth + col, ingredient);
                }
            }

            JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
            ItemStack output = ShapedRecipe.itemStackFromJson(resultJson);

            return new ForgingRecipe2(inputs, hammering, output, recipeId, patternWidth, patternHeight);
        }


        /*@Override
        public ForgingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(10, Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            return new ForgingRecipe(inputs, hammering, output, pRecipeId);
        }*/

        /*@Override
        public @Nullable ForgingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(pBuffer));
            }

            ItemStack output = pBuffer.readItem();
            int hammering = pBuffer.readVarInt();
            return new ForgingRecipe(inputs, hammering, output, pRecipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ForgingRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.inputItems.size());

            for (Ingredient ingredient : pRecipe.getIngredients()) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeItemStack(pRecipe.getResultItem(null), false);
            pBuffer.writeVarInt(pRecipe.hammeringRequired);
        }*/

        @Override
        public ForgingRecipe2 fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            int size = buffer.readVarInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }
            ItemStack output = buffer.readItem();
            int hammering = buffer.readVarInt();
            return new ForgingRecipe2(inputs, hammering, output, id, width, height);
        }


        @Override
        public void toNetwork(FriendlyByteBuf buffer, ForgingRecipe2 recipe) {
            buffer.writeVarInt(recipe.patternWidth);
            buffer.writeVarInt(recipe.patternHeight);
            buffer.writeVarInt(recipe.inputItems.size());
            for (Ingredient ingredient : recipe.inputItems) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.hammeringRequired);
        }


    }
}
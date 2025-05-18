package net.stirdrem.overgearedmod.recipe;

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
import net.stirdrem.overgearedmod.OvergearedMod;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ForgingRecipe implements Recipe<SimpleContainer> {
    private final NonNullList<Ingredient> inputItems;
    private final ItemStack output;
    private final ResourceLocation id;
    private final int hammeringRequired;
    //private static final int hammering = 5;

    public ForgingRecipe(NonNullList<Ingredient> inputItems, int hammeringRequired, ItemStack output, ResourceLocation id) {
        this.inputItems = inputItems;
        this.output = output;
        this.id = id;
        this.hammeringRequired = hammeringRequired;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        if (pLevel.isClientSide()) {
            return false;
        }

        // Convert 3x3 grid to linear slots
        for (int slot = 0; slot < 9; slot++) {
            if (!inputItems.get(slot).test(pContainer.getItem(slot))) {
                return false;
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

    public static class Type implements RecipeType<ForgingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "forging";
    }

    public int getHammeringRequired() {
        return hammeringRequired;
    }

    public static class Serializer implements RecipeSerializer<ForgingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "forging");

        @Override
        public ForgingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            // Parse hammering requirement, defaulting to 1 if not specified
            int hammering = GsonHelper.getAsInt(pSerializedRecipe, "hammering", 1);

            // Parse the 'key' object mapping symbols to ingredients
            JsonObject keyJson = GsonHelper.getAsJsonObject(pSerializedRecipe, "key");
            Map<Character, Ingredient> keyMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : keyJson.entrySet()) {
                String key = entry.getKey();
                if (key.length() != 1) {
                    throw new JsonSyntaxException("Invalid key entry: '" + key + "' is not a single character.");
                }
                keyMap.put(key.charAt(0), Ingredient.fromJson(entry.getValue()));
            }

            // Parse the 'pattern' array
            JsonArray patternJson = GsonHelper.getAsJsonArray(pSerializedRecipe, "pattern");
            int patternHeight = patternJson.size();
            if (patternHeight == 0 || patternHeight > 3) {
                throw new JsonSyntaxException("Invalid pattern: must have between 1 and 3 rows.");
            }

            String[] pattern = new String[patternHeight];
            for (int i = 0; i < patternHeight; i++) {
                String line = GsonHelper.convertToString(patternJson.get(i), "pattern[" + i + "]");
                if (line.length() == 0 || line.length() > 3) {
                    throw new JsonSyntaxException("Invalid pattern line: '" + line + "'. Each line must have between 1 and 3 characters.");
                }
                pattern[i] = line;
            }

            // Initialize ingredients list
            NonNullList<Ingredient> inputs = NonNullList.withSize(10, Ingredient.EMPTY);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    Ingredient ingredient = Ingredient.EMPTY;
                    if (row < patternHeight && col < pattern[row].length()) {
                        char symbol = pattern[row].charAt(col);
                        if (symbol != ' ') {
                            ingredient = keyMap.getOrDefault(symbol, Ingredient.EMPTY);
                        }
                    }
                    inputs.set(row * 3 + col, ingredient);
                }
            }

            // Parse the 'result' object
            JsonObject resultJson = GsonHelper.getAsJsonObject(pSerializedRecipe, "result");
            ItemStack output = ShapedRecipe.itemStackFromJson(resultJson);

            return new ForgingRecipe(inputs, hammering, output, pRecipeId);
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
        public @Nullable ForgingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            int inputSize = pBuffer.readInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(inputSize, Ingredient.EMPTY);

            for (int i = 0; i < inputSize; i++) {
                inputs.set(i, Ingredient.fromNetwork(pBuffer));
            }

            ItemStack output = pBuffer.readItem();
            int hammering = pBuffer.readVarInt(); // Read the hammering value from the buffer
            return new ForgingRecipe(inputs, hammering, output, pRecipeId);
        }


        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ForgingRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.inputItems.size());

            for (Ingredient ingredient : pRecipe.getIngredients()) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeItemStack(pRecipe.getResultItem(null), false);
            pBuffer.writeVarInt(pRecipe.getHammeringRequired()); // Write the hammering value to the buffer
        }

    }
}
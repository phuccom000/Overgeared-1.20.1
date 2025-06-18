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
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ForgingBookRecipeBookTab;

import javax.annotation.Nullable;
import java.util.*;

public class ForgingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final String group;
    private final ForgingBookRecipeBookTab tab;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final int hammering;
    private final boolean hasQuality;
    private final boolean showNotification;
    public final int width;
    public final int height;

    public ForgingRecipe(ResourceLocation id, String group, @Nullable ForgingBookRecipeBookTab tab, NonNullList<Ingredient> ingredients,
                         ItemStack result, int hammering, boolean hasQuality, boolean showNotification, int width, int height) {
        this.id = id;
        this.group = group;
        this.tab = tab;
        this.ingredients = ingredients;
        this.result = result;
        this.hammering = hammering;

        this.hasQuality = hasQuality;
        this.showNotification = showNotification;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean matches(Container inv, Level world) {
        ForgingRecipe bestMatch = null;
        int bestPriority = -1;

        // Check all possible positions for all possible recipes
        for (int y = 0; y <= 3 - height; y++) {
            for (int x = 0; x <= 3 - width; x++) {
                if (matchesPattern(inv, x, y) && checkSurroundingBlanks(inv, x, y)) {
                    int currentPriority = calculatePriority();
                    if (currentPriority > bestPriority) {
                        bestPriority = currentPriority;
                        bestMatch = this;
                    }
                }
            }
        }

        return bestMatch == this;
    }

    private boolean checkSurroundingBlanks(Container inv, int xOffset, int yOffset) {
        // Check if slots outside the recipe pattern are empty
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                // Skip slots that are part of the recipe
                if (x >= xOffset && x < xOffset + width &&
                        y >= yOffset && y < yOffset + height) {
                    continue;
                }

                // Check if non-recipe slots are empty
                int invSlot = y * 3 + x;
                if (!inv.getItem(invSlot).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private int calculatePriority() {
        // Calculate priority based on recipe size (bigger recipes have higher priority)
        // Add a small bonus for recipes that use more items to break ties
        int itemCount = 0;
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.isEmpty()) {
                itemCount++;
            }
        }
        return width * height * 100 + itemCount; // Multiplier ensures size dominates
    }

    private boolean matchesPattern(Container inv, int xOffset, int yOffset) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int invSlot = (y + yOffset) * 3 + (x + xOffset);
                Ingredient ingredient = ingredients.get(y * width + x);

                // If recipe expects empty, inventory slot must be empty
                if (ingredient.isEmpty()) {
                    if (!inv.getItem(invSlot).isEmpty()) {
                        return false;
                    }
                }
                // If recipe expects item, must match and have at least 1 count
                else if (!ingredient.test(inv.getItem(invSlot))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.width && height >= this.height;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FORGING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FORGING.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ForgingRecipe that = (ForgingRecipe) obj;
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }


    public int getHammeringRequired() {
        return hammering;
    }

    public boolean hasQuality() {
        return hasQuality;
    }

    public String getGroup() {
        return group;
    }

    public boolean showNotification() {
        return showNotification;
    }

    public int getRemainingHits() {
        return hammering;
    }


    @Nullable
    public ForgingBookRecipeBookTab getRecipeBookTab() {
        return this.tab;
    }


    public static class Type implements RecipeType<ForgingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "forging";
    }

    public static class Serializer implements RecipeSerializer<ForgingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "forging");

        @Override
        public ForgingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            int hammering = GsonHelper.getAsInt(json, "hammering", 1);
            boolean hasQuality = GsonHelper.getAsBoolean(json, "has_quality", true);
            boolean showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);

            Map<Character, Ingredient> keyMap = parseKey(GsonHelper.getAsJsonObject(json, "key"));
            JsonArray pattern = GsonHelper.getAsJsonArray(json, "pattern");
            final String tabKeyIn = GsonHelper.getAsString(json, "category", null);
            final ForgingBookRecipeBookTab tabIn = ForgingBookRecipeBookTab.findByName(tabKeyIn);
            if (tabKeyIn != null && tabIn == null) {
                OvergearedMod.LOGGER.warn("Optional field 'category' does not match any valid tab. If defined, must be one of the following: " + EnumSet.allOf(ForgingBookRecipeBookTab.class));
            }
            int width = pattern.get(0).getAsString().length();
            int height = pattern.size();
            NonNullList<Ingredient> ingredients = dissolvePattern(pattern, keyMap, width, height);

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new ForgingRecipe(recipeId, group, tabIn, ingredients, result, hammering, hasQuality, showNotification, width, height);
        }

        private static Map<Character, Ingredient> parseKey(JsonObject keyJson) {
            Map<Character, Ingredient> keyMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : keyJson.entrySet()) {
                if (entry.getKey().length() != 1) {
                    throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is not a single character");
                }
                keyMap.put(entry.getKey().charAt(0), Ingredient.fromJson(entry.getValue()));
            }
            return keyMap;
        }

        private static NonNullList<Ingredient> dissolvePattern(JsonArray pattern, Map<Character, Ingredient> keys, int width, int height) {
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int y = 0; y < height; y++) {
                String row = GsonHelper.convertToString(pattern.get(y), "pattern[" + y + "]");
                if (row.length() != width) {
                    throw new JsonSyntaxException("Pattern row width mismatch");
                }
                for (int x = 0; x < width; x++) {
                    char c = row.charAt(x);
                    Ingredient ingredient = keys.getOrDefault(c, c == ' ' ? Ingredient.EMPTY : null);

                    if (ingredient == null) {
                        throw new JsonSyntaxException("Pattern references undefined symbol: '" + c + "'");
                    }

                    ingredients.set(y * width + x, ingredient);
                }
            }
            return ingredients;
        }


        @Override
        public ForgingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            ForgingBookRecipeBookTab tabIn = ForgingBookRecipeBookTab.findByName(buffer.readUtf());
            int hammering = buffer.readVarInt();
            boolean hasQuality = buffer.readBoolean();  // Changed order to match writing
            boolean showNotification = buffer.readBoolean();
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();

            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }

            ItemStack result = buffer.readItem();
            return new ForgingRecipe(recipeId, group, tabIn, ingredients, result, hammering, hasQuality, showNotification, width, height);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ForgingRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeUtf(recipe.tab != null ? recipe.tab.toString() : "");
            buffer.writeVarInt(recipe.hammering);
            buffer.writeBoolean(recipe.hasQuality);
            buffer.writeBoolean(recipe.showNotification);
            buffer.writeVarInt(recipe.width);
            buffer.writeVarInt(recipe.height);

            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItem(recipe.result);
        }
    }

    public static Optional<ForgingRecipe> findBestMatch(Level world, Container inv) {
        return world.getRecipeManager().getAllRecipesFor(ModRecipeTypes.FORGING.get())
                .stream()
                .filter(recipe -> recipe.matches(inv, world))
                .max(Comparator.comparingInt(ForgingRecipe::getRecipeSize));
    }

    private int getRecipeSize() {
        return width * height;
    }

    /*public record Ingredients(String group, List<String> pattern, Map<String, Ingredient> recipe, ItemStack result) {
        private static final Function<String, DataResult<String>> VERIFY_LENGTH_1 =
                s -> s.length() == 1 ? DataResult.success(s) : DataResult.error(() -> "Key must be a single character!");

        private static final Function<String, DataResult<String>> VERIFY_LENGTH_2 =
                s -> s.length() == 2 ? DataResult.success(s) : DataResult.error(() -> "Key row length must be of 2!");

        private static final Function<List<String>, DataResult<List<String>>> VERIFY_SIZE = l -> {
            if (l.size() <= 4 && l.size() >= 1) {
                List<String> temp = new ArrayList<>(l);
                Collections.reverse(temp); //reverse so the first row is at the bottom in the json.
                return DataResult.success(ImmutableList.copyOf(temp));
            }
            return DataResult.error(() -> "Pattern must have between 1 and 4 rows of keys");
        };

        public static final Codec<Ingredient> INGREDIENT_CODEC = Codec.PASSTHROUGH.comapFlatMap(obj -> {
            JsonElement json = obj.convert(JsonOps.INSTANCE).getValue();
            try {
                return DataResult.success(Ingredient.fromJson(json));
            } catch (Exception e) {
                return DataResult.error(() -> "Failed to parse ingredient: " + e.getMessage());
            }
        }, ingredient -> new Dynamic<>(JsonOps.INSTANCE, ingredient.toJson()));

        public static final Codec<Ingredients> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.fieldOf("group").forGetter(Ingredients::group),
                        Codec.STRING.flatXmap(VERIFY_LENGTH_2, VERIFY_LENGTH_2).listOf().flatXmap(VERIFY_SIZE, VERIFY_SIZE).fieldOf("pattern").forGetter(Ingredients::pattern),
                        Codec.unboundedMap(Codec.STRING.flatXmap(VERIFY_LENGTH_1, VERIFY_LENGTH_1), INGREDIENT_CODEC).fieldOf("key").forGetter(Ingredients::recipe),
                        ItemStack.CODEC.fieldOf("result").forGetter(Ingredients::result)
                ).apply(inst, Ingredients::new)
        );
    }
*/

}
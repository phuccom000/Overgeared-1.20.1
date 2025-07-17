package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.*;
import java.util.stream.Collectors;

public class ForgingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final String group;
    private final Set<String> blueprintTypes;
    private final String tier;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final ItemStack failedResult;
    private final int hammering;
    private final boolean hasQuality;
    private final boolean needsMinigame;
    private final boolean requiresBlueprint;
    private final boolean hasPolishing;
    private final boolean showNotification;
    private final ForgingQuality minimumQuality;
    public final int width;
    public final int height;
    private static int BLUEPRINT_SLOT = 11;

    public ForgingRecipe(ResourceLocation id, String group, boolean requireBlueprint, Set<String> blueprintTypes, String tier, NonNullList<Ingredient> ingredients,
                         ItemStack result, ItemStack failedResult, int hammering, boolean hasQuality, boolean needsMinigame, boolean hasPolishing, boolean showNotification, ForgingQuality minimumQuality, int width, int height) {
        this.id = id;
        this.group = group;
        this.blueprintTypes = blueprintTypes;
        this.tier = tier;
        this.ingredients = ingredients;
        this.result = result;
        this.failedResult = failedResult;
        this.hammering = hammering;
        this.hasQuality = hasQuality;
        this.requiresBlueprint = requireBlueprint;
        this.needsMinigame = needsMinigame;
        this.hasPolishing = hasPolishing;
        this.showNotification = showNotification;
        this.minimumQuality = minimumQuality;
        this.width = width;
        this.height = height;
    }

    private boolean checkBlueprint(Container inv) {
        ItemStack blueprintStack = inv.getItem(BLUEPRINT_SLOT);

        // If no blueprints required
        if (blueprintTypes.isEmpty()) {
            return blueprintStack.isEmpty();
        }

        // Blueprint required, but slot empty
        if (blueprintStack.isEmpty()) return false;

        CompoundTag nbt = blueprintStack.getTag();
        if (nbt == null || !nbt.contains("ToolType")) return false;

        String toolType = nbt.getString("ToolType");
        return blueprintTypes.contains(toolType);
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

    public ItemStack getFailedResultItem(RegistryAccess registryAccess) {
        return failedResult == null || failedResult.isEmpty() || failedResult.is(result.getItem()) ? ItemStack.EMPTY : failedResult.copy();
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

    public String getAnvilTier() {
        return tier;
    }

    public boolean hasQuality() {
        return hasQuality;
    }

    public boolean needsMinigame() {
        return needsMinigame;
    }

    public String getGroup() {
        return group;
    }

    public ForgingQuality getMinimumQuality() {
        return minimumQuality;
    }

    public boolean showNotification() {
        return showNotification;
    }

    public int getRemainingHits() {
        return hammering;
    }

    public boolean hasPolishing() {
        return hasPolishing;
    }

    public Set<String> getBlueprintTypes() {
        return blueprintTypes.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public boolean requiresBlueprint() {
        return requiresBlueprint;
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
            Set<String> blueprintTypes = new LinkedHashSet<>();
            if (json.has("blueprint")) {
                JsonElement blueprintElement = json.get("blueprint");

                if (blueprintElement.isJsonArray()) {
                    for (JsonElement element : blueprintElement.getAsJsonArray()) {
                        String bp = element.getAsString().toLowerCase(Locale.ROOT);
                        if (!bp.isBlank()) {
                            blueprintTypes.add(bp);
                        }
                    }
                } else if (blueprintElement.isJsonPrimitive()) {
                    String bp = blueprintElement.getAsString().toLowerCase(Locale.ROOT);
                    if (!bp.isBlank()) {
                        blueprintTypes.add(bp);
                    }
                } else {
                    throw new JsonSyntaxException("'blueprint' must be either a string or array of strings");
                }
            }
            boolean requiresBlueprint = GsonHelper.getAsBoolean(json, "requires_blueprint", false);

            String tier = GsonHelper.getAsString(json, "tier", AnvilTier.IRON.getDisplayName());
            int hammering = GsonHelper.getAsInt(json, "hammering", 1);
            boolean hasQuality = GsonHelper.getAsBoolean(json, "has_quality", true);
            boolean needsMinigame = GsonHelper.getAsBoolean(json, "needs_minigame", false);
            boolean hasPolishing = GsonHelper.getAsBoolean(json, "has_polishing", true);
            boolean showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);
            ForgingQuality minimumQuality = ForgingQuality.fromString(GsonHelper.getAsString(json, "minimumQuality", ForgingQuality.POOR.getDisplayName()));
            Map<Character, Ingredient> keyMap = parseKey(GsonHelper.getAsJsonObject(json, "key"));
            JsonArray pattern = GsonHelper.getAsJsonArray(json, "pattern");

            int width = pattern.get(0).getAsString().length();
            int height = pattern.size();
            NonNullList<Ingredient> ingredients = dissolvePattern(pattern, keyMap, width, height);

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            ItemStack failedResult = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result_failed", GsonHelper.getAsJsonObject(json, "result")));
            return new ForgingRecipe(recipeId, group, requiresBlueprint, blueprintTypes, tier, ingredients, result, failedResult, hammering, hasQuality, needsMinigame, hasPolishing, showNotification, minimumQuality, width, height);
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
            boolean requiresBlueprint = buffer.readBoolean();  // Changed order to match writing
            int blueprintCount = buffer.readVarInt();
            Set<String> blueprintTypes = new LinkedHashSet<>();
            for (int i = 0; i < blueprintCount; i++) {
                blueprintTypes.add(buffer.readUtf());
            }
            String tier = buffer.readUtf();
            int hammering = buffer.readVarInt();
            boolean hasQuality = buffer.readBoolean();  // Changed order to match writing
            boolean needsMinigame = buffer.readBoolean();  // Changed order to match writing
            boolean hasPolishing = buffer.readBoolean();  // Changed order to match writing
            boolean showNotification = buffer.readBoolean();
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            ForgingQuality minimumQuality = ForgingQuality.fromString(buffer.readUtf());
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }

            ItemStack result = buffer.readItem();
            ItemStack failedResult = buffer.readItem();
            return new ForgingRecipe(recipeId, group, requiresBlueprint, blueprintTypes, tier, ingredients, result, failedResult, hammering, hasQuality, needsMinigame, hasPolishing, showNotification, minimumQuality, width, height);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ForgingRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeBoolean(recipe.requiresBlueprint);
            buffer.writeVarInt(recipe.blueprintTypes.size());
            for (String type : recipe.blueprintTypes) {
                buffer.writeUtf(type);
            }
            buffer.writeUtf(recipe.tier);
            buffer.writeVarInt(recipe.hammering);
            buffer.writeBoolean(recipe.hasQuality);
            buffer.writeBoolean(recipe.needsMinigame);
            buffer.writeBoolean(recipe.hasPolishing);
            buffer.writeBoolean(recipe.showNotification);
            buffer.writeVarInt(recipe.width);
            buffer.writeVarInt(recipe.height);
            buffer.writeUtf(recipe.minimumQuality.toString());

            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItem(recipe.result);
            buffer.writeItem(recipe.failedResult);
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
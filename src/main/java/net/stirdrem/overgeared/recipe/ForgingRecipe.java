package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;

import java.util.*;
import java.util.stream.Collectors;

public class ForgingRecipe implements Recipe<RecipeInput> {
    private static final int BLUEPRINT_SLOT = 11;
    public final int width;
    public final int height;
    private final String group;
    private final Set<String> blueprintTypes;
    private final String tier;
    private final List<String> pattern;
    private final Map<String, Ingredient> key;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final ItemStack failedResult;
    private final int hammering;
    private final boolean hasQuality;
    private final boolean needsMinigame;
    private final boolean requiresBlueprint;
    private final boolean hasPolishing;
    private final boolean needQuenching;
    private final boolean showNotification;
    private final ForgingQuality minimumQuality;
    private final ForgingQuality qualityDifficulty;

    public ForgingRecipe(String group, boolean requireBlueprint, Set<String> blueprintTypes, String tier, 
                         List<String> pattern, Map<String, Ingredient> key, NonNullList<Ingredient> ingredients,
                         ItemStack result, ItemStack failedResult, int hammering, boolean hasQuality, boolean needsMinigame, boolean hasPolishing, boolean needQuenching, boolean showNotification, ForgingQuality minimumQuality, ForgingQuality qualityDifficulty, int width, int height) {
        this.group = group;
        this.blueprintTypes = blueprintTypes;
        this.tier = tier;
        this.pattern = pattern;
        this.key = key;
        this.ingredients = ingredients;
        this.result = result;
        this.failedResult = failedResult;
        this.hammering = hammering;
        this.hasQuality = hasQuality;
        this.requiresBlueprint = requireBlueprint;
        this.needsMinigame = needsMinigame;
        this.hasPolishing = hasPolishing;
        this.needQuenching = needQuenching;
        this.showNotification = showNotification;
        this.minimumQuality = minimumQuality;
        this.width = width;
        this.height = height;
        this.qualityDifficulty = qualityDifficulty;
    }

    public static Optional<ForgingRecipe> findBestMatch(Level world, RecipeInput recipeInput) {
        return findBestMatchHolder(world, recipeInput).map(RecipeHolder::value);
    }

    public static Optional<RecipeHolder<ForgingRecipe>> findBestMatchHolder(Level world, RecipeInput recipeInput) {
        return world.getRecipeManager().getAllRecipesFor(ModRecipeTypes.FORGING.get())
                .stream()
                .filter(holder -> holder.value().matches(recipeInput, world))
                .max(Comparator.comparingInt(holder -> holder.value().getRecipeSize()));
    }

    private boolean checkBlueprint(RecipeInput recipeInput) {
        ItemStack blueprintStack = recipeInput.getItem(BLUEPRINT_SLOT);

        // If no blueprints required
        if (blueprintTypes.isEmpty()) {
            return blueprintStack.isEmpty();
        }

        // Blueprint required, but slot empty
        if (blueprintStack.isEmpty()) return false;

        BlueprintData data = blueprintStack.get(ModComponents.BLUEPRINT_DATA);
        if (data == null) return false;
        
        String toolType = data.toolType();
        if (toolType.isEmpty()) return false;

        return blueprintTypes.contains(toolType);
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level world) {
        ForgingRecipe bestMatch = null;
        int bestPriority = -1;

        // Check all possible positions for all possible recipes
        for (int y = 0; y <= 3 - height; y++) {
            for (int x = 0; x <= 3 - width; x++) {
                if (matchesPattern(recipeInput, x, y) && checkSurroundingBlanks(recipeInput, x, y)) {
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

    private boolean checkSurroundingBlanks(RecipeInput recipeInput, int xOffset, int yOffset) {
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
                if (!recipeInput.getItem(invSlot).isEmpty()) {
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

    private boolean matchesPattern(RecipeInput recipeInput, int xOffset, int yOffset) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int invSlot = (y + yOffset) * 3 + (x + xOffset);
                Ingredient ingredient = ingredients.get(y * width + x);

                // If recipe expects empty, inventory slot must be empty
                if (ingredient.isEmpty()) {
                    if (!recipeInput.getItem(invSlot).isEmpty()) {
                        return false;
                    }
                }
                // If recipe expects item, must match and have at least 1 count
                else if (!ingredient.test(recipeInput.getItem(invSlot))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.width && height >= this.height;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    public ItemStack getFailedResultItem(HolderLookup.Provider registries) {
        return failedResult == null || failedResult.isEmpty() || failedResult.is(result.getItem()) ? ItemStack.EMPTY : failedResult.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FORGING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FORGING.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ForgingRecipe that = (ForgingRecipe) obj;
        return Objects.equals(this.result.getItem(), that.result.getItem()) &&
               Objects.equals(this.ingredients, that.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result.getItem(), ingredients);
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

    public ForgingQuality getQualityDifficulty() {
        return qualityDifficulty;
    }

    @Override
    public boolean showNotification() {
        return false;
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

    public boolean needQuenching() {
        return needQuenching;
    }

    public List<String> getPattern() {
        return pattern;
    }

    public Map<String, Ingredient> getKey() {
        return key;
    }

    private int getRecipeSize() {
        return width * height;
    }

    public static class Serializer implements RecipeSerializer<ForgingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        // Codec for ForgingQuality enum
        private static final Codec<ForgingQuality> FORGING_QUALITY_CODEC = Codec.STRING.xmap(
                ForgingQuality::fromString,
                ForgingQuality::getDisplayName
        );

        // Codec for Set<String> of blueprint types
        private static final Codec<Set<String>> BLUEPRINT_TYPES_CODEC = Codec.either(
                Codec.STRING,
                Codec.STRING.listOf()
        ).xmap(
                either -> either.map(
                        s -> s.isBlank() ? Set.of() : Set.of(s.toLowerCase(Locale.ROOT)),
                        list -> list.stream()
                                .filter(s -> !s.isBlank())
                                .map(s -> s.toLowerCase(Locale.ROOT))
                                .collect(Collectors.toCollection(LinkedHashSet::new))
                ),
                set -> com.mojang.datafixers.util.Either.right(new ArrayList<>(set))
        );

        // StreamCodec for Set<String>
        private static final StreamCodec<RegistryFriendlyByteBuf, Set<String>> BLUEPRINT_TYPES_STREAM_CODEC =
                new StreamCodec<>() {
            @Override
            public Set<String> decode(RegistryFriendlyByteBuf buffer) {
                int count = buffer.readVarInt();
                Set<String> set = new LinkedHashSet<>();
                for (int i = 0; i < count; i++) {
                    set.add(buffer.readUtf());
                }
                return set;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, Set<String> value) {
                buffer.writeVarInt(value.size());
                for (String s : value) {
                    buffer.writeUtf(s);
                }
            }
        };

        // StreamCodec for ForgingQuality
        private static final StreamCodec<RegistryFriendlyByteBuf, ForgingQuality> FORGING_QUALITY_STREAM_CODEC = new StreamCodec<>() {
            @Override
            public ForgingQuality decode(RegistryFriendlyByteBuf buffer) {
                return ForgingQuality.fromString(buffer.readUtf());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ForgingQuality value) {
                buffer.writeUtf(value.getDisplayName());
            }
        };

        // StreamCodec for NonNullList<Ingredient>
        private static final StreamCodec<RegistryFriendlyByteBuf, NonNullList<Ingredient>> INGREDIENTS_STREAM_CODEC = new StreamCodec<>() {
            @Override
            public NonNullList<Ingredient> decode(RegistryFriendlyByteBuf buffer) {
                int size = buffer.readVarInt();
                NonNullList<Ingredient> list = NonNullList.withSize(size, Ingredient.EMPTY);
                for (int i = 0; i < size; i++) {
                    list.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
                }
                return list;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, NonNullList<Ingredient> value) {
                buffer.writeVarInt(value.size());
                for (Ingredient ingredient : value) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
                }
            }
        };

        @Override
        public MapCodec<ForgingRecipe> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(ForgingRecipe::getGroup),
                    Codec.BOOL.optionalFieldOf("requires_blueprint", false).forGetter(ForgingRecipe::requiresBlueprint),
                    BLUEPRINT_TYPES_CODEC.optionalFieldOf("blueprint", Set.of()).forGetter(ForgingRecipe::getBlueprintTypes),
                    Codec.STRING.optionalFieldOf("tier", AnvilTier.IRON.getDisplayName()).forGetter(ForgingRecipe::getAnvilTier),
                    Codec.list(Codec.STRING).fieldOf("pattern").forGetter(ForgingRecipe::getPattern),
                    Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).fieldOf("key").forGetter(ForgingRecipe::getKey),
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                    ItemStack.CODEC.optionalFieldOf("result_failed", ItemStack.EMPTY).forGetter(r -> r.failedResult),
                    Codec.INT.optionalFieldOf("hammering", 1).forGetter(ForgingRecipe::getHammeringRequired),
                    Codec.BOOL.optionalFieldOf("has_quality", true).forGetter(ForgingRecipe::hasQuality),
                    Codec.BOOL.optionalFieldOf("needs_minigame", false).forGetter(ForgingRecipe::needsMinigame),
                    Codec.BOOL.optionalFieldOf("has_polishing", true).forGetter(ForgingRecipe::hasPolishing),
                    Codec.BOOL.optionalFieldOf("need_quenching", true).forGetter(ForgingRecipe::needQuenching),
                    Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ForgingRecipe::showNotification),
                    FORGING_QUALITY_CODEC.optionalFieldOf("minimumQuality", ForgingQuality.POOR).forGetter(ForgingRecipe::getMinimumQuality),
                    FORGING_QUALITY_CODEC.optionalFieldOf("quality_difficulty", ForgingQuality.NONE).forGetter(ForgingRecipe::getQualityDifficulty)
            ).apply(instance, (group, requiresBlueprint, blueprintTypes, tier, pattern, key, result, failedResult,
                               hammering, hasQuality, needsMinigame, hasPolishing, needQuenching, showNotification,
                               minimumQuality, qualityDifficulty) -> {
                // Parse the pattern using the key map
                if (pattern.isEmpty()) {
                    throw new IllegalArgumentException("Pattern cannot be empty");
                }
                int width = pattern.getFirst().length();
                int height = pattern.size();
                
                NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
                for (int y = 0; y < height; y++) {
                    String row = pattern.get(y);
                    if (row.length() != width) {
                        throw new IllegalArgumentException("Pattern row width mismatch");
                    }
                    for (int x = 0; x < width; x++) {
                        char c = row.charAt(x);
                        String keyStr = String.valueOf(c);
                        Ingredient ingredient = c == ' ' ? Ingredient.EMPTY : key.getOrDefault(keyStr, Ingredient.EMPTY);
                        ingredients.set(y * width + x, ingredient);
                    }
                }
                
                // Default quenching based on whether result is armor
                boolean actualNeedQuenching = needQuenching;
                if (result.getItem() instanceof ArmorItem) {
                    actualNeedQuenching = false;
                }
                
                ItemStack actualFailedResult = failedResult.isEmpty() ? result.copy() : failedResult;
                
                return new ForgingRecipe(group, requiresBlueprint, new LinkedHashSet<>(blueprintTypes), tier, 
                        new ArrayList<>(pattern), new LinkedHashMap<>(key), ingredients,
                        result, actualFailedResult, hammering, hasQuality, needsMinigame, hasPolishing, actualNeedQuenching,
                        showNotification, minimumQuality, qualityDifficulty, width, height);
            }));
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ForgingRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public ForgingRecipe decode(RegistryFriendlyByteBuf buffer) {
                    String group = buffer.readUtf();
                    boolean requiresBlueprint = buffer.readBoolean();
                    Set<String> blueprintTypes = BLUEPRINT_TYPES_STREAM_CODEC.decode(buffer);
                    String tier = buffer.readUtf();
                    int hammering = buffer.readVarInt();
                    boolean hasQuality = buffer.readBoolean();
                    boolean needsMinigame = buffer.readBoolean();
                    boolean hasPolishing = buffer.readBoolean();
                    boolean needQuenching = buffer.readBoolean();
                    boolean showNotification = buffer.readBoolean();
                    int width = buffer.readVarInt();
                    int height = buffer.readVarInt();
                    ForgingQuality minimumQuality = FORGING_QUALITY_STREAM_CODEC.decode(buffer);
                    ForgingQuality qualityDifficulty = FORGING_QUALITY_STREAM_CODEC.decode(buffer);
                    NonNullList<Ingredient> ingredients = INGREDIENTS_STREAM_CODEC.decode(buffer);
                    ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
                    ItemStack failedResult = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
                    
                    // Pattern and key are not synced over network - only ingredients are needed at runtime
                    return new ForgingRecipe(group, requiresBlueprint, blueprintTypes, tier, 
                            List.of(), Map.of(), ingredients,
                            result, failedResult, hammering, hasQuality, needsMinigame, hasPolishing,
                            needQuenching, showNotification, minimumQuality, qualityDifficulty, width, height);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ForgingRecipe recipe) {
                    buffer.writeUtf(recipe.group);
                    buffer.writeBoolean(recipe.requiresBlueprint);
                    BLUEPRINT_TYPES_STREAM_CODEC.encode(buffer, recipe.blueprintTypes);
                    buffer.writeUtf(recipe.tier);
                    buffer.writeVarInt(recipe.hammering);
                    buffer.writeBoolean(recipe.hasQuality);
                    buffer.writeBoolean(recipe.needsMinigame);
                    buffer.writeBoolean(recipe.hasPolishing);
                    buffer.writeBoolean(recipe.needQuenching);
                    buffer.writeBoolean(recipe.showNotification);
                    buffer.writeVarInt(recipe.width);
                    buffer.writeVarInt(recipe.height);
                    FORGING_QUALITY_STREAM_CODEC.encode(buffer, recipe.minimumQuality);
                    FORGING_QUALITY_STREAM_CODEC.encode(buffer, recipe.qualityDifficulty);
                    INGREDIENTS_STREAM_CODEC.encode(buffer, recipe.ingredients);
                    ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.failedResult);
                }
            };
        }
    }
}
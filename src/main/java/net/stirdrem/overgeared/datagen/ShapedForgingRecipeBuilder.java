package net.stirdrem.overgeared.datagen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.NonNullList;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingBookCategory;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ShapedForgingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final ForgingBookCategory bookCategory;
    private final Item result;

    private final int count;
    private final int hammering;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    @Nullable
    private final List<String> blueprintTypes = new java.util.ArrayList<>();
    @Nullable
    private Boolean requiresBlueprint;
    @Nullable
    private Boolean hasQuality;
    @Nullable
    private Boolean hasPolishing;
    @Nullable
    private Boolean needsMinigame;
    @Nullable
    private String group;
    @Nullable
    private String tier;
    @Nullable
    private Item failedResult;
    @Nullable
    private int failedResultCount;

    @Nullable
    private ForgingQuality minimumQuality;

    private boolean showNotification = true;


    public ShapedForgingRecipeBuilder(RecipeCategory category, ForgingBookCategory bookCategory, ItemLike result, int count, int hammering) {
        this.category = category;
        this.bookCategory = bookCategory;
        this.result = result.asItem();
        this.count = count;
        this.hammering = hammering;
    }

    private static boolean isTools(Item item) {
        return item instanceof SwordItem ||
                item instanceof DiggerItem ||
                item instanceof ProjectileWeaponItem;
    }

    public static boolean isToolPart(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModTags.Items.TOOL_PARTS);
    }

    public static boolean isToolPart(Item item) {
        return item.builtInRegistryHolder().is(ModTags.Items.TOOL_PARTS);
    }

    private static ForgingBookCategory determineWeaponRecipeCategory(ItemLike pResult) {
        if (isTools(pResult.asItem()) || isToolPart(pResult.asItem())) {
            return ForgingBookCategory.TOOLS;
        } else {
            return pResult.asItem() instanceof ArmorItem ? ForgingBookCategory.ARMORS : ForgingBookCategory.MISC;
        }
    }


    public static ShapedForgingRecipeBuilder shaped(RecipeCategory category, ItemLike result, int hammering) {
        return new ShapedForgingRecipeBuilder(category, determineWeaponRecipeCategory(result), result, 1, hammering);
    }

    public static ShapedForgingRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count, int hammering) {
        return new ShapedForgingRecipeBuilder(category, determineWeaponRecipeCategory(result), result, count, hammering);
    }

    public ShapedForgingRecipeBuilder define(Character pSymbol, TagKey<Item> pTag) {
        return this.define(pSymbol, Ingredient.of(pTag));
    }

    public ShapedForgingRecipeBuilder define(Character symbol, ItemLike item) {
        return this.define(symbol, Ingredient.of(item));
    }

    public ShapedForgingRecipeBuilder define(Character pSymbol, Ingredient pIngredient) {
        if (this.key.containsKey(pSymbol)) {
            throw new IllegalArgumentException("Symbol '" + pSymbol + "' is already defined!");
        } else if (pSymbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(pSymbol, pIngredient);
            return this;
        }
    }

    public ShapedForgingRecipeBuilder pattern(String pPattern) {
        if (!this.rows.isEmpty() && pPattern.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(pPattern);
            return this;
        }
    }

    public ShapedForgingRecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
        this.advancement.addCriterion(pCriterionName, pCriterionTrigger);
        return this;
    }

    public ShapedForgingRecipeBuilder group(@Nullable String pGroupName) {
        this.group = pGroupName;
        return this;
    }

    public ShapedForgingRecipeBuilder tier(@Nullable AnvilTier pTier) {
        this.tier = pTier.getDisplayName();
        return this;
    }

    public ShapedForgingRecipeBuilder setQuality(@Nullable boolean hasQuality) {
        this.hasQuality = hasQuality;
        return this;
    }

    public ShapedForgingRecipeBuilder requiresBlueprint(@Nullable boolean requiresBlueprint) {
        this.requiresBlueprint = requiresBlueprint;
        return this;
    }

    public ShapedForgingRecipeBuilder needsMinigame(@Nullable boolean needsMinigame) {
        this.needsMinigame = needsMinigame;
        return this;
    }

    public ShapedForgingRecipeBuilder failedResult(ItemLike result) {
        this.failedResult = result.asItem();
        this.failedResultCount = 1;
        return this;
    }

    public ShapedForgingRecipeBuilder failedResult(ItemLike result, int count) {
        this.failedResult = result.asItem();
        this.failedResultCount = count;
        return this;
    }

    public ShapedForgingRecipeBuilder setBlueprint(String blueprintType) {
        if (blueprintType != null && !blueprintType.isBlank()) {
            this.blueprintTypes.add(blueprintType.toLowerCase());
        }
        return this;
    }

    public ShapedForgingRecipeBuilder minimumQuality(@Nullable ForgingQuality minimumQuality) {
        this.minimumQuality = minimumQuality;
        return this;
    }

    public ShapedForgingRecipeBuilder setPolishing(@Nullable boolean hasPolishing) {
        this.hasPolishing = hasPolishing;
        return this;
    }

    public ShapedForgingRecipeBuilder showNotification(boolean pShowNotification) {
        this.showNotification = pShowNotification;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    public Item getFailedResult() {
        return this.failedResult;
    }

    @Override
    public void save(Consumer<FinishedRecipe> pRecipeOutput, ResourceLocation pRecipeId) {
        this.ensureValid(pRecipeId);

        int width = this.rows.get(0).length();
        int height = this.rows.size();
        NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);

        for (int i = 0; i < height; ++i) {
            String patternLine = this.rows.get(i);
            for (int j = 0; j < width; ++j) {
                char symbol = patternLine.charAt(j);
                Ingredient ingredient = this.key.getOrDefault(symbol, Ingredient.EMPTY);
                ingredients.set(i * width + j, ingredient);
            }
        }

        pRecipeOutput.accept(new ShapedForgingRecipeBuilder.Result(
                ingredients,
                this.hammering,
                new ItemStack(this.result, this.count),
                pRecipeId,
                this.failedResult != null ? new ItemStack(this.failedResult, this.failedResultCount) : ItemStack.EMPTY,
                this.group == null ? "" : this.group,
                this.bookCategory,
                this.rows,
                this.key,
                this.advancement,
                pRecipeId.withPrefix("recipes/" + this.category.getFolderName() + "/"),
                this.showNotification,
                this.blueprintTypes,
                this.hasQuality != null && !this.hasQuality ? null : (this.requiresBlueprint != null ? this.requiresBlueprint : false),
                this.hasQuality == null || this.hasQuality,
                this.hasQuality != null && !this.hasQuality ? null : (this.hasPolishing != null ? this.hasPolishing : true),
                this.hasQuality != null && this.hasQuality ? null : (this.needsMinigame != null && this.needsMinigame),
                this.hasQuality != null && !this.hasQuality ? "" : (this.minimumQuality != null ? this.minimumQuality.getDisplayName() : ForgingQuality.POOR.getDisplayName()),
                this.tier == null ? "" : this.tier
        ));
    }


    private void ensureValid(ResourceLocation pRecipeId) {
        if (this.rows.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped forging recipe " + pRecipeId + "!");
        }
        int width = this.rows.get(0).length();
        for (String row : this.rows) {
            if (row.length() != width) {
                throw new IllegalStateException("Pattern must be the same width on every line!");
            }
        }
    }


    static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final NonNullList<Ingredient> ingredients;
        private final int hammering;
        private final ItemStack result;
        private final ItemStack failedResult;
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final boolean showNotification;
        private final String group;
        private final List<String> blueprintTypes;
        private final ForgingBookCategory category;
        private final Boolean requiresBlueprint;
        private final Boolean hasQuality;
        private final Boolean hasPolishing;
        private final Boolean needsMinigame;
        private final String minimumQuality;
        private final String tier;


        public Result(NonNullList<Ingredient> ingredients, int hammering, ItemStack result, ResourceLocation id, ItemStack failedResult, String group, ForgingBookCategory category, List<String> pattern, Map<Character, Ingredient> key, Advancement.Builder advancement, ResourceLocation advancementId, boolean showNotification, List<String> blueprintTypes, Boolean requiresBlueprint, Boolean hasQuality, Boolean hasPolishing, Boolean needsMinigame, String minimumQuality, String tier) {
            this.ingredients = ingredients;
            this.hammering = hammering;
            this.result = result;
            this.failedResult = failedResult;
            this.category = category;
            this.id = id;
            this.group = group;
            this.pattern = pattern;
            this.key = key;
            this.advancement = advancement;
            this.advancementId = advancementId;
            this.showNotification = showNotification;
            this.blueprintTypes = blueprintTypes;
            this.requiresBlueprint = requiresBlueprint;
            this.hasQuality = hasQuality;
            this.hasPolishing = hasPolishing;
            this.needsMinigame = needsMinigame;
            this.minimumQuality = minimumQuality;
            this.tier = tier;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            if (this.requiresBlueprint != null) {
                json.addProperty("requires_blueprint", this.requiresBlueprint);
            }

            if (!this.blueprintTypes.isEmpty()) {
                JsonArray blueprintArray = new JsonArray();
                for (String type : this.blueprintTypes) {
                    blueprintArray.add(type);
                }
                json.add("blueprint", blueprintArray);
            }

            json.addProperty("category", this.category.getSerializedName());

            JsonArray patternArray = new JsonArray();
            for (String s : this.pattern) {
                patternArray.add(s);
            }
            json.add("pattern", patternArray);

            if (!this.tier.isBlank()) {
                json.addProperty("tier", this.tier);
            }

            json.addProperty("hammering", this.hammering);

            if (this.hasQuality != null) {
                json.addProperty("has_quality", this.hasQuality);
            }
            if (!this.minimumQuality.isEmpty()) {
                json.addProperty("minimum_quality", this.minimumQuality);
            }
            if (this.needsMinigame != null || !this.needsMinigame) {
                json.addProperty("needs_minigame", this.needsMinigame);
            }

            if (this.hasPolishing != null) {
                json.addProperty("has_polishing", this.hasPolishing);
            }

            JsonObject keyObj = new JsonObject();
            for (Map.Entry<Character, Ingredient> entry : this.key.entrySet()) {
                keyObj.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }
            json.add("key", keyObj);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result.getItem()).toString());
            if (this.result.getCount() > 1) {
                resultObj.addProperty("count", this.result.getCount());
            }
            json.add("result", resultObj);

            if (!this.failedResult.isEmpty() && this.failedResult.getItem() != Items.AIR) {
                JsonObject failedResultObj = new JsonObject();
                failedResultObj.addProperty("item", BuiltInRegistries.ITEM.getKey(this.failedResult.getItem()).toString());
                if (this.failedResult.getCount() > 1) {
                    failedResultObj.addProperty("count", this.failedResult.getCount());
                }
                json.add("result_failed", failedResultObj);
            }

            json.addProperty("show_notification", this.showNotification);
        }


        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ForgingRecipe.Serializer.INSTANCE; // Replace with your actual serializer instance
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            {
                return this.advancementId;
            }
        }

    }
}
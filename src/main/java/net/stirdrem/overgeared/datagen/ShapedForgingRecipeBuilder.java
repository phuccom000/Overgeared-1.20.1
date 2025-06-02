package net.stirdrem.overgeared.datagen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.NonNullList;
import net.stirdrem.overgeared.recipe.ForgingRecipe;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ShapedForgingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final int hammering;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    @Nullable
    private Boolean hasQuality; // Can be null, true, or false
    @Nullable
    private String group;
    private boolean showNotification = true;


    public ShapedForgingRecipeBuilder(RecipeCategory category, ItemLike result, int count, int hammering) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
        this.hammering = hammering;
    }

    public static ShapedForgingRecipeBuilder shaped(RecipeCategory category, ItemLike result, int hammering) {
        return new ShapedForgingRecipeBuilder(category, result, 1, hammering);
    }

    public static ShapedForgingRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count, int hammering) {
        return new ShapedForgingRecipeBuilder(category, result, count, hammering);
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

    public ShapedForgingRecipeBuilder setQuality(@Nullable boolean hasQuality) {
        this.hasQuality = hasQuality;
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
                this.group == null ? "" : this.group,
                this.rows,
                this.key,
                this.advancement,
                pRecipeId.withPrefix("recipes/" + this.category.getFolderName() + "/"),
                this.showNotification,
                this.hasQuality == null ? true : this.hasQuality
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
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final boolean showNotification;
        private final String group;
        private final Boolean hasQuality;


        public Result(NonNullList<Ingredient> ingredients, int hammering, ItemStack result, ResourceLocation id, String group, List<String> pattern, Map<Character, Ingredient> key, Advancement.Builder advancement, ResourceLocation advancementId, boolean showNotification, Boolean hasQuality) {
            this.ingredients = ingredients;
            this.hammering = hammering;
            this.result = result;
            this.id = id;
            this.group = group;
            this.pattern = pattern;
            this.key = key;
            this.advancement = advancement;
            this.advancementId = advancementId;
            this.showNotification = showNotification;
            this.hasQuality = hasQuality;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }
            JsonArray patternArray = new JsonArray();

            for (String s : this.pattern) {
                patternArray.add(s);
            }

            json.add("pattern", patternArray);
            JsonObject jsonobject = new JsonObject();
            //json.addProperty("category", this.category);

            json.addProperty("hammering", this.hammering);
            // Add quality flag if not null
            if (this.hasQuality != null || this.hasQuality) {
                json.addProperty("has_quality", this.hasQuality);
            }

            for (Map.Entry<Character, Ingredient> entry : this.key.entrySet()) {
                jsonobject.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }
            json.add("key", jsonobject);

            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result.getItem()).toString());
            if (this.result.getCount() > 1) {
                resultObject.addProperty("count", this.result.getCount());
            }
            json.add("result", resultObject);
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
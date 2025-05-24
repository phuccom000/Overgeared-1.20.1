package net.stirdrem.overgeared.datagen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.ForgingRecipe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ShapedForgingRecipeBuilder2 implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final int hammering;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    @Nullable
    private String group;
    private boolean showNotification = true;


    public ShapedForgingRecipeBuilder2(RecipeCategory category, ItemLike result, int count, int hammering) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;
        this.hammering = hammering;
    }

    public static ShapedForgingRecipeBuilder2 shaped(RecipeCategory category, ItemLike result, int hammering) {
        return new ShapedForgingRecipeBuilder2(category, result, 1, hammering);
    }

    public static ShapedForgingRecipeBuilder2 shaped(RecipeCategory category, ItemLike result, int count, int hammering) {
        return new ShapedForgingRecipeBuilder2(category, result, count, hammering);
    }

    public ShapedForgingRecipeBuilder2 define(Character pSymbol, TagKey<Item> pTag) {
        return this.define(pSymbol, Ingredient.of(pTag));
    }

    public ShapedForgingRecipeBuilder2 define(Character symbol, ItemLike item) {
        return this.define(symbol, Ingredient.of(item));
    }

    public ShapedForgingRecipeBuilder2 define(Character pSymbol, Ingredient pIngredient) {
        if (this.key.containsKey(pSymbol)) {
            throw new IllegalArgumentException("Symbol '" + pSymbol + "' is already defined!");
        } else if (pSymbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(pSymbol, pIngredient);
            return this;
        }
    }

    public ShapedForgingRecipeBuilder2 pattern(String pPattern) {
        if (!this.rows.isEmpty() && pPattern.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(pPattern);
            return this;
        }
    }

    public ShapedForgingRecipeBuilder2 unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
        this.advancement.addCriterion(pCriterionName, pCriterionTrigger);
        return this;
    }

    public ShapedForgingRecipeBuilder2 group(@Nullable String pGroupName) {
        this.group = pGroupName;
        return this;
    }

    public ShapedForgingRecipeBuilder2 showNotification(boolean pShowNotification) {
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

        boolean needsVariants = width < 3 || height < 3;

        if (needsVariants) {
            generateAll3x3Variants(pRecipeOutput, pRecipeId);
        } else {
            NonNullList<Ingredient> ingredients = getIngredients(width, height);
            pRecipeOutput.accept(new Result(
                    ingredients, this.hammering, new ItemStack(this.result, this.count),
                    pRecipeId, this.rows, this.key, this.advancement,
                    pRecipeId.withPrefix("recipes/" + this.category.getFolderName() + "/"),
                    this.showNotification
            ));
        }
    }

    private void generateAll3x3Variants(Consumer<FinishedRecipe> pRecipeOutput, ResourceLocation baseId) {
        int originalWidth = this.rows.isEmpty() ? 0 : this.rows.get(0).length();
        int originalHeight = this.rows.size();

        int padX = 3 - originalWidth;
        int padY = 3 - originalHeight;

        List<String> baseRows = new ArrayList<>(this.rows);

        // Ensure all rows are same width
        for (int i = 0; i < baseRows.size(); i++) {
            baseRows.set(i, padRight(baseRows.get(i), originalWidth));
        }

        int variantId = 0;

        for (int top = 0; top <= padY; top++) {
            for (int left = 0; left <= padX; left++) {
                List<String> padded = new ArrayList<>();

                // Add top empty rows
                for (int i = 0; i < top; i++) padded.add("   ");

                // Add existing rows with left/right padding
                for (String row : baseRows) {
                    String paddedRow = padLeft(row, left);
                    paddedRow = padRight(paddedRow, 3);
                    padded.add(paddedRow);
                }

                // Add bottom empty rows
                while (padded.size() < 3) padded.add("   ");

                // Now create variant recipe
                int width = 3, height = 3;
                NonNullList<Ingredient> ingredients = NonNullList.withSize(9, Ingredient.EMPTY);

                for (int i = 0; i < 3; i++) {
                    String row = padded.get(i);
                    for (int j = 0; j < 3; j++) {
                        char c = row.charAt(j);
                        Ingredient ingredient = this.key.getOrDefault(c, Ingredient.EMPTY);
                        ingredients.set(i * 3 + j, ingredient);
                    }
                }

                ResourceLocation variantIdLoc = ResourceLocation.tryBuild(baseId.getNamespace(), baseId.getPath() + "_variant" + (++variantId));

                pRecipeOutput.accept(new Result(
                        ingredients, this.hammering, new ItemStack(this.result, this.count),
                        variantIdLoc, padded, this.key, this.advancement,
                        variantIdLoc.withPrefix("recipes/" + this.category.getFolderName() + "/"),
                        this.showNotification
                ));
            }
        }
    }

    private String padLeft(String s, int n) {
        return " ".repeat(n) + s;
    }

    private String padRight(String s, int n) {
        return s + " ".repeat(n - s.length());
    }

    private NonNullList<Ingredient> getIngredients(int width, int height) {
        NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
        for (int i = 0; i < height; ++i) {
            String row = this.rows.get(i);
            for (int j = 0; j < width; ++j) {
                char c = row.charAt(j);
                Ingredient ingredient = this.key.getOrDefault(c, Ingredient.EMPTY);
                ingredients.set(i * width + j, ingredient);
            }
        }
        return ingredients;
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


        public Result(NonNullList<Ingredient> ingredients, int hammering, ItemStack result, ResourceLocation id, List<String> pattern, Map<Character, Ingredient> key, Advancement.Builder advancement, ResourceLocation advancementId, boolean showNotification) {
            this.ingredients = ingredients;
            this.hammering = hammering;
            this.result = result;
            this.id = id;
            this.pattern = pattern;
            this.key = key;
            this.advancement = advancement;
            this.advancementId = advancementId;
            this.showNotification = showNotification;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray patternArray = new JsonArray();

            for (String s : this.pattern) {
                patternArray.add(s);
            }

            json.add("pattern", patternArray);
            JsonObject jsonobject = new JsonObject();

            json.addProperty("hammering", this.hammering);
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
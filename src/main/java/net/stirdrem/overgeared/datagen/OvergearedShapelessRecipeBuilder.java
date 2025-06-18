package net.stirdrem.overgeared.datagen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.ModRecipeSerializers;

public class OvergearedShapelessRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
    @Nullable
    private String group;

    public OvergearedShapelessRecipeBuilder(RecipeCategory pCategory, ItemLike pResult, int pCount) {
        this.category = pCategory;
        this.result = pResult.asItem();
        this.count = pCount;
    }

    public static OvergearedShapelessRecipeBuilder shapeless(RecipeCategory pCategory, ItemLike pResult) {
        return new OvergearedShapelessRecipeBuilder(pCategory, pResult, 1);
    }

    public static OvergearedShapelessRecipeBuilder shapeless(RecipeCategory pCategory, ItemLike pResult, int pCount) {
        return new OvergearedShapelessRecipeBuilder(pCategory, pResult, pCount);
    }

    public OvergearedShapelessRecipeBuilder requires(TagKey<Item> pTag) {
        return this.requires(Ingredient.of(pTag));
    }

    public OvergearedShapelessRecipeBuilder requires(ItemLike pItem) {
        return this.requires(pItem, 1);
    }

    public OvergearedShapelessRecipeBuilder requires(ItemLike pItem, int pQuantity) {
        for (int i = 0; i < pQuantity; ++i) {
            this.requires(Ingredient.of(pItem));
        }
        return this;
    }

    public OvergearedShapelessRecipeBuilder requires(Ingredient pIngredient) {
        return this.requires(pIngredient, 1);
    }

    public OvergearedShapelessRecipeBuilder requires(Ingredient pIngredient, int pQuantity) {
        for (int i = 0; i < pQuantity; ++i) {
            this.ingredients.add(pIngredient);
        }
        return this;
    }

    public OvergearedShapelessRecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
        this.advancement.addCriterion(pCriterionName, pCriterionTrigger);
        return this;
    }

    public OvergearedShapelessRecipeBuilder group(@Nullable String pGroupName) {
        this.group = pGroupName;
        return this;
    }

    public Item getResult() {
        return this.result;
    }

    public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
        this.ensureValid(pRecipeId);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId))
                .rewards(AdvancementRewards.Builder.recipe(pRecipeId))
                .requirements(RequirementsStrategy.OR);
        pFinishedRecipeConsumer.accept(new Result(pRecipeId, this.result, this.count,
                this.group == null ? "" : this.group,
                determineBookCategory(this.category),
                this.ingredients, this.advancement,
                pRecipeId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation pId) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + pId);
        }
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final Item result;
        private final int count;
        private final String group;
        private final CraftingBookCategory category;
        private final List<Ingredient> ingredients;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation pId, Item pResult, int pCount, String pGroup,
                      CraftingBookCategory pCategory, List<Ingredient> pIngredients,
                      Advancement.Builder pAdvancement, ResourceLocation pAdvancementId) {
            this.id = pId;
            this.result = pResult;
            this.count = pCount;
            this.group = pGroup;
            this.category = pCategory;
            this.ingredients = pIngredients;
            this.advancement = pAdvancement;
            this.advancementId = pAdvancementId;
        }

        public void serializeRecipeData(JsonObject pJson) {
            if (!this.group.isEmpty()) {
                pJson.addProperty("group", this.group);
            }
            pJson.addProperty("category", this.category.getSerializedName());

            JsonArray jsonarray = new JsonArray();
            for (Ingredient ingredient : this.ingredients) {
                jsonarray.add(ingredient.toJson());
            }
            pJson.add("ingredients", jsonarray);

            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
            if (this.count > 1) {
                jsonobject.addProperty("count", this.count);
            }
            pJson.add("result", jsonobject);
        }

        public RecipeSerializer<?> getType() {
            return ModRecipeSerializers.CRAFTING_SHAPELESS.get();
        }

        public ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
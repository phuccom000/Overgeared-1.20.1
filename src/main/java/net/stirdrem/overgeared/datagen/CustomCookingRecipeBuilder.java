package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CustomCookingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final CookingBookCategory bookCategory;
    private final Item resultItem;
    private final Ingredient ingredient;
    private final float experience;
    private final int cookingTime;
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
    @Nullable
    private String group;
    private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;
    @Nullable
    private CompoundTag resultNbt; // The NBT tag for the output

    CustomCookingRecipeBuilder(RecipeCategory category, CookingBookCategory bookCategory,
                               @Nullable CompoundTag resultNbt,
                               ItemLike result, Ingredient ingredient,
                               float experience, int cookingTime, RecipeSerializer<? extends AbstractCookingRecipe> serializer) {
        this.category = category;
        this.bookCategory = bookCategory;
        this.resultNbt = resultNbt;
        this.resultItem = result.asItem();
        this.ingredient = ingredient;
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.serializer = serializer;
    }

    public static CustomCookingRecipeBuilder generic(Ingredient pIngredient, RecipeCategory pCategory, @Nullable CompoundTag resultNbt,
                                                     ItemLike pResult, float pExperience, int pCookingTime, RecipeSerializer<? extends AbstractCookingRecipe> pSerializer) {
        return new CustomCookingRecipeBuilder(pCategory, determineRecipeCategory(pSerializer, pResult), resultNbt, pResult, pIngredient, pExperience, pCookingTime, pSerializer);
    }

    public static CustomCookingRecipeBuilder smelting(Ingredient pIngredient, RecipeCategory pCategory, @Nullable CompoundTag resultNbt,
                                                      ItemLike pResult, float pExperience, int pCookingTime) {
        return new CustomCookingRecipeBuilder(pCategory, determineBlastingRecipeCategory(pResult), resultNbt, pResult, pIngredient, pExperience, pCookingTime, RecipeSerializer.SMELTING_RECIPE);
    }

    public static CustomCookingRecipeBuilder blasting(Ingredient pIngredient, RecipeCategory pCategory, @Nullable CompoundTag resultNbt,
                                                      ItemLike pResult, float pExperience, int pCookingTime) {
        return new CustomCookingRecipeBuilder(pCategory, determineBlastingRecipeCategory(pResult), resultNbt, pResult, pIngredient, pExperience, pCookingTime, RecipeSerializer.BLASTING_RECIPE);
    }

    // You can add campfireCooking, blasting, smoking factory methods similarly if needed.

    public CustomCookingRecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    public CustomCookingRecipeBuilder unlockedBy(String criterionName, CriterionTriggerInstance criterionTrigger) {
        this.advancement.addCriterion(criterionName, criterionTrigger);
        return this;
    }

    /**
     * Set the NBT data for the output item
     */
    public CustomCookingRecipeBuilder withResultNbt(String nbtJson) {
        try {
            this.resultNbt = TagParser.parseTag(nbtJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid NBT json: " + nbtJson, e);
        }
        return this;
    }

    public Item getResult() {
        return this.resultItem;
    }

    private static CookingBookCategory determineBlastingRecipeCategory(ItemLike pResult) {
        return pResult.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
    }

    private static CookingBookCategory determineRecipeCategory(RecipeSerializer<? extends AbstractCookingRecipe> pSerializer, ItemLike pResult) {
        if (pSerializer == RecipeSerializer.SMELTING_RECIPE) {
            return determineSmeltingRecipeCategory(pResult);
        } else if (pSerializer == RecipeSerializer.BLASTING_RECIPE) {
            return determineBlastingRecipeCategory(pResult);
        } else if (pSerializer != RecipeSerializer.SMOKING_RECIPE && pSerializer != RecipeSerializer.CAMPFIRE_COOKING_RECIPE) {
            throw new IllegalStateException("Unknown cooking recipe type");
        } else {
            return CookingBookCategory.FOOD;
        }
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation recipeId) {
        this.ensureValid(recipeId);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId))
                .requirements(RequirementsStrategy.OR);

        consumer.accept(new Result(recipeId, this.group == null ? "" : this.group, this.bookCategory, this.ingredient,
                this.resultItem, this.experience, this.cookingTime, this.advancement, recipeId.withPrefix("recipes/" + this.category.getFolderName() + "/"),
                this.serializer, this.resultNbt));
    }

    private void ensureValid(ResourceLocation id) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }

    private static CookingBookCategory determineSmeltingRecipeCategory(ItemLike result) {
        if (result.asItem().isEdible()) {
            return CookingBookCategory.FOOD;
        } else {
            return result.asItem() instanceof BlockItem ? CookingBookCategory.BLOCKS : CookingBookCategory.MISC;
        }
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final String group;
        private final CookingBookCategory category;
        private final Ingredient ingredient;
        private final Item result;
        private final float experience;
        private final int cookingTime;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;
        private final RecipeSerializer<? extends AbstractCookingRecipe> serializer;
        @Nullable
        private final CompoundTag resultNbt;

        public Result(ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient,
                      Item result, float experience, int cookingTime, Advancement.Builder advancement,
                      ResourceLocation advancementId, RecipeSerializer<? extends AbstractCookingRecipe> serializer,
                      @Nullable CompoundTag resultNbt) {
            this.id = id;
            this.group = group;
            this.category = category;
            this.ingredient = ingredient;
            this.result = result;
            this.experience = experience;
            this.cookingTime = cookingTime;
            this.advancement = advancement;
            this.advancementId = advancementId;
            this.serializer = serializer;
            this.resultNbt = resultNbt;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (!this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }
            json.addProperty("category", this.category.getSerializedName());
            json.add("ingredient", this.ingredient.toJson());
            json.addProperty("experience", this.experience);
            json.addProperty("cookingtime", this.cookingTime);

            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());

            if (this.resultNbt != null && !this.resultNbt.isEmpty()) {
                // Convert the CompoundTag to a JsonObject
                JsonObject nbtJson = new JsonObject();
                for (String key : this.resultNbt.getAllKeys()) {
                    Tag tag = this.resultNbt.get(key);
                    // Handle different tag types appropriately
                    if (tag instanceof IntTag) {
                        nbtJson.addProperty(key, ((IntTag) tag).getAsInt());
                    } else if (tag instanceof StringTag) {
                        nbtJson.addProperty(key, tag.getAsString());
                    }
                    // Add more cases as needed for other tag types
                }
                resultObject.add("nbt", nbtJson);
            }

            json.add("result", resultObject);
        }


        @Override
        public RecipeSerializer<?> getType() {
            return this.serializer;
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}

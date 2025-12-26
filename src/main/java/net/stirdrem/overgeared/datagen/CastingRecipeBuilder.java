package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;
import net.stirdrem.overgeared.recipe.ModRecipes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CastingRecipeBuilder implements RecipeBuilder {

    private final ItemLike result;
    private final float experience;
    private final int cookTime;

    private final Map<String, Integer> materialInput = new HashMap<>();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    private String toolType;
    @Nullable
    private Boolean needPolishing = null;
    @Nullable
    private String group = "";
    @Nullable
    private String category = "misc";

    private CastingRecipeBuilder(ItemLike result, float xp, int cookTime) {
        this.result = result;
        this.experience = xp;
        this.cookTime = cookTime;
    }

    /* ================= FACTORY ================= */

    public static CastingRecipeBuilder casting(ItemLike result, float xp, int cookTime) {
        return new CastingRecipeBuilder(result, xp, cookTime);
    }

    /* ================= FLUENT API ================= */

    public CastingRecipeBuilder toolType(String type) {
        this.toolType = type;
        return this;
    }

    public CastingRecipeBuilder material(String material, int amount) {
        this.materialInput.put(material, amount);
        return this;
    }

    public CastingRecipeBuilder needsPolishing(boolean flag) {
        this.needPolishing = flag;
        return this;
    }

    @Override
    public CastingRecipeBuilder unlockedBy(String name, CriterionTriggerInstance trigger) {
        this.advancement.addCriterion(name, trigger);
        return this;
    }

    @Override
    public CastingRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public CastingRecipeBuilder category(String category) {
        this.category = category;
        return this;
    }

    /* ================= RECIPEBUILDER ================= */

    @Override
    public Item getResult() {
        return result.asItem();
    }

    @Override
    public void save(Consumer<FinishedRecipe> out, ResourceLocation id) {
        ensureValid(id);
        ResourceLocation recipeId =
                new ResourceLocation(id.getNamespace(), id.getPath() + "_from_cast_furnace");
        advancement.parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(RequirementsStrategy.OR);

        out.accept(new Result(
                recipeId,
                result,
                group,
                category,
                toolType,
                materialInput,
                experience,
                cookTime,
                needPolishing,
                advancement,
                id.withPrefix("recipes/casting/")
        ));
    }

    /* ================= VALIDATION ================= */

    private void ensureValid(ResourceLocation id) {
        if (toolType == null)
            throw new IllegalStateException("Missing tool_type for casting recipe " + id);

        if (materialInput.isEmpty())
            throw new IllegalStateException("No material input defined for " + id);

        if (advancement.getCriteria().isEmpty())
            throw new IllegalStateException("No unlock criteria for " + id);
    }

    /* ================= RESULT ================= */

    public static class Result implements FinishedRecipe {

        private final ResourceLocation id;
        private final ItemLike result;
        private final String group;
        private final String category;
        private final String toolType;
        private final Map<String, Integer> input;
        private final float xp;
        private final int cookTime;
        private final Boolean needPolishing;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(
                ResourceLocation id,
                ItemLike result,
                String group,
                String category,
                String toolType,
                Map<String, Integer> input,
                float xp,
                int cookTime,
                Boolean needPolishing,
                Advancement.Builder advancement,
                ResourceLocation advancementId
        ) {
            this.id = id;
            this.result = result;
            this.group = group;
            this.category = category;
            this.toolType = toolType;
            this.input = input;
            this.xp = xp;
            this.cookTime = cookTime;
            this.needPolishing = needPolishing;
            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {

            if (group != null && !group.isEmpty())
                json.addProperty("group", group);

            if (category != null)
                json.addProperty("category", category);

            json.addProperty("tool_type", toolType);

            JsonObject inputObj = new JsonObject();
            JsonObject materialObj = new JsonObject();

            input.forEach(materialObj::addProperty);

            inputObj.add("material", materialObj);
            json.add("input", inputObj);


            JsonObject resultObj = new JsonObject();
            resultObj.addProperty(
                    "item",
                    BuiltInRegistries.ITEM.getKey(result.asItem()).toString()
            );
            json.add("result", resultObj);

            if (needPolishing != null)
                json.addProperty("need_polishing", needPolishing);

            json.addProperty("experience", xp);
            json.addProperty("cookingtime", cookTime);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return ModRecipes.CASTING.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return advancementId;
        }
    }
}

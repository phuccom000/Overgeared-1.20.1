package net.stirdrem.overgeared.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.CastBlastingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ToolCastBlastingRecipeBuilder implements RecipeBuilder {

    private final ItemLike result;
    private final float experience;
    private final int cookTime;

    private final Map<String, Integer> materialInput = new HashMap<>();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private String toolType;
    @Nullable
    private Boolean needPolishing = null;
    @Nullable
    private String group = "misc";
    @Nullable
    private String category = "misc";

    public ToolCastBlastingRecipeBuilder(ItemLike result, float experience, int cookTime) {
        this.result = result;
        this.experience = experience;
        this.cookTime = cookTime;
    }

    public static ToolCastBlastingRecipeBuilder cast(ItemLike result, float xp, int time) {
        return new ToolCastBlastingRecipeBuilder(result, xp, time);
    }

    public ToolCastBlastingRecipeBuilder toolType(String type) {
        this.toolType = type;
        return this;
    }

    public ToolCastBlastingRecipeBuilder material(String material, int amount) {
        this.materialInput.put(material, amount);
        return this;
    }

    public ToolCastBlastingRecipeBuilder needsPolishing(boolean flag) {
        this.needPolishing = flag;
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public ToolCastBlastingRecipeBuilder category(String category) {
        this.category = category;
        return this;
    }

    @Override
    public Item getResult() {
        return result.asItem();
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        ensureValid(id);

        Advancement.Builder advBuilder = Advancement.Builder.recipeAdvancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advBuilder::addCriterion);

        // Convert Integer map to Double map for recipe
        Map<String, Double> requiredMaterials = new HashMap<>();
        materialInput.forEach((mat, amt) -> requiredMaterials.put(mat, (double) amt));

        // Determine cooking book category
        CookingBookCategory bookCategory = CookingBookCategory.MISC;
        
        // Create the actual CastBlastingRecipe instance
        CastBlastingRecipe recipe = new CastBlastingRecipe(
                group == null ? "" : group,
                bookCategory,
                new ItemStack(result),
                experience,
                cookTime,
                requiredMaterials,
                toolType,
                needPolishing != null ? needPolishing : true
        );

        // Use the suffix convention from the old Result.getId()
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_from_cast_blasting");
        output.accept(recipeId, recipe, advBuilder.build(id.withPrefix("recipes/misc/")));
    }

    private void ensureValid(ResourceLocation id) {
        if (toolType == null)
            throw new IllegalStateException("Tool type missing for " + id);

        if (materialInput.isEmpty())
            throw new IllegalStateException("No material input for " + id);

        if (criteria.isEmpty())
            throw new IllegalStateException("No unlock criteria for " + id);
    }
}

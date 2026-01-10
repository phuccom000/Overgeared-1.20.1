package net.stirdrem.overgeared.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.recipe.CastingRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class CastingRecipeBuilder implements RecipeBuilder {

    private final ItemLike result;
    private final float experience;
    private final int cookTime;

    private final Map<String, Integer> materialInput = new LinkedHashMap<>();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    private String toolType;
    @Nullable
    private Boolean needPolishing;
    @Nullable
    private String group;

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
        this.materialInput.put(material.toLowerCase(), amount);
        return this;
    }

    public CastingRecipeBuilder needsPolishing(boolean flag) {
        this.needPolishing = flag;
        return this;
    }

    @Override
    public CastingRecipeBuilder unlockedBy(String name, net.minecraft.advancements.Criterion<?> criterion) {
        this.advancement.addCriterion(name, criterion);
        return this;
    }

    @Override
    public CastingRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result.asItem();
    }

    /* ================= SAVE ================= */

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        ensureValid(id);

        Advancement.Builder advBuilder = Advancement.Builder.recipeAdvancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        CastingRecipe recipe = getCastingRecipe();
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_from_casting_furnace");

        output.accept(
                recipeId,
                recipe,
                advBuilder.build(id.withPrefix("recipes/casting/"))
        );
    }

    private @NotNull CastingRecipe getCastingRecipe() {
        CraftingBookCategory bookCategory = CraftingBookCategory.MISC;
        // Build recipe instance
        ItemStack resultStack = new ItemStack(this.result);
        return new CastingRecipe(
                this.group == null ? "" : this.group,
                bookCategory,
                this.materialInput,
                resultStack,
                this.experience,
                this.cookTime,
                this.toolType,
                Boolean.TRUE.equals(this.needPolishing)
        );
    }

    /* ================= VALIDATION ================= */

    private void ensureValid(ResourceLocation id) {
        if (toolType == null || toolType.isBlank()) {
            throw new IllegalStateException("Missing tool_type for casting recipe " + id);
        }

        if (materialInput.isEmpty()) {
            throw new IllegalStateException("No material input defined for casting recipe " + id);
        }
    }
}

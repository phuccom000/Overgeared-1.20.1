package net.stirdrem.overgeared.datagen;

import com.google.common.collect.Lists;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;

import javax.annotation.Nullable;
import java.util.*;

public class ShapedForgingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;

    private final int count;
    private final int hammering;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

    @Nullable
    private final List<String> blueprintTypes = new ArrayList<>();
    @Nullable
    private Boolean requiresBlueprint;
    @Nullable
    private Boolean hasQuality;
    @Nullable
    private Boolean hasPolishing;
    @Nullable
    private Boolean needQuenching;
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
    @Nullable
    private ForgingQuality qualityDifficulty;

    private boolean showNotification = true;


    public ShapedForgingRecipeBuilder(RecipeCategory category, ItemLike result, int count, int hammering) {
        this.category = category;
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

    @Override
    public ShapedForgingRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancement.addCriterion(name, criterion);
        return this;
    }

    @Override
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

    public ShapedForgingRecipeBuilder qualityDifficulty(@Nullable ForgingQuality qualityDifficulty) {
        this.qualityDifficulty = qualityDifficulty;
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

    public ShapedForgingRecipeBuilder setNeedQuenching(@Nullable boolean needQuenching) {
        this.needQuenching = needQuenching;
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
    public void save(RecipeOutput output, ResourceLocation id) {
        this.ensureValid(id);

        Advancement.Builder advBuilder = Advancement.Builder.recipeAdvancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advBuilder::addCriterion);

        // Pattern parsing logic
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
        
        // Resolve booleans and defaults
        boolean actualHasQuality = this.hasQuality == null || this.hasQuality;
        boolean actualRequiresBlueprint = this.requiresBlueprint != null ? this.requiresBlueprint : false;
        boolean actualNeedsMinigame = this.needsMinigame != null ? this.needsMinigame : false;
        boolean actualHasPolishing = this.hasPolishing != null ? this.hasPolishing : true;
        boolean actualNeedQuenching = this.needQuenching != null ? this.needQuenching : !(this.result instanceof ArmorItem);
        
        Set<String> actualBlueprintTypes = new LinkedHashSet<>(this.blueprintTypes);
        
        ForgingQuality actualMinQuality = this.minimumQuality != null ? this.minimumQuality : ForgingQuality.POOR;
        ForgingQuality actualQualityDiff = this.qualityDifficulty != null ? this.qualityDifficulty : ForgingQuality.NONE;
        String actualTier = this.tier == null ? AnvilTier.IRON.getDisplayName() : this.tier;
        ItemStack actualFailedResult = this.failedResult != null ? new ItemStack(this.failedResult, this.failedResultCount) : ItemStack.EMPTY;

        // Create the actual ForgingRecipe instance
        ForgingRecipe recipe = new ForgingRecipe(
                this.group == null ? "" : this.group,
                actualRequiresBlueprint,
                actualBlueprintTypes,
                actualTier,
                ingredients,
                new ItemStack(this.result, this.count),
                actualFailedResult,
                this.hammering,
                actualHasQuality,
                actualNeedsMinigame,
                actualHasPolishing,
                actualNeedQuenching,
                this.showNotification,
                actualMinQuality,
                actualQualityDiff,
                width,
                height
        );

        output.accept(id, recipe, advBuilder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }


    private void ensureValid(ResourceLocation pRecipeId) {
        if (this.rows.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped forging recipe " + pRecipeId + "!");
        }
        int width = this.rows.getFirst().length();
        for (String row : this.rows) {
            if (row.length() != width) {
                throw new IllegalStateException("Pattern must be the same width on every line!");
            }
        }
    }
}

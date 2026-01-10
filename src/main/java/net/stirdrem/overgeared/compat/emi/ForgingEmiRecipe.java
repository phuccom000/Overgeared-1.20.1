package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ForgingEmiRecipe implements EmiRecipe {
    private static final int DISPLAY_WIDTH = 150;
    private static final int DISPLAY_HEIGHT = 66;
    private static final int X_OFFSET = 8; // Offset to center the recipe

    private final ResourceLocation id;
    private final ForgingRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final List<EmiStack> blueprintStacks;

    public ForgingEmiRecipe(RecipeHolder<ForgingRecipe> holder) {
        this.id = holder.id();
        this.recipe = holder.value();

        // Convert ingredients to EMI format
        this.inputs = recipe.getIngredients().stream()
                .map(EmiIngredient::of)
                .toList();

        this.outputs = List.of(EmiStack.of(recipe.getResultItem(null)));

        // Create blueprint stacks for recipes that support blueprints
        this.blueprintStacks = createBlueprintStacks();
    }

    /**
     * Create blueprint ItemStacks for this recipe's valid blueprint types.
     */
    private List<EmiStack> createBlueprintStacks() {
        Set<String> types = recipe.getBlueprintTypes();
        if (types.isEmpty()) {
            return List.of();
        }

        List<EmiStack> stacks = new ArrayList<>();
        for (String type : types) {
            ItemStack stack = new ItemStack(ModItems.BLUEPRINT.get());
            // Use BlueprintData with builder pattern
            BlueprintData data = BlueprintData.createDefault()
                            .withToolType(type)
                            .withRequired(recipe.requiresBlueprint());
            stack.set(ModComponents.BLUEPRINT_DATA, data);
            stacks.add(EmiStack.of(stack));
        }
        return stacks;
    }


    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.FORGING_CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return DISPLAY_WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return DISPLAY_HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Blueprint slot on the left
        int blueprintX = X_OFFSET;
        int blueprintY = 24;
        if (!blueprintStacks.isEmpty()) {
            // Show blueprint variants cycling through
            widgets.addSlot(EmiIngredient.of(blueprintStacks), blueprintX, blueprintY);
        } else {
            widgets.addSlot(EmiStack.EMPTY, blueprintX, blueprintY);
        }

        // 3x3 crafting grid
        int gridStartX = X_OFFSET + 24;
        int gridStartY = 6;
        int slotSize = 18;

        int recipeWidth = recipe.width;
        int recipeHeight = recipe.height;

        // Create all 9 slots of the 3x3 grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int x = gridStartX + col * slotSize;
                int y = gridStartY + row * slotSize;

                int recipeIndex = row * recipeWidth + col;

                // Check if this grid position has an ingredient in the recipe
                if (col < recipeWidth && row < recipeHeight && recipeIndex < inputs.size()) {
                    widgets.addSlot(inputs.get(recipeIndex), x, y);
                } else {
                    widgets.addSlot(EmiStack.EMPTY, x, y);
                }
            }
        }

        // Arrow texture
        widgets.addTexture(EmiTexture.EMPTY_ARROW, X_OFFSET + 82, 24);

        // Output slot
        int outputX = X_OFFSET + 110;
        int outputY = 24;
        widgets.addSlot(outputs.get(0), outputX, outputY).large(true).recipeContext(this);

        // Draw "Hits: X" text (top right, above arrow)
        String hitsText = Component.translatable("tooltip.overgeared.recipe.hits", recipe.getRemainingHits()).getString();
        widgets.addText(Component.literal(hitsText), X_OFFSET + 82, 6, 0xFF808080, false);

        // Draw "Tier: X" text (bottom, below grid)
        String tierRaw = recipe.getAnvilTier();
        AnvilTier tierEnum = AnvilTier.fromDisplayName(tierRaw);
        Component tierText = Component.translatable("tooltip.overgeared.recipe.tier")
                .append(Component.literal(" "))
                .append(Component.translatable(tierEnum.getLang()));
        widgets.addText(tierText, X_OFFSET + 82, 54, 0xFF808080, false);
    }
}

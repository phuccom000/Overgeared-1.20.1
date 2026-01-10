package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;

import java.util.ArrayList;
import java.util.List;

public class KnappingEmiRecipe implements EmiRecipe {
    private final ResourceLocation id;
    private final RockKnappingRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public KnappingEmiRecipe(RecipeHolder<RockKnappingRecipe> holder) {
        this.id = holder.id();
        this.recipe = holder.value();
        this.outputs = List.of(EmiStack.of(recipe.output()));

        // Build inputs based on pattern (true = Rock, false = Empty)
        List<EmiIngredient> inputList = new ArrayList<>();
        boolean[][] pattern = recipe.pattern();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (pattern[row][col]) {
                    inputList.add(EmiStack.of(ModItems.ROCK.get()));
                } else {
                    inputList.add(EmiStack.EMPTY);
                }
            }
        }
        this.inputs = inputList;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.KNAPPING_CATEGORY;
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
        return 130;
    }

    @Override
    public int getDisplayHeight() {
        return 60;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Draw grid of slots
        int startX = 4;
        int startY = 4;
        int slotSize = 18; // Standard EMI slot size

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                EmiIngredient input = inputs.get(index);
                widgets.addSlot(input, startX + col * slotSize, startY + row * slotSize);
            }
        }

        // Arrow
        widgets.addTexture(EmiTexture.EMPTY_ARROW, startX + 3 * slotSize + 4, startY + slotSize);

        // Output
        widgets.addSlot(outputs.getFirst(), startX + 3 * slotSize + 32, startY + slotSize - 4).large(true).recipeContext(this);
    }
}

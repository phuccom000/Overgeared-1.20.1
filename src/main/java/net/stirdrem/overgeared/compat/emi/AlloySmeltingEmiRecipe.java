package net.stirdrem.overgeared.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.AlloySmeltingRecipe;

import java.util.List;

public class AlloySmeltingEmiRecipe implements EmiRecipe {
    private static final ResourceLocation TEXTURE = OvergearedMod.loc("textures/gui/brick_alloy_furnace.png");
    
    private final ResourceLocation id;
    private final AlloySmeltingRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public AlloySmeltingEmiRecipe(RecipeHolder<AlloySmeltingRecipe> holder) {
        this.id = holder.id();
        this.recipe = holder.value();
        
        this.inputs = recipe.getIngredientsList().stream()
                .map(EmiIngredient::of)
                .toList();
        
        this.outputs = List.of(EmiStack.of(recipe.getResultItem()));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.ALLOY_SMELTING_CATEGORY;
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
        widgets.addTexture(TEXTURE, 55, 14, 24, 17, 176, 14); // Arrow
        widgets.addTexture(TEXTURE, 2, 28, 14, 14, 176, 0); // Flame

        // Inputs (2x2 grid logic)
        // 0: 39, 26 -> 9, 6
        // 1: 57, 26 -> 27, 6
        // 2: 39, 44 -> 9, 24
        // 3: 57, 44 -> 27, 24
        
        int inputCount = inputs.size();
        
        // Slot 0 (39, 26)
        if (inputCount > 0) widgets.addSlot(inputs.get(0), 9, 6);
        else widgets.addSlot(EmiStack.EMPTY, 9, 6);

        // Slot 1 (57, 26)
        if (inputCount > 1) widgets.addSlot(inputs.get(1), 27, 6);
        else widgets.addSlot(EmiStack.EMPTY, 27, 6);

        // Slot 2 (39, 44)
        if (inputCount > 2) widgets.addSlot(inputs.get(2), 9, 24);
        else widgets.addSlot(EmiStack.EMPTY, 9, 24);

        // Slot 3 (57, 44)
        if (inputCount > 3) widgets.addSlot(inputs.get(3), 27, 24);
        else widgets.addSlot(EmiStack.EMPTY, 27, 24);

        // Output (124, 35) -> 94, 15
        widgets.addSlot(outputs.getFirst(), 94, 15).large(true).recipeContext(this);
        
        // Cooking time text?
        float xp = recipe.getExperience();
        if (xp > 0) {
            widgets.addText(Component.translatable("emi.cooking.experience", xp), 55, 34, 0xFF808080, false);
        }
    }
}

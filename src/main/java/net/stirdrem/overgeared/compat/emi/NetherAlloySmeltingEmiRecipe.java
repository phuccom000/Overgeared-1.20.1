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
import net.stirdrem.overgeared.recipe.NetherAlloySmeltingRecipe;

import java.util.List;

public class NetherAlloySmeltingEmiRecipe implements EmiRecipe {
    private static final ResourceLocation TEXTURE = OvergearedMod.loc("textures/gui/nether_alloy_furnace.png");
    
    private final ResourceLocation id;
    private final NetherAlloySmeltingRecipe recipe;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public NetherAlloySmeltingEmiRecipe(RecipeHolder<NetherAlloySmeltingRecipe> holder) {
        this.id = holder.id();
        this.recipe = holder.value();
        
        this.inputs = recipe.getIngredientsList().stream()
                .map(EmiIngredient::of)
                .toList();
        
        this.outputs = List.of(EmiStack.of(recipe.getResultItem(null)));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return OvergearedEmiPlugin.NETHER_ALLOY_SMELTING_CATEGORY;
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
        return 140;
    }

    @Override
    public int getDisplayHeight() {
        return 80;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEXTURE, 69, 24, 24, 17, 176, 14);
        widgets.addTexture(TEXTURE, 4, 38, 14, 14, 176, 0);

        int inputCount = inputs.size();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                int x = 10 + col * 18;
                int y = 7 + row * 18;
                
                if (index < inputCount) {
                    widgets.addSlot(inputs.get(index), x, y);
                } else {
                    widgets.addSlot(EmiStack.EMPTY, x, y);
                }
            }
        }

        widgets.addSlot(outputs.getFirst(), 104, 25).large(true).recipeContext(this);
        
        float xp = recipe.getExperience();
        if (xp > 0) {
            widgets.addText(Component.translatable("emi.cooking.experience", xp), 69, 44, 0xFF808080, false);
        }
    }
}

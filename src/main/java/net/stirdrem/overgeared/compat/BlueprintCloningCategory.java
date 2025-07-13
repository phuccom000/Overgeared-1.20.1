package net.stirdrem.overgeared.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.BlueprintCloningRecipe;

import java.util.List;

public class BlueprintCloningCategory implements IRecipeCategory<BlueprintCloningRecipe> {
    public static final RecipeType<BlueprintCloningRecipe> TYPE =
            RecipeType.create("overgeared", "blueprint_cloning", BlueprintCloningRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public BlueprintCloningCategory(IJeiHelpers helpers) {
        this.background = helpers.getGuiHelper().createBlankDrawable(150, 50);
        this.icon = helpers.getGuiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.BLUEPRINT.get()));
    }

    @Override
    public RecipeType<BlueprintCloningRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.overgeared.blueprint_cloning");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlueprintCloningRecipe recipe, IFocusGroup focusGroup) {
        int x = 20;
        int y = 15;
        builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                .addIngredients(VanillaTypes.ITEM_STACK, List.of(new ItemStack(ModItems.BLUEPRINT.get())));

        builder.addSlot(RecipeIngredientRole.INPUT, x + 20, y)
                .addIngredients(VanillaTypes.ITEM_STACK, List.of(new ItemStack(ModItems.EMPTY_BLUEPRINT.get())));

        builder.addSlot(RecipeIngredientRole.OUTPUT, x + 60, y)
                .addItemStack(recipe.getResultItem(null)); // You can construct a dummy result
    }
}

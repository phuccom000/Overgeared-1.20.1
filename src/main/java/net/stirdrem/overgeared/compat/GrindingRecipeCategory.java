package net.stirdrem.overgeared.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.GrindingRecipe;

public class GrindingRecipeCategory implements IRecipeCategory<GrindingRecipe> {
    // Recipe type and UID
    public static final RecipeType<GrindingRecipe> TYPE =
            RecipeType.create(OvergearedMod.MOD_ID, "grinding", GrindingRecipe.class);

    public static final ResourceLocation UID = new ResourceLocation(OvergearedMod.MOD_ID, "grinding");

    // Custom GUI texture location
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(OvergearedMod.MOD_ID, "textures/gui/grinding_jei.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public GrindingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(TEXTURE, 0, 0, 76, 18).setTextureSize(76, 18).build(); // Adjust width/height as needed
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.GRINDSTONE)); // Or your custom icon
        this.title = Component.translatable("gui.overgeared.jei.category.grinding");
    }

    @Override
    public RecipeType<GrindingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
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
    public void setRecipe(IRecipeLayoutBuilder builder, GrindingRecipe recipe, IFocusGroup focuses) {
        // Define input slot (position based on your grinding_jei.png layout)
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addIngredients(recipe.getInput());

        // Define output slot (position based on your grinding_jei.png layout)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 1)
                .addItemStack(recipe.getOutput());
    }
}
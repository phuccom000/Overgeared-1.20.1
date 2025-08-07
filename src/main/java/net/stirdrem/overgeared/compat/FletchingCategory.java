package net.stirdrem.overgeared.compat;

import mezz.jei.api.constants.VanillaTypes;
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
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.FletchingRecipe;

public class FletchingCategory implements IRecipeCategory<FletchingRecipe> {

    public static final ResourceLocation UID = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "fletching");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/fletching_table_jei.png"); // match your actual texture

    public static final RecipeType<FletchingRecipe> FLETCHING_RECIPE_TYPE =
            new RecipeType<>(UID, FletchingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public FletchingCategory(IGuiHelper helper) {
        // width & height should match the area where recipe is displayed in JEI
        this.background = helper.createDrawable(TEXTURE, 29, 16, 118, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(Blocks.FLETCHING_TABLE)); // Or the block item for the station
    }


    @Override
    public RecipeType<FletchingRecipe> getRecipeType() {
        return FLETCHING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.jei.category.fletching");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FletchingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 37, 1).addIngredients(recipe.getTip());
        builder.addSlot(RecipeIngredientRole.INPUT, 19, 19).addIngredients(recipe.getShaft());
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 37).addIngredients(recipe.getFeather());
        builder.addSlot(RecipeIngredientRole.INPUT, 63, 37).addIngredients(recipe.getPotion());


        // If no focus (viewing all recipes), show the default output
        builder.addSlot(RecipeIngredientRole.OUTPUT, 97, 19)
                .addItemStack(recipe.getDefaultResult());

    }
}

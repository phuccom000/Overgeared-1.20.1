package net.stirdrem.overgeared.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.SlotItemHandler;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.recipe.ForgingRecipe;

public class ForgingRecipeCategory implements IRecipeCategory<ForgingRecipe> {
    public static final ResourceLocation UID = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "forging");
    public static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(OvergearedMod.MOD_ID,
            "textures/gui/smithing_anvil_jei.png");

    public static final RecipeType<ForgingRecipe> FORGING_RECIPE_TYPE =
            new RecipeType<>(UID, ForgingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private static final int imageWidth = 116;
    private static final int imageHeight = 54;


    public ForgingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 29, 16, 116, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SMITHING_ANVIL.get()));
    }

    @Override
    public RecipeType<ForgingRecipe> getRecipeType() {
        return FORGING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.overgeared.smithing_anvil.gui");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void draw(ForgingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //guiGraphics.blit(TEXTURE, 0, 0, 0, 0, imageWidth, imageHeight);
        String hitsText = "Hits Need: " + recipe.getRemainingHits();
        int x = imageWidth / 2;
        int y = imageHeight / 2;
        guiGraphics.drawString(Minecraft.getInstance().font, hitsText, 57, 1, 4210752, false); // White color
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ForgingRecipe recipe, IFocusGroup focuses) {
        int width = recipe.width;
        int height = recipe.height;
        NonNullList<Ingredient> ingredients = recipe.getIngredients();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                Ingredient ingredient = ingredients.get(index);
                builder.addSlot(RecipeIngredientRole.INPUT, 1 + x * 18, 1 + y * 18)
                        .addIngredients(ingredient);
            }
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .addItemStack(recipe.getResultItem(null));

    }

}
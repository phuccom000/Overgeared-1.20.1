package net.stirdrem.overgeared.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.recipe.AlloySmeltingRecipe;

import java.util.List;


public class AlloySmeltingRecipeCategory implements IRecipeCategory<AlloySmeltingRecipe> {

    public static final ResourceLocation UID =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "alloy_smelting");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/furnace_jei.png");

    // JEI recipe type — placed under vanilla smelting tab
    public static final RecipeType<AlloySmeltingRecipe> ALLOY_SMELTING_TYPE =
            new RecipeType<>(UID, AlloySmeltingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final int animationTime = 200; // full cycle in ticks
    private final IDrawableAnimated arrowAnimated;
    private final IDrawableStatic arrowStatic;
    private final IDrawableAnimated flameAnimated;
    private final IDrawableStatic flameStatic;

    public AlloySmeltingRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 107, 43)
                .setTextureSize(130, 43)
                .build();

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.ALLOY_FURNACE.get()));

        arrowStatic = helper.drawableBuilder(TEXTURE, 107, 14, 22, 16).setTextureSize(130, 43).build();
        arrowAnimated = helper.createAnimatedDrawable(arrowStatic, animationTime, IDrawableAnimated.StartDirection.LEFT, false);

        flameStatic = helper.drawableBuilder(TEXTURE, 107, 0, 14, 13).setTextureSize(130, 43).build();
        flameAnimated = helper.createAnimatedDrawable(
                flameStatic,
                100, // burn time
                IDrawableAnimated.StartDirection.TOP,
                true
        );
    }

    @Override
    public RecipeType<AlloySmeltingRecipe> getRecipeType() {
        return ALLOY_SMELTING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.jei.category.alloy_smelting");
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
    public void draw(AlloySmeltingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Float exp = recipe.getExperience();
        arrowAnimated.draw(guiGraphics, 47, 9);
        flameAnimated.draw(guiGraphics, 51, 29);

        // Draw experience with formatting to avoid trailing .0
        String expText;
        if (exp == exp.intValue()) {
            expText = exp.intValue() + " XP";
        } else {
            expText = String.format("%.1f XP", exp);
        }

        // Calculate X position to right-align the text
        int textWidth = Minecraft.getInstance().font.width(expText);
        int xPos = this.background.getWidth() - textWidth; // 5 pixels from right edge

        guiGraphics.drawString(Minecraft.getInstance().font, expText, xPos, 35, 0xFFFFFFFF, true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlloySmeltingRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.getIngredientsList();

        // Add multiple input slots based on the recipe ingredients
        // Position inputs in a grid pattern to accommodate up to 4 ingredients
        int inputCount = ingredients.size();

        if (inputCount >= 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                    .addIngredients(ingredients.get(0));
        }
        if (inputCount >= 2) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                    .addIngredients(ingredients.get(1));
        }
        if (inputCount >= 3) {
            builder.addSlot(RecipeIngredientRole.INPUT, 19, 1)
                    .addIngredients(ingredients.get(2));
        }
        if (inputCount >= 4) {
            builder.addSlot(RecipeIngredientRole.INPUT, 19, 19)
                    .addIngredients(ingredients.get(3));
        }

        // If there are more than 4 ingredients (unlikely but possible), add them in additional rows
        if (inputCount > 4) {
            for (int i = 4; i < inputCount && i < 8; i++) {
                int row = (i - 4) / 2;
                int col = (i - 4) % 2;
                builder.addSlot(RecipeIngredientRole.INPUT, 37 + col * 18, 1 + row * 18)
                        .addIngredients(ingredients.get(i));
            }
        }

        // Output slot
        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 10)
                .addItemStack(recipe.getResultItem(null));
    }
}
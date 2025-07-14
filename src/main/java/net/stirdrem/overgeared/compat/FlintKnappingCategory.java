package net.stirdrem.overgeared.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.ExplanationRecipe;

import java.util.List;

public class FlintKnappingCategory implements IRecipeCategory<ExplanationRecipe> {
    private static final ResourceLocation BACKGROUND_LOCATION =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/explanation_jei.png");

    public static final ResourceLocation UID =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "flint_knapping");

    public static final RecipeType<ExplanationRecipe> FLINT_KNAPPING =
            new RecipeType<>(UID, ExplanationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public FlintKnappingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, 150, 200)
                .setTextureSize(150, 200) // This ensures the texture isn't stretched
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.FLINT));
        this.title = Component.translatable("jei.overgeared.category.flint_knapping");
    }

    @Override
    public RecipeType<ExplanationRecipe> getRecipeType() {
        return FLINT_KNAPPING;
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
    public void setRecipe(IRecipeLayoutBuilder builder, ExplanationRecipe recipe, IFocusGroup focuses) {
        // Add input slot (flint)
        builder.addSlot(RecipeIngredientRole.INPUT, 72, 18)
                .addItemStack(new ItemStack(Items.FLINT))
                /*.addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.add(Component.translatable("jei.overgeared.flint_knapping.input_tooltip"));
                })*/
        ;

        // Add output slot (result)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 60)
                .addItemStack(recipe.getResultItem())
                /*.addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.add(Component.translatable("jei.overgeared.flint_knapping.output_tooltip1"));
                    tooltip.add(Component.translatable("jei.overgeared.flint_knapping.output_tooltip2"));
                })*/
        ;
    }

    @Override
    public void draw(ExplanationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // You can add additional drawing here if needed
        // For example, draw text explanations
        int textWidth = 140;
        int textX = 5;
        int textY = 85;
        renderWrappedText(
                guiGraphics,
                Component.translatable("jei.overgeared.flint_knapping.description"),
                textX, textY,
                textWidth,
                ChatFormatting.DARK_GRAY.getColor(), // White color
                false
        );
    }

    public static void renderWrappedText(GuiGraphics guiGraphics, Component text, int x, int y, int width, int color, boolean shadow) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = font.split(text, width);

        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawString(font, lines.get(i), x, y + (i * font.lineHeight), color, shadow);
        }
    }

    public static int calculateTextHeight(Component text, int width) {
        Font font = Minecraft.getInstance().font;
        return font.split(text, width).size() * font.lineHeight;
    }
}
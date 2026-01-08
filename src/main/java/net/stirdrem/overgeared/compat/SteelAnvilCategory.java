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
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.recipe.ExplanationRecipe;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;
import java.util.stream.Collectors;

public class SteelAnvilCategory implements IRecipeCategory<ExplanationRecipe> {
    private static final ResourceLocation BACKGROUND_LOCATION =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/explanation_jei.png");

    public static final ResourceLocation UID =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_anvil");

    public static final RecipeType<ExplanationRecipe> STEEL_ANVIL_GET =
            new RecipeType<>(UID, ExplanationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    private final List<ItemStack> hammerItems;
    private static final int CYCLE_TIME = 60; // 3 seconds at 20 ticks/second

    public SteelAnvilCategory(IGuiHelper guiHelper, RegistryAccess registryAccess) {
        this.background = guiHelper.drawableBuilder(BACKGROUND_LOCATION, 0, 0, 150, 120)
                .setTextureSize(150, 120) // This ensures the texture isn't stretched
                .build();
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.SMITHING_ANVIL.get()));
        this.title = Component.translatable("jei.overgeared.category.steel_anvil");

        // Get all items from the smithing hammers tag
        TagKey<Item> hammerTag = ModTags.Items.SMITHING_HAMMERS;
        this.hammerItems = registryAccess.registryOrThrow(Registries.ITEM)
                .getTag(hammerTag)
                .stream()
                .flatMap(tag -> tag.stream())
                .map(ItemStack::new)
                .collect(Collectors.toList());
    }

    @Override
    public RecipeType<ExplanationRecipe> getRecipeType() {
        return STEEL_ANVIL_GET;
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
        // Add input slot for tagged hammers
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 18)
                .addItemStacks(hammerItems);

        // Add output slot (result)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 113, 18)
                .addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(ExplanationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw explanatory text
        int textWidth = 140;
        int textX = 5;
        int textY = 43;

        // First paragraph
        renderWrappedText(
                guiGraphics,
                Component.translatable("jei.overgeared.steel_anvil.description"),
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
}
/*
package net.stirdrem.overgeared.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.util.TooltipButton;

public class SmithingAnvilScreen2 extends AbstractContainerScreen<SmithingAnvilMenu> implements RecipeUpdateListener {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/smithing_anvil.png");
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = ResourceLocation.tryBuild("minecraft", "textures/gui/recipe_button.png");

    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean widthTooNarrow;

    public SmithingAnvilScreen2(SmithingAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (p_289630_) -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            p_289630_.setPosition(this.leftPos + 5, this.height / 2 - 49);
        }));
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
        this.titleLabelX = 29;

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(guiGraphics, x, y);
        //renderResultPreview(guiGraphics, x, y); // Add this line
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            guiGraphics.blit(TEXTURE, x + 89, y + 35, 176, 0, menu.getScaledProgress(), 17);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(guiGraphics, delta, mouseX, mouseY);
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, delta);
        } else {
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, delta);
            super.render(guiGraphics, mouseX, mouseY, delta);
            this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, true, delta);
        }
        renderHitsRemaining(guiGraphics);
        for (var widget : this.renderables) {
            if (widget instanceof TooltipButton button && button.isHovered()) {
                guiGraphics.renderTooltip(this.font, button.getTooltipComponent(), mouseX, mouseY);
            }
        }
        renderTooltip(guiGraphics, mouseX, mouseY);


    }

    private void renderHitsRemaining(GuiGraphics guiGraphics) {
        int remainingHits = menu.getRemainingHits();
        if (remainingHits == 0) return;

        Component hitsText = Component.translatable("gui.overgeared.remaining_hits", remainingHits);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.drawString(font, hitsText, x + 89, y + 17, 4210752, false);
    }


    private void renderResultPreview(GuiGraphics guiGraphics, int x, int y) {
        ItemStack result = menu.getResultItem();
        if (!result.isEmpty()) {
            // Set transparency (0.5f = 50% opacity)
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);

            // Position the preview (adjust these coordinates as needed)
            int itemX = x + 143;
            int itemY = y + 35;

            // Render the item with transparency
            guiGraphics.renderItem(result, itemX, itemY);
            guiGraphics.renderItemDecorations(font, result, itemX, itemY);

            // Reset transparency
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
    }


    @Override
    public void recipesUpdated() {

    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return null;
    }


}
*/

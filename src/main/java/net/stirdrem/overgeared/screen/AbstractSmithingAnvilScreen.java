package net.stirdrem.overgeared.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.util.TooltipButton;

public abstract class AbstractSmithingAnvilScreen<T extends AbstractSmithingAnvilMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/smithing_anvil.png");
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = ResourceLocation.tryParse("textures/gui/recipe_button.png");
    //private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean widthTooNarrow;

    public AbstractSmithingAnvilScreen(T menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 29;
        //this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        //this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        /*this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (button) ->
        {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            ((ImageButton) button).setPosition(this.leftPos + 5, this.height / 2 - 49);
        }));
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);*/

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;

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
        /*if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(guiGraphics, delta, mouseX, mouseY);
            renderHitsRemaining(guiGraphics);
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, delta);
        } else {
            this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, delta);
            super.render(guiGraphics, mouseX, mouseY, delta);
            renderHitsRemaining(guiGraphics);
            this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, true, delta);
        }*/
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderHitsRemaining(guiGraphics);
        for (var widget : this.renderables) {
            if (widget instanceof TooltipButton button && button.isHovered()) {
                guiGraphics.renderTooltip(this.font, button.getTooltipComponent(), mouseX, mouseY);
            }
        }
        renderGhostResult(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY); // Add ghost result rendering
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderHitsRemaining(GuiGraphics guiGraphics) {
        int remainingHits = menu.getRemainingHits();
        if (remainingHits == 0) return;

        Component hitsText = Component.translatable("gui.overgeared.remaining_hits", remainingHits);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.drawString(font, hitsText, x + 89, y + 17, 4210752, false);
    }

    private void renderGhostResult(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        ItemStack ghostResult = menu.getGhostResult();
        if (!ghostResult.isEmpty()) {
            int itemX = x + 124;
            int itemY = y + 35;

            guiGraphics.pose().pushPose();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F); // 50% transparency

            guiGraphics.renderFakeItem(ghostResult, itemX, itemY);
            guiGraphics.renderItemDecorations(this.font, ghostResult, itemX, itemY);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset alpha
            RenderSystem.disableBlend();

            guiGraphics.pose().popPose();

            // Tooltip when hovering over ghost item
            if (mouseX >= itemX - 1 && mouseX < itemX + 17 && mouseY >= itemY - 1 && mouseY < itemY + 17) {
                guiGraphics.renderTooltip(this.font, ghostResult, mouseX, mouseY);
            }
        }
    }


    /*@Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        pGuiGraphics.drawString(font, this.title, this.titleLabelX, this.titleLabelY, 4210752);
    }*/

   /* protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow ||
                !this.recipeBookComponent.isVisible()) &&
                super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, buttonId)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        return this.widthTooNarrow && this.recipeBookComponent.isVisible() || super.mouseClicked(mouseX, mouseY, buttonId);
    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int x, int y, int buttonIdx) {
        boolean flag = mouseX < (double) x || mouseY < (double) y || mouseX >= (double) (x + this.imageWidth) || mouseY >= (double) (y + this.imageHeight);
        return flag && this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, buttonIdx);
    }

    protected void slotClicked(Slot slot, int mouseX, int mouseY, ClickType clickType) {
        super.slotClicked(slot, mouseX, mouseY, clickType);
        this.recipeBookComponent.slotClicked(slot);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }*/
}

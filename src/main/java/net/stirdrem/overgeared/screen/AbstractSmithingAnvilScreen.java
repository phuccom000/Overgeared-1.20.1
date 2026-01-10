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
    private static final ResourceLocation TEXTURE = OvergearedMod.loc("textures/gui/smithing_anvil.png");

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
    }

    protected void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            guiGraphics.blit(TEXTURE, x + 89, y + 35, 176, 0, menu.getScaledProgress(), 17);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
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
        // If the result slot already has an item, don't show the ghost result
        // This prevents double rendering and double tooltips
        if (menu.getResultSlot().hasItem()) {
            return;
        }

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
}

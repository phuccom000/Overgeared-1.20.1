package net.stirdrem.overgeared.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.util.TooltipButton;

public class SmithingAnvilScreen extends AbstractContainerScreen<SmithingAnvilMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/smithing_anvil.png");

    public SmithingAnvilScreen(SmithingAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        //this.titleLabelY = -18;
        //this.inventoryLabelY = 18;
        this.titleLabelX = 29;
        //this.inventoryLabelY = this.imageHeight - 93;

        /*// Button size and spacing
        int buttonWidth = 16;
        int buttonHeight = 16;
        int spacingX = 2;
        int spacingY = 2;

        // Starting position (adjust if needed)
        int startX = (this.width - this.imageWidth) / 2 + 97;
        int startY = (this.height - this.imageHeight) / 2 + 59;

        // Add 8 buttons in a 4x2 grid (2 rows, 4 columns)
        for (int i = 0; i < 8; i++) {
            int row = i / 4;
            int col = i % 4;

            int x = startX + col * (buttonWidth + spacingX);
            int y = startY + row * (buttonHeight + spacingY);

            int techniqueIndex = i;
            Component label = Component.literal(""); // No text inside 16x16 button

            Button button = Button.builder(label, b -> onTechniqueButtonPressed(techniqueIndex))
                    .pos(x, y)
                    .size(buttonWidth, buttonHeight)
                    .build();

            this.addRenderableWidget(button);
        }*/
      /*  // Starting position (adjust if needed)
        int startX = (this.width - this.imageWidth) / 2 + 54;
        int startY = (this.height - this.imageHeight) / 2 + 95;
        Component tooltip = Component.literal("Baller"); // No text inside 16x16 button
        TooltipButton button = new TooltipButton(startX, startY, 68, 15, tooltip, b -> onTechniqueButtonPressed(1));
        this.addRenderableWidget(button);*/

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(guiGraphics, x, y);
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
        renderHitsRemaining(guiGraphics);
        for (var widget : this.renderables) {
            if (widget instanceof TooltipButton button && button.isHovered()) {
                guiGraphics.renderTooltip(this.font, button.getTooltipComponent(), mouseX, mouseY);
            }
        }
        renderTooltip(guiGraphics, mouseX, mouseY);


    }

    private void renderHitsRemaining(GuiGraphics guiGraphics) {
        // Draw remaining hits label
        if (menu.getRemainingHits() == 0) return;
        else {
            String hitsText = "Remaining Hits: " + menu.getRemainingHits();
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;
            guiGraphics.drawString(font, hitsText, x + 89, y + 17, 4210752, false); // White color
        }
    }

    private void onTechniqueButtonPressed(int techniqueIndex) {
        // Implement the logic for each technique here
        // For example:
        System.out.println("Technique " + (techniqueIndex + 1) + " selected.");
        // You can add more complex logic based on the techniqueIndex
    }
}

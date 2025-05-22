package net.stirdrem.overgeared.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.stirdrem.overgeared.OvergearedMod;

public class AnvilMinigameOverlay {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/smithing_anvil_minigame.png");
    public static boolean isVisible = false;
    public static final int barTotalWidth = 184;

    public static final IGuiOverlay ANVIL_MG = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!isVisible) return;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int imageWidth = 238;
        int imageHeight = 36;
        int x = (screenWidth - imageWidth) / 2;
        int y = (screenHeight - imageHeight) / 8 * 7;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    });

    /*private void renderArrow(GuiGraphics guiGraphics, int x, int y) {
        if (isForging()) {
            guiGraphics.blit(TEXTURE, x + 89, y + 35, 176, 0, moveLeftAndRight(), 17);
        }
    }*/

    private int moveLeftAndRight() {
        return
                barTotalWidth;
    }
}

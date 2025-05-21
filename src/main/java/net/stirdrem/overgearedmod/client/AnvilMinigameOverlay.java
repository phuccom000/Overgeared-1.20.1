package net.stirdrem.overgearedmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.stirdrem.overgearedmod.OvergearedMod;

public class AnvilMinigameOverlay {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/smithing_anvil_minigame.png");
    public static boolean isVisible = false;

    public static final IGuiOverlay ANVIL_MG = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!isVisible) return;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int imageWidth = 238;
        int imageHeight = 36;
        int x = (screenWidth - imageWidth) / 2;
        int y = (screenHeight - imageHeight) / 4;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    });
}

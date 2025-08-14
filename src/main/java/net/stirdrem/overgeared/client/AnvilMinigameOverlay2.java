package net.stirdrem.overgeared.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

@OnlyIn(Dist.CLIENT)
public class AnvilMinigameOverlay2 {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/smithing_anvil_minigame.png");

    // UI dimensions
    public static final int barTotalWidth = 184;
    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_HEIGHT = 20;

    public static final IGuiOverlay ANVIL_MG = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        HudOverlayHandler.setHudVisibility(!AnvilMinigameEvents.isIsVisible());
        if (!AnvilMinigameEvents.isIsVisible()) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int imageWidth = 238;
        int imageHeight = 36;
        int textureWidth = 256;
        int textureHeight = 128;
        int x = (screenWidth - imageWidth) / 2;
        //int y = (screenHeight - imageHeight) / 8 * 7;
        int y = (screenHeight - imageHeight) - ClientConfig.MINIGAME_OVERLAY_HEIGHT.get();

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, textureWidth, textureHeight);

        int barX = x + 9;
        int barY = y + 9;
        int barWidth = 220;
        int barHeight = 18;

        // Fetch zone data
        int perfectZoneStart = AnvilMinigameEvents.getPerfectZoneStart();
        int perfectZoneEnd = AnvilMinigameEvents.getPerfectZoneEnd();
        int goodZoneStart = AnvilMinigameEvents.getGoodZoneStart();
        int goodZoneEnd = AnvilMinigameEvents.getGoodZoneEnd();
        float arrowPosition = AnvilMinigameEvents.getArrowPosition();

        // Draw good zone (textured instead of filled rectangle)
        int goodStartPx = (int) (barWidth * goodZoneStart / 100f);
        int goodEndPx = (int) (barWidth * goodZoneEnd / 100f);
        int goodWidth = goodEndPx - goodStartPx;

        if (goodWidth > 0) {
            guiGraphics.blit(
                    TEXTURE,
                    barX + goodStartPx, barY,              // screen position
                    9, 94,                                 // texture U,V for good zone
                    goodWidth, barHeight,                   // size
                    textureWidth, textureHeight
            );
        }

        // Draw perfect zone (brighter texture region)
        int perfectStartPx = (int) (barWidth * perfectZoneStart / 100f);
        int perfectEndPx = (int) (barWidth * perfectZoneEnd / 100f);
        int perfectWidth = perfectEndPx - perfectStartPx;

        if (perfectWidth > 0) {
            guiGraphics.blit(
                    TEXTURE,
                    barX + perfectStartPx, barY,           // screen position
                    9, 64,                                 // texture U,V for perfect zone
                    perfectWidth, barHeight,   // size
                    textureWidth, textureHeight
            );
        }


        // Draw the arrow
        int arrowX = barX + (int) (barWidth * arrowPosition / 100f) - 5;
        guiGraphics.blit(TEXTURE, arrowX, barY - 1, 8, 39, ARROW_WIDTH, ARROW_HEIGHT, textureWidth, textureHeight);
        int hitsRemain = AnvilMinigameEvents.getHitsRemaining();

        // Display forging stats
        Component stats = Component.translatable(
                "gui.overgeared.forging_stats",
                AnvilMinigameEvents.getHitsRemaining(),
                AnvilMinigameEvents.getPerfectHits(),
                AnvilMinigameEvents.getGoodHits(),
                AnvilMinigameEvents.getMissedHits()
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                stats,
                x + 10,
                y - 10,
                0xFFFFFFFF
        );
    });
}

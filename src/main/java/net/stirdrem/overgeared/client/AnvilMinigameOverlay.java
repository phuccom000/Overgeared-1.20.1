package net.stirdrem.overgeared.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

@OnlyIn(Dist.CLIENT)
public class AnvilMinigameOverlay implements LayeredDraw.Layer {

    public static final AnvilMinigameOverlay INSTANCE = new AnvilMinigameOverlay();
    public static final ResourceLocation ID = OvergearedMod.loc("anvil_minigame");

    private static final ResourceLocation TEXTURE =
            OvergearedMod.loc("textures/gui/smithing_anvil_minigame.png");

    // UI dimensions
    public static final int barTotalWidth = 184;
    private static final int ARROW_WIDTH = 8;
    private static final int ARROW_HEIGHT = 16;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        boolean showMainOverlay = AnvilMinigameEvents.isVisible();
        if (!showMainOverlay) return; // Early return if not visible

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        int imageWidth = 238;
        int imageHeight = 37;
        int textureWidth = 256;
        int textureHeight = 128;

        int x = (screenWidth - imageWidth) / 2;
        int y = (screenHeight - imageHeight)
                - ClientConfig.MINIGAME_OVERLAY_HEIGHT.get();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        // Draw the main overlay
        guiGraphics.blit(TEXTURE, x, y,
                0, 0, imageWidth, imageHeight,
                textureWidth, textureHeight);

        int barX = x + 9;
        int barY = y + 21;
        int barWidth = 220;
        int barHeight = 10;

        // Zones
        int perfectZoneStart = AnvilMinigameEvents.getPerfectZoneStart();
        int perfectZoneEnd = AnvilMinigameEvents.getPerfectZoneEnd();
        int goodZoneStart = AnvilMinigameEvents.getGoodZoneStart();
        int goodZoneEnd = AnvilMinigameEvents.getGoodZoneEnd();
        float arrowPosition = AnvilMinigameEvents.getArrowPosition();

        int goodStartPx = (int) (barWidth * goodZoneStart / 100f);
        int goodEndPx = (int) (barWidth * goodZoneEnd / 100f);

        if (goodEndPx > goodStartPx) {
            guiGraphics.blit(TEXTURE,
                    barX + goodStartPx, barY,
                    9, 94,
                    goodEndPx - goodStartPx, barHeight,
                    textureWidth, textureHeight);
        }

        int perfectStartPx = (int) (barWidth * perfectZoneStart / 100f);
        int perfectEndPx = (int) (barWidth * perfectZoneEnd / 100f);

        if (perfectEndPx > perfectStartPx) {
            guiGraphics.blit(TEXTURE,
                    barX + perfectStartPx, barY,
                    9, 72,
                    perfectEndPx - perfectStartPx, barHeight,
                    textureWidth, textureHeight);
        }

        // Progress bar
        int progressLengthPx = (int) (222 * (1 - ((float) AnvilMinigameEvents.getHitsRemaining() / AnvilMinigameEvents.getMaxHits())));

        guiGraphics.blit(TEXTURE,
                x + 8, y + 12,
                8, 62,
                progressLengthPx, 5,
                textureWidth, textureHeight);

        int arrowX = barX + (int) (barWidth * arrowPosition / 100f) - 5;
        guiGraphics.blit(TEXTURE,
                arrowX, barY - 3,
                9, 41,
                ARROW_WIDTH, ARROW_HEIGHT,
                textureWidth, textureHeight);

        /*
        int hitsRemain = AnvilMinigameEvents.getHitsRemaining();
        int perfect = AnvilMinigameEvents.getPerfectHits();
        int good = AnvilMinigameEvents.getGoodHits();
        int miss = AnvilMinigameEvents.getMissedHits();

        Component stats = Component.translatable(
                "gui.overgeared.forging_stats",
                hitsRemain, perfect, good, miss
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                stats,
                x + 10,
                y + 10,
                0x404040,
                false
        );
        */
    }
}

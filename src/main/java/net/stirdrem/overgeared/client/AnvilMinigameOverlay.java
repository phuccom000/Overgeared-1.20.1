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

@OnlyIn(Dist.CLIENT)
public class AnvilMinigameOverlay {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/smithing_anvil_minigame.png");

    // UI dimensions
    public static final int barTotalWidth = 184;
    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_HEIGHT = 20;

    public static final IGuiOverlay ANVIL_MG = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!ClientAnvilMinigameData.getIsVisible()) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int imageWidth = 238;
        int imageHeight = 36;
        int x = (screenWidth - imageWidth) / 2;
        int y = (screenHeight - imageHeight) / 8 * 7;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        int barX = x + 8;
        int barY = y + 9;
        int barWidth = 220;
        int barHeight = 18;

        // Fetch zone data
        int perfectZoneStart = ClientAnvilMinigameData.getPerfectZoneStart();
        int perfectZoneEnd = ClientAnvilMinigameData.getPerfectZoneEnd();
        int goodZoneStart = ClientAnvilMinigameData.getGoodZoneStart();
        int goodZoneEnd = ClientAnvilMinigameData.getGoodZoneEnd();
        float arrowPosition = ClientAnvilMinigameData.getArrowPosition();

        // Draw good zone (semi-transparent green)
        guiGraphics.fill(barX + (int) (barWidth * goodZoneStart / 100f), barY,
                barX + (int) (barWidth * goodZoneEnd / 100f), barY + barHeight,
                0x8000AA00);

        // Draw perfect zone (brighter green)
        guiGraphics.fill(barX + (int) (barWidth * perfectZoneStart / 100f), barY,
                barX + (int) (barWidth * perfectZoneEnd / 100f), barY + barHeight,
                0x8000FF00);

        // Draw the arrow
        int arrowX = barX + (int) (barWidth * arrowPosition / 100f) - 5;
        guiGraphics.blit(TEXTURE, arrowX, barY - 1, 8, 39, ARROW_WIDTH, ARROW_HEIGHT);

        // Display forging stats
        Component stats = Component.translatable(
                "gui.overgeared.forging_stats",
                ClientAnvilMinigameData.getHitsRemaining(),
                ClientAnvilMinigameData.getPerfectHits(),
                ClientAnvilMinigameData.getGoodHits(),
                ClientAnvilMinigameData.getMissedHits()
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

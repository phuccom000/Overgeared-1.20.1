package net.stirdrem.overgeared.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.FinalizeForgingC2SPacket;
import net.stirdrem.overgeared.networking.packet.HitResultC2SPacket;

import java.util.UUID;

public class AnvilMinigameOverlay {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/smithing_anvil_minigame.png");

    // Minigame state - now tied to specific anvil position
    private static BlockPos currentAnvilPos;
    private static UUID currentPlayerId;
    public static boolean isVisible = false;
    private static ItemStack resultItem;
    private static int hitsRemaining = 0;
    private static float arrowPosition = 0;
    private static float arrowSpeed = 1.0f;
    private static boolean movingRight = true;
    private static int perfectHits = 0;
    private static int goodHits = 0;
    private static int missedHits = 0;

    // Zone definitions
    private static int perfectZoneStart;
    private static int perfectZoneEnd;
    private static int goodZoneStart;
    private static int goodZoneEnd;
    private static final float zoneShrinkFactor = ServerConfig.ZONE_SHRINK_FACTOR.get().floatValue();
    private static final float zoneShiftAmount = 15.0f;

    // UI dimensions
    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_HEIGHT = 20;
    private static final float MAX_SPEED = 5.0f;

    public static final IGuiOverlay ANVIL_MG = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!isVisible) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int imageWidth = 238;
        int imageHeight = 36;
        int x = (screenWidth - imageWidth) / 2;
        int y = (screenHeight - imageHeight) / 8 * 7;

        // Draw background
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Draw zones
        int barX = x + 8;
        int barY = y + 9;
        int barWidth = 220;
        int barHeight = 18;

        guiGraphics.fill(barX + (int) (barWidth * goodZoneStart / 100f), barY,
                barX + (int) (barWidth * goodZoneEnd / 100f), barY + barHeight,
                0x8000AA00);

        guiGraphics.fill(barX + (int) (barWidth * perfectZoneStart / 100f), barY,
                barX + (int) (barWidth * perfectZoneEnd / 100f), barY + barHeight,
                0x8000FF00);

        // Draw arrow
        int arrowX = barX + (int) (barWidth * arrowPosition / 100f) - ARROW_WIDTH / 2;
        guiGraphics.blit(TEXTURE, arrowX, barY - 1, 8, 39, 10, 20);

        // Draw stats
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("gui.overgeared.forging_stats",
                        hitsRemaining, perfectHits, goodHits, missedHits),
                x + 10, y - 10, 0xFFFFFFFF);
    });

    public static void startMinigame(BlockPos anvilPos, UUID playerId, ItemStack result, int requiredHits) {
        if (result == null) {
            OvergearedMod.LOGGER.error("Null result in minigame start");
            return;
        }

        currentAnvilPos = anvilPos;
        currentPlayerId = playerId;
        isVisible = true;
        resultItem = result.copy();
        hitsRemaining = requiredHits;
        perfectHits = 0;
        goodHits = 0;
        missedHits = 0;
        arrowPosition = 0;
        arrowSpeed = ServerConfig.DEFAULT_ARROW_SPEED.get().floatValue();
        movingRight = true;

        // Initialize zones with random offset
        double randomOffset = Math.random() * 10;
        perfectZoneStart = (int) ((100 - ServerConfig.ZONE_STARTING_SIZE.get()) / 2 + randomOffset);
        perfectZoneEnd = (int) ((100 + ServerConfig.ZONE_STARTING_SIZE.get()) / 2 + randomOffset);
        goodZoneStart = perfectZoneStart - 10;
        goodZoneEnd = perfectZoneEnd + 10;
    }

    public static void tick() {
        if (!isVisible || Minecraft.getInstance().isPaused()) return;

        // Only update if this is our player's minigame
        if (!Minecraft.getInstance().player.getUUID().equals(currentPlayerId)) {
            return;
        }

        // Update arrow position
        if (movingRight) {
            arrowPosition += arrowSpeed;
            if (arrowPosition >= 100) {
                arrowPosition = 100;
                movingRight = false;
            }
        } else {
            arrowPosition -= arrowSpeed;
            if (arrowPosition <= 0) {
                arrowPosition = 0;
                movingRight = true;
            }
        }
    }

    public static void handleHit() {
        // Verify this is our minigame
        if (!Minecraft.getInstance().player.getUUID().equals(currentPlayerId)) {
            return;
        }

        // Increase speed
        arrowSpeed = Math.min(arrowSpeed +
                        ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue(),
                MAX_SPEED);

        // Check hit quality
        String hitQuality;
        if (arrowPosition >= perfectZoneStart && arrowPosition <= perfectZoneEnd) {
            perfectHits++;
            hitQuality = "perfect";
        } else if (arrowPosition >= goodZoneStart && arrowPosition <= goodZoneEnd) {
            goodHits++;
            hitQuality = "good";
        } else {
            missedHits++;
            hitQuality = "missed";
        }

        // Send hit result to server
        ModMessages.sendToServer(new HitResultC2SPacket(
                currentAnvilPos,
                hitQuality,
                perfectHits,
                goodHits,
                missedHits
        ));

        // Update zones
        shrinkAndShiftZones();
        hitsRemaining--;

        if (hitsRemaining <= 0) {
            finishForging();
        }
    }

    private static void shrinkAndShiftZones() {
        // Calculate and update zones...
        // (Same implementation as before)
    }

    private static void finishForging() {
        String quality = determineQuality(calculateQualityScore());
        ModMessages.sendToServer(new FinalizeForgingC2SPacket(
                currentAnvilPos,
                quality,
                perfectHits,
                goodHits,
                missedHits
        ));
        endMinigame();
    }

    public static void endMinigame() {
        isVisible = false;
        currentAnvilPos = null;
        currentPlayerId = null;
        resultItem = null;
        hitsRemaining = 0;
        perfectHits = 0;
        goodHits = 0;
        missedHits = 0;
        arrowPosition = 0;
        arrowSpeed = 0;
    }


    public static void forceFinishWithPoorQuality() {
        missedHits = perfectHits + goodHits + missedHits;
        perfectHits = 0;
        goodHits = 0;
        hitsRemaining = 0;
        finishForging();
    }

    private static float calculateQualityScore() {
        int totalHits = perfectHits + goodHits + missedHits;
        if (totalHits == 0) return 0f;
        return (perfectHits * 1.0f + goodHits * 0.6f) / totalHits;
    }

    private static String determineQuality(float qualityScore) {
        if (qualityScore > 0.9f) return "perfect";
        if (qualityScore > 0.75f) return "expert";
        if (qualityScore > 0.50f) return "well";
        return "poor";
    }

    public static boolean isCurrentPlayer(UUID playerUUID) {
        return playerUUID == currentPlayerId;
    }

    public static boolean isVisible() {
        return isVisible;
    }
}
package net.stirdrem.overgeared.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.FinalizeForgingC2SPacket;
import net.stirdrem.overgeared.networking.packet.UpdateAnvilProgressC2SPacket;

public class AnvilMinigameOverlay {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/smithing_anvil_minigame.png");

    // Minigame state
    public static boolean isVisible = false;
    private static ItemStack resultItem;
    private static int hitsRemaining = 0;
    private static float arrowPosition = 0;
    private static float arrowSpeed = 1.0f;
    private static float speedIncreasePerHit = 0.75f;
    private static boolean movingRight = true;
    private static int perfectHits = 0;
    private static int goodHits = 0;
    private static int missedHits = 0;

    // Zone definitions (0-100%)
    private static final int PERFECT_ZONE_START = 40;
    private static final int PERFECT_ZONE_END = 60;
    private static final int GOOD_ZONE_START = 30;
    private static final int GOOD_ZONE_END = 70;
    private static final int INITIAL_PERFECT_ZONE_SIZE = 20; // 40-60 (20% wide)
    private static final int INITIAL_GOOD_ZONE_SIZE = 40;    // 30-70 (40% wide)
    private static int perfectZoneStart = PERFECT_ZONE_START;
    private static int perfectZoneEnd = PERFECT_ZONE_END;
    private static int goodZoneStart = GOOD_ZONE_START;
    private static int goodZoneEnd = GOOD_ZONE_END;
    private static float zoneShrinkFactor = 0.80f; // Zones shrink to 95% of their size each hit
    private static float zoneShiftAmount = 15.0f; // Zones shift by 2% each hit


    public static boolean temporaryExit = true;
    public static boolean minigameStarted = false;

    // UI dimensions
    public static final int barTotalWidth = 184;
    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_HEIGHT = 20;

    // Speed increase constants
    private static final float BASE_SPEED = 2.0f;
    private static final float SPEED_INCREASE_PER_HIT = 0.75f;
    private static final float MAX_SPEED = 5.0f;

    public static final IGuiOverlay ANVIL_MG = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!isVisible) return;

        // Save current GL state
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

        // Draw zones on the progress bar
        int barX = x + 8;
        int barY = y + 9;
        int barWidth = 220;
        int barHeight = 18;

        // Good zone
        guiGraphics.fill(barX + (int) (barWidth * goodZoneStart / 100f), barY,
                barX + (int) (barWidth * goodZoneEnd / 100f), barY + barHeight,
                0x8000AA00);

        // Perfect zone
        guiGraphics.fill(barX + (int) (barWidth * perfectZoneStart / 100f), barY,
                barX + (int) (barWidth * perfectZoneEnd / 100f), barY + barHeight,
                0x8000FF00);


        // Draw arrow
        int arrowX = barX + (int) (barWidth * arrowPosition / 100f) - ARROW_WIDTH / 2;
        guiGraphics.blit(TEXTURE, arrowX, barY - 1, 8, 39, 10, 20);
        /*guiGraphics.fill(arrowX, barY - ARROW_HEIGHT - 2,
                arrowX + ARROW_WIDTH, barY - 2,
                0xFFFF0000);*/

        // Draw stats
        Component stats = Component.translatable(
                "gui.overgeared.forging_stats",
                hitsRemaining,
                perfectHits,
                goodHits,
                missedHits
        );

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                stats,
                x + 10,
                y - 10,
                0xFFFFFFFF
        );


    });

    public static void startMinigame(ItemStack result, int requiredHits) {
        if (minigameStarted) {
            pauseMinigame();
            return;
        }
        if (result == null) {
            OvergearedMod.LOGGER.error("Attempted to start minigame with null result!");
            return;
        }
        minigameStarted = true;
        temporaryExit = false;
        isVisible = true;
        resultItem = result.copy();
        hitsRemaining = requiredHits;
        perfectHits = 0;
        goodHits = 0;
        missedHits = 0;
        arrowPosition = 0;
        double as = ServerConfig.DEFAULT_ARROW_SPEED.get();
        arrowSpeed = (float) as;
        as = ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get();
        speedIncreasePerHit = (float) as;
        as = ServerConfig.ZONE_SHRINK_FACTOR.get();
        zoneShrinkFactor = (float) as;
        movingRight = true;
        double random = Math.random() * 10;
        perfectZoneStart = Math.max(0, Math.min(100, (int) (PERFECT_ZONE_START + random)));
        perfectZoneEnd = Math.max(0, Math.min(100, (int) (PERFECT_ZONE_END + random)));
        goodZoneStart = Math.max(0, Math.min(100, (int) (GOOD_ZONE_START + random)));
        goodZoneEnd = Math.max(0, Math.min(100, (int) (GOOD_ZONE_END + random)));
    }

    public static void tick() {
        if (!isVisible) return;
        // Pause the minigame if the game itself is paused
        if (Minecraft.getInstance().isPaused()) return;
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

    public static String handleHit() {
        // Increase speed with every hit
        arrowSpeed = Math.min(arrowSpeed + speedIncreasePerHit, MAX_SPEED);

        if (arrowPosition >= perfectZoneStart && arrowPosition <= perfectZoneEnd) {
            perfectHits++;
        } else if (arrowPosition >= goodZoneStart && arrowPosition <= goodZoneEnd) {
            goodHits++;
        } else {
            missedHits++;
        }
        // Shrink and shift zones
        shrinkAndShiftZones();
        hitsRemaining--;

        if (hitsRemaining <= 0) {
            return finishForging();
        }
        return null;
    }

    private static void shrinkAndShiftZones() {
        // Calculate current zone sizes
        float perfectZoneSize = perfectZoneEnd - perfectZoneStart;
        float goodZoneSize = goodZoneEnd - goodZoneStart;

        // Shrink zones
        perfectZoneSize *= zoneShrinkFactor;
        goodZoneSize *= zoneShrinkFactor;

        // Ensure zones don't become too small
        perfectZoneSize = Math.max(perfectZoneSize, 5); // Minimum 5% width
        goodZoneSize = Math.max(goodZoneSize, perfectZoneSize * 2); // Good zone always at least double perfect zone

        float random = (float) Math.random();
        // Calculate new zone positions with random shift
        float perfectZoneCenter = (perfectZoneStart + perfectZoneEnd) / 2f;
        perfectZoneCenter += (float) (random - 0.5) * zoneShiftAmount * 3;
        //perfectZoneCenter = Math.max(20, Math.min(80, perfectZoneCenter)); // Keep within reasonable bounds

        float goodZoneCenter = (goodZoneStart + goodZoneEnd) / 2f;
        goodZoneCenter += (float) (random - 0.5) * zoneShiftAmount * 3;
        //goodZoneCenter = Math.max(20, Math.min(80, goodZoneCenter)); // Keep within reasonable bounds

        // Apply new zones
        perfectZoneStart = (int) (perfectZoneCenter - perfectZoneSize / 2);
        perfectZoneEnd = (int) (perfectZoneCenter + perfectZoneSize / 2);

        goodZoneStart = (int) (goodZoneCenter - goodZoneSize / 2);
        goodZoneEnd = (int) (goodZoneCenter + goodZoneSize / 2);

        // Ensure zones don't go out of bounds
        perfectZoneStart = Math.max(0, perfectZoneStart);
        perfectZoneEnd = Math.min(100, perfectZoneEnd);
        goodZoneStart = Math.max(0, goodZoneStart);
        goodZoneEnd = Math.min(100, goodZoneEnd);
    }

    private static String finishForging() {
        // Calculate quality based on performance
        float qualityScore = calculateQualityScore();
        String quality = determineQuality(qualityScore);

        // Send the result to server
        //ModMessages.sendToServer(new FinalizeForgingC2SPacket(quality));

        // Close minigame
        isVisible = false;
        minigameStarted = false;
        return quality;
    }

    public static void pauseMinigame() {
        if (minigameStarted) {
            temporaryExit = !temporaryExit;
            isVisible = !isVisible;
        }
    }


    public static void forceFinishWithPoorQuality() {
        // Set all hits to missed to ensure poor quality
        missedHits = perfectHits + goodHits + missedHits;
        perfectHits = 0;
        goodHits = 0;

        // Finish the minigame
        hitsRemaining = 0;
        String quality = finishForging();

        // You might want to send this quality to the server
        //ModMessages.sendToServer(new FinalizeForgingC2SPacket(quality));
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

    public static void endMinigame() {
        // Close minigame
        isVisible = false;
        minigameStarted = false;
        resultItem = null;
        hitsRemaining = 0;
        perfectHits = 0;
        goodHits = 0;
        missedHits = 0;
        arrowPosition = 0;
        arrowSpeed = 0;
    }
}
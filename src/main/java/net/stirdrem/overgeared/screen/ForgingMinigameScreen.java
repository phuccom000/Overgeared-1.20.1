package net.stirdrem.overgeared.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgingMinigameScreen extends Screen {
    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 20;
    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_HEIGHT = 15;

    private final ItemStack resultItem;
    private int progress = 0;
    private int requiredProgress = 100;
    private float arrowPosition = 0;
    private float arrowSpeed = 1.0f;
    private boolean movingRight = true;
    private int perfectHits = 0;
    private int goodHits = 0;
    private int missedHits = 0;

    // Zone definitions (0-100%)
    private static final int PERFECT_ZONE_START = 40;
    private static final int PERFECT_ZONE_END = 60;
    private static final int GOOD_ZONE_START = 30;
    private static final int GOOD_ZONE_END = 70;

    public ForgingMinigameScreen(ItemStack resultItem) {
        super(Component.translatable("screen.forging_minigame.title"));
        this.resultItem = resultItem;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Draw progress bar background
        guiGraphics.fill(centerX - BAR_WIDTH / 2, centerY - BAR_HEIGHT / 2,
                centerX + BAR_WIDTH / 2, centerY + BAR_HEIGHT / 2,
                0xFF555555);

        // Draw zones
        // Good zone
        guiGraphics.fill(centerX - BAR_WIDTH / 2 + (int) (BAR_WIDTH * GOOD_ZONE_START / 100f), centerY - BAR_HEIGHT / 2,
                centerX - BAR_WIDTH / 2 + (int) (BAR_WIDTH * GOOD_ZONE_END / 100f), centerY + BAR_HEIGHT / 2,
                0xFF00AA00);

        // Perfect zone
        guiGraphics.fill(centerX - BAR_WIDTH / 2 + (int) (BAR_WIDTH * PERFECT_ZONE_START / 100f), centerY - BAR_HEIGHT / 2,
                centerX - BAR_WIDTH / 2 + (int) (BAR_WIDTH * PERFECT_ZONE_END / 100f), centerY + BAR_HEIGHT / 2,
                0xFF00FF00);

        // Draw progress
        guiGraphics.fill(centerX - BAR_WIDTH / 2, centerY - BAR_HEIGHT / 2,
                centerX - BAR_WIDTH / 2 + (int) (BAR_WIDTH * (progress / (float) requiredProgress)),
                centerY + BAR_HEIGHT / 2,
                0xFF0000FF);

        // Draw arrow
        int arrowX = centerX - BAR_WIDTH / 2 + (int) (BAR_WIDTH * arrowPosition / 100f) - ARROW_WIDTH / 2;
        guiGraphics.fill(arrowX, centerY - ARROW_HEIGHT / 2 - BAR_HEIGHT,
                arrowX + ARROW_WIDTH, centerY + ARROW_HEIGHT / 2 - BAR_HEIGHT,
                0xFFFF0000);

        // Draw stats
        guiGraphics.drawString(Minecraft.getInstance().font,
                "Perfect: " + perfectHits + " Good: " + goodHits + " Missed: " + missedHits,
                centerX - 50, centerY + 30,
                0xFFFFFFFF);
    }

    @Override
    public void tick() {
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

        // Check if minigame is complete
        if (progress >= requiredProgress) {
            finishForging();
        }
    }

    @SubscribeEvent
    public void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (this.minecraft != null && this.minecraft.screen == this) {
            if (event.isAttack()) {
                handleHit();
                event.setCanceled(true);
            }
        }
    }

    private void handleHit() {
        if (arrowPosition >= PERFECT_ZONE_START && arrowPosition <= PERFECT_ZONE_END) {
            // Perfect hit
            progress += 15;
            perfectHits++;
            arrowSpeed += 0.2f;
        } else if (arrowPosition >= GOOD_ZONE_START && arrowPosition <= GOOD_ZONE_END) {
            // Good hit
            progress += 10;
            goodHits++;
            arrowSpeed += 0.1f;
        } else {
            // Missed
            progress = Math.max(0, progress - 5);
            missedHits++;
            arrowSpeed = Math.max(0.5f, arrowSpeed - 0.15f);
        }
    }

    private void finishForging() {
        // Calculate quality based on performance
        float qualityScore = (perfectHits * 1.0f + goodHits * 0.5f) /
                (perfectHits + goodHits + missedHits + 1);

        String quality;
        if (qualityScore > 0.8f) {
            quality = "legendary";
        } else if (qualityScore > 0.6f) {
            quality = "epic";
        } else if (qualityScore > 0.4f) {
            quality = "rare";
        } else if (qualityScore > 0.2f) {
            quality = "common";
        } else {
            quality = "poor";
        }

        // Apply quality to item
        resultItem.getOrCreateTag().putString("ForgingQuality", quality);

        // Close screen
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
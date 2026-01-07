package net.stirdrem.overgeared.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

@OnlyIn(Dist.CLIENT)
public class PopupOverlay implements LayeredDraw.Layer {

    public static final PopupOverlay INSTANCE = new PopupOverlay();
    public static final ResourceLocation ID = OvergearedMod.loc("popup");

    private static final float POPUP_DURATION_MS = 10000f;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!ClientConfig.POP_UP_TOGGLE.get()) return;

        // Always render popups regardless of game state
        var popups = AnvilMinigameEvents.getPopups();
        if (popups.isEmpty()) return;

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        for (int i = 0; i < popups.size(); i++) {
            var popup = popups.get(i);

            float progress = popup.age / POPUP_DURATION_MS;
            progress = Math.min(progress, 1f);

            float alpha = 1f - progress;
            float floatUp = progress * 12f;
            float scale = 1f + (1f - progress) * 0.15f;

            int color = ((int) (alpha * 255) << 24) | 0xFFFFFF;

            var font = Minecraft.getInstance().font;
            int textWidth = font.width(popup.text);

            // Slight vertical offset per popup so they overlap naturally
            float yOffset = i * 6f;

            // Always position in center of screen for popups
            float popupY = screenHeight / 2f - 40 - floatUp - yOffset;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(
                    screenWidth / 2f,
                    popupY,
                    0
            );
            guiGraphics.pose().scale(scale, scale, 1f);

            guiGraphics.drawString(
                    font,
                    popup.text,
                    -textWidth / 2,
                    screenHeight / 2 - 18 - ClientConfig.MINIGAME_OVERLAY_HEIGHT.get(),
                    color,
                    false
            );

            guiGraphics.pose().popPose();
        }
    }
}

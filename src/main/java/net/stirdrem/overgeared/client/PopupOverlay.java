package net.stirdrem.overgeared.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

@OnlyIn(Dist.CLIENT)
public class PopupOverlay {

    private static final float POPUP_DURATION_MS = 10000f;

    public static final IGuiOverlay POPUP_OVERLAY = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!ClientConfig.POP_UP_TOGGLE.get()) return;
        // Always render popups regardless of game state
        var popups = AnvilMinigameEvents.getPopups();
        if (popups.isEmpty()) return;
        int y = screenHeight + 20
                - ClientConfig.MINIGAME_OVERLAY_HEIGHT.get();
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
    });
}
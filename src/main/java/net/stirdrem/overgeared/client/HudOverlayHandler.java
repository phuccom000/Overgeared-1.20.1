package net.stirdrem.overgeared.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ClientConfig;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID, value = Dist.CLIENT)
public class HudOverlayHandler {

    private static boolean hideHud = false;

    public static void setHudVisibility(boolean visible) {
        hideHud = !visible;
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiOverlayEvent.Pre event) {
        if (hideHud && !ClientConfig.PLAYER_HUD_TOGGLE.get()) {
            if (event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id()) ||
                    event.getOverlay().id().equals(VanillaGuiOverlay.MOUNT_HEALTH.id()) ||
                    event.getOverlay().id().equals(VanillaGuiOverlay.ARMOR_LEVEL.id()) ||
                    event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id()) ||
                    event.getOverlay().id().equals(VanillaGuiOverlay.ITEM_NAME.id()) ||
                    event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
                event.setCanceled(true);
            }
        } else event.setCanceled(false);
    }
}

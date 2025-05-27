package net.stirdrem.overgeared.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.renderer.SmithingAnvilBlockEntityRenderer;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.minecraftforge.client.event.RegisterColorHandlersEvent; // Updated event class
import net.stirdrem.overgeared.client.WaterBarrelBlockColor;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("anvil_mg", AnvilMinigameOverlay.ANVIL_MG);
    }


    // In your client setup (e.g., ModClientEvents.java)
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                new WaterBarrelBlockColor(),
                ModBlocks.WATER_BARREL_FULL.get()  // Your block here
        );
    }


}

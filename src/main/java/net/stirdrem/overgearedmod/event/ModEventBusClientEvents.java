package net.stirdrem.overgearedmod.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgearedmod.OvergearedMod;
import net.stirdrem.overgearedmod.block.entity.ModBlockEntities;
import net.stirdrem.overgearedmod.block.entity.renderer.SmithingAnvilBlockEntityRenderer;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {
    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
    }
}

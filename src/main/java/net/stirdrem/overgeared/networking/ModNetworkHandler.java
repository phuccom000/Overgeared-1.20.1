package net.stirdrem.overgeared.networking;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.networking.packet.KnappingChipC2SPacket;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModNetworkHandler {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(OvergearedMod.MOD_ID);

        // Register client-to-server payload for knapping chip
        registrar.playToServer(
                KnappingChipC2SPacket.TYPE,
                KnappingChipC2SPacket.STREAM_CODEC,
                KnappingChipC2SPacket::handle
        );
    }
}


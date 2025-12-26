package net.stirdrem.overgeared.networking;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.networking.packet.KnappingChipC2SPacket;
import net.stirdrem.overgeared.screen.RockKnappingMenu;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModNetworkHandler {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(OvergearedMod.MOD_ID);

        // Register client-to-server payload for knapping chip
        registrar.playToServer(
                KnappingChipC2SPacket.TYPE,
                KnappingChipC2SPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof ServerPlayer player) {
                            if (player.containerMenu instanceof RockKnappingMenu menu) {
                                // Validate the index is within bounds
                                if (payload.index() >= 0 && payload.index() < 9) {
                                    menu.setChip(payload.index());
                                    OvergearedMod.LOGGER.debug("Player {} chipped spot {} in knapping grid", 
                                            player.getName().getString(), payload.index());
                                }
                            }
                        }
                    });
                }
        );
    }
}


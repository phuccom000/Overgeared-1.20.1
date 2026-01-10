package net.stirdrem.overgeared.networking;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.networking.packet.*;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModNetworkHandler {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(OvergearedMod.MOD_ID);

        // Server to Client packets
        registrar.playToClient(
                HideMinigameS2CPacket.TYPE,
                HideMinigameS2CPacket.STREAM_CODEC,
                HideMinigameS2CPacket::handle
        );

        registrar.playToClient(
                MinigameSetStartedS2CPacket.TYPE,
                MinigameSetStartedS2CPacket.STREAM_CODEC,
                MinigameSetStartedS2CPacket::handle
        );

        registrar.playToClient(
                MinigameSyncS2CPacket.TYPE,
                MinigameSyncS2CPacket.STREAM_CODEC,
                MinigameSyncS2CPacket::handle
        );

        registrar.playToClient(
                OnlyResetMinigameS2CPacket.TYPE,
                OnlyResetMinigameS2CPacket.STREAM_CODEC,
                OnlyResetMinigameS2CPacket::handle
        );

        registrar.playToClient(
                ResetMinigameS2CPacket.TYPE,
                ResetMinigameS2CPacket.STREAM_CODEC,
                ResetMinigameS2CPacket::handle
        );

        // Client to Server packets
        registrar.playToServer(
                KnappingChipC2SPacket.TYPE,
                KnappingChipC2SPacket.STREAM_CODEC,
                KnappingChipC2SPacket::handle
        );

        registrar.playToServer(
                MinigameSetStartedC2SPacket.TYPE,
                MinigameSetStartedC2SPacket.STREAM_CODEC,
                MinigameSetStartedC2SPacket::handle
        );

        registrar.playToServer(
                PacketSendCounterC2SPacket.TYPE,
                PacketSendCounterC2SPacket.STREAM_CODEC,
                PacketSendCounterC2SPacket::handle
        );

        registrar.playToServer(
                SelectToolTypeC2SPacket.TYPE,
                SelectToolTypeC2SPacket.STREAM_CODEC,
                SelectToolTypeC2SPacket::handle
        );

        registrar.playToServer(
                SetMinigameVisibleC2SPacket.TYPE,
                SetMinigameVisibleC2SPacket.STREAM_CODEC,
                SetMinigameVisibleC2SPacket::handle
        );
    }
}


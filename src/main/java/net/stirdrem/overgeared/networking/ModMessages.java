package net.stirdrem.overgeared.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.networking.packet.*;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(MinigameHitResultC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MinigameHitResultC2SPacket::decode)
                .encoder(MinigameHitResultC2SPacket::encode)
                .consumerMainThread(MinigameHitResultC2SPacket::handle)
                .add();

        net.messageBuilder(StartMinigameC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StartMinigameC2SPacket::new)
                .encoder(StartMinigameC2SPacket::toBytes)
                .consumerMainThread(StartMinigameC2SPacket::handle)
                .add();

        /*net.messageBuilder(UpdateAnvilProgressC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(UpdateAnvilProgressC2SPacket::new)
                .encoder(UpdateAnvilProgressC2SPacket::toBytes)
                .consumerMainThread(UpdateAnvilProgressC2SPacket::handle)
                .add();*/

        net.messageBuilder(FinalizeForgingC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(FinalizeForgingC2SPacket::new)
                .encoder(FinalizeForgingC2SPacket::toBytes)
                .consumerMainThread(FinalizeForgingC2SPacket::handle)
                .add();

        net.messageBuilder(MinigameSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MinigameSyncS2CPacket::new)
                .encoder(MinigameSyncS2CPacket::toBytes)
                .consumerMainThread(MinigameSyncS2CPacket::handle)
                .add();

        net.messageBuilder(MinigameHitResultC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(MinigameHitResultC2SPacket::encode)
                .decoder(MinigameHitResultC2SPacket::decode)
                .consumerMainThread(MinigameHitResultC2SPacket::handle)
                .add();

        net.messageBuilder(KnappingChipC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(KnappingChipC2SPacket::encode)
                .decoder(KnappingChipC2SPacket::decode)
                .consumerMainThread(KnappingChipC2SPacket::handle)
                .add();

        net.messageBuilder(SelectToolTypeC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(SelectToolTypeC2SPacket::toBytes)
                .decoder(SelectToolTypeC2SPacket::new)
                .consumerMainThread(SelectToolTypeC2SPacket::handle)
                .add();

        net.messageBuilder(PacketSendCounterC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketSendCounterC2SPacket::encode)
                .decoder(PacketSendCounterC2SPacket::decode)
                .consumerMainThread(PacketSendCounterC2SPacket::handle)
                .add();

        net.messageBuilder(SetMinigameVisibleC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetMinigameVisibleC2SPacket::encode)
                .decoder(SetMinigameVisibleC2SPacket::decode)
                .consumerMainThread(SetMinigameVisibleC2SPacket::handle)
                .add();

        net.messageBuilder(MinigameSetStartedC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(MinigameSetStartedC2SPacket::encode)
                .decoder(MinigameSetStartedC2SPacket::decode)
                .consumerMainThread(MinigameSetStartedC2SPacket::handle)
                .add();

        net.messageBuilder(MinigameSetStartedS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(MinigameSetStartedS2CPacket::new)
                .encoder(MinigameSetStartedS2CPacket::toBytes)
                .consumerMainThread(MinigameSetStartedS2CPacket::handle)
                .add();

        net.messageBuilder(HideMinigameS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(HideMinigameS2CPacket::new)
                .encoder(HideMinigameS2CPacket::toBytes)
                .consumerMainThread(HideMinigameS2CPacket::handle)
                .add();

        net.messageBuilder(ResetMinigameS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ResetMinigameS2CPacket::new)
                .encoder(ResetMinigameS2CPacket::toBytes)
                .consumerMainThread(ResetMinigameS2CPacket::handle)
                .add();

        net.messageBuilder(OnlyResetMinigameS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OnlyResetMinigameS2CPacket::new)
                .encoder(OnlyResetMinigameS2CPacket::toBytes)
                .consumerMainThread(OnlyResetMinigameS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToAll(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
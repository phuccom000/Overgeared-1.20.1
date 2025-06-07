package net.stirdrem.overgeared.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.networking.packet.FinalizeForgingC2SPacket;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;
import net.stirdrem.overgeared.networking.packet.StartMinigameC2SPacket;
import net.stirdrem.overgeared.networking.packet.ToggleMinigamePauseC2SPacket;

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

        net.messageBuilder(ToggleMinigamePauseC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ToggleMinigamePauseC2SPacket::decode)
                .encoder(ToggleMinigamePauseC2SPacket::encode)
                .consumerMainThread(ToggleMinigamePauseC2SPacket::handle)
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


    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
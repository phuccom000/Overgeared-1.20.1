package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

import static net.stirdrem.overgeared.event.ModItemInteractEvents.playerAnvilPositions;
import static net.stirdrem.overgeared.event.ModItemInteractEvents.playerMinigameVisibility;

public record MinigameSetStartedC2SPacket(BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvergearedMod.loc("minigame_set_started_c2s");
    public static final CustomPacketPayload.Type<MinigameSetStartedC2SPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, MinigameSetStartedC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeBlockPos(packet.pos),
            buffer -> new MinigameSetStartedC2SPacket(buffer.readBlockPos())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MinigameSetStartedC2SPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.level().getBlockEntity(payload.pos) instanceof AbstractSmithingAnvilBlockEntity anvilEntity)) return;
            AnvilMinigameEvents.setMinigameStarted(payload.pos, true);
            PacketDistributor.sendToPlayer(player, new MinigameSetStartedS2CPacket(payload.pos));
            playerAnvilPositions.put(player.getUUID(), payload.pos);
            playerMinigameVisibility.put(player.getUUID(), true);
            anvilEntity.setPlayer(player);
            anvilEntity.setMinigameOn(true);
        });
    }
}

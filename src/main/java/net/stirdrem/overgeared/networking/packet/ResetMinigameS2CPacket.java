package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

public record ResetMinigameS2CPacket(BlockPos anvilPos) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvergearedMod.loc("reset_minigame");
    public static final CustomPacketPayload.Type<ResetMinigameS2CPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ResetMinigameS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> BlockPos.STREAM_CODEC.encode(buffer, packet.anvilPos),
            buffer -> new ResetMinigameS2CPacket(BlockPos.STREAM_CODEC.decode(buffer))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResetMinigameS2CPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            BlockEntity be = player.level().getBlockEntity(payload.anvilPos);
            if (!(be instanceof AbstractSmithingAnvilBlockEntity anvil)) return;
            String quality = anvil.minigameQuality();
            OvergearedMod.LOGGER.debug(
                    "Resetting minigame for {} at anvil {} with quality {}",
                    player.getName().getString(), payload.anvilPos, quality
            );
            // Only reset if the player's tracked anvil matches
            if (ModItemInteractEvents.playerAnvilPositions
                    .getOrDefault(player.getUUID(), BlockPos.ZERO)
                    .equals(payload.anvilPos)) {
                ModItemInteractEvents.playerAnvilPositions.remove(player.getUUID());
                ModItemInteractEvents.playerMinigameVisibility.remove(player.getUUID());
                AnvilMinigameEvents.reset(quality);
            }
        });
    }
}

package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

public record SetMinigameVisibleC2SPacket (Boolean visible, BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvergearedMod.loc("set_minigame_visible");
    public static final CustomPacketPayload.Type<SetMinigameVisibleC2SPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SetMinigameVisibleC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                ByteBufCodecs.BOOL.encode(buffer, packet.visible);
                BlockPos.STREAM_CODEC.encode(buffer, packet.pos);
            },
            buffer -> new SetMinigameVisibleC2SPacket(
                    ByteBufCodecs.BOOL.decode(buffer),
                    BlockPos.STREAM_CODEC.decode(buffer)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetMinigameVisibleC2SPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.level().getBlockEntity(payload.pos) instanceof AbstractSmithingAnvilBlockEntity anvilBlock)) return;

            anvilBlock.setMinigameOn(payload.visible);
            ModItemInteractEvents.playerMinigameVisibility.put(player.getUUID(), payload.visible);
        });
    }
}

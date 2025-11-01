package net.stirdrem.overgeared.util;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

public record HitsRemainingPacket(int hitsRemaining) implements CustomPacketPayload {
    public static final Type<HitsRemainingPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("overgeared", "hits_remaining"));

    public static final StreamCodec<FriendlyByteBuf, HitsRemainingPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            HitsRemainingPacket::hitsRemaining,
            HitsRemainingPacket::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.OvergearedMod;

public record KnappingChipC2SPacket(int index) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvergearedMod.loc("knapping_chip");
    public static final CustomPacketPayload.Type<KnappingChipC2SPacket> TYPE = new CustomPacketPayload.Type<>(ID);
    
    public static final StreamCodec<FriendlyByteBuf, KnappingChipC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeInt(packet.index()),
            buffer -> new KnappingChipC2SPacket(buffer.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
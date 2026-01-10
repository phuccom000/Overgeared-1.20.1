package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

public record OnlyResetMinigameS2CPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID = OvergearedMod.loc("only_reset_minigame");
    public static final CustomPacketPayload.Type<OnlyResetMinigameS2CPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OnlyResetMinigameS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {},
            buffer -> new OnlyResetMinigameS2CPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OnlyResetMinigameS2CPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> AnvilMinigameEvents.reset());
    }
}
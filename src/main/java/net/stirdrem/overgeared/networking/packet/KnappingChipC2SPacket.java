package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.screen.RockKnappingMenu;

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

    public static void handle(KnappingChipC2SPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof RockKnappingMenu menu)) return;
            // Validate the index is within bounds
            if (!(payload.index() >= 0 && payload.index() < 9)) {
                OvergearedMod.LOGGER.error("Invalid index received in KnappingChipC2SPacket from {} at {}",
                        player.getName().getString(), payload.index());
                return;
            }
            menu.setChip(payload.index());
            OvergearedMod.LOGGER.debug("Player {} chipped spot {} in knapping grid",
                    player.getName().getString(), payload.index());
        });
    }
}
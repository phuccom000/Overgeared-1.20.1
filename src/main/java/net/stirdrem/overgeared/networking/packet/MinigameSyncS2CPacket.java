package net.stirdrem.overgeared.networking.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

public record MinigameSyncS2CPacket(CompoundTag minigameData) implements CustomPacketPayload {
    public static final ResourceLocation ID = OvergearedMod.loc("minigame_sync");
    public static final CustomPacketPayload.Type<MinigameSyncS2CPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, MinigameSyncS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeNbt(packet.minigameData),
            buffer -> new MinigameSyncS2CPacket(buffer.readNbt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MinigameSyncS2CPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Validate the NBT data first
            if (payload.minigameData == null) {
                OvergearedMod.LOGGER.error("Received null minigame data in packet");
                return;
            }
            ClientAnvilMinigameData.loadFromNBT(payload.minigameData);
            ModItemInteractEvents.handleAnvilOwnershipSync(payload.minigameData);
        });
    }
}

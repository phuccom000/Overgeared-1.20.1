package net.stirdrem.overgeared.networking.packet;/*
package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class AnvilOccupationSyncS2CPacket {
    private final Map<BlockPos, UUID> occupiedAnvils;

    public AnvilOccupationSyncS2CPacket(Map<BlockPos, UUID> map) {
        this.occupiedAnvils = map;
    }

    public AnvilOccupationSyncS2CPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.occupiedAnvils = new HashMap<>();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buf.readBlockPos();
            UUID uuid = buf.readUUID();
            occupiedAnvils.put(pos, uuid);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(occupiedAnvils.size());
        for (Map.Entry<BlockPos, UUID> entry : occupiedAnvils.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            buf.writeUUID(entry.getValue());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                SmithingAnvilBlockEntity.handleClientSync(occupiedAnvils);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
*/

package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;

import java.util.UUID;
import java.util.function.Supplier;

public class StartMinigameS2CPacket {
    private final BlockPos anvilPos;
    private final UUID playerUUID;
    private final ItemStack resultItem;
    private final int requiredHits;

    public StartMinigameS2CPacket(BlockPos anvilPos, UUID playerUUID, ItemStack resultItem, int requiredHits) {
        this.anvilPos = anvilPos;
        this.playerUUID = playerUUID;
        this.resultItem = resultItem;
        this.requiredHits = requiredHits;
    }

    public StartMinigameS2CPacket(FriendlyByteBuf buf) {
        this.anvilPos = buf.readBlockPos();
        this.playerUUID = buf.readUUID();
        this.resultItem = buf.readItem();
        this.requiredHits = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(anvilPos);
        buf.writeUUID(playerUUID);
        buf.writeItem(resultItem);
        buf.writeInt(requiredHits);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Verify this is for the current player
            if (AnvilMinigameOverlay.isCurrentPlayer(playerUUID)) {
                AnvilMinigameOverlay.startMinigame(anvilPos, playerUUID, resultItem, requiredHits);
            }
        });
        return true;
    }
}
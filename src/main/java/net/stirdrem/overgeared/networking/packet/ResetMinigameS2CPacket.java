package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.networking.ModMessages;

import java.util.function.Supplier;

public class ResetMinigameS2CPacket {
    private final BlockPos anvilPos;

    public ResetMinigameS2CPacket(BlockPos anvilPos) {
        this.anvilPos = anvilPos;
    }

    public ResetMinigameS2CPacket(FriendlyByteBuf buf) {
        this.anvilPos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(anvilPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            try {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    OvergearedMod.LOGGER.info("Resetting player's position for {} at anvil {}", player.getName().getString(), anvilPos);
                }
                // Only reset if the current anvil position matches the one in the packet
                if (ModItemInteractEvents.playerAnvilPositions.getOrDefault(player.getUUID(), BlockPos.ZERO).equals(anvilPos)) {
                    ModItemInteractEvents.playerAnvilPositions.remove(player.getUUID());
                    ModItemInteractEvents.playerMinigameVisibility.remove(player.getUUID());
                    AnvilMinigameEvents.reset();
                }
            } catch (Exception e) {
                OvergearedMod.LOGGER.error("Failed to process ResetMinigameS2CPacket for anvil at {}", anvilPos, e);
            }
        });
        return true;
    }

    public BlockPos getAnvilPos() {
        return anvilPos;
    }
}
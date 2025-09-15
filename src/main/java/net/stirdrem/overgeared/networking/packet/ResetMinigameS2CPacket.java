package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

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
                    BlockEntity be = player.level.getBlockEntity(anvilPos);
                    if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
                        String quality = anvil.blueprintQuality(); // ✅ get directly from BE
                        OvergearedMod.LOGGER.info(
                                "Resetting minigame for {} at anvil {} with quality {}",
                                player.getName().getString(), anvilPos, quality
                        );

                        // Only reset if the player's tracked anvil matches
                        if (ModItemInteractEvents.playerAnvilPositions
                                .getOrDefault(player.getUUID(), BlockPos.ZERO)
                                .equals(anvilPos)) {
                            ModItemInteractEvents.playerAnvilPositions.remove(player.getUUID());
                            ModItemInteractEvents.playerMinigameVisibility.remove(player.getUUID());
                            AnvilMinigameEvents.reset(quality);
                        }
                    }
                }
            } catch (Exception e) {
                OvergearedMod.LOGGER.error(
                        "Failed to process ResetMinigameS2CPacket for anvil at {}",
                        anvilPos, e
                );
            }
        });
        return true;
    }

    public BlockPos getAnvilPos() {
        return anvilPos;
    }
}

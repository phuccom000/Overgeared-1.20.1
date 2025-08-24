package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;

import java.util.function.Supplier;

public class OnlyResetMinigameS2CPacket {

    public OnlyResetMinigameS2CPacket() {

    }

    public OnlyResetMinigameS2CPacket(FriendlyByteBuf buf) {

    }

    public void toBytes(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            try {
                // Only reset if the current anvil position matches the one in the packet
                AnvilMinigameEvents.reset();

            } catch (Exception e) {
                OvergearedMod.LOGGER.error("Failed to process ResetMinigameS2CPacket for anvil", e);
            }
        });
        return true;
    }
}
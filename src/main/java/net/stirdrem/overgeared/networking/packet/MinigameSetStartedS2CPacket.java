package net.stirdrem.overgeared.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

import java.util.function.Supplier;

public class MinigameSetStartedS2CPacket {
    private final BlockPos pos;

    public MinigameSetStartedS2CPacket(BlockPos pos) {
        this.pos = pos;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public MinigameSetStartedS2CPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            AnvilMinigameEvents.setMinigameStarted(pos, true);
            AnvilMinigameEvents.setIsVisible(pos, true);
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}

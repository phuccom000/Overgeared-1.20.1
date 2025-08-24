package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvilNew;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.networking.ModMessages;

import java.util.function.Supplier;

import static net.stirdrem.overgeared.event.ModItemInteractEvents.playerAnvilPositions;
import static net.stirdrem.overgeared.event.ModItemInteractEvents.playerMinigameVisibility;

public class MinigameSetStartedC2SPacket {
    private final BlockPos pos;

    public MinigameSetStartedC2SPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(MinigameSetStartedC2SPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static MinigameSetStartedC2SPacket decode(FriendlyByteBuf buf) {
        return new MinigameSetStartedC2SPacket(buf.readBlockPos());
    }


    public static void handle(MinigameSetStartedC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                BlockEntity be = sender.level().getBlockEntity(msg.pos);
                if (be instanceof AbstractSmithingAnvilBlockEntity anvilEntity) {
                    //anvilEntity.resetProgress(); // or resetProgress(), whichever you want
                    AnvilMinigameEvents.setMinigameStarted(msg.pos, true);
                    ModMessages.sendToPlayer(new MinigameSetStartedS2CPacket(msg.pos), sender);
                    playerAnvilPositions.put(sender.getUUID(), msg.pos);
                    playerMinigameVisibility.put(sender.getUUID(), true);
                }
                if (sender.level().getBlockState(msg.pos).getBlock() instanceof AbstractSmithingAnvilNew anvilBlock) {
                    anvilBlock.setMinigameStarted(true);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }


}

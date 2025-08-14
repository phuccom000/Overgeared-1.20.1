package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvilNew;

import java.util.function.Supplier;

public class PacketSendMinigameC2SPacket {
    private final Boolean visible;
    private final BlockPos pos;

    public PacketSendMinigameC2SPacket(BlockPos pos, Boolean visible) {
        this.visible = visible;
        this.pos = pos;
    }

    public static void encode(PacketSendMinigameC2SPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeBoolean(pkt.visible);
    }

    public static PacketSendMinigameC2SPacket decode(FriendlyByteBuf buf) {
        return new PacketSendMinigameC2SPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public Boolean getVisible() {
        return visible;
    }

    public static void handle(PacketSendMinigameC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null && sender.level().getBlockState(msg.pos).getBlock() instanceof AbstractSmithingAnvilNew anvilBlock) {
                //sender.sendSystemMessage(Component.literal("Server visible: " + msg.getVisible()));
                // Update the anvil's minigame state
                anvilBlock.setMinigameOn(msg.getVisible());
            }
        });
        ctx.get().setPacketHandled(true);
    }


}

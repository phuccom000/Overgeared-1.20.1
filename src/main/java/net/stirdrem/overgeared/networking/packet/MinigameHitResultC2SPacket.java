package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.HitResult;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;

import java.util.function.Supplier;

public class MinigameHitResultC2SPacket {
    private final BlockPos anvilPos;
    private final HitResult result;

    public MinigameHitResultC2SPacket(BlockPos anvilPos, HitResult result) {
        this.anvilPos = anvilPos;
        this.result = result;
    }

    public static void encode(MinigameHitResultC2SPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.anvilPos);
        buf.writeEnum(msg.result);
    }

    public static MinigameHitResultC2SPacket decode(FriendlyByteBuf buf) {
        return new MinigameHitResultC2SPacket(
                buf.readBlockPos(),
                buf.readEnum(HitResult.class)
        );
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                    if (minigame.getAnvilPos() != null && minigame.getAnvilPos().equals(anvilPos)) {
                        minigame.serverHandleHit(player, result);
                    }
                });
            }
        });
        return true;
    }
}
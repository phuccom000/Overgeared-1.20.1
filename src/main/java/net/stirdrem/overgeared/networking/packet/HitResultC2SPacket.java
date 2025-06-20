package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;

import java.util.function.Supplier;

public class HitResultC2SPacket {
    private final BlockPos anvilPos;
    private final String hitQuality;
    private final int perfectHits;
    private final int goodHits;
    private final int missedHits;

    public HitResultC2SPacket(BlockPos anvilPos, String hitQuality, int perfectHits, int goodHits, int missedHits) {
        this.anvilPos = anvilPos;
        this.hitQuality = hitQuality;
        this.perfectHits = perfectHits;
        this.goodHits = goodHits;
        this.missedHits = missedHits;
    }

    public HitResultC2SPacket(FriendlyByteBuf buf) {
        this.anvilPos = buf.readBlockPos();
        this.hitQuality = buf.readUtf();
        this.perfectHits = buf.readInt();
        this.goodHits = buf.readInt();
        this.missedHits = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(anvilPos);
        buf.writeUtf(hitQuality);
        buf.writeInt(perfectHits);
        buf.writeInt(goodHits);
        buf.writeInt(missedHits);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            // Validate player is near the anvil
            if (player.distanceToSqr(anvilPos.getX() + 0.5, anvilPos.getY() + 0.5, anvilPos.getZ() + 0.5) > 64) {
                return;
            }

            if (player.level().getBlockEntity(anvilPos) instanceof SmithingAnvilBlockEntity anvil) {
                // Verify this player is the one currently forging
                if (!anvil.isPlayerForging(player)) {
                    return;
                }

                // Register the hit with the anvil
                anvil.registerHit(hitQuality);
            }
        });
        return true;
    }
}
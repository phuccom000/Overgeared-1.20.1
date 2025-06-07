package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.minigame.AnvilMinigame;

import java.util.function.Supplier;

public class ToggleMinigamePauseC2SPacket {
    private final boolean unpaused;
    private final BlockPos anvilPos;
    private final int hitsRemaining;
    private final int perfectHits;
    private final int goodHits;
    private final int missedHits;
    private final float arrowPosition;
    private final float arrowSpeed;
    private final int perfectZoneStart;
    private final int perfectZoneEnd;
    private final int goodZoneStart;
    private final int goodZoneEnd;
    private final boolean isVisible;

    public ToggleMinigamePauseC2SPacket(boolean unpaused, BlockPos anvilPos, int hitsRemaining, int perfectHits,
                                        int goodHits, int missedHits, float arrowPosition, float arrowSpeed,
                                        int perfectZoneStart, int perfectZoneEnd, int goodZoneStart, int goodZoneEnd,
                                        boolean isVisible) {
        this.unpaused = unpaused;
        this.anvilPos = anvilPos;
        this.hitsRemaining = hitsRemaining;
        this.perfectHits = perfectHits;
        this.goodHits = goodHits;
        this.missedHits = missedHits;
        this.arrowPosition = arrowPosition;
        this.arrowSpeed = arrowSpeed;
        this.perfectZoneStart = perfectZoneStart;
        this.perfectZoneEnd = perfectZoneEnd;
        this.goodZoneStart = goodZoneStart;
        this.goodZoneEnd = goodZoneEnd;
        this.isVisible = isVisible;
    }

    public static void encode(ToggleMinigamePauseC2SPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.unpaused);
        buf.writeBlockPos(msg.anvilPos);
        buf.writeInt(msg.hitsRemaining);
        buf.writeInt(msg.perfectHits);
        buf.writeInt(msg.goodHits);
        buf.writeInt(msg.missedHits);
        buf.writeFloat(msg.arrowPosition);
        buf.writeFloat(msg.arrowSpeed);
        buf.writeInt(msg.perfectZoneStart);
        buf.writeInt(msg.perfectZoneEnd);
        buf.writeInt(msg.goodZoneStart);
        buf.writeInt(msg.goodZoneEnd);
        buf.writeBoolean(msg.isVisible);
    }

    public static ToggleMinigamePauseC2SPacket decode(FriendlyByteBuf buf) {
        return new ToggleMinigamePauseC2SPacket(
                buf.readBoolean(),
                buf.readBlockPos(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean()
        );
    }

    public static void handle(ToggleMinigamePauseC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update the server-side minigame state
           /* AnvilMinigame.setUnpaused(msg.unpaused);
            AnvilMinigame.setIsVisible(msg.isVisible);
            AnvilMinigame.setHitsRemaining(msg.hitsRemaining);
            AnvilMinigame.setPerfectHits(msg.perfectHits);
            AnvilMinigame.setGoodHits(msg.goodHits);
            AnvilMinigame.setMissedHits(msg.missedHits);
            AnvilMinigame.setArrowPosition(msg.arrowPosition);
            AnvilMinigame.setArrowSpeed(msg.arrowSpeed);
            AnvilMinigame.setPerfectZoneStart(msg.perfectZoneStart);
            AnvilMinigame.setPerfectZoneEnd(msg.perfectZoneEnd);
            AnvilMinigame.setGoodZoneStart(msg.goodZoneStart);
            AnvilMinigame.setGoodZoneEnd(msg.goodZoneEnd);*/
        });
        ctx.get().setPacketHandled(true);
    }

    public boolean unpaused() {
        return unpaused;
    }
}

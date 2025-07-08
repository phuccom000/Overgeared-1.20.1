/*
package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;

import java.util.function.Supplier;

public class MinigameSyncS2CPacket {
    private final boolean isVisible;
    private final boolean minigameStarted;
    private final ItemStack resultItem;
    private final int hitsRemaining;
    private final float arrowPosition;
    private final float arrowSpeed;
    private final float maxArrowSpeed;
    private final float speedIncreasePerHit;
    private final boolean movingRight;
    private final int perfectHits;
    private final int goodHits;
    private final int missedHits;
    private final int perfectZoneStart;
    private final int perfectZoneEnd;
    private final int goodZoneStart;
    private final int goodZoneEnd;
    private final float zoneShrinkFactor;
    private final float zoneShiftAmount;
    private final BlockPos anvilPos;

    public MinigameSyncS2CPacket(
            boolean isVisible,
            boolean minigameStarted,
            ItemStack resultItem,
            int hitsRemaining,
            float arrowPosition,
            float arrowSpeed,
            float maxArrowSpeed,
            float speedIncreasePerHit,
            boolean movingRight,
            int perfectHits,
            int goodHits,
            int missedHits,
            int perfectZoneStart,
            int perfectZoneEnd,
            int goodZoneStart,
            int goodZoneEnd,
            float zoneShrinkFactor,
            float zoneShiftAmount,
            BlockPos anvilPos
    ) {
        this.isVisible = isVisible;
        this.minigameStarted = minigameStarted;
        this.resultItem = resultItem;
        this.hitsRemaining = hitsRemaining;
        this.arrowPosition = arrowPosition;
        this.arrowSpeed = arrowSpeed;
        this.maxArrowSpeed = maxArrowSpeed;
        this.speedIncreasePerHit = speedIncreasePerHit;
        this.movingRight = movingRight;
        this.perfectHits = perfectHits;
        this.goodHits = goodHits;
        this.missedHits = missedHits;
        this.perfectZoneStart = perfectZoneStart;
        this.perfectZoneEnd = perfectZoneEnd;
        this.goodZoneStart = goodZoneStart;
        this.goodZoneEnd = goodZoneEnd;
        this.zoneShrinkFactor = zoneShrinkFactor;
        this.zoneShiftAmount = zoneShiftAmount;
        this.anvilPos = anvilPos;
    }

    public MinigameSyncS2CPacket(FriendlyByteBuf buf) {
        this.isVisible = buf.readBoolean();
        this.minigameStarted = buf.readBoolean();
        this.resultItem = buf.readItem();
        this.hitsRemaining = buf.readInt();
        this.arrowPosition = buf.readFloat();
        this.arrowSpeed = buf.readFloat();
        this.maxArrowSpeed = buf.readFloat();
        this.speedIncreasePerHit = buf.readFloat();
        this.movingRight = buf.readBoolean();
        this.perfectHits = buf.readInt();
        this.goodHits = buf.readInt();
        this.missedHits = buf.readInt();
        this.perfectZoneStart = buf.readInt();
        this.perfectZoneEnd = buf.readInt();
        this.goodZoneStart = buf.readInt();
        this.goodZoneEnd = buf.readInt();
        this.zoneShrinkFactor = buf.readFloat();
        this.zoneShiftAmount = buf.readFloat();
        this.anvilPos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isVisible);
        buf.writeBoolean(minigameStarted);
        buf.writeItem(resultItem);
        buf.writeInt(hitsRemaining);
        buf.writeFloat(arrowPosition);
        buf.writeFloat(arrowSpeed);
        buf.writeFloat(maxArrowSpeed);
        buf.writeFloat(speedIncreasePerHit);
        buf.writeBoolean(movingRight);
        buf.writeInt(perfectHits);
        buf.writeInt(goodHits);
        buf.writeInt(missedHits);
        buf.writeInt(perfectZoneStart);
        buf.writeInt(perfectZoneEnd);
        buf.writeInt(goodZoneStart);
        buf.writeInt(goodZoneEnd);
        buf.writeFloat(zoneShrinkFactor);
        buf.writeFloat(zoneShiftAmount);
        buf.writeBlockPos(anvilPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Update the client-side minigame data
            ClientAnvilMinigameData.setIsVisible(isVisible);
            ClientAnvilMinigameData.setResultItem(resultItem);
            ClientAnvilMinigameData.setHitsRemaining(hitsRemaining);
            ClientAnvilMinigameData.setArrowPosition(arrowPosition);
            ClientAnvilMinigameData.setArrowSpeed(arrowSpeed);
            ClientAnvilMinigameData.setSpeedIncreasePerHit(speedIncreasePerHit);
            ClientAnvilMinigameData.setMovingRight(movingRight);
            ClientAnvilMinigameData.setPerfectHits(perfectHits);
            ClientAnvilMinigameData.setGoodHits(goodHits);
            ClientAnvilMinigameData.setMissedHits(missedHits);
            ClientAnvilMinigameData.setPerfectZoneStart(perfectZoneStart);
            ClientAnvilMinigameData.setPerfectZoneEnd(perfectZoneEnd);
            ClientAnvilMinigameData.setGoodZoneStart(goodZoneStart);
            ClientAnvilMinigameData.setGoodZoneEnd(goodZoneEnd);
            ClientAnvilMinigameData.setZoneShrinkFactor(zoneShrinkFactor);
            ClientAnvilMinigameData.setZoneShiftAmount(zoneShiftAmount);

            // Optionally, log or trigger events for debugging
            // OvergearedMod.LOGGER.info("Synced minigame data to client");
        });
        return true;
    }
}
*/

package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.item.custom.SmithingHammer;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;

import java.util.function.Supplier;

public class MinigameSyncS2CPacket {
    private final CompoundTag minigameData;

    public MinigameSyncS2CPacket(CompoundTag minigameData) {
        this.minigameData = minigameData;
    }

    public MinigameSyncS2CPacket(FriendlyByteBuf buf) {
        this.minigameData = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(minigameData);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Validate the NBT data first
            if (minigameData == null) {
                OvergearedMod.LOGGER.error("Received null minigame data in packet");
                return;
            }

            try {
                // Update all client-side data at once
                ClientAnvilMinigameData.loadFromNBT(minigameData);
                ModItemInteractEvents.handleAnvilOwnershipSync(minigameData);
            } catch (Exception e) {
                OvergearedMod.LOGGER.error("Failed to process minigame sync packet", e);
            }
        });
        return true;
    }
}

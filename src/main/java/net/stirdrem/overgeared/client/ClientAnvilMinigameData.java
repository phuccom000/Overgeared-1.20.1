package net.stirdrem.overgeared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.config.ServerConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientAnvilMinigameData {
    //private static final Map<UUID, PlayerMinigameData> playerData = new HashMap<>();
    public static UUID ownerUUID = null;
    public static boolean isVisible = false;
    public static boolean minigameStarted = false;
    public static ItemStack resultItem = null;
    public static int hitsRemaining = 0;
    public static float arrowPosition = 0;
    public static float arrowSpeed = ServerConfig.DEFAULT_ARROW_SPEED.get().floatValue();
    public static final float maxArrowSpeed = ServerConfig.MAX_SPEED.get().floatValue();
    public static float speedIncreasePerHit = ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue();
    public static boolean movingRight = true;
    public static int perfectHits = 0;
    public static int goodHits = 0;
    public static int missedHits = 0;
    public static int perfectZoneStart = (100 - ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    public static int perfectZoneEnd = (100 + ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    public static int goodZoneStart = perfectZoneStart - 10;
    public static int goodZoneEnd = perfectZoneEnd + 10;
    public static float zoneShrinkFactor = 0.80f;
    public static float zoneShiftAmount = 15.0f;
    public static Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());


    /*private static PlayerMinigameData ClientAnvilMinigameData {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return playerData.computeIfAbsent(mc.player.getUUID(), k -> new PlayerMinigameData());
        }
        return new PlayerMinigameData(); // Fallback
    }*/

    // Visibility
    public static void setIsVisible(boolean visible) {
        ClientAnvilMinigameData.isVisible = visible;
    }

    public static boolean getIsVisible() {
        return ClientAnvilMinigameData.isVisible;
    }

    // Result item
    public static void setResultItem(ItemStack item) {
        ClientAnvilMinigameData.resultItem = item;
    }

    public static ItemStack getResultItem() {
        return ClientAnvilMinigameData.resultItem;
    }

    // Hits remaining
    public static void setHitsRemaining(int hits) {
        ClientAnvilMinigameData.hitsRemaining = hits;
    }

    public static int getHitsRemaining() {
        return ClientAnvilMinigameData.hitsRemaining;
    }

    // Arrow position
    public static void setArrowPosition(float position) {
        ClientAnvilMinigameData.arrowPosition = position;
    }

    public static float getArrowPosition() {
        return ClientAnvilMinigameData.arrowPosition;
    }

    // Arrow speed
    public static void setArrowSpeed(float speed) {
        ClientAnvilMinigameData.arrowSpeed = speed;
    }

    public static float getArrowSpeed() {
        return ClientAnvilMinigameData.arrowSpeed;
    }

    // Max arrow speed
    public static float getMaxArrowSpeed() {
        return ClientAnvilMinigameData.maxArrowSpeed;
    }

    // Speed increase per hit
    public static void setSpeedIncreasePerHit(float increase) {
        ClientAnvilMinigameData.speedIncreasePerHit = increase;
    }

    public static float getSpeedIncreasePerHit() {
        return ClientAnvilMinigameData.speedIncreasePerHit;
    }

    // Arrow direction
    public static void setMovingRight(boolean right) {
        ClientAnvilMinigameData.movingRight = right;
    }

    public static boolean isMovingRight() {
        return ClientAnvilMinigameData.movingRight;
    }

    // Perfect hits
    public static void setPerfectHits(int hits) {
        ClientAnvilMinigameData.perfectHits = hits;
    }

    public static int getPerfectHits() {
        return ClientAnvilMinigameData.perfectHits;
    }

    // Good hits
    public static void setGoodHits(int hits) {
        ClientAnvilMinigameData.goodHits = hits;
    }

    public static int getGoodHits() {
        return ClientAnvilMinigameData.goodHits;
    }

    // Missed hits
    public static void setMissedHits(int hits) {
        ClientAnvilMinigameData.missedHits = hits;
    }

    public static int getMissedHits() {
        return ClientAnvilMinigameData.missedHits;
    }

    // Perfect zone
    public static void setPerfectZoneStart(int start) {
        ClientAnvilMinigameData.perfectZoneStart = start;
    }

    public static int getPerfectZoneStart() {
        return ClientAnvilMinigameData.perfectZoneStart;
    }

    public static void setPerfectZoneEnd(int end) {
        ClientAnvilMinigameData.perfectZoneEnd = end;
    }

    public static int getPerfectZoneEnd() {
        return ClientAnvilMinigameData.perfectZoneEnd;
    }

    // Good zone
    public static void setGoodZoneStart(int start) {
        ClientAnvilMinigameData.goodZoneStart = start;
    }

    public static int getGoodZoneStart() {
        return ClientAnvilMinigameData.goodZoneStart;
    }

    public static void setGoodZoneEnd(int end) {
        ClientAnvilMinigameData.goodZoneEnd = end;
    }

    public static int getGoodZoneEnd() {
        return ClientAnvilMinigameData.goodZoneEnd;
    }

    // Zone shrink factor
    public static void setZoneShrinkFactor(float factor) {
        ClientAnvilMinigameData.zoneShrinkFactor = factor;
    }

    public static float getZoneShrinkFactor() {
        return ClientAnvilMinigameData.zoneShrinkFactor;
    }

    // Zone shift amount
    public static void setZoneShiftAmount(float amount) {
        ClientAnvilMinigameData.zoneShiftAmount = amount;
    }

    public static float getZoneShiftAmount() {
        return ClientAnvilMinigameData.zoneShiftAmount;
    }

    public static void loadFromNBT(CompoundTag nbt) {
        isVisible = nbt.contains("isVisible") && nbt.getBoolean("isVisible");

        if (nbt.hasUUID("ownerUUID")) {
            ownerUUID = nbt.getUUID("ownerUUID");
        } else {
            ownerUUID = null;
        }

        minigameStarted = nbt.contains("minigameStarted") && nbt.getBoolean("minigameStarted");

        if (nbt.contains("resultItem")) {
            resultItem = ItemStack.of(nbt.getCompound("resultItem"));
        } else {
            resultItem = ItemStack.EMPTY;
        }

        hitsRemaining = nbt.contains("hitsRemaining") ? nbt.getInt("hitsRemaining") : 0;
        perfectHits = nbt.contains("perfectHits") ? nbt.getInt("perfectHits") : 0;
        goodHits = nbt.contains("goodHits") ? nbt.getInt("goodHits") : 0;
        missedHits = nbt.contains("missedHits") ? nbt.getInt("missedHits") : 0;

        arrowPosition = nbt.contains("arrowPosition") ? nbt.getFloat("arrowPosition") : 0f;
        arrowSpeed = nbt.contains("arrowSpeed") ? nbt.getFloat("arrowSpeed") : ServerConfig.DEFAULT_ARROW_SPEED.get().floatValue();
        speedIncreasePerHit = nbt.contains("speedIncreasePerHit")
                ? nbt.getFloat("speedIncreasePerHit")
                : ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue();

        movingRight = !nbt.contains("movingRight") || nbt.getBoolean("movingRight");

        perfectZoneStart = nbt.contains("perfectZoneStart") ? nbt.getInt("perfectZoneStart") : (100 - ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
        perfectZoneEnd = nbt.contains("perfectZoneEnd") ? nbt.getInt("perfectZoneEnd") : (100 + ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
        goodZoneStart = nbt.contains("goodZoneStart") ? nbt.getInt("goodZoneStart") : Mth.clamp(perfectZoneStart - 20, 0, 100);
        goodZoneEnd = nbt.contains("goodZoneEnd") ? nbt.getInt("goodZoneEnd") : Mth.clamp(perfectZoneEnd + 20, goodZoneStart, 100);

        if (nbt.contains("zoneShrinkFactor")) {
            zoneShrinkFactor = nbt.getFloat("zoneShrinkFactor");
        }
        if (nbt.contains("zoneShiftAmount")) {
            zoneShiftAmount = nbt.getFloat("zoneShiftAmount");
        }

        // Clamp values
        arrowSpeed = Math.min(arrowSpeed, maxArrowSpeed);
        arrowPosition = Mth.clamp(arrowPosition, 0f, 100f);
        perfectZoneStart = Mth.clamp(perfectZoneStart, 0, 100);
        perfectZoneEnd = Mth.clamp(perfectZoneEnd, perfectZoneStart, 100);
        goodZoneStart = Mth.clamp(goodZoneStart, 0, 100);
        goodZoneEnd = Mth.clamp(goodZoneEnd, goodZoneStart, 100);
    }

    public static UUID getOccupiedAnvil(BlockPos pos) {
        return occupiedAnvils.get(pos);
    }

    public static void putOccupiedAnvil(BlockPos pos, UUID me) {
        occupiedAnvils.put(pos, me);
    }

    private static BlockPos pendingMinigamePos = null;

    public static void setPendingMinigame(BlockPos pos) {
        pendingMinigamePos = pos;
    }

    public static BlockPos getPendingMinigamePos() {
        return pendingMinigamePos;
    }

    public static void clearPendingMinigame() {
        pendingMinigamePos = null;
    }



    /*public static void clearData(UUID playerId) {
        playerData.remove(playerId);
    }*/
}
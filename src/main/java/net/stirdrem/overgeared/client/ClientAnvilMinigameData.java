package net.stirdrem.overgeared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.config.ServerConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientAnvilMinigameData {
    //private static final Map<UUID, PlayerMinigameData> playerData = new HashMap<>();

    public static boolean isVisible = false;
    public static boolean minigameStarted = false;
    public static ItemStack resultItem = null;
    public static int hitsRemaining = 0;
    public static int progress = 0;
    public static int maxprogress = 0;
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
        // Basic game state
        isVisible = nbt.getBoolean("isVisible");
        minigameStarted = nbt.getBoolean("minigameStarted");

        // Item data
        if (nbt.contains("resultItem")) {
            resultItem = ItemStack.of(nbt.getCompound("resultItem"));
        }

        // Game progress
        hitsRemaining = nbt.getInt("hitsRemaining");
        perfectHits = nbt.getInt("perfectHits");
        goodHits = nbt.getInt("goodHits");
        missedHits = nbt.getInt("missedHits");

        // Arrow mechanics
        arrowPosition = nbt.getFloat("arrowPosition");
        arrowSpeed = nbt.getFloat("arrowSpeed");
        speedIncreasePerHit = nbt.getFloat("speedIncreasePerHit");
        movingRight = nbt.getBoolean("movingRight");

        // Zone data
        perfectZoneStart = nbt.getInt("perfectZoneStart");
        perfectZoneEnd = nbt.getInt("perfectZoneEnd");
        goodZoneStart = nbt.getInt("goodZoneStart");
        goodZoneEnd = nbt.getInt("goodZoneEnd");

        // Zone behavior modifiers
        zoneShrinkFactor = nbt.getFloat("zoneShrinkFactor");
        zoneShiftAmount = nbt.getFloat("zoneShiftAmount");

        // Additional validation
        if (arrowSpeed > maxArrowSpeed) {
            arrowSpeed = maxArrowSpeed;
        }

        // Clamp values to valid ranges
        arrowPosition = Math.max(0, Math.min(100, arrowPosition));
        perfectZoneStart = Math.max(0, Math.min(100, perfectZoneStart));
        perfectZoneEnd = Math.max(0, Math.min(100, perfectZoneEnd));
        goodZoneStart = Math.max(0, Math.min(100, goodZoneStart));
        goodZoneEnd = Math.max(0, Math.min(100, goodZoneEnd));
    }

    /*public static void clearData(UUID playerId) {
        playerData.remove(playerId);
    }*/

    public static void resetData() {
        // Reset visibility and state
        isVisible = false;
        minigameStarted = false;

        // Clear item reference
        resultItem = null;

        // Reset progress tracking
        hitsRemaining = 0;
        perfectHits = 0;
        goodHits = 0;
        missedHits = 0;

        // Reset arrow mechanics
        arrowPosition = 0;
        arrowSpeed = ServerConfig.DEFAULT_ARROW_SPEED.get().floatValue();
        speedIncreasePerHit = ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue();
        movingRight = true;

        // Reset zones to default positions
        perfectZoneStart = (100 - ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
        perfectZoneEnd = (100 + ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
        goodZoneStart = perfectZoneStart - 10;
        goodZoneEnd = perfectZoneEnd + 10;

        // Reset zone behavior modifiers
        zoneShrinkFactor = 0.80f;
        zoneShiftAmount = 15.0f;
    }

    public static int getProgress() {
        return progress;
    }

    public static int getMaxProgress() {
        return maxprogress;
    }
}
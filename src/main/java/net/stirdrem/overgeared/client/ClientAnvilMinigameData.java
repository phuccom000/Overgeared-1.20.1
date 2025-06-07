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
    private static final Map<UUID, PlayerMinigameData> playerData = new HashMap<>();

    public static class PlayerMinigameData {
        public boolean isVisible = false;
        public boolean minigameStarted = false;
        public ItemStack resultItem = null;
        public int hitsRemaining = 0;
        public float arrowPosition = 0;
        public float arrowSpeed = ServerConfig.DEFAULT_ARROW_SPEED.get().floatValue();
        public final float maxArrowSpeed = ServerConfig.MAX_SPEED.get().floatValue();
        public float speedIncreasePerHit = ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue();
        public boolean movingRight = true;
        public int perfectHits = 0;
        public int goodHits = 0;
        public int missedHits = 0;
        public int perfectZoneStart = (100 - ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
        public int perfectZoneEnd = (100 + ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
        public int goodZoneStart = perfectZoneStart - 10;
        public int goodZoneEnd = perfectZoneEnd + 10;
        public float zoneShrinkFactor = 0.80f;
        public float zoneShiftAmount = 15.0f;
    }

    private static PlayerMinigameData getData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return playerData.computeIfAbsent(mc.player.getUUID(), k -> new PlayerMinigameData());
        }
        return new PlayerMinigameData(); // Fallback
    }

    // Visibility
    public static void setIsVisible(boolean visible) {
        getData().isVisible = visible;
    }

    public static boolean getIsVisible() {
        return getData().isVisible;
    }

    // Result item
    public static void setResultItem(ItemStack item) {
        getData().resultItem = item;
    }

    public static ItemStack getResultItem() {
        return getData().resultItem;
    }

    // Hits remaining
    public static void setHitsRemaining(int hits) {
        getData().hitsRemaining = hits;
    }

    public static int getHitsRemaining() {
        return getData().hitsRemaining;
    }

    // Arrow position
    public static void setArrowPosition(float position) {
        getData().arrowPosition = position;
    }

    public static float getArrowPosition() {
        return getData().arrowPosition;
    }

    // Arrow speed
    public static void setArrowSpeed(float speed) {
        getData().arrowSpeed = speed;
    }

    public static float getArrowSpeed() {
        return getData().arrowSpeed;
    }

    // Max arrow speed
    public static float getMaxArrowSpeed() {
        return getData().maxArrowSpeed;
    }

    // Speed increase per hit
    public static void setSpeedIncreasePerHit(float increase) {
        getData().speedIncreasePerHit = increase;
    }

    public static float getSpeedIncreasePerHit() {
        return getData().speedIncreasePerHit;
    }

    // Arrow direction
    public static void setMovingRight(boolean right) {
        getData().movingRight = right;
    }

    public static boolean isMovingRight() {
        return getData().movingRight;
    }

    // Perfect hits
    public static void setPerfectHits(int hits) {
        getData().perfectHits = hits;
    }

    public static int getPerfectHits() {
        return getData().perfectHits;
    }

    // Good hits
    public static void setGoodHits(int hits) {
        getData().goodHits = hits;
    }

    public static int getGoodHits() {
        return getData().goodHits;
    }

    // Missed hits
    public static void setMissedHits(int hits) {
        getData().missedHits = hits;
    }

    public static int getMissedHits() {
        return getData().missedHits;
    }

    // Perfect zone
    public static void setPerfectZoneStart(int start) {
        getData().perfectZoneStart = start;
    }

    public static int getPerfectZoneStart() {
        return getData().perfectZoneStart;
    }

    public static void setPerfectZoneEnd(int end) {
        getData().perfectZoneEnd = end;
    }

    public static int getPerfectZoneEnd() {
        return getData().perfectZoneEnd;
    }

    // Good zone
    public static void setGoodZoneStart(int start) {
        getData().goodZoneStart = start;
    }

    public static int getGoodZoneStart() {
        return getData().goodZoneStart;
    }

    public static void setGoodZoneEnd(int end) {
        getData().goodZoneEnd = end;
    }

    public static int getGoodZoneEnd() {
        return getData().goodZoneEnd;
    }

    // Zone shrink factor
    public static void setZoneShrinkFactor(float factor) {
        getData().zoneShrinkFactor = factor;
    }

    public static float getZoneShrinkFactor() {
        return getData().zoneShrinkFactor;
    }

    // Zone shift amount
    public static void setZoneShiftAmount(float amount) {
        getData().zoneShiftAmount = amount;
    }

    public static float getZoneShiftAmount() {
        return getData().zoneShiftAmount;
    }

    public static void loadFromNBT(CompoundTag nbt) {
        PlayerMinigameData data = getData();

        // Basic game state
        data.isVisible = nbt.getBoolean("isVisible");
        data.minigameStarted = nbt.getBoolean("minigameStarted");

        // Item data
        if (nbt.contains("resultItem")) {
            data.resultItem = ItemStack.of(nbt.getCompound("resultItem"));
        }

        // Game progress
        data.hitsRemaining = nbt.getInt("hitsRemaining");
        data.perfectHits = nbt.getInt("perfectHits");
        data.goodHits = nbt.getInt("goodHits");
        data.missedHits = nbt.getInt("missedHits");

        // Arrow mechanics
        data.arrowPosition = nbt.getFloat("arrowPosition");
        data.arrowSpeed = nbt.getFloat("arrowSpeed");
        data.speedIncreasePerHit = nbt.getFloat("speedIncreasePerHit");
        data.movingRight = nbt.getBoolean("movingRight");

        // Zone data
        data.perfectZoneStart = nbt.getInt("perfectZoneStart");
        data.perfectZoneEnd = nbt.getInt("perfectZoneEnd");
        data.goodZoneStart = nbt.getInt("goodZoneStart");
        data.goodZoneEnd = nbt.getInt("goodZoneEnd");

        // Zone behavior modifiers
        data.zoneShrinkFactor = nbt.getFloat("zoneShrinkFactor");
        data.zoneShiftAmount = nbt.getFloat("zoneShiftAmount");

        // Additional validation
        if (data.arrowSpeed > data.maxArrowSpeed) {
            data.arrowSpeed = data.maxArrowSpeed;
        }

        // Clamp values to valid ranges
        data.arrowPosition = Math.max(0, Math.min(100, data.arrowPosition));
        data.perfectZoneStart = Math.max(0, Math.min(100, data.perfectZoneStart));
        data.perfectZoneEnd = Math.max(0, Math.min(100, data.perfectZoneEnd));
        data.goodZoneStart = Math.max(0, Math.min(100, data.goodZoneStart));
        data.goodZoneEnd = Math.max(0, Math.min(100, data.goodZoneEnd));
    }

    public static void clearData(UUID playerId) {
        playerData.remove(playerId);
    }
}
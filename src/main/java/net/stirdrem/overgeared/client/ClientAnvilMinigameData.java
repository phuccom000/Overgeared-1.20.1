package net.stirdrem.overgeared.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.config.ServerConfig;

public class ClientAnvilMinigameData {
    private static boolean isVisible = false;
    private static boolean minigameStarted = false;
    private static ItemStack resultItem;
    private static int hitsRemaining = 0;
    private static float arrowPosition = 0;
    private static double temp1 = ServerConfig.DEFAULT_ARROW_SPEED.get();
    private static float arrowSpeed = (float) temp1;
    private static double temp2 = ServerConfig.MAX_SPEED.get();
    private static final float maxArrowSpeed = (float) temp2;
    private static double temp3 = ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get();
    private static float speedIncreasePerHit = (float) temp3;
    private static boolean movingRight = true;
    private static int perfectHits = 0;
    private static int goodHits = 0;
    private static int missedHits = 0;

    private static final int INITIAL_PERFECT_ZONE_SIZE = 20; // 40-60 (20% wide)
    private static final int INITIAL_GOOD_ZONE_SIZE = 40;    // 30-70 (40% wide)
    private static final int PERFECT_ZONE_START = (100 - ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    private static final int PERFECT_ZONE_END = (100 + ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    private static final int GOOD_ZONE_START = PERFECT_ZONE_START - 10;
    private static final int GOOD_ZONE_END = PERFECT_ZONE_END + 10;
    private static int perfectZoneStart = PERFECT_ZONE_START;
    private static int perfectZoneEnd = PERFECT_ZONE_END;
    private static int goodZoneStart = GOOD_ZONE_START;
    private static int goodZoneEnd = GOOD_ZONE_END;
    private static float zoneShrinkFactor = 0.80f; // Zones shrink to 80% of their size each hit
    private static float zoneShiftAmount = 15.0f; // Zones shift by 15% each hit

    // Visibility
    public static void setIsVisible(boolean visible) {
        isVisible = visible;
    }

    public static boolean getIsVisible() {
        return isVisible;
    }

    // Result item
    public static void setResultItem(ItemStack item) {
        resultItem = item;
    }

    public static ItemStack getResultItem() {
        return resultItem;
    }

    // Hits remaining
    public static void setHitsRemaining(int hits) {
        hitsRemaining = hits;
    }

    public static int getHitsRemaining() {
        return hitsRemaining;
    }

    // Arrow position
    public static void setArrowPosition(float position) {
        arrowPosition = position;
    }

    public static float getArrowPosition() {
        return arrowPosition;
    }

    // Arrow speed
    public static void setArrowSpeed(float speed) {
        arrowSpeed = speed;
    }

    public static float getArrowSpeed() {
        return arrowSpeed;
    }

    // Max arrow speed (no setter since it's final)
    public static float getMaxArrowSpeed() {
        return maxArrowSpeed;
    }

    // Speed increase per hit
    public static void setSpeedIncreasePerHit(float increase) {
        speedIncreasePerHit = increase;
    }

    public static float getSpeedIncreasePerHit() {
        return speedIncreasePerHit;
    }

    // Arrow direction
    public static void setMovingRight(boolean right) {
        movingRight = right;
    }

    public static boolean isMovingRight() {
        return movingRight;
    }

    // Perfect hits
    public static void setPerfectHits(int hits) {
        perfectHits = hits;
    }

    public static int getPerfectHits() {
        return perfectHits;
    }

    // Good hits
    public static void setGoodHits(int hits) {
        goodHits = hits;
    }

    public static int getGoodHits() {
        return goodHits;
    }

    // Missed hits
    public static void setMissedHits(int hits) {
        missedHits = hits;
    }

    public static int getMissedHits() {
        return missedHits;
    }

    // Perfect zone
    public static void setPerfectZoneStart(int start) {
        perfectZoneStart = start;
    }

    public static int getPerfectZoneStart() {
        return perfectZoneStart;
    }

    public static void setPerfectZoneEnd(int end) {
        perfectZoneEnd = end;
    }

    public static int getPerfectZoneEnd() {
        return perfectZoneEnd;
    }

    // Good zone
    public static void setGoodZoneStart(int start) {
        goodZoneStart = start;
    }

    public static int getGoodZoneStart() {
        return goodZoneStart;
    }

    public static void setGoodZoneEnd(int end) {
        goodZoneEnd = end;
    }

    public static int getGoodZoneEnd() {
        return goodZoneEnd;
    }

    // Zone shrink factor
    public static void setZoneShrinkFactor(float factor) {
        zoneShrinkFactor = factor;
    }

    public static float getZoneShrinkFactor() {
        return zoneShrinkFactor;
    }

    // Zone shift amount
    public static void setZoneShiftAmount(float amount) {
        zoneShiftAmount = amount;
    }

    public static float getZoneShiftAmount() {
        return zoneShiftAmount;
    }

    public static void loadFromNBT(CompoundTag nbt) {
        // Basic game state
        setIsVisible(nbt.getBoolean("isVisible"));
        setMinigameStarted(nbt.getBoolean("minigameStarted"));

        // Item data
        if (nbt.contains("resultItem")) {
            setResultItem(ItemStack.of(nbt.getCompound("resultItem")));
        }

        // Game progress
        setHitsRemaining(nbt.getInt("hitsRemaining"));
        setPerfectHits(nbt.getInt("perfectHits"));
        setGoodHits(nbt.getInt("goodHits"));
        setMissedHits(nbt.getInt("missedHits"));

        // Arrow mechanics
        setArrowPosition(nbt.getFloat("arrowPosition"));
        setArrowSpeed(nbt.getFloat("arrowSpeed"));
        setSpeedIncreasePerHit(nbt.getFloat("speedIncreasePerHit"));
        setMovingRight(nbt.getBoolean("movingRight"));

        // Zone data
        setPerfectZoneStart(nbt.getInt("perfectZoneStart"));
        setPerfectZoneEnd(nbt.getInt("perfectZoneEnd"));
        setGoodZoneStart(nbt.getInt("goodZoneStart"));
        setGoodZoneEnd(nbt.getInt("goodZoneEnd"));

        // Zone behavior modifiers
        setZoneShrinkFactor(nbt.getFloat("zoneShrinkFactor"));
        setZoneShiftAmount(nbt.getFloat("zoneShiftAmount"));

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

    private static void setMinigameStarted(boolean mstarted) {
        minigameStarted = mstarted;
    }
}

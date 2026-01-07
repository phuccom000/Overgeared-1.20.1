package net.stirdrem.overgeared.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.networking.packet.SetMinigameVisibleC2SPacket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID, value = Dist.CLIENT)
public class AnvilMinigameEvents {
    public static UUID ownerUUID = null;
    private static boolean isVisible = false;
    public static boolean minigameStarted = false;
    public static ItemStack resultItem = null;
    public static int hitsRemaining = 0;
    public static int maxHits = 0;
    public static float arrowPosition = 1;

    // Initialize with placeholder defaults (will be overridden later)
    public static float arrowSpeed = 0;
    public static float maxArrowSpeed = 0;
    public static float speedIncreasePerHit = 0;
    public static boolean movingRight = true;
    public static int perfectHits = 0;
    public static int goodHits = 0;
    public static int missedHits = 0;

    public static int perfectZoneStart = 45;
    public static int perfectZoneEnd = 55;
    public static int goodZoneStart = 35;
    public static int goodZoneEnd = 65;
    public static float zoneShrinkFactor = 0.95f;
    public static float zoneShiftAmount = 15.0f;
    public static float perfectZoneSize = perfectZoneEnd - perfectZoneStart;
    public static float minPerfectSize = 4;
    public static Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());
    public static int skillLevel = 0;

    private static int TICKS_PER_PRINT = 1;
    private static int tickAccumulator = 0;
    private static boolean movingDown = false;

    // ===============================
    // Popup system
    // ===============================
    private static final java.util.List<Popup> POPUPS = new java.util.ArrayList<>();
    private static final float POPUP_DURATION_MS = 1500f;
    private static int lastPerfect = 0;
    private static int lastGood = 0;
    private static int lastMiss = 0;

    public static void ensureInitialized() {
        // Only run if defaults haven't been set yet
        if (arrowSpeed == 0) {
            setupForQuality("none"); // or whichever quality you want as baseline
        }
    }

    public static void setupForQuality(String quality) {
        switch (quality.toLowerCase()) {
            case "none" -> {
                arrowSpeed = ServerConfig.DEFAULT_ARROW_SPEED.get().floatValue();
                speedIncreasePerHit = ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue();
                maxArrowSpeed = ServerConfig.DEFAULT_MAX_ARROW_SPEED.get().floatValue();
                zoneShrinkFactor = ServerConfig.DEFAULT_ZONE_SHRINK_FACTOR.get().floatValue();
                perfectZoneStart = (100 - ServerConfig.DEFAULT_ZONE_STARTING_SIZE.get()) / 2;
                perfectZoneEnd = (100 + ServerConfig.DEFAULT_ZONE_STARTING_SIZE.get()) / 2;
                goodZoneStart = Math.max((100 - ServerConfig.DEFAULT_ZONE_STARTING_SIZE.get() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + ServerConfig.DEFAULT_ZONE_STARTING_SIZE.get() * 3) / 2, 100);
                minPerfectSize = ServerConfig.DEFAULT_MIN_PERFECT_ZONE.get();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
            case "master" -> {
                arrowSpeed = ServerConfig.MASTER_ARROW_SPEED.get().floatValue();
                speedIncreasePerHit = ServerConfig.MASTER_ARROW_SPEED_INCREASE.get().floatValue();
                maxArrowSpeed = ServerConfig.MASTER_MAX_ARROW_SPEED.get().floatValue();
                zoneShrinkFactor = ServerConfig.MASTER_ZONE_SHRINK_FACTOR.get().floatValue();
                perfectZoneStart = (100 - ServerConfig.MASTER_ZONE_STARTING_SIZE.get()) / 2;
                perfectZoneEnd = (100 + ServerConfig.MASTER_ZONE_STARTING_SIZE.get()) / 2;
                goodZoneStart = Math.max((100 - ServerConfig.MASTER_ZONE_STARTING_SIZE.get() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + ServerConfig.MASTER_ZONE_STARTING_SIZE.get() * 3) / 2, 100);
                minPerfectSize = ServerConfig.MASTER_MIN_PERFECT_ZONE.get();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
            case "perfect" -> {
                arrowSpeed = ServerConfig.PERFECT_ARROW_SPEED.get().floatValue();
                speedIncreasePerHit = ServerConfig.PERFECT_ARROW_SPEED_INCREASE.get().floatValue();
                maxArrowSpeed = ServerConfig.PERFECT_MAX_ARROW_SPEED.get().floatValue();
                zoneShrinkFactor = ServerConfig.PERFECT_ZONE_SHRINK_FACTOR.get().floatValue();
                perfectZoneStart = (100 - ServerConfig.PERFECT_ZONE_STARTING_SIZE.get()) / 2;
                perfectZoneEnd = (100 + ServerConfig.PERFECT_ZONE_STARTING_SIZE.get()) / 2;
                goodZoneStart = Math.max((100 - ServerConfig.PERFECT_ZONE_STARTING_SIZE.get() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + ServerConfig.PERFECT_ZONE_STARTING_SIZE.get() * 3) / 2, 100);
                minPerfectSize = ServerConfig.PERFECT_MIN_PERFECT_ZONE.get();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
            case "expert" -> {
                arrowSpeed = ServerConfig.EXPERT_ARROW_SPEED.get().floatValue();
                speedIncreasePerHit = ServerConfig.EXPERT_ARROW_SPEED_INCREASE.get().floatValue();
                maxArrowSpeed = ServerConfig.EXPERT_MAX_ARROW_SPEED.get().floatValue();
                zoneShrinkFactor = ServerConfig.EXPERT_ZONE_SHRINK_FACTOR.get().floatValue();
                perfectZoneStart = (100 - ServerConfig.EXPERT_ZONE_STARTING_SIZE.get()) / 2;
                perfectZoneEnd = (100 + ServerConfig.EXPERT_ZONE_STARTING_SIZE.get()) / 2;
                goodZoneStart = Math.max((100 - ServerConfig.EXPERT_ZONE_STARTING_SIZE.get() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + ServerConfig.EXPERT_ZONE_STARTING_SIZE.get() * 3) / 2, 100);
                minPerfectSize = ServerConfig.EXPERT_MIN_PERFECT_ZONE.get();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
            case "well" -> {
                arrowSpeed = ServerConfig.WELL_ARROW_SPEED.get().floatValue();
                speedIncreasePerHit = ServerConfig.WELL_ARROW_SPEED_INCREASE.get().floatValue();
                maxArrowSpeed = ServerConfig.WELL_MAX_ARROW_SPEED.get().floatValue();
                zoneShrinkFactor = ServerConfig.WELL_ZONE_SHRINK_FACTOR.get().floatValue();
                perfectZoneStart = (100 - ServerConfig.WELL_ZONE_STARTING_SIZE.get()) / 2;
                perfectZoneEnd = (100 + ServerConfig.WELL_ZONE_STARTING_SIZE.get()) / 2;
                goodZoneStart = Math.max((100 - ServerConfig.WELL_ZONE_STARTING_SIZE.get() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + ServerConfig.WELL_ZONE_STARTING_SIZE.get() * 3) / 2, 100);
                minPerfectSize = ServerConfig.WELL_MIN_PERFECT_ZONE.get();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
            default -> { // poor
                arrowSpeed = ServerConfig.POOR_ARROW_SPEED.get().floatValue();
                speedIncreasePerHit = ServerConfig.POOR_ARROW_SPEED_INCREASE.get().floatValue();
                maxArrowSpeed = ServerConfig.POOR_MAX_ARROW_SPEED.get().floatValue();
                zoneShrinkFactor = ServerConfig.POOR_ZONE_SHRINK_FACTOR.get().floatValue();
                perfectZoneStart = (100 - ServerConfig.POOR_ZONE_STARTING_SIZE.get()) / 2;
                perfectZoneEnd = (100 + ServerConfig.POOR_ZONE_STARTING_SIZE.get()) / 2;
                goodZoneStart = Math.max((100 - ServerConfig.POOR_ZONE_STARTING_SIZE.get() * 3) / 2, 1);
                goodZoneEnd = Math.min((100 + ServerConfig.POOR_ZONE_STARTING_SIZE.get() * 3) / 2, 100);
                minPerfectSize = ServerConfig.POOR_MIN_PERFECT_ZONE.get();
                perfectZoneSize = perfectZoneEnd - perfectZoneStart;
            }
        }
    }


    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ensureInitialized();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.isPaused()) updatePopups();
        if (mc.isPaused() || !isVisible()) return;

        tickAccumulator++;
        if (tickAccumulator < TICKS_PER_PRINT) return;
        tickAccumulator = 0;

        // Update arrow movement
        if (arrowPosition >= 100) {
            movingDown = true;
        } else if (arrowPosition <= 1) {
            movingDown = false;
        }

        // Determine movement based on current speed and direction
        float delta = arrowSpeed * (movingDown ? -1 : 1);
        arrowPosition = Math.max(1, Math.min(arrowPosition + delta, 100));

        // Update popups

    }

    private static void updatePopups() {
        // Age existing popups - each tick is 50ms (20 TPS)
        for (int i = 0; i < POPUPS.size(); i++) {
            Popup popup = POPUPS.get(i);
            popup.age += 50f; // 50ms per tick
            if (popup.age >= POPUP_DURATION_MS) {
                POPUPS.remove(i--);
            }
        }
    }

    public static void triggerPopup(Component text) {
        POPUPS.add(new Popup(text));
    }

    public static java.util.List<Popup> getPopups() {
        return POPUPS;
    }

    public static void speedUp() {
        arrowSpeed = Math.min(arrowSpeed + speedIncreasePerHit, maxArrowSpeed);
    }

    public static float getArrowPosition() {
        return arrowPosition;
    }

    public static boolean isVisible() {
        return isVisible;
    }

    public static void setIsVisible(BlockPos pos, boolean isVisible) {
        AnvilMinigameEvents.isVisible = isVisible;
        PacketDistributor.sendToServer(new SetMinigameVisibleC2SPacket(isVisible, pos));
    }

    public static void resetPopUps() {
        POPUPS.clear();
    }

    public static void reset(String blueprintQuality) {
        isVisible = false;
        minigameStarted = false;
        hitsRemaining = 0;
        perfectHits = 0;
        goodHits = 0;
        missedHits = 0;
        arrowPosition = 50;
        movingDown = false;
        currentPerfectZoneSize = 0;
        currentGoodZoneSize = 0;
        lastPerfect = 0;
        lastGood = 0;
        lastMiss = 0;

        setupForQuality(blueprintQuality == null ? "none" : blueprintQuality); // 🔥 initialize from blueprint

        randomizeCenter();
    }

    public static void reset() {
        reset(null);
    }

    // Utility clamp
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void randomizeCenter() {
        // Randomize zone center directly
        float randomCenter = 20 + (float) Math.random() * (60); // random between 20 and 80
        float zoneSize = perfectZoneEnd - perfectZoneStart;
        float goodZoneSize = goodZoneEnd - goodZoneStart;

        int halfPerfect = (int) (zoneSize / 2f);
        int halfGood = (int) (goodZoneSize / 2f);

        perfectZoneStart = clamp((int) randomCenter - halfPerfect, 0, 100);
        perfectZoneEnd = clamp((int) randomCenter + halfPerfect, 0, 100);
        goodZoneStart = clamp((int) randomCenter - halfGood, 0, 100);
        goodZoneEnd = clamp((int) randomCenter + halfGood, 0, 100);
    }

    public static int getPerfectZoneStart() {
        return perfectZoneStart;
    }

    public static int getPerfectZoneEnd() {
        return perfectZoneEnd;
    }

    public static int getGoodZoneStart() {
        return goodZoneStart;
    }

    public static int getGoodZoneEnd() {
        return goodZoneEnd;
    }

    public static int getHitsRemaining() {
        return hitsRemaining;
    }

    public static int getMaxHits() {
        return maxHits;
    }

    public static int getPerfectHits() {
        return perfectHits;
    }

    public static int getGoodHits() {
        return goodHits;
    }

    public static int getMissedHits() {
        return missedHits;
    }

    public static String handleHit() {
        arrowSpeed = Math.min(arrowSpeed + speedIncreasePerHit, maxArrowSpeed);

        if (arrowPosition >= perfectZoneStart && arrowPosition <= perfectZoneEnd) {
            perfectHits++;
            lastPerfect = perfectHits;
            triggerPopup(Component.translatable("overgeared.forging.perfect")
                    .withStyle(s -> s.withBold(true).withColor(0xFFD700)));
        } else if (arrowPosition >= goodZoneStart && arrowPosition <= goodZoneEnd) {
            goodHits++;
            lastGood = goodHits;
            triggerPopup(Component.translatable("overgeared.forging.good")
                    .withStyle(s -> s.withBold(true).withColor(0x55FF55)));
        } else {
            missedHits++;
            lastMiss = missedHits;
            triggerPopup(Component.translatable("overgeared.forging.miss")
                    .withStyle(s -> s.withBold(true).withColor(0xFF5555)));
        }

        shrinkAndShiftZones();
        hitsRemaining--;

        if (hitsRemaining <= 0) {
            return finishForging();
        }
        return "poor";
    }


    public static void setHitsRemaining(int hitsRemaining) {
        AnvilMinigameEvents.hitsRemaining = hitsRemaining;
        AnvilMinigameEvents.maxHits = hitsRemaining;
    }

    public static String finishForging() {
        isVisible = false;
        minigameStarted = false;
        int totalHits = perfectHits + goodHits + missedHits;
        float qualityScore = 0;
        if (totalHits > 0)
            qualityScore = (perfectHits * 1.0f + goodHits * 0.6f) / totalHits;
        if (qualityScore > ServerConfig.PERFECT_QUALITY_SCORE.get()) return "perfect";
        if (qualityScore > ServerConfig.EXPERT_QUALITY_SCORE.get()) return "expert";
        if (qualityScore > ServerConfig.WELL_QUALITY_SCORE.get()) return "well";
        return "poor";
    }

    // Add two static fields to track current sizes independently of clamped values:
    private static float currentPerfectZoneSize = 0;
    private static float currentGoodZoneSize = 0;

    public static void shrinkAndShiftZones() {
        // Initialize once if not yet done
        if (currentPerfectZoneSize == 0 || currentGoodZoneSize == 0) {
            currentPerfectZoneSize = perfectZoneEnd - perfectZoneStart;
            currentGoodZoneSize = goodZoneEnd - goodZoneStart;
        }

        // --- Step 1: Shrink zones based on stored sizes ---
        currentPerfectZoneSize = Math.max(minPerfectSize, currentPerfectZoneSize * zoneShrinkFactor);
        currentGoodZoneSize = Math.max(currentPerfectZoneSize * 3, currentGoodZoneSize * zoneShrinkFactor);

        // --- Step 2: Get old data for comparison ---
        float oldPerfectCenter = (perfectZoneStart + perfectZoneEnd) / 2f;
        float oldGoodCenter = (goodZoneStart + goodZoneEnd) / 2f;
        int oldPerfectStart = perfectZoneStart;
        int oldPerfectEnd = perfectZoneEnd;
        int oldGoodStart = goodZoneStart;
        int oldGoodEnd = goodZoneEnd;

        // --- Step 3: Attempt to find a new valid zone placement ---
        int attempts = 0;
        int newPerfectStart = perfectZoneStart, newPerfectEnd = perfectZoneEnd;
        int newGoodStart = goodZoneStart, newGoodEnd = goodZoneEnd;

        while (attempts < 30) {
            attempts++;

            // Weighted random center: bias near middle but allow full range
            float newCenter = getWeightedRandomCenter(50f);

            // Compute bounds
            int pStart = Math.round(newCenter - currentPerfectZoneSize / 2);
            int pEnd = Math.round(newCenter + currentPerfectZoneSize / 2);
            int gStart = Math.round(newCenter - currentGoodZoneSize / 2);
            int gEnd = Math.round(newCenter + currentGoodZoneSize / 2);

            // Clamp to 0–100 range
            pStart = clamp(pStart, 0, 100);
            pEnd = clamp(pEnd, 0, 100);
            gStart = clamp(gStart, 0, 100);
            gEnd = clamp(gEnd, 0, 100);

            float newPerfectCenter = (pStart + pEnd) / 2f;
            float newGoodCenter = (gStart + gEnd) / 2f;

            // --- Step 4: Check separation conditions ---
            // Tolerances (in percent of bar width)
            float minCenterDiff = 5f;   // must move at least this much from previous center
            float minEdgeDiff = 3f;   // must move edges by at least this much

            boolean perfectTooClose =
                    Math.abs(newPerfectCenter - oldPerfectCenter) < minCenterDiff ||
                            Math.abs(pStart - oldPerfectStart) < minEdgeDiff ||
                            Math.abs(pEnd - oldPerfectEnd) < minEdgeDiff;

            boolean goodTooClose =
                    Math.abs(newGoodCenter - oldGoodCenter) < minCenterDiff ||
                            Math.abs(gStart - oldGoodStart) < minEdgeDiff ||
                            Math.abs(gEnd - oldGoodEnd) < minEdgeDiff;

            // Regenerate if *either* zone is too close
            if (perfectTooClose || goodTooClose) continue;

            // --- Step 5: Accept the new zones ---
            newPerfectStart = pStart;
            newPerfectEnd = pEnd;
            newGoodStart = gStart;
            newGoodEnd = gEnd;
            break;
        }

        // --- Step 6: Apply the new zones ---
        perfectZoneStart = newPerfectStart;
        perfectZoneEnd = newPerfectEnd;
        goodZoneStart = newGoodStart;
        goodZoneEnd = newGoodEnd;
    }


    private static boolean edgesTooCloseOrOverlapping(
            int newStart, int newEnd, int oldStart, int oldEnd, float overlapRatio,
            boolean clampedLeft, boolean clampedRight
    ) {
        int newSize = Math.max(1, newEnd - newStart);
        int tolerance = 1;
        boolean startClose = Math.abs(newStart - oldStart) <= tolerance;
        boolean endClose = Math.abs(newEnd - oldEnd) <= tolerance;

        int overlap = Math.min(newEnd, oldEnd) - Math.max(newStart, oldStart);
        float overlapFraction = overlap > 0 ? (float) overlap / newSize : 0f;

        // Edge fix: allow shifts even if clamped edges stay constant
        if ((clampedLeft && newStart == 0 && oldStart == 0) ||
                (clampedRight && newEnd == 100 && oldEnd == 100)) {
            // If one edge is pinned, only compare the *free* edge
            return overlapFraction > overlapRatio &&
                    ((clampedLeft && endClose) || (clampedRight && startClose));
        }

        return (startClose && endClose) || overlapFraction > overlapRatio;
    }


    private static float getWeightedRandomCenter(float bias) {
        // bias toward middle but allow full 0–100 range
        float rand = (float) Math.random();
        float weighted = (float) Math.pow(rand, 1.5); // tweak exponent for more/less bias
        return bias + (weighted - 0.5f) * 100f;
    }

    public static BlockPos getAnvilPos(UUID playerId) {
        return ModItemInteractEvents.playerAnvilPositions.getOrDefault(playerId, BlockPos.ZERO);
    }

    public static void setAnvilPos(UUID playerId, BlockPos pos) {
        ModItemInteractEvents.playerAnvilPositions.put(playerId, pos);
    }

    public static void clearAnvilPos(UUID playerId) {
        ModItemInteractEvents.playerAnvilPositions.remove(playerId);
    }

    public static void setMinigameStarted(BlockPos pos, boolean minigameStarted) {
        AnvilMinigameEvents.minigameStarted = minigameStarted;

    }

    public static UUID getOccupiedAnvil(BlockPos pos) {
        return occupiedAnvils.get(pos);
    }

    public static void putOccupiedAnvil(BlockPos pos, UUID me) {
        occupiedAnvils.put(pos, me);
    }

    public static boolean hasAnvilPosition(UUID playerId) {
        BlockPos pos = ModItemInteractEvents.playerAnvilPositions.get(playerId);
        return pos != null && !pos.equals(BlockPos.ZERO);
    }

    // ✅ Player-specific hide
    public static void hideMinigame(UUID playerId) {
        isVisible = false;
        BlockPos pos = ModItemInteractEvents.playerAnvilPositions.get(playerId);
        if (pos != null && !pos.equals(BlockPos.ZERO)) {
            PacketDistributor.sendToServer(new SetMinigameVisibleC2SPacket(false, pos));
        }
        //clearAnvilPos(playerId);
    }

    // ===============================
    // Popup class
    // ===============================
    public static class Popup {
        public final Component text;
        public float age; // milliseconds

        public Popup(Component text) {
            this.text = text;
            this.age = 0f;
        }
    }
}
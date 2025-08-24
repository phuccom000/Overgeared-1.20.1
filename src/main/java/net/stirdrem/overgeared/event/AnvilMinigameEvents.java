package net.stirdrem.overgeared.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.SetMinigameVisibleC2SPacket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AnvilMinigameEvents {
    public static UUID ownerUUID = null;
    private static boolean isVisible = false;
    public static boolean minigameStarted = false;
    public static ItemStack resultItem = null;
    public static int hitsRemaining = 0;
    public static float arrowPosition = 1;
    public static float arrowSpeed = ServerConfig.DEFAULT_ARROW_SPEED.get().floatValue();
    public static final float maxArrowSpeed = ServerConfig.MAX_ARROW_SPEED.get().floatValue();
    public static float speedIncreasePerHit = ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue();
    public static boolean movingRight = true;
    public static int perfectHits = 0;
    public static int goodHits = 0;
    public static int missedHits = 0;
    public static int perfectZoneStart = (100 - ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    public static int perfectZoneEnd = (100 + ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    public static int goodZoneStart = Math.max((100 - ServerConfig.ZONE_STARTING_SIZE.get() * 3) / 2, 1);
    public static int goodZoneEnd = Math.min((100 + ServerConfig.ZONE_STARTING_SIZE.get() * 3) / 2, 100);
    public static float zoneShrinkFactor = 0.80f;
    public static float zoneShiftAmount = 15.0f;
    public static Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());
    public static int skillLevel = 0;
    //public static BlockPos anvilPos;

    private static int TICKS_PER_PRINT = 1;

    // Current internal tick accumulator
    private static int tickAccumulator = 0;

    // Current arrowPosition value

    // Direction 1 = counting up, -1 = counting down
    private static boolean movingDown = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.isPaused() || !isIsVisible()) return;

        tickAccumulator++;
        if (tickAccumulator < TICKS_PER_PRINT) return;
        tickAccumulator = 0;

        // Commented out display for brevity
        // Only change direction at endpoints
        if (arrowPosition >= 100) {
            movingDown = true;
        } else if (arrowPosition <= 1) {
            movingDown = false;
        }

        // Determine movement based on current speed and direction
        float delta = arrowSpeed * (movingDown ? -1 : 1);
        arrowPosition = Math.max(1, Math.min(arrowPosition + delta, 100));
    }

    public static void speedUp() {
        arrowSpeed = Math.min(
                arrowSpeed + ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue(),
                ServerConfig.MAX_ARROW_SPEED.get().floatValue()
        );
    }

    public static float getArrowPosition() {
        return arrowPosition;
    }

    public static boolean isIsVisible() {
        return isVisible;
    }

    public static void setIsVisible(BlockPos pos, boolean isVisible) {
        AnvilMinigameEvents.isVisible = isVisible;
        ModMessages.sendToServer(new SetMinigameVisibleC2SPacket(pos, isVisible));
    }


    public static void reset() {
        isVisible = false;
       /* if (anvilPos != null)
            ModMessages.sendToServer(new SetMinigameVisibleC2SPacket(anvilPos, false));*/
        minigameStarted = false;
        hitsRemaining = 0;
        perfectHits = 0;
        goodHits = 0;
        missedHits = 0;
        arrowPosition = 50;
        movingDown = false;
        perfectZoneStart = (100 - ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
        perfectZoneEnd = (100 + ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
        goodZoneStart = Math.max((100 - ServerConfig.ZONE_STARTING_SIZE.get() * 3) / 2, 1);
        goodZoneEnd = Math.min((100 + ServerConfig.ZONE_STARTING_SIZE.get() * 3) / 2, 100);
        arrowSpeed = ServerConfig.DEFAULT_ARROW_SPEED.get().floatValue();
        hitsRemaining = 0;
        randomizeCenter();
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
        } else if (arrowPosition >= goodZoneStart && arrowPosition <= goodZoneEnd) {
            goodHits++;
        } else {
            missedHits++;
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

    public static void shrinkAndShiftZones() {
        float perfectZoneSize = perfectZoneEnd - perfectZoneStart;
        float goodZoneSize = goodZoneEnd - goodZoneStart;

        perfectZoneSize *= zoneShrinkFactor;
        goodZoneSize *= zoneShrinkFactor;

        perfectZoneSize = Math.max(perfectZoneSize, ServerConfig.MIN_PERFECT_ZONE.get());
        goodZoneSize = Math.max(goodZoneSize, perfectZoneSize + 20);

        float originalCenter = (perfectZoneStart + perfectZoneEnd) / 2f;

        boolean shifted = false;
        int attempts = 0;

        int newPerfectStart = perfectZoneStart, newPerfectEnd = perfectZoneEnd;
        int newGoodStart = goodZoneStart, newGoodEnd = goodZoneEnd;

        while (!shifted && attempts < 5) {
            attempts++;
            float zoneCenter = getWeightedRandomCenter(originalCenter);

            int ps = (int) (zoneCenter - perfectZoneSize / 2);
            int pe = (int) (zoneCenter + perfectZoneSize / 2);
            int gs = (int) (zoneCenter - goodZoneSize / 2);
            int ge = (int) (zoneCenter + goodZoneSize / 2);

            ps = clamp(ps, 0, 100);
            pe = clamp(pe, 0, 100);
            gs = clamp(gs, 0, 100);
            ge = clamp(ge, 0, 100);

            if (ps != perfectZoneStart || pe != perfectZoneEnd) {
                newPerfectStart = ps;
                newPerfectEnd = pe;
                newGoodStart = gs;
                newGoodEnd = ge;
                shifted = true;
            }
        }

        // Apply the shifts—either a shifted version, or fallback to the last attempt
        perfectZoneStart = newPerfectStart;
        perfectZoneEnd = newPerfectEnd;
        goodZoneStart = newGoodStart;
        goodZoneEnd = newGoodEnd;
    }


    public static float getWeightedRandomCenter(float currentCenter) {
        // 70% chance for small shift, 20% medium, 10% large
        float rand = (float) Math.random();
        float shiftMagnitude;

        if (rand < 0.7) {
            shiftMagnitude = 0.5f; // Small shift
        } else if (rand < 0.9) {
            shiftMagnitude = 1.5f; // Medium shift
        } else {
            shiftMagnitude = 3.0f; // Large shift
        }

        // Apply shift in random direction
        float direction = Math.signum((float) Math.random() - 0.5f);
        return Math.max(20, Math.min(80,
                currentCenter + direction * zoneShiftAmount * shiftMagnitude));
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
            ModMessages.sendToServer(new SetMinigameVisibleC2SPacket(pos, false));
        }
        //clearAnvilPos(playerId);
    }
}

package net.stirdrem.overgeared.minigame;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.HitResult;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.custom.SmithingHammer;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;
import net.stirdrem.overgeared.networking.packet.MinigameHitResultC2SPacket;

public class AnvilMinigame {
    // Instance fields instead of static
    private boolean isVisible = ClientAnvilMinigameData.getIsVisible();
    private boolean minigameStarted = false;
    private ItemStack resultItem = ClientAnvilMinigameData.getResultItem();
    private int hitsRemaining = ClientAnvilMinigameData.getHitsRemaining();
    private float arrowPosition = ClientAnvilMinigameData.getArrowPosition();
    private float arrowSpeed = ServerConfig.DEFAULT_ARROW_SPEED.get().floatValue();
    private final float maxArrowSpeed = ServerConfig.MAX_SPEED.get().floatValue();
    private float speedIncreasePerHit = ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue();
    private boolean movingRight = ClientAnvilMinigameData.isMovingRight();
    private int perfectHits = ClientAnvilMinigameData.getPerfectHits();
    private int goodHits = ClientAnvilMinigameData.getGoodHits();
    private int missedHits = ClientAnvilMinigameData.getMissedHits();
    private final int PERFECT_ZONE_START = (100 - ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    private final int PERFECT_ZONE_END = (100 + ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    private final int GOOD_ZONE_START = PERFECT_ZONE_START - 10;
    private final int GOOD_ZONE_END = PERFECT_ZONE_END + 10;
    private int perfectZoneStart = ClientAnvilMinigameData.getPerfectZoneStart();
    private int perfectZoneEnd = ClientAnvilMinigameData.getPerfectZoneEnd();
    private int goodZoneStart = ClientAnvilMinigameData.getGoodZoneStart();
    private int goodZoneEnd = ClientAnvilMinigameData.getGoodZoneEnd();
    private final float zoneShrinkFactor = ServerConfig.ZONE_SHRINK_FACTOR.get().floatValue();
    private float zoneShiftAmount = 15.0f;
    private BlockPos anvilPos;
    private boolean isUnpaused = false;
    private HitResult result;
    private String quality;


    // Save to NBT
    public void saveNBTData(CompoundTag nbt) {
        nbt.putBoolean("isVisible", isVisible);
        nbt.putBoolean("minigameStarted", minigameStarted);
        nbt.putInt("hitsRemaining", hitsRemaining);
        nbt.putFloat("arrowPosition", arrowPosition);
        nbt.putFloat("arrowSpeed", arrowSpeed);
        nbt.putBoolean("movingRight", movingRight);
        nbt.putInt("perfectHits", perfectHits);
        nbt.putInt("goodHits", goodHits);
        nbt.putInt("missedHits", missedHits);
        nbt.putInt("perfectZoneStart", perfectZoneStart);
        nbt.putInt("perfectZoneEnd", perfectZoneEnd);
        nbt.putInt("goodZoneStart", goodZoneStart);
        nbt.putInt("goodZoneEnd", goodZoneEnd);
        nbt.putBoolean("isUnpaused", isUnpaused);

        if (anvilPos != null) {
            nbt.putInt("anvilX", anvilPos.getX());
            nbt.putInt("anvilY", anvilPos.getY());
            nbt.putInt("anvilZ", anvilPos.getZ());
        }

        if (resultItem != null) {
            CompoundTag itemTag = new CompoundTag();
            resultItem.save(itemTag);
            nbt.put("resultItem", itemTag);
        }

    }

    // Load from NBT
    public void loadNBTData(CompoundTag nbt) {
        isVisible = nbt.getBoolean("isVisible");
        minigameStarted = nbt.getBoolean("minigameStarted");
        hitsRemaining = nbt.getInt("hitsRemaining");
        arrowPosition = nbt.getFloat("arrowPosition");
        arrowSpeed = nbt.getFloat("arrowSpeed");
        movingRight = nbt.getBoolean("movingRight");
        perfectHits = nbt.getInt("perfectHits");
        goodHits = nbt.getInt("goodHits");
        missedHits = nbt.getInt("missedHits");
        perfectZoneStart = nbt.getInt("perfectZoneStart");
        perfectZoneEnd = nbt.getInt("perfectZoneEnd");
        goodZoneStart = nbt.getInt("goodZoneStart");
        goodZoneEnd = nbt.getInt("goodZoneEnd");
        isUnpaused = nbt.getBoolean("isUnpaused");

        if (nbt.contains("anvilX")) {
            anvilPos = new BlockPos(
                    nbt.getInt("anvilX"),
                    nbt.getInt("anvilY"),
                    nbt.getInt("anvilZ")
            );
        }

        if (nbt.contains("resultItem")) {
            resultItem = ItemStack.of(nbt.getCompound("resultItem"));
        }
    }

    public void start(ItemStack result, int requiredHits, BlockPos pos, ServerPlayer player) {
        if (minigameStarted) {
            isVisible = !isVisible;
            OvergearedMod.LOGGER.info("isVisible: " + isVisible);
            sendUpdatePacket(player);
            return;
        }

        if (result == null) return;

        anvilPos = pos;
        minigameStarted = true;
        isVisible = true;
        resultItem = result.copy();
        hitsRemaining = requiredHits;
        perfectHits = 0;
        goodHits = 0;
        missedHits = 0;
        arrowPosition = 0;
        double temp = ServerConfig.DEFAULT_ARROW_SPEED.get();
        arrowSpeed = (float) temp;
        temp = ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get();
        speedIncreasePerHit = (float) temp;
        movingRight = true;

        double random = Math.random() * 10;
        perfectZoneStart = Math.max(0, Math.min(100, (int) (PERFECT_ZONE_START + random)));
        perfectZoneEnd = Math.max(0, Math.min(100, (int) (PERFECT_ZONE_END + random)));
        goodZoneStart = Math.max(0, Math.min(100, (int) (GOOD_ZONE_START + random)));
        goodZoneEnd = Math.max(0, Math.min(100, (int) (GOOD_ZONE_END + random)));

        sendUpdatePacket(player);
        OvergearedMod.LOGGER.info("Minigame started");
    }

    public void tick() {
        if (!isVisible) return;

        if (movingRight) {
            arrowPosition += arrowSpeed;
            if (arrowPosition >= 100) {
                arrowPosition = 100;
                movingRight = false;
            }
        } else {
            arrowPosition -= arrowSpeed;
            if (arrowPosition <= 0) {
                arrowPosition = 0;
                movingRight = true;
            }
        }
    }

    public void clientHandleHit() {
        arrowSpeed = Math.min(arrowSpeed + ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue(),
                ServerConfig.MAX_SPEED.get().floatValue());

        // Client-side visual updates
        shrinkAndShiftZones();

        // Send hit result to server for validation
        sendHitResultToServer();
    }

    public String handleHit(ServerPlayer player) {
        arrowSpeed = Math.min(arrowSpeed + speedIncreasePerHit, maxArrowSpeed);

        if (arrowPosition >= perfectZoneStart && arrowPosition <= perfectZoneEnd) {
            perfectHits++;
        } else if (arrowPosition >= goodZoneStart && arrowPosition <= goodZoneEnd) {
            goodHits++;
        } else {
            missedHits++;
        }
        /*if (arrowPosition >= perfectZoneStart && arrowPosition <= perfectZoneEnd) {
            result = HitResult.PERFECT;
        } else if (arrowPosition >= goodZoneStart && arrowPosition <= goodZoneEnd) {
            result = HitResult.GOOD;
        } else {
            result = HitResult.MISSED;
            }*/
        shrinkAndShiftZones();
        hitsRemaining--;
        //sendUpdatePacket();
        // sendHitResultToServer();

        if (hitsRemaining <= 0) {
            return finishForging(player);
        }
        //syncRemainingHits(player);
        return null;
    }

    public void serverHandleHit(ServerPlayer player, HitResult result) {
        switch (result) {
            case PERFECT -> perfectHits++;
            case GOOD -> goodHits++;
            case MISSED -> missedHits++;
        }

        hitsRemaining--;

        if (hitsRemaining <= 0) {
            quality = finishForging(player);
            // Handle the forged item quality
        }
        // Optionally sync remaining hits back to client
        syncRemainingHits(player);
    }

    private void shrinkAndShiftZones() {
        float perfectZoneSize = perfectZoneEnd - perfectZoneStart;
        float goodZoneSize = goodZoneEnd - goodZoneStart;

        perfectZoneSize *= zoneShrinkFactor;
        goodZoneSize *= zoneShrinkFactor;

        perfectZoneSize = Math.max(perfectZoneSize, 5);
        goodZoneSize = Math.max(goodZoneSize, perfectZoneSize * 2);

        float random = (float) Math.random();
        float perfectZoneCenter = (perfectZoneStart + perfectZoneEnd) / 2f;
        perfectZoneCenter += (random - 0.5f) * zoneShiftAmount * 3;
        perfectZoneCenter = Math.max(20, Math.min(80, perfectZoneCenter));

        float goodZoneCenter = (goodZoneStart + goodZoneEnd) / 2f;
        goodZoneCenter += (random - 0.5f) * zoneShiftAmount * 3;
        goodZoneCenter = Math.max(20, Math.min(80, goodZoneCenter));

        perfectZoneStart = (int) (perfectZoneCenter - perfectZoneSize / 2);
        perfectZoneEnd = (int) (perfectZoneCenter + perfectZoneSize / 2);

        goodZoneStart = (int) (goodZoneCenter - goodZoneSize / 2);
        goodZoneEnd = (int) (goodZoneCenter + goodZoneSize / 2);

        perfectZoneStart = Math.max(0, perfectZoneStart);
        perfectZoneEnd = Math.min(100, perfectZoneEnd);
        goodZoneStart = Math.max(0, goodZoneStart);
        goodZoneEnd = Math.min(100, goodZoneEnd);
    }

    public String finishForging(ServerPlayer player) {
        isVisible = false;
        minigameStarted = false;
        SmithingHammer.releaseAnvil(anvilPos);
        float qualityScore = calculateQualityScore();
        sendUpdatePacket(player);
        return determineQuality(qualityScore);
    }

    private float calculateQualityScore() {
        int totalHits = perfectHits + goodHits + missedHits;
        if (totalHits == 0) return 0f;
        return (perfectHits * 1.0f + goodHits * 0.6f) / totalHits;
    }

    private String determineQuality(float qualityScore) {
        if (qualityScore > 0.9f) return "perfect";
        if (qualityScore > 0.75f) return "expert";
        if (qualityScore > 0.5f) return "well";
        return "poor";
    }

    /*public void end() {
        isVisible = false;
        minigameStarted = false;
        resultItem = null;
        hitsRemaining = 0;
        perfectHits = 0;
        goodHits = 0;
        missedHits = 0;
        arrowPosition = 0;
        arrowSpeed = 0;
        sendUpdatePacket(player);
    }*/

    private void sendHitResultToServer() {
        if (anvilPos == null) return;

        // Determine hit quality client-side
        HitResult result;
        if (arrowPosition >= perfectZoneStart && arrowPosition <= perfectZoneEnd) {
            result = HitResult.PERFECT;
        } else if (arrowPosition >= goodZoneStart && arrowPosition <= goodZoneEnd) {
            result = HitResult.GOOD;
        } else {
            result = HitResult.MISSED;
        }

        // Send to server
        ModMessages.sendToServer(new MinigameHitResultC2SPacket(
                anvilPos,
                result
        ));
    }

    // Server-side hit validation


    private void syncRemainingHits(ServerPlayer player) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("hitsRemaining", hitsRemaining);
        ModMessages.sendToPlayer(new MinigameSyncS2CPacket(nbt), player);
    }


    /* public void sendUpdatePacket() {
         ServerPlayer player = anvilPos != null ? SmithingHammer.getUsingPlayer(anvilPos) : null;
         if (player != null) {
             ModMessages.sendToPlayer(new MinigameSyncS2CPacket(
                     isVisible,
                     minigameStarted,
                     ClientAnvilMinigameData.getResultItem(),
                     ClientAnvilMinigameData.getHitsRemaining(),
                     ClientAnvilMinigameData.getArrowPosition(),
                     ClientAnvilMinigameData.getArrowSpeed(),
                     maxArrowSpeed,
                     ClientAnvilMinigameData.getSpeedIncreasePerHit(),
                     ClientAnvilMinigameData.isMovingRight(),
                     ClientAnvilMinigameData.getPerfectHits(),
                     ClientAnvilMinigameData.getGoodHits(),
                     ClientAnvilMinigameData.getMissedHits(),
                     ClientAnvilMinigameData.getPerfectZoneStart(),
                     ClientAnvilMinigameData.getPerfectZoneEnd(),
                     ClientAnvilMinigameData.getGoodZoneStart(),
                     ClientAnvilMinigameData.getGoodZoneEnd(),
                     zoneShrinkFactor,
                     zoneShiftAmount,
                     anvilPos
             ), player);
         }
     }*/
   /* private void sendUpdatePacket() {
        ServerPlayer player = anvilPos != null ? SmithingHammer.getUsingPlayer(anvilPos) : null;
        if (player != null) {
            CompoundTag nbt = new CompoundTag();
            this.saveNBTData(nbt);
            nbt.putUUID("playerId", player.getUUID()); // Include player ID
            ModMessages.sendToPlayer(new MinigameSyncS2CPacket(nbt), player);
        }
    }*/
    public void sendUpdatePacket(ServerPlayer player) {
        CompoundTag nbt = new CompoundTag();
        this.saveNBTData(nbt);
        nbt.putUUID("playerId", player.getUUID()); // Include player ID
        player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(capability -> {
            ModMessages.sendToPlayer(new MinigameSyncS2CPacket(nbt), player);
        });
    }

    private void setAnvilPos(BlockPos anvilPos) {
    }

    private void setResultItem(ItemStack copy) {
    }

    // Getters and Setters
    public boolean isVisible() {
        return isVisible;
    }

    public boolean isForging() {
        return minigameStarted;
    }

    public float getArrowPosition() {
        return arrowPosition;
    }

    public float getArrowSpeed() {
        return arrowSpeed;
    }

    public int getPerfectZoneStart() {
        return perfectZoneStart;
    }

    public int getPerfectZoneEnd() {
        return perfectZoneEnd;
    }

    public int getGoodZoneStart() {
        return goodZoneStart;
    }

    public int getGoodZoneEnd() {
        return goodZoneEnd;
    }

    public int getHitsRemaining() {
        return hitsRemaining;
    }

    public int getPerfectHits() {
        return perfectHits;
    }

    public int getGoodHits() {
        return goodHits;
    }

    public int getMissedHits() {
        return missedHits;
    }

    public boolean isUnpaused() {
        return isUnpaused;
    }

    public void setUnpaused(boolean unpaused) {
        isUnpaused = unpaused;
    }

    public void setIsVisible(boolean visible) {
        isVisible = visible;
    }

    public void setHitsRemaining(int hits) {
        hitsRemaining = hits;
    }

    public void setPerfectHits(int hits) {
        perfectHits = hits;
    }

    public void setGoodHits(int hits) {
        goodHits = hits;
    }

    public void setMissedHits(int hits) {
        missedHits = hits;
    }

    public void setArrowPosition(float position) {
        arrowPosition = position;
    }

    public void setArrowSpeed(float speed) {
        arrowSpeed = speed;
    }

    public void setPerfectZoneStart(int start) {
        perfectZoneStart = start;
    }

    public void setPerfectZoneEnd(int end) {
        perfectZoneEnd = end;
    }

    public void setGoodZoneStart(int start) {
        goodZoneStart = start;
    }

    public void setGoodZoneEnd(int end) {
        goodZoneEnd = end;
    }

    public void copyFrom(AnvilMinigame minigame) {
        this.isVisible = minigame.isVisible;
    }

    public boolean getMovingRight() {
        return movingRight;
    }

    public void setMovingRight(boolean b) {
        movingRight = b;
    }

    public boolean getVisible() {
        return isVisible;
    }

    public ItemStack getResultItem() {
        return resultItem;
    }

    public BlockPos getAnvilPos() {
        return anvilPos;
    }

    public void clientTick() {
        if (!isVisible || !minigameStarted) return;

        if (movingRight) {
            arrowPosition += arrowSpeed;
            if (arrowPosition >= 100) {
                arrowPosition = 100;
                movingRight = false;
            }
        } else {
            arrowPosition -= arrowSpeed;
            if (arrowPosition <= 0) {
                arrowPosition = 0;
                movingRight = true;
            }
        }
    }


    public String getQuality() {
        if (hitsRemaining <= 0) {
            return quality;
        }
        //syncRemainingHits(player);
        return null;
    }

    public BlockPos getAnvilPosition() {
        return anvilPos;
    }

    public boolean hasAnvilPosition() {
        return anvilPos != null;
    }

    public void reset(ServerPlayer player) {
        isVisible = false;
        minigameStarted = false;
        resultItem = null;
        hitsRemaining = 0;
        arrowPosition = 0;
        arrowSpeed = ServerConfig.DEFAULT_ARROW_SPEED.get().floatValue();
        movingRight = true;
        perfectHits = 0;
        goodHits = 0;
        missedHits = 0;
        perfectZoneStart = PERFECT_ZONE_START;
        perfectZoneEnd = PERFECT_ZONE_END;
        goodZoneStart = GOOD_ZONE_START;
        goodZoneEnd = GOOD_ZONE_END;
        isUnpaused = false;
        anvilPos = null;
        result = null;
        quality = null;
        sendUpdatePacket(player);
    }

}
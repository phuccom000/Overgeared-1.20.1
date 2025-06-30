package net.stirdrem.overgeared.block.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.stirdrem.overgeared.AnvilTier;

import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.custom.SmithingHammer;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.sound.ModSounds;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;
import org.joml.Random;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractSmithingAnvil extends BaseEntityBlock {

    protected static final int HAMMER_SOUND_DURATION_TICKS = 6; // adjust to match your sound

    protected static String quality = null;
    protected static AnvilTier tier;

    public AbstractSmithingAnvil(AnvilTier anvilTier, Properties properties) {
        super(properties);
        tier = anvilTier;
    }

    // In your SmithingAnvil class, ensure getQuality() never returns null:
    public static String getQuality() {
        // Return current quality or default if null
        return quality != null ? quality : "no_quality";
    }

    protected boolean minigameStarted = false;
    protected ItemStack resultItem;
    protected int hitsRemaining = 0;
    protected float arrowPosition = 0;
    protected float arrowSpeed = 1.0f;
    double temp2 = ServerConfig.MAX_SPEED.get();
    protected final float maxArrowSpeed = (float) temp2;
    protected float speedIncreasePerHit = 0.75f;
    protected boolean movingRight = true;
    protected int perfectHits = 0;
    protected int goodHits = 0;
    protected int missedHits = 0;
    protected final int PERFECT_ZONE_START = (100 - ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    protected final int PERFECT_ZONE_END = (100 + ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    protected final int GOOD_ZONE_START = PERFECT_ZONE_START - 10;
    protected final int GOOD_ZONE_END = PERFECT_ZONE_END + 10;
    protected int perfectZoneStart = PERFECT_ZONE_START;
    protected int perfectZoneEnd = PERFECT_ZONE_END;
    protected int goodZoneStart = GOOD_ZONE_START;
    protected int goodZoneEnd = GOOD_ZONE_END;
    double temp3 = ServerConfig.ZONE_SHRINK_FACTOR.get();
    protected final float zoneShrinkFactor = (float) temp3;
    protected float zoneShiftAmount = 15.0f;
    protected BlockPos anvilPos;
    protected boolean isUnpaused = false;

    @Override
    public abstract VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext);

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof AbstractSmithingAnvilBlockEntity) {
                ((AbstractSmithingAnvilBlockEntity) blockEntity).drops();
                resetMinigameData(pLevel, pPos);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        long now = level.getGameTime();
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AbstractSmithingAnvilBlockEntity anvil)) {
            return InteractionResult.PASS;
        }

        // Reject if still playing hammer sound
        if (anvil.isBusy(now)) {
            return InteractionResult.CONSUME;
        }

        ItemStack held = player.getItemInHand(hand);
        boolean isHammer = held.is(ModTags.Items.SMITHING_HAMMERS);  // Tag-based check

        AtomicBoolean isHit = new AtomicBoolean(false);

        player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
            if (isHammer && anvil.hasRecipe()) {
                if (minigame.getVisible() && pos.equals(minigame.getAnvilPos()) || !anvil.hasQuality() || !ServerConfig.ENABLE_MINIGAME.get()) {
                    // Hammer logic (particles, sound, cooldown)
                    if (!ServerConfig.ENABLE_MINIGAME.get())
                        anvil.setBusyUntil(now + HAMMER_SOUND_DURATION_TICKS);
/*            for (int i = 0; i < 3; i++) {
                int delay = 7 * i;
                TickScheduler.schedule(delay, () -> spawnAnvilParticles(level, pos));
            }*/
                    spawnAnvilParticles(level, pos);
                    //level.playSound(null, pos, SoundEvents.ANVIL_, SoundSource.BLOCKS, 1f, 1f);
                    if (anvil.getHitsRemaining() == 1)
                        level.playSound(null, pos, ModSounds.FORGING_COMPLETE.get(), SoundSource.BLOCKS, 1f, 1f);
                    else level.playSound(null, pos, ModSounds.ANVIL_HIT.get(), SoundSource.BLOCKS, 1f, 1f);
                    held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                    if (anvil.hasQuality() && ServerConfig.ENABLE_MINIGAME.get())
                        quality = minigame.handleHit((ServerPlayer) player);
                    anvil.increaseForgingProgress(level, pos, state);
                    held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                    isHit.set(true);
                } //else AnvilMinigameOverlay.endMinigame();
            }
            if (minigame.isForging() && !pos.equals(minigame.getAnvilPos()) && player instanceof ServerPlayer serverPlayer) {
                isHit.set(true);
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.another_anvil_in_use").withStyle(ChatFormatting.RED), true);
            }

            // Open GUI if not hammering
        });
        if (isHit.get()) return InteractionResult.SUCCESS;
        NetworkHooks.openScreen((ServerPlayer) player, anvil, pos);

        return InteractionResult.sidedSuccess(level.isClientSide());
    }


    protected void spawnAnvilParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {

            Random random = new Random();
            for (int i = 0; i < 6; i++) {
                double offsetX = 0.5 + (random.nextFloat() - 0.5);
                double offsetY = 1.0 + random.nextFloat() * 0.5;
                double offsetZ = 0.5 + (random.nextFloat() - 0.5);
                double velocityX = (random.nextFloat() - 0.5) * 0.1;
                double velocityY = random.nextFloat() * 0.1;
                double velocityZ = (random.nextFloat() - 0.5) * 0.1;

                // For orange-colored dust particles
                /*serverLevel.sendParticles(ParticleTypes.FLAME,
                        pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ,
                        velocityX, velocityY, velocityZ);*/
                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(1.0f, 0.5f, 0.0f), 1.0f),
                        pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 1,
                        velocityX, velocityY, velocityZ, 1);
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, 1,
                        velocityX, velocityY, velocityZ, 1);
            }
        }
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pPos, BlockState pState);

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        resetMinigameData(level, pos);
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        resetMinigameData(level, pos);
        super.onBlockExploded(state, level, pos, explosion);
    }

    protected void resetMinigameData(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            ServerPlayer usingPlayer = SmithingHammer.getUsingPlayer(pos);
            if (usingPlayer != null) {
                // Reset server-side data
                usingPlayer.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                    //minigame.resetNBTData();
                    minigame.reset(usingPlayer); // Implement this in your capability
                    //minigame.setIsVisible(false, usingPlayer);

                    /*// Notify client to reset
                    CompoundTag resetTag = new CompoundTag();
                    resetTag.putBoolean("isVisible", false);
                    ModMessages.sendToPlayer(new MinigameSyncS2CPacket(resetTag), usingPlayer);*/
                });
            }
        }
    }

    public static String getTier() {
        return tier.getDisplayName();
    }

}
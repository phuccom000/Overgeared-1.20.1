package net.stirdrem.overgeared.block.custom;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.PacketSendCounterC2SPacket;
import net.stirdrem.overgeared.networking.packet.ResetMinigameS2CPacket;
import net.stirdrem.overgeared.sound.ModSounds;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;
import org.joml.Random;
import org.joml.Vector3f;

import java.util.UUID;

public abstract class AbstractSmithingAnvilNew extends BaseEntityBlock implements Fallable {

    protected static final int HAMMER_SOUND_DURATION_TICKS = 6; // adjust to match your sound

    protected static String quality = null;
    protected static AnvilTier tier;

    public AbstractSmithingAnvilNew(AnvilTier anvilTier, Properties properties) {
        super(properties);
        tier = anvilTier;
    }

    // In your SmithingAnvil class, ensure getQuality() never returns null:
    public static String getQuality() {
        // Return current quality or default if null
        return quality != null ? quality : "no_quality";
    }

    public static void setQuality(String quality) {
        AbstractSmithingAnvilNew.quality = quality;
    }

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

                // Fix: Use server-side reset method for the specific player
                if (!pLevel.isClientSide()) {
                    ModEvents.resetMinigameForAnvil(pLevel, pPos);
                }
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        boolean isHammer = held.is(ModTags.Items.SMITHING_HAMMERS);  // Tag-based check
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AbstractSmithingAnvilBlockEntity anvil)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            if (player.isCrouching()) return InteractionResult.SUCCESS;
            if (anvil.hasRecipe() && isHammer) {
                if (!pos.equals(AnvilMinigameEvents.getAnvilPos(player.getUUID()))) {
                    //player.sendSystemMessage(Component.translatable("message.overgeared.another_anvil_in_use").withStyle(ChatFormatting.RED));
                    return InteractionResult.SUCCESS;
                }
                if (!AnvilMinigameEvents.isIsVisible()) return InteractionResult.SUCCESS;
                // Read the current counter at the moment of right-click:
                String quality = AnvilMinigameEvents.handleHit();
                ModMessages.sendToServer(new PacketSendCounterC2SPacket(pos, quality));
                AnvilMinigameEvents.speedUp();

                return InteractionResult.SUCCESS;
            } else
                AnvilMinigameEvents.setIsVisible(pos, false);
            return InteractionResult.SUCCESS;
        }

        long now = level.getGameTime();

        // Reject if still playing hammer sound
       /* if (anvil.isBusy(now)) {
            return InteractionResult.CONSUME;
        }*/

        if (anvil.hasRecipe() && isHammer) {
            UUID currentOwner = anvil.getOwnerUUID();
            if (currentOwner != null && !currentOwner.equals(player.getUUID()) && player instanceof ServerPlayer serverPlayer) {
                // Get the player entity from the UUID
                Player ownerPlayer = level.getPlayerByUUID(currentOwner);
                String ownerName;

                if (ownerPlayer != null) {
                    // Player is online - use their display name
                    ownerName = ownerPlayer.getDisplayName().getString();
                } else {
                    // Player is offline - try to get username from server
                    GameProfile ownerProfile = level.getServer().getProfileCache().get(currentOwner).orElse(null);
                    if (ownerProfile != null) {
                        ownerName = ownerProfile.getName();
                    } else {
                        // Fallback if we can't get the name
                        ownerName = "Another player";
                    }
                }

                serverPlayer.sendSystemMessage(
                        Component.translatable("message.overgeared.anvil_in_use_by_another", ownerName)
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResult.FAIL;
            }

            if (anvil.isMinigameOn() || !anvil.hasQuality() && !anvil.needsMinigame() || !ServerConfig.ENABLE_MINIGAME.get()) {
                BlockPos pos1 = ModItemInteractEvents.playerAnvilPositions.get(player.getUUID());
                if (!pos.equals(ModItemInteractEvents.playerAnvilPositions.get(player.getUUID()))) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.another_anvil_in_use").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }
                if (!ServerConfig.ENABLE_MINIGAME.get())
                    anvil.setBusyUntil(now + HAMMER_SOUND_DURATION_TICKS);
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                anvil.increaseForgingProgress(level, pos, state);
                spawnAnvilParticles(level, pos);
                if (anvil.getHitsRemaining() == 1) {
                    if (anvil.isFailedResult()) {
                        level.playSound(null, pos, ModSounds.FORGING_FAILED.get(), SoundSource.BLOCKS, 1f, 1f);
                    } else
                        level.playSound(null, pos, ModSounds.FORGING_COMPLETE.get(), SoundSource.BLOCKS, 1f, 1f);
                } else level.playSound(null, pos, ModSounds.ANVIL_HIT.get(), SoundSource.BLOCKS, 1f, 1f);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            NetworkHooks.openScreen((ServerPlayer) player, anvil, pos);
        } else {
            ModItemInteractEvents.releaseAnvil((ServerPlayer) player, pos);
            NetworkHooks.openScreen((ServerPlayer) player, anvil, pos);
        }

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
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // For player destruction, we can still use the targeted approach
            ModEvents.resetMinigameForPlayer(serverPlayer, pos);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide()) {
            ModEvents.resetMinigameForAnvil(level, pos);
        }
        super.onBlockExploded(state, level, pos, explosion);
    }

    protected void resetMinigameData(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                UUID playerId = player.getUUID();
                if (ModItemInteractEvents.playerAnvilPositions.getOrDefault(playerId, BlockPos.ZERO).equals(pos)) {
                    // Send reset packet only to this specific player
                    ModMessages.sendToPlayer(new ResetMinigameS2CPacket(pos), player);

                    // Clear server-side tracking for this player
                    ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                    ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
                    break; // Only reset the first player found (should only be one)
                }
            }
        }
    }

    public static String getTier() {
        return tier.getDisplayName();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        level.scheduleTick(pos, this, 2); // Schedule an immediate fall check
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource pRandom) {
        if (!level.isClientSide) {
            BlockPos below = pos.below();
            BlockState stateBelow = level.getBlockState(below);
            if (FallingBlock.isFree(stateBelow)) {
                // Convert the block into a falling block entity
                FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
                customizeFallingEntity(falling, level);
            }
        }
    }

    protected void customizeFallingEntity(FallingBlockEntity entity, Level level) {
        // Optional: prevent it from breaking on landing
        entity.setHurtsEntities(2.0F, 40);

        entity.dropItem = true; // drop as item on breaking
        // entity.time = 1; // fall delay
        // entity.setHurtsEntities(0.0F, 0); // disable damage
    }

    @Override
    public void onLand(Level pLevel, BlockPos pPos, BlockState pState, BlockState pReplaceableState, FallingBlockEntity pFallingBlock) {
        if (!pFallingBlock.isSilent()) {
            pLevel.levelEvent(1031, pPos, 0);
        }
    }

    @Override
    public DamageSource getFallDamageSource(Entity pEntity) {
        return pEntity.damageSources().anvil(pEntity);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        pLevel.scheduleTick(pPos, this, 2);
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }


}
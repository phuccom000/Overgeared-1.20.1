package net.stirdrem.overgeared.block.custom;

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
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.networking.packet.PacketSendCounterC2SPacket;
import net.stirdrem.overgeared.sound.ModSounds;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;
import org.joml.Random;
import org.joml.Vector3f;

import java.util.UUID;

public abstract class AbstractSmithingAnvil extends BaseEntityBlock implements Fallable {

    protected static final int HAMMER_SOUND_DURATION_TICKS = 6; // adjust to match your sound

    protected static String quality = null;
    protected static AnvilTier tier;

    public AbstractSmithingAnvil(AnvilTier anvilTier, Properties properties) {
        super(properties);
        tier = anvilTier;
    }

    public String getQuality() {
        // Return current quality or default if null
        return quality != null ? quality : "none";
    }

    public static void setQuality(String quality) {
        AbstractSmithingAnvil.quality = quality;
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

                 if (!pLevel.isClientSide()) {
                     ModEvents.resetMinigameForAnvil(pLevel, pPos);
                 }
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AbstractSmithingAnvilBlockEntity anvil)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Check ownership
        if (anvil.hasRecipe()) {
            UUID currentOwner = anvil.getOwnerUUID();
            if (currentOwner != null && !currentOwner.equals(player.getUUID()) && player instanceof ServerPlayer serverPlayer) {
                Player ownerPlayer = level.getPlayerByUUID(currentOwner);
                String ownerName = ownerPlayer != null ? ownerPlayer.getDisplayName().getString() : "Another player";
                
                serverPlayer.sendSystemMessage(
                        Component.translatable("message.overgeared.anvil_in_use_by_another", ownerName)
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return InteractionResult.FAIL;
            }
        }

        player.openMenu(anvil, pos);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hit) {
        boolean isHammer = held.is(ModTags.Items.SMITHING_HAMMERS);
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AbstractSmithingAnvilBlockEntity anvil)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // CLIENT-SIDE: Handle minigame hit processing
        if (level.isClientSide()) {
            if (player.isCrouching()) {
                return ItemInteractionResult.SUCCESS; // Let server handle opening menu
            }
            
            if (anvil.hasRecipe() && isHammer) {
                // Check if this is our anvil
                BlockPos ourAnvilPos = AnvilMinigameEvents.getAnvilPos(player.getUUID());
                if (ourAnvilPos != null && !pos.equals(ourAnvilPos)) {
                    // Not our anvil - don't process hit
                    return ItemInteractionResult.SUCCESS;
                }
                
                // Check if minigame is visible (quality recipe with minigame enabled)
                if (AnvilMinigameEvents.isVisible()) {
                    // Process the hit client-side to update minigame state
                    AnvilMinigameEvents.resetPopUps();
                    String hitQuality = AnvilMinigameEvents.handleHit();
                    // Send the quality result to server
                    PacketDistributor.sendToServer(new PacketSendCounterC2SPacket(hitQuality, pos));
                    return ItemInteractionResult.SUCCESS;
                } else if (!anvil.hasQuality() && !anvil.needsMinigame()) {
                    // Non-quality recipe without minigame - allow direct hammering
                    return ItemInteractionResult.SUCCESS;
                } else if (!ServerConfig.ENABLE_MINIGAME.get()) {
                    // Minigame disabled - allow direct hammering
                    return ItemInteractionResult.SUCCESS;
                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        // SERVER-SIDE handling
        // Check ownership
        if (anvil.hasRecipe()) {
            UUID currentOwner = anvil.getOwnerUUID();
            if (currentOwner != null && !currentOwner.equals(player.getUUID()) && player instanceof ServerPlayer serverPlayer) {
                Player ownerPlayer = level.getPlayerByUUID(currentOwner);
                String ownerName = ownerPlayer != null ? ownerPlayer.getDisplayName().getString() : "Another player";
                
                serverPlayer.sendSystemMessage(
                        Component.translatable("message.overgeared.anvil_in_use_by_another", ownerName)
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return ItemInteractionResult.FAIL;
            }

            // Allow hammering for non-quality recipes OR when minigame is disabled
            if (isHammer && (anvil.isMinigameOn() || (!anvil.hasQuality() && !anvil.needsMinigame()) || !ServerConfig.ENABLE_MINIGAME.get())) {
                // Check if player is at the correct anvil
                BlockPos playerAnvilPos = ModItemInteractEvents.playerAnvilPositions.get(player.getUUID());
                if (playerAnvilPos != null && !pos.equals(playerAnvilPos)) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    serverPlayer.sendSystemMessage(
                            Component.translatable("message.overgeared.another_anvil_in_use")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    return ItemInteractionResult.FAIL;
                }
                
                // Check minigame visibility from server tracking
                Boolean visible = ModItemInteractEvents.playerMinigameVisibility.get(player.getUUID());
                if (visible == null && anvil.isMinigameOn()) {
                    // Player hasn't started minigame yet, open menu instead
                    ModItemInteractEvents.hideMinigame((ServerPlayer) player);
                    player.openMenu(anvil, pos);
                    return ItemInteractionResult.sidedSuccess(level.isClientSide());
                }
                
                // Process the hammer hit
                held.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                anvil.increaseForgingProgress(level, pos, state);
                spawnAnvilParticles(level, pos);
                
                // Play appropriate sound based on hits remaining
                if (anvil.getHitsRemaining() == 1) {
                    if (anvil.isFailedResult()) {
                        level.playSound(null, pos, ModSounds.FORGING_FAILED.get(), SoundSource.BLOCKS, 1f, 1f);
                    } else {
                        level.playSound(null, pos, ModSounds.FORGING_COMPLETE.get(), SoundSource.BLOCKS, 1f, 1f);
                    }
                } else {
                    level.playSound(null, pos, ModSounds.ANVIL_HIT.get(), SoundSource.BLOCKS, 1f, 1f);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
            
            // Has recipe but can't hammer - open menu
            ModItemInteractEvents.hideMinigame((ServerPlayer) player);
        } else {
            // No recipe - release anvil ownership
            ModItemInteractEvents.releaseAnvil((ServerPlayer) player, pos);
        }

        player.openMenu(anvil, pos);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
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
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
         if (!level.isClientSide()) {
             ModEvents.resetMinigameForAnvil(level, pos);
         }
        super.onBlockExploded(state, level, pos, explosion);
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
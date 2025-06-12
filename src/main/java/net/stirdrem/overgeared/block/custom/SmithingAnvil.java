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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.custom.SmithingHammer;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.sound.ModSounds;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;
import org.joml.Random;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicBoolean;

public class SmithingAnvil extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape Z1 = Block.box(3, 9, 0, 13, 16, 16);
    private static final VoxelShape Z2 = Block.box(3, 0, 1, 13, 3, 15);
    private static final VoxelShape Z3 = Block.box(4, 0, 4, 12, 3, 12);
    private static final VoxelShape Z4 = Block.box(5, 3, 3, 11, 4, 13);
    private static final VoxelShape Z5 = Block.box(6, 4, 4, 10, 9, 12);
    private static final VoxelShape X1 = Block.box(0, 9, 3, 16, 16, 13);
    private static final VoxelShape X2 = Block.box(1, 0, 3, 15, 3, 13);
    private static final VoxelShape X3 = Block.box(4, 0, 4, 12, 3, 12);
    private static final VoxelShape X4 = Block.box(3, 3, 5, 13, 4, 11);
    private static final VoxelShape X5 = Block.box(4, 4, 6, 12, 9, 10);

    // X-axis oriented shape
    private static final VoxelShape X_AXIS_AABB = Shapes.or(X1, X2, X3, X4, X5);

    // Z-axis oriented shape
    private static final VoxelShape Z_AXIS_AABB = Shapes.or(Z1, Z2, Z3, Z4, Z5);

    private static final int HAMMER_SOUND_DURATION_TICKS = 0; // adjust to match your sound

    private static String quality = null;

    public SmithingAnvil(Properties properties) {
        super(properties);
    }

    // In your SmithingAnvil class, ensure getQuality() never returns null:
    public static String getQuality() {
        // Return current quality or default if null
        return quality != null ? quality : "no_quality";
    }

    private boolean minigameStarted = false;
    private ItemStack resultItem;
    private int hitsRemaining = 0;
    private float arrowPosition = 0;
    private float arrowSpeed = 1.0f;
    double temp2 = ServerConfig.MAX_SPEED.get();
    private final float maxArrowSpeed = (float) temp2;
    private float speedIncreasePerHit = 0.75f;
    private boolean movingRight = true;
    private int perfectHits = 0;
    private int goodHits = 0;
    private int missedHits = 0;
    private final int PERFECT_ZONE_START = (100 - ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    private final int PERFECT_ZONE_END = (100 + ServerConfig.ZONE_STARTING_SIZE.get()) / 2;
    private final int GOOD_ZONE_START = PERFECT_ZONE_START - 10;
    private final int GOOD_ZONE_END = PERFECT_ZONE_END + 10;
    private int perfectZoneStart = PERFECT_ZONE_START;
    private int perfectZoneEnd = PERFECT_ZONE_END;
    private int goodZoneStart = GOOD_ZONE_START;
    private int goodZoneEnd = GOOD_ZONE_END;
    double temp3 = ServerConfig.ZONE_SHRINK_FACTOR.get();
    private final float zoneShrinkFactor = (float) temp3;
    private float zoneShiftAmount = 15.0f;
    private BlockPos anvilPos;
    private boolean isUnpaused = false;

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
    /* FACING */

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getClockWise());
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof SmithingAnvilBlockEntity) {
                ((SmithingAnvilBlockEntity) blockEntity).drops();
                resetMinigameData(pLevel, pPos);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            ItemStack heldItem = player.getItemInHand(hand);
            Item item = heldItem.getItem();
            // Check if the held item is the smithing hammer
            if (heldItem.getItem() == ModItems.SMITHING_HAMMER.get()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof SmithingAnvilBlockEntity anvilEntity) {
                    if (anvilEntity.hasRecipe()) {
                        // Increase crafting progress
                        anvilEntity.increaseCraftingProgressIfValid();

                        // Play hammering sound
                        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

                        // Optionally, damage the hammer
                        heldItem.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                        player.getCooldowns().addCooldown(item, 300);
                        // Trigger the swing animation three times
                        for (int i = 0; i < 3; i++) {
                            player.startUsingItem(hand);
                        }

                        return InteractionResult.SUCCESS;
                    } else {
                        // Open GUI if not holding the smithing hammer
                        BlockEntity entity = level.getBlockEntity(pos);
                        if (entity instanceof SmithingAnvilBlockEntity) {
                            NetworkHooks.openScreen((ServerPlayer) player, (SmithingAnvilBlockEntity) entity, pos);
                        } else {
                            throw new IllegalStateException("Our Container provider is missing!");
                        }
                    }
                }
            } else {
                // Open GUI if not holding the smithing hammer
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof SmithingAnvilBlockEntity) {
                    NetworkHooks.openScreen((ServerPlayer) player, (SmithingAnvilBlockEntity) entity, pos);
                } else {
                    throw new IllegalStateException("Our Container provider is missing!");
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }*/

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        long now = level.getGameTime();
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SmithingAnvilBlockEntity anvil)) {
            return InteractionResult.PASS;
        }

        // Reject if still playing hammer sound
        if (anvil.isBusy(now)) {
            return InteractionResult.CONSUME;
        }

        ItemStack held = player.getItemInHand(hand);
        boolean isHammer = held.is(ModTags.Items.SMITHING_HAMMERS);  // Tag-based check
       /* OvergearedMod.LOGGER.info("== SmithingAnvil Debug ==");
        OvergearedMod.LOGGER.info("isHammer: " + isHammer);
        OvergearedMod.LOGGER.info("hasRecipe: " + anvil.hasRecipe());
        OvergearedMod.LOGGER.info("AnvilMinigame.isVisible (CLIENT-ONLY!): " + AnvilMinigame.isVisible());
        OvergearedMod.LOGGER.info("AnvilMinigame.isUnpaused: " + AnvilMinigame.isUnpaused());
        OvergearedMod.LOGGER.info("== END DEBUG ==");*/

        AtomicBoolean isHit = new AtomicBoolean(false);

        player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
            boolean test = anvil.hasQuality();
            boolean recipe = anvil.hasRecipe();
            boolean poshas = pos.equals(minigame.getAnvilPos());
            if (isHammer && anvil.hasRecipe()) {
                if (minigame.getVisible() && pos.equals(minigame.getAnvilPos()) || !anvil.hasQuality()) {
                    // Hammer logic (particles, sound, cooldown)
                    //anvil.setBusyUntil(now + HAMMER_SOUND_DURATION_TICKS);
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
                    //player.getCooldowns().addCooldown(held.getItem(), HAMMER_SOUND_DURATION_TICKS);
                    //if (player instanceof ServerPlayer serverPlayer) {
                    //ModMessages.sendToServer(new FinalizeForgingC2SPacket("test"));
                    //player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                    //minigame.clientHandleHit();
                    //quality = minigame.getQuality();
                    if (anvil.hasQuality()) quality = minigame.handleHit((ServerPlayer) player);
                    // quality = AnvilMinigame.handleHit(serverPlayer);
                    //}
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


    private void spawnAnvilParticles(Level level, BlockPos pos) {
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
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SmithingAnvilBlockEntity(pPos, pState);
    }

    /*@Nullable
    @Override
    public <T extends
            BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.SMITHING_TABLE_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }
    */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (!pLevel.isClientSide && pBlockEntityType == ModBlockEntities.SMITHING_ANVIL_BE.get()) {
            return createTickerHelper(pBlockEntityType, ModBlockEntities.SMITHING_ANVIL_BE.get(),
                    (pLevel1, pPos, pState1, pBlockEntity) ->
                            pBlockEntity.tick(pLevel, pPos, pState1));
        }
        return null;
    }

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

    private void resetMinigameData(Level level, BlockPos pos) {
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

}
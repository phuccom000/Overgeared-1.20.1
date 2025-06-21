package net.stirdrem.overgeared.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.TickScheduler;
import org.jetbrains.annotations.Nullable;
import org.joml.Random;
import org.joml.Vector3f;

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

    private static final int HAMMER_SOUND_DURATION_TICKS = 0;
    public static final BooleanProperty FORGING = BooleanProperty.create("forging");

    public SmithingAnvil(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORGING, false));
    }

    // Update createBlockStateDefinition
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORGING);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

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
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof SmithingAnvilBlockEntity) {
                ((SmithingAnvilBlockEntity) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            // Client-side handling
            ItemStack held = player.getItemInHand(hand);
            if (held.is(ModTags.Items.SMITHING_HAMMERS) && AnvilMinigameOverlay.isVisible()) {
                AnvilMinigameOverlay.handleHit();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }

        // Server-side handling
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SmithingAnvilBlockEntity anvil)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        boolean isHammer = held.is(ModTags.Items.SMITHING_HAMMERS);

        if (isHammer && anvil.hasRecipe()) {
            // Start minigame if not already started
            /*if (!AnvilMinigameOverlay.minigameStarted) {
                //ModMessages.sendToServer(new StartMinigameS2CPacket(pos));
            }*/

            // Hammer effects
            for (int i = 0; i < 3; i++) {
                int delay = 7 * i;
                TickScheduler.schedule(delay, () -> spawnAnvilParticles(level, pos));
            }

            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1f, 1f);
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            return InteractionResult.SUCCESS;
        }

        // Open GUI if not hammering
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (!pLevel.isClientSide && pBlockEntityType == ModBlockEntities.SMITHING_ANVIL_BE.get()) {
            return createTickerHelper(pBlockEntityType, ModBlockEntities.SMITHING_ANVIL_BE.get(),
                    (pLevel1, pPos, pState1, pBlockEntity) ->
                            pBlockEntity.tick(pLevel1, pPos, pState1));
        }
        return null;
    }
}
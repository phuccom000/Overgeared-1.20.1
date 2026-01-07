package net.stirdrem.overgeared.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;
import org.jetbrains.annotations.Nullable;

public class SteelSmithingAnvil extends AbstractSmithingAnvil {
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

    private static final VoxelShape X_AXIS_AABB = Shapes.or(X1, X2, X3, X4, X5);
    private static final VoxelShape Z_AXIS_AABB = Shapes.or(Z1, Z2, Z3, Z4, Z5);

    public SteelSmithingAnvil(AnvilTier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SteelSmithingAnvilBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (!pLevel.isClientSide && pBlockEntityType == ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get()) {
            return createTickerHelper(pBlockEntityType, ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get(),
                    (level, pos, state, blockEntity) -> blockEntity.tick(level, pos, state));
        }
        return null;
    }
}

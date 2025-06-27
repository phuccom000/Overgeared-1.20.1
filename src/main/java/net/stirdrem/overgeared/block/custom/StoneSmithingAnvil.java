package net.stirdrem.overgeared.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.stirdrem.overgeared.block.AnvilTier;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.StoneSmithingAnvilBlockEntity;
import org.jetbrains.annotations.Nullable;

public class StoneSmithingAnvil extends AbstractSmithingAnvilBlock {
    public StoneSmithingAnvil(Properties properties) {
        super(properties, AnvilTier.STONE);
    }

    public static final VoxelShape SHAPE_X = Shapes.or(
            Block.box(0, 9, 3, 16, 16, 13),
            Block.box(1, 0, 3, 15, 3, 13),
            Block.box(4, 0, 4, 12, 3, 12),
            Block.box(3, 3, 5, 13, 4, 11),
            Block.box(4, 4, 6, 12, 9, 10)
    );

    public static final VoxelShape SHAPE_Z = Shapes.or(
            Block.box(3, 9, 0, 13, 16, 16),
            Block.box(3, 0, 1, 13, 3, 15),
            Block.box(4, 0, 4, 12, 3, 12),
            Block.box(5, 3, 3, 11, 4, 13),
            Block.box(6, 4, 4, 10, 9, 12)
    );

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(FACING).getAxis() == net.minecraft.core.Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StoneSmithingAnvilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide && type == ModBlockEntities.STONE_SMITHING_ANVIL_BE.get()) {
            return createTickerHelper(type, ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(),
                    (lvl, pos, st, be) -> be.tick(lvl, pos, st));
        }
        return null;
    }
}

package net.stirdrem.overgearedmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaterBarrel extends Block {
    private static final VoxelShape p1 = Block.box(0, 0, 0, 16, 2, 16);
    private static final VoxelShape p2 = Block.box(0, 0, 0, 2, 10, 16);
    private static final VoxelShape p3 = Block.box(14, 0, 0, 16, 10, 16);
    private static final VoxelShape p4 = Block.box(2, 0, 0, 14, 10, 2);
    private static final VoxelShape p5 = Block.box(2, 0, 14, 14, 10, 16);

    private static final VoxelShape BARREL = Shapes.or(p1, p2, p3, p4, p5);

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return BARREL;
    }

    public WaterBarrel(Properties properties) {
        super(properties);
    }
}

package net.stirdrem.overgeared.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.stirdrem.overgeared.core.waterbarrel.BarrelInteraction;

import java.util.Map;

public abstract class AbstractWaterBarrel extends Block {
    private static final VoxelShape p1 = Block.box(0, 0, 0, 16, 2, 16);
    private static final VoxelShape p2 = Block.box(0, 0, 0, 2, 10, 16);
    private static final VoxelShape p3 = Block.box(14, 0, 0, 16, 10, 16);
    private static final VoxelShape p4 = Block.box(2, 0, 0, 14, 10, 2);
    private static final VoxelShape p5 = Block.box(2, 0, 14, 14, 10, 16);
    private static final VoxelShape INSIDE = Block.box(2, 0, 2, 14, 8, 14);
    private static final VoxelShape BARREL = Shapes.or(p1, p2, p3, p4, p5);
    private final Map<Item, BarrelInteraction> interactions;

    public AbstractWaterBarrel(BlockBehaviour.Properties pProperties, Map<Item, BarrelInteraction> pInteractions) {
        super(pProperties);
        this.interactions = pInteractions;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return BARREL;
    }

    protected double getContentHeight(BlockState pState) {
        return 0.0D;
    }

    protected boolean isEntityInsideContent(BlockState pState, BlockPos pPos, Entity pEntity) {
        return pEntity.getY() < (double) pPos.getY() + this.getContentHeight(pState) && pEntity.getBoundingBox().maxY > (double) pPos.getY() + 0.25D;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        BarrelInteraction BarrelInteraction = this.interactions.get(itemstack.getItem());
        return BarrelInteraction.interact(pState, pLevel, pPos, pPlayer, pHand, itemstack);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return INSIDE;
    }

    /**
     * @deprecated call via {@link
     * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#hasAnalogOutputSignal} whenever possible.
     * Implementing/overriding is fine.
     */
    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    public abstract boolean isFull(BlockState pState);

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        BlockPos blockpos = PointedDripstoneBlock.findStalactiteTipAboveCauldron(pLevel, pPos);
        if (blockpos != null) {
            Fluid fluid = PointedDripstoneBlock.getCauldronFillFluidType(pLevel, blockpos);
            if (fluid != Fluids.EMPTY && this.canReceiveStalactiteDrip(fluid)) {
                this.receiveStalactiteDrip(pState, pLevel, pPos, fluid);
            }

        }
    }

    protected boolean canReceiveStalactiteDrip(Fluid pFluid) {
        return false;
    }

    protected void receiveStalactiteDrip(BlockState pState, Level pLevel, BlockPos pPos, Fluid pFluid) {
    }
}

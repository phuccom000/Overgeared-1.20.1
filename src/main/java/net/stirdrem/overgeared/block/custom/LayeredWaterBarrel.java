package net.stirdrem.overgeared.block.custom;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.core.waterbarrel.BarrelInteraction;

import java.util.Map;
import java.util.function.Predicate;

public class LayeredWaterBarrel extends AbstractWaterBarrel {
    public static final int MIN_FILL_LEVEL = 1;
    public static final int MAX_FILL_LEVEL = 2;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", MIN_FILL_LEVEL, MAX_FILL_LEVEL);
    private static final int BASE_CONTENT_HEIGHT = 6;
    private static final double HEIGHT_PER_LEVEL = 3.0D;
    public static final Predicate<Biome.Precipitation> RAIN = (precipitation) -> {
        return precipitation == Biome.Precipitation.RAIN;
    };
    public static final Predicate<Biome.Precipitation> SNOW = (precipitation) -> {
        return precipitation == Biome.Precipitation.SNOW;
    };
    private final Predicate<Biome.Precipitation> fillPredicate;

    public LayeredWaterBarrel(BlockBehaviour.Properties pProperties, Predicate<Biome.Precipitation> pFillPredicate, Map<Item, BarrelInteraction> pInteractions) {
        super(pProperties, pInteractions);
        this.fillPredicate = pFillPredicate;
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(MIN_FILL_LEVEL)));
        ;
    }

    @Override
    public boolean isFull(BlockState pState) {
        return pState.getValue(LEVEL) == MAX_FILL_LEVEL;
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid pFluid) {
        return pFluid == Fluids.WATER && this.fillPredicate == RAIN;
    }

    @Override

    protected double getContentHeight(BlockState pState) {
        return (BASE_CONTENT_HEIGHT + (double) pState.getValue(LEVEL).intValue() * HEIGHT_PER_LEVEL) / 16.0D;
    }

    @Override

    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (!pLevel.isClientSide && pEntity.isOnFire() && this.isEntityInsideContent(pState, pPos, pEntity)) {
            pEntity.clearFire();
            if (pEntity.mayInteract(pLevel, pPos)) {
                this.handleEntityOnFireInside(pState, pLevel, pPos);
            }
        }

    }

    protected void handleEntityOnFireInside(BlockState pState, Level pLevel, BlockPos pPos) {
        lowerFillLevel(pState, pLevel, pPos);
    }

    public static void lowerFillLevel(BlockState pState, Level pLevel, BlockPos pPos) {
        int i = pState.getValue(LEVEL) - 1;
        BlockState blockstate = i == 0 ? ModBlocks.WATER_BARREL.get().defaultBlockState() : pState.setValue(LEVEL, Integer.valueOf(i));
        pLevel.setBlockAndUpdate(pPos, blockstate);
        pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(blockstate));
    }

    @Override

    public void handlePrecipitation(BlockState pState, Level pLevel, BlockPos pPos, Biome.Precipitation pPrecipitation) {
        if (WaterBarrel.shouldHandlePrecipitation(pLevel, pPrecipitation) && pState.getValue(LEVEL) != MAX_FILL_LEVEL && this.fillPredicate.test(pPrecipitation)) {
            BlockState blockstate = pState.cycle(LEVEL);
            pLevel.setBlockAndUpdate(pPos, blockstate);
            pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(blockstate));
        }
    }

    /**
     * Returns the analog signal this block emits. This is the signal a comparator can read from it.
     *
     * @deprecated call via {@link
     * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getAnalogOutputSignal} whenever possible.
     * Implementing/overriding is fine.
     */
    @Override

    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        return pState.getValue(LEVEL);
    }

    @Override

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(LEVEL);
    }

    @Override

    protected void receiveStalactiteDrip(BlockState pState, Level pLevel, BlockPos pPos, Fluid pFluid) {
        if (!this.isFull(pState)) {
            BlockState blockstate = pState.setValue(LEVEL, Integer.valueOf(pState.getValue(LEVEL) + 1));
            pLevel.setBlockAndUpdate(pPos, blockstate);
            pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(blockstate));
            pLevel.levelEvent(1047, pPos, 0);
        }
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    // In your block class:


}
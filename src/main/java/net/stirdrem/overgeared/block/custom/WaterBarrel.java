package net.stirdrem.overgeared.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
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

public class WaterBarrel extends AbstractWaterBarrel {
    private static final float RAIN_FILL_CHANCE = 0.05F;
    private static final float POWDER_SNOW_FILL_CHANCE = 0.1F;

    public WaterBarrel(BlockBehaviour.Properties properties) {
        super(properties, BarrelInteraction.EMPTY);
    }

    @Override
    public boolean isFull(BlockState pState) {
        return false;
    }

    protected static boolean shouldHandlePrecipitation(Level pLevel, Biome.Precipitation pPrecipitation) {
        if (pPrecipitation == Biome.Precipitation.RAIN) {
            return pLevel.getRandom().nextFloat() < 0.05F;
        } else if (pPrecipitation == Biome.Precipitation.SNOW) {
            return pLevel.getRandom().nextFloat() < 0.1F;
        } else {
            return false;
        }
    }

    @Override
    public void handlePrecipitation(BlockState pState, Level pLevel, BlockPos pPos, Biome.Precipitation pPrecipitation) {
        if (shouldHandlePrecipitation(pLevel, pPrecipitation)) {
            if (pPrecipitation == Biome.Precipitation.RAIN) {
                pLevel.setBlockAndUpdate(pPos, ModBlocks.WATER_BARREL_FULL.get().defaultBlockState());
                pLevel.gameEvent((Entity) null, GameEvent.BLOCK_CHANGE, pPos);
            } 
            /*else if (pPrecipitation == Biome.Precipitation.SNOW) {
                pLevel.setBlockAndUpdate(pPos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
                pLevel.gameEvent((Entity) null, GameEvent.BLOCK_CHANGE, pPos);
            }*/

        }
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid pFluid) {
        return true;
    }

    @Override
    protected void receiveStalactiteDrip(BlockState pState, Level pLevel, BlockPos pPos, Fluid pFluid) {
        if (pFluid == Fluids.WATER) {
            BlockState blockstate = ModBlocks.WATER_BARREL_FULL.get().defaultBlockState();
            pLevel.setBlockAndUpdate(pPos, blockstate);
            pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(blockstate));
            pLevel.levelEvent(1047, pPos, 0);
        }
        /*else if (pFluid == Fluids.LAVA) {
            BlockState blockstate1 = Blocks.LAVA_CAULDRON.defaultBlockState();
            pLevel.setBlockAndUpdate(pPos, blockstate1);
            pLevel.gameEvent(GameEvent.BLOCK_CHANGE, pPos, GameEvent.Context.of(blockstate1));
            pLevel.levelEvent(1046, pPos, 0);
        }*/

    }
}

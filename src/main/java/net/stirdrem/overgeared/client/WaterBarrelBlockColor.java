package net.stirdrem.overgeared.client;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

// Client-side only! (Put in your client package)
public class WaterBarrelBlockColor implements BlockColor {
    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tintIndex) {
        // Use biome water color if world/pos exists, otherwise fall back to default water blue
        return world != null && pos != null ?
                BiomeColors.getAverageWaterColor(world, pos) :
                0x3F76E4; // Default water color
    }
}

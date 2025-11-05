/*
package commoble.jumbofurnace.jumbo_furnace;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import commoble.jumbofurnace.JumboFurnace;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent.EntityMultiPlaceEvent;

public class MultiBlockHelper {
    public static final List<Pair<BlockPos, BlockState>> NO_SNAPSHOTS = ImmutableList.of();

    */
/**
 * Scans the area around the placement position to see if a new jumbo furnace can be formed
 * If it can, returns a list of pairs of
 * -- a block snapshot of the existing block
 * -- the jumbo furnace state that that block should be set to
 *
 * @param world
 * @param placePos
 * @return
 *//*

    public static List<Pair<BlockPos, BlockState>> getJumboFurnaceStates(ResourceKey<Level> key, LevelAccessor world, BlockPos placePos, BlockState againstState, Entity entity) {
        return get3x3CubeAround(placePos)
                .filter(pos -> canJumboFurnaceFormAt(world, pos, placePos))
                .map(pos -> getStatesForPlacementIfPermitted(key, world, pos, againstState, entity))
                .filter(list -> !list.isEmpty())
                .findFirst()
                .orElse(ImmutableList.of());
    }

    */
/**
 * Returns whether a jumbo furnace can form around the given core position.
 *
 * @param world
 * @param corePos  The core position a jumbo furnace would form around
 * @param placePos The position that a furnace block is about to be placed at
 * @return
 *//*

    public static boolean canJumboFurnaceFormAt(LevelAccessor world, BlockPos corePos, BlockPos placePos) {
        return get3x3CubeAround(corePos)
                .allMatch(pos -> pos.equals(placePos) || world.getBlockState(pos).is(JumboFurnace.JUMBOFURNACEABLE_TAG));
    }

    */
/**
 * Returns whether the world contains sufficiently empty space for a jumbo furnace around the given core position
 *
 * @param world
 * @param corePos
 * @return true if the 3x3 cube around the position contains replaceable blockstates (air, plants, etc)
 *//*

    public static boolean canJumboFurnacePlaceAt(LevelAccessor world, BlockPos corePos, BlockPlaceContext useContext) {
        // the two-blockpos constructor for AABB is [inclusive, exclusive)
        // so we have to add 2 to the second arg
        boolean noEntitiesInArea = world.getEntitiesOfClass(LivingEntity.class, new AABB(corePos.offset(-1, -1, -1), corePos.offset(2, 2, 2))).isEmpty();
        return noEntitiesInArea && get3x3CubeAround(corePos)
                .allMatch(pos ->
                        world.getBlockState(pos)
                                .canBeReplaced(useContext));
    }

    public static Stream<BlockPos> get3x3CubeAround(BlockPos pos) {
        return BlockPos.betweenClosedStream(pos.offset(-1, -1, -1), pos.offset(1, 1, 1));
    }

    public static List<Pair<BlockPos, BlockState>> getStatesForPlacementIfPermitted(ResourceKey<Level> key, LevelAccessor world, BlockPos corePos, BlockState againstState, Entity placer) {
        List<Pair<BlockPos, BlockState>> pairs = JumboFurnace.get().jumboFurnaceBlock.get().getStatesForFurnace(corePos);
        return doesPlayerHavePermissionToMakeJumboFurnace(key, world, pairs, againstState, placer)
                ? pairs
                : NO_SNAPSHOTS;
    }

    public static boolean doesPlayerHavePermissionToMakeJumboFurnace(ResourceKey<Level> key, LevelAccessor world, List<Pair<BlockPos, BlockState>> pairs, BlockState placedAgainst, Entity entity) {
        List<BlockSnapshot> snapshots = pairs.stream()
                .map(pair -> BlockSnapshot.create(key, world, pair.getFirst()))
                .collect(Collectors.toList());
        EntityMultiPlaceEvent event = new EntityMultiPlaceEvent(snapshots, placedAgainst, entity);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }
}*/

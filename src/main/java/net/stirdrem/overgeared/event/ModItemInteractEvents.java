package net.stirdrem.overgeared.event;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
//import net.stirdrem.overgeared.block.custom.LayeredWaterBarrel;
import net.stirdrem.overgeared.util.ModTags;


@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModItemInteractEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack heldStack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        BlockState state = level.getBlockState(pos);

        // Only handle if holding a heated metal
        if (!heldStack.is(ModTags.Items.HEATED_METALS)) {
            return;
        }

        // Handle water cauldron interaction
        if (state.is(Blocks.WATER_CAULDRON)) {
            handleCauldronInteraction(level, pos, player, heldStack, state);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
        // Handle water barrel interaction
       /* else if (state.is(ModBlocks.WATER_BARREL_FULL.get())) {
            handleWaterBarrelInteraction(level, pos, player, heldStack, state);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }*/
    }

    private static void handleCauldronInteraction(Level level, BlockPos pos, Player player,
                                                  ItemStack heldStack, BlockState state) {
        IntegerProperty levelProperty = LayeredCauldronBlock.LEVEL;
        int waterLevel = state.getValue(levelProperty);

        if (waterLevel > 0) {
            // Update water level
            if (waterLevel == 1) {
                level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            } else {
                level.setBlockAndUpdate(pos, state.setValue(levelProperty, waterLevel - 1));
            }
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

            // Cool the ingot
            coolIngot(player, heldStack);
        }
    }

    /*private static void handleWaterBarrelInteraction(Level level, BlockPos pos, Player player,
                                                     ItemStack heldStack, BlockState state) {
        IntegerProperty levelProperty = LayeredWaterBarrel.LEVEL;
        int waterLevel = state.getValue(levelProperty);

        if (waterLevel > 0) {
            // Update water level
            if (waterLevel == 1) {
                level.setBlockAndUpdate(pos, ModBlocks.WATER_BARREL.get().defaultBlockState());
            } else {
                level.setBlockAndUpdate(pos, state.setValue(levelProperty, waterLevel - 1));
            }
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

            // Cool the ingot
            coolIngot(player, heldStack);
        }
    }*/

    private static void coolIngot(Player player, ItemStack heldStack) {
        Item cooledItem = getCooledIngot(heldStack.getItem());
        if (cooledItem != null) {
            ItemStack cooledIngot = new ItemStack(cooledItem);
            heldStack.shrink(1);

            if (heldStack.isEmpty()) {
                player.setItemInHand(player.getUsedItemHand(), cooledIngot);
            } else {
                if (!player.getInventory().add(cooledIngot)) {
                    player.drop(cooledIngot, false);
                }
            }

            if (player != null) {
                player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
            }
        }
    }

    private static Item getCooledIngot(Item heatedItem) {
        var heatedTag = ForgeRegistries.ITEMS.tags().getTag(ModTags.Items.HEATED_METALS);
        var cooledTag = ForgeRegistries.ITEMS.tags().getTag(ModTags.Items.HEATABLE_METALS);

        int index = 0;
        for (Item item : heatedTag) {
            if (item == heatedItem) {
                int i = 0;
                for (Item cooledItem : cooledTag) {
                    if (i == index) {
                        return cooledItem;
                    }
                    i++;
                }
            }
            index++;
        }
        return null;
    }
}
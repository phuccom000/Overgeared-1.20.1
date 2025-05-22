/*
package net.stirdrem.overgeared.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.heat.HeatCapability;
import net.stirdrem.overgeared.heat.HeatCapabilityProvider;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.HeatableItem;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModEvents {
    private static int TICK = 0;
    private static final int perTick = 1;
    private static boolean hasTongs = false;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) return;

        Level level = event.level;

        // Define the area to search for ItemEntities
        AABB searchArea = new AABB(
                level.getWorldBorder().getMinX(), level.getMinBuildHeight(), level.getWorldBorder().getMinZ(),
                level.getWorldBorder().getMaxX(), level.getMaxBuildHeight(), level.getWorldBorder().getMaxZ()
        );

        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, searchArea);

        for (ItemEntity itemEntity : itemEntities) {
            BlockPos pos = itemEntity.blockPosition();
            BlockState state = level.getBlockState(pos);
            ItemStack itemStack = itemEntity.getItem();

            if (!itemStack.is(ModTags.Items.HEATED_METALS)) continue;

            // Check if the item is in water or in a water cauldron
            boolean inWater = itemEntity.isInWater();
            boolean inWaterCauldron = state.is(Blocks.WATER_CAULDRON) && state.getValue(LayeredCauldronBlock.LEVEL) > 0;

            if (!inWater && !inWaterCauldron) continue;

            Item cooledItem = getCooledIngot(itemStack.getItem());
            if (cooledItem == null) continue;

            // Create cooled item stack
            ItemStack cooledStack = new ItemStack(cooledItem);

            // Handle item stack reduction
            itemStack.shrink(1);
            if (itemStack.isEmpty()) {
                itemEntity.discard();
            }

            // Spawn new item with proper motion
            Block.popResource(level, pos, cooledStack); // spawn cooled ingot

            // Play sound
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);

            // If in water cauldron, reduce water level by 1
            if (inWaterCauldron) {
                int currentLevel = state.getValue(LayeredCauldronBlock.LEVEL);
                if (currentLevel == 1) {
                    // Replace Water Cauldron with normal (empty) Cauldron
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                } else {
                    // Decrease water level by 1
                    level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, currentLevel - 1));
                }
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            Level level = player.level();
            //player.sendSystemMessage(Component.literal(String.valueOf(TICK)));
            boolean hasHeatedIngot = false;
            // Check inventory
            //if (level.isClientSide()) return; // Ensure this runs only on the server side

            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && stack.is(ModTags.Items.HEATED_METALS)) {
                    hasHeatedIngot = true;
                    break;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffhandItem();
            if (!offhandStack.isEmpty() && offhandStack.is(ModTags.Items.HEATED_METALS)) {
                hasHeatedIngot = true;
            }

            // Apply effect if player has heated ingot
            if (hasHeatedIngot) {
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty() && offhand.is(ModTags.Items.TONGS)) {
                    hasTongs = true;
                    if (TICK % perTick == 0)
                        offhandStack.hurtAndBreak(1, event.player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
                if (!hasTongs)            //player.sendSystemMessage(Component.literal("Player does not have tongs!"));
                    player.hurt(player.damageSources().hotFloor(), 1.0F); // Apply burn damage
            }

            */
/*for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && stack.is(ModTags.Items.HEATABLE_METALS)
                        && stack.hasTag() && stack.getTag().contains("heat")) {
                    // Decrease durability over time
                    //if (level.getGameTime() % 20 == 0) { // Every second
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    //}

                    // Send debug message to the player
                    int currentDurability = stack.getMaxDamage() - stack.getDamageValue();
                    //player.sendSystemMessage(Component.literal("Item: " + stack.getItem().getDescription().getString() +
                            //", Durability: " + currentDurability + "/" + stack.getMaxDamage()));
                }
            }*//*


            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && stack.is(ModTags.Items.HEATED_METALS)) {
                    if (stack.getDamageValue() == stack.getMaxDamage() - 1) {
                        Item cooledItem = getCooledIngot(stack.getItem());
                        if (cooledItem != null) {
                            ItemStack cooledIngot = new ItemStack(cooledItem);
                            stack.shrink(1);
                            if (stack.isEmpty()) {
                                player.setItemInHand(player.getUsedItemHand(), cooledIngot);
                                level.playSound(null, player.getOnPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                            } else {
                                if (!player.getInventory().add(cooledIngot)) {
                                    player.drop(cooledIngot, false);
                                }
                            }
                        }
                    } else {
                        stack.hurtAndBreak(1, event.player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    }
                    break;
                }
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
*/

package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.block.ModBlocks;
//import net.stirdrem.overgeared.block.custom.LayeredWaterBarrel;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeatedItem extends Item {
    public HeatedItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack heldStack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.WATER_CAULDRON)) {
            IntegerProperty levelProperty = LayeredCauldronBlock.LEVEL;
            int waterLevel = state.getValue(levelProperty);

            if (waterLevel > 0 && heldStack.is(ModTags.Items.HEATED_METALS)) {
                // Decrease water level by 1
                if (waterLevel == 1) {
                    // Replace Water Cauldron with normal (empty) Cauldron
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                } else {
                    // Decrease water level by 1
                    level.setBlockAndUpdate(pos, state.setValue(levelProperty, waterLevel - 1));
                }
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

                // Determine the cooled ingot
                Item cooledItem = getCooledIngot(heldStack.getItem());
                if (cooledItem != null) {
                    ItemStack cooledIngot = new ItemStack(cooledItem);
                    heldStack.shrink(1);
                    if (heldStack.isEmpty()) {
                        player.setItemInHand(context.getHand(), cooledIngot);
                    } else {
                        if (!player.getInventory().add(cooledIngot)) {
                            player.drop(cooledIngot, false);
                        }
                    }
                    if (player != null) {
                        //player.sendSystemMessage(Component.literal("The heated ingot has been cooled in the cauldron."));
                        player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        } /*else if (state.is(ModBlocks.WATER_BARREL_FULL.get())) {
            IntegerProperty levelProperty = LayeredWaterBarrel.LEVEL;
            int waterLevel = state.getValue(levelProperty);

            if (waterLevel > 0 && heldStack.is(ModTags.Items.HEATED_METALS)) {
                // Decrease water level by 1
                if (waterLevel == 1) {
                    // Replace Water Cauldron with normal (empty) Cauldron
                    level.setBlockAndUpdate(pos, ModBlocks.WATER_BARREL.get().defaultBlockState());
                } else {
                    // Decrease water level by 1
                    level.setBlockAndUpdate(pos, state.setValue(levelProperty, waterLevel - 1));
                }
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

                // Determine the cooled ingot
                Item cooledItem = getCooledIngot(heldStack.getItem());
                if (cooledItem != null) {
                    ItemStack cooledIngot = new ItemStack(cooledItem);
                    heldStack.shrink(1);
                    if (heldStack.isEmpty()) {
                        player.setItemInHand(context.getHand(), cooledIngot);
                    } else {
                        if (!player.getInventory().add(cooledIngot)) {
                            player.drop(cooledIngot, false);
                        }
                    }
                    if (player != null) {
                        //player.sendSystemMessage(Component.literal("The heated ingot has been cooled in the cauldron."));
                        player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }*/
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;

        long time = level.getGameTime();

        // Damage every second (20 ticks) only if item is heated.
        if (time % 20 != 0) return;

        if (stack.is(ModTags.Items.HEATED_METALS)) {
            boolean hasTongs = player.getOffhandItem().is(ModTags.Items.TONGS) || player.getMainHandItem().is(ModTags.Items.TONGS);
            if (hasTongs) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else {
                player.hurt(player.damageSources().hotFloor(), 1.0F);
            }
        }
    }

    private Item getCooledIngot(Item heatedItem) {
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

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        String quality = pStack.getOrCreateTag().getString("ForgingQuality");
        if (!quality.isEmpty()) {
            pTooltipComponents.add(Component.translatable(getDisplayQuality(quality)).withStyle(ChatFormatting.GRAY));
        }
        pTooltipComponents.add(Component.translatable("tooltip.overgeared.heatedingots.tooltip").withStyle(ChatFormatting.GRAY)
        );

    }


    private String getDisplayQuality(String key) {
        return switch (key) {
            case "poor" -> "tooltip.overgeared.poor";
            case "well" -> "tooltip.overgeared.well";
            case "expert" -> "tooltip.overgeared.expert";
            case "perfect" -> "tooltip.overgeared.perfect";
            case "master" -> "tooltip.overgeared.master";
            default -> "";
        };
    }
}

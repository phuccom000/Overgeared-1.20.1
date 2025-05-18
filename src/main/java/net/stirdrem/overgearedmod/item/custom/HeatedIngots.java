package net.stirdrem.overgearedmod.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
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
import net.stirdrem.overgearedmod.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeatedIngots extends Item {
    public HeatedIngots(Properties properties) {
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
        }

        return InteractionResult.PASS;
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
        pTooltipComponents.add(Component.translatable("tooltip.overgearedmod.heatedingots.tooltip").withStyle(ChatFormatting.GRAY)
        );

    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        // Show the bar if the item has taken any damage
        return stack.isDamaged();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        // Calculate the width of the bar based on remaining durability
        return Math.round(13.0F * (1.0F - (float) stack.getDamageValue() / stack.getMaxDamage()));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Calculate color from red (hot) to orange as durability decreases
        float durabilityRatio = 1.0F - (float) stack.getDamageValue() / stack.getMaxDamage();
        float hue = 0.05F * durabilityRatio; // Adjust hue for color transition
        return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 1.0F, 1.0F);
    }
}

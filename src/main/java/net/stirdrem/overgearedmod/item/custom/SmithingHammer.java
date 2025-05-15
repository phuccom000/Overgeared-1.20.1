package net.stirdrem.overgearedmod.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgearedmod.block.ModBlocks;
import net.stirdrem.overgearedmod.item.ModItems;

public class SmithingHammer extends Item {
    public SmithingHammer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if (!pContext.getLevel().isClientSide()) {
            BlockPos posClicked = pContext.getClickedPos();
            Player player = pContext.getPlayer();
            boolean foundBlock = false;
            BlockState state = pContext.getLevel().getBlockState(posClicked);

            if (isSmithingAnvil(state)) {
                outputResult(player, state.getBlock());
            }
        }
/*
        pContext.getItemInHand().hurtAndBreak(1, pContext.getPlayer(),
                player -> player.broadcastBreakEvent(player.getUsedItemHand()));*/

        return InteractionResult.SUCCESS;
    }

    private void outputResult(Player player, Block block) {
        player.sendSystemMessage(Component.literal("Right clicked on anvil"));
    }


    private boolean isSmithingAnvil(BlockState state) {
        return state.is(ModBlocks.SMITHING_ANVIL.get());
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        ItemStack stack = itemStack.copy();
        return stack;
    }
}

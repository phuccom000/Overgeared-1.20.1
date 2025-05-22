package net.stirdrem.overgeared.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.util.ModTags;

public class SmithingHammer extends Item {
    public SmithingHammer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack held = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && held.is(ModTags.Items.SMITHING_HAMMERS)) {
            if (state.is(ModBlocks.SMITHING_ANVIL.get())) {
                if (!level.isClientSide()) {
                    level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1f, 1f);
                    held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                } else {
                    // Client-side only
                    AnvilMinigameOverlay.isVisible = !AnvilMinigameOverlay.isVisible;
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && held.is(ModTags.Items.SMITHING_HAMMERS)) {
            if (!level.isClientSide()) {
                level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1f, 1f);
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            } else {
                // Client-side only
                AnvilMinigameOverlay.isVisible = !AnvilMinigameOverlay.isVisible;
            }
            return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
        }
        return InteractionResultHolder.pass(held);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return itemStack.copy();
    }
}
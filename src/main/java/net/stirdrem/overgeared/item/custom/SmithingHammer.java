package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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

        if (player.isCrouching() && held.is(ModItems.SMITHING_HAMMER.get())) {
            if (state.is(ModBlocks.SMITHING_ANVIL.get())) {
                if (level.isClientSide()) {
                    // Client-side only
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof SmithingAnvilBlockEntity anvilBE) {
                        Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                        if (anvilBE.hasRecipe()) {
                            ItemStack result = recipeOpt.get().getResultItem(level.registryAccess());
                            int progress = anvilBE.getRequiredProgress();
                            AnvilMinigameOverlay.startMinigame(result, progress);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }


   /* @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (player.isCrouching() && held.is(ModItems.SMITHING_HAMMER.get())) {
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
    }*/

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return itemStack.copy();
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        // Check if shift is being held down
        if (Screen.hasShiftDown()) {
            // Advanced tooltip (only shown when holding shift)
            pTooltipComponents.add(Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line1")
                    .withStyle(ChatFormatting.GRAY));
            pTooltipComponents.add(Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line2")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            // Hint about holding shift for more info
            pTooltipComponents.add(Component.translatable("tooltip.overgeared.smithing_hammer.hold_shift")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
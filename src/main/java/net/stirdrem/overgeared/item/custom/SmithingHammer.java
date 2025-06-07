package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;

import net.stirdrem.overgeared.minigame.AnvilMinigame;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.FinalizeForgingC2SPacket;
import net.stirdrem.overgeared.networking.packet.StartMinigameC2SPacket;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;

public class SmithingHammer extends DiggerItem {

    private static final Map<BlockPos, UUID> activeAnvils = new ConcurrentHashMap<>();

    public SmithingHammer(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Item.Properties pProperties) {
        super(pAttackDamageModifier, pAttackSpeedModifier, pTier, ModTags.Blocks.SMITHING, pProperties);
    }


    public static ServerPlayer getUsingPlayer(BlockPos pos) {
        return ServerLifecycleHooks.getCurrentServer()
                .getPlayerList()
                .getPlayers()
                .stream()
                .filter(player -> player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME)
                        .map(minigame -> pos.equals(minigame.getAnvilPos()))
                        .orElse(false))
                .findFirst()
                .orElse(null);
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
                // Server-side check
                if (!level.isClientSide()) {
                    // When starting the minigame:
                    if (!activeAnvils.containsKey(pos)) {
                        activeAnvils.put(pos, player.getUUID());
                    } else if (activeAnvils.get(pos).equals(player.getUUID())) {
                        if (level.isClientSide()) {
                            if (FMLEnvironment.dist.isClient()) {
                                // Client-side minigame logic (unchanged)
                                BlockEntity be = level.getBlockEntity(pos);
                                if (be instanceof SmithingAnvilBlockEntity anvilBE) {
                                    Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                                    if (anvilBE.hasRecipe()) {
                                        ItemStack result = recipeOpt.get().getResultItem(level.registryAccess());
                                        int progress = anvilBE.getRequiredProgress();
                                        //AnvilMinigame.start(result, progress, pos);
                                    }
                                }
                            }
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("Anvil in use by another player!"));
                        return InteractionResult.FAIL;
                    }
                }
                if (level.isClientSide()) {
                    if (FMLEnvironment.dist.isClient()) {
                        // Client-side minigame logic (unchanged)
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof SmithingAnvilBlockEntity anvilBE) {
                            Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                            if (anvilBE.hasRecipe()) {
                                ItemStack result = recipeOpt.get().getResultItem(level.registryAccess());
                                int progress = anvilBE.getRequiredProgress();
                                //AnvilMinigame.start(result, progress, pos);
                                ModMessages.sendToServer(new StartMinigameC2SPacket(result, progress, pos));
                                //ModMessages.sendToServer(new FinalizeForgingC2SPacket("shift right clicked"));


                            }
                        }
                    }
                }

                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity
            pEntityLiving) {
        if (pState.getDestroySpeed(pLevel, pPos) != 0.0F) {
            pStack.hurtAndBreak(2, pEntityLiving, (p_43276_) -> {
                p_43276_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }

        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
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
    public void appendHoverText(ItemStack pStack, @Nullable Level
            pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
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

    public static void releaseAnvil(BlockPos pos) {
        activeAnvils.remove(pos);
    }

    public static void cleanupStaleAnvils(Level level) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return; // Only run on server side with ServerLevel access
        }

        // Create a copy to avoid ConcurrentModificationException
        Set<BlockPos> positionsToCheck = new HashSet<>(activeAnvils.keySet());

        for (BlockPos pos : positionsToCheck) {
            // Check if chunk is loaded before accessing block state
            if (serverLevel.isLoaded(pos)) {
                BlockState state = serverLevel.getBlockState(pos);
                UUID user = activeAnvils.get(pos);

                // Remove if:
                // 1. Block is no longer an anvil
                // 2. Or player is no longer online (optional)
                if (!state.is(ModBlocks.SMITHING_ANVIL.get()) ||
                        (user != null && serverLevel.getPlayerByUUID(user) == null)) {
                    activeAnvils.remove(pos);
                }
            } else {
                // If chunk isn't loaded, we can't check - leave it for now
                // Optionally could remove if you want to aggressively clean
            }
        }
    }

    // New method to release all anvils for a specific player
    public static void releaseAnvilsForPlayer(UUID playerId) {
        activeAnvils.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(playerId)) {
                return true; // Remove this entry
            }
            return false;
        });
    }
}
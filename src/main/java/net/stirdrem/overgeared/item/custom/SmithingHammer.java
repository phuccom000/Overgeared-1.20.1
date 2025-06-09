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
import net.minecraftforge.server.ServerLifecycleHooks;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.StartMinigameC2SPacket;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;

import javax.annotation.Nullable;
import java.util.*;

public class SmithingHammer extends DiggerItem {

    // Track both position and player UUID
    private static final Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());

    public SmithingHammer(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(attackDamage, attackSpeed, tier, ModTags.Blocks.SMITHING, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack held = player.getItemInHand(hand);

        if (player == null || !player.isCrouching()) {
            return InteractionResult.PASS;
        }

        if (!level.getBlockState(pos).is(ModBlocks.SMITHING_ANVIL.get())) {
            return InteractionResult.PASS;
        }

        // Server-side handling
/*
        UUID playerId = player.getUUID();
        UUID currentUser = occupiedAnvils.get(pos);
        if (currentUser != null) {
            if (!currentUser.equals(playerId)) {
                player.sendSystemMessage(Component.literal("This anvil is already in use!").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
        }
        // If player is already using this anvil, check minigame visibility
        if (level instanceof ServerLevel serverLevel) {
            ServerPlayer serverPlayer = serverLevel.getServer().getPlayerList().getPlayer(playerId);
            if (serverPlayer != null) {
                serverPlayer.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                    if (!minigame.isVisible()) {
                        // Release anvil if minigame is not visible
                        releaseAnvil(pos);
                    }
                });
            }
        }


        if (level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SmithingAnvilBlockEntity anvilBE && anvilBE.hasRecipe()) {
                ModMessages.sendToServer(new StartMinigameC2SPacket(
                        anvilBE.getResultItem(),
                        anvilBE.getRequiredProgress(),
                        pos
                ));
            }
            return InteractionResult.SUCCESS;
        }

        // Claim the anvil for this player
        occupyAnvil(pos, playerId);
        return InteractionResult.SUCCESS;*/

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
                            ModMessages.sendToServer(new StartMinigameC2SPacket(
                                    result,
                                    progress,
                                    pos
                            ));
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    // Server-side anvil management
    public static boolean isAnvilOccupied(BlockPos pos) {
        return occupiedAnvils.containsKey(pos);
    }

    public static boolean isAnvilOccupiedBy(BlockPos pos, UUID playerId) {
        return playerId.equals(occupiedAnvils.get(pos));
    }

    public static void occupyAnvil(BlockPos pos, UUID playerId) {
        occupiedAnvils.put(pos, playerId);
    }

    public static void releaseAnvil(BlockPos pos) {
        occupiedAnvils.remove(pos);
    }

    public static void cleanupStaleAnvils(ServerLevel level) {
        occupiedAnvils.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            UUID playerId = entry.getValue();

            // Remove if:
            // 1. Block is no longer an anvil
            // 2. Or player is no longer online
            return !level.isLoaded(pos) ||
                    !level.getBlockState(pos).is(ModBlocks.SMITHING_ANVIL.get()) ||
                    level.getPlayerByUUID(playerId) == null;
        });
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
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, entity, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.overgeared.smithing_hammer.hold_shift")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line1")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line2")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
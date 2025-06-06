package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.StartMinigameC2SPacket;
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
        BlockPos pos = context.getClickedPos();

        if (player == null || !player.isCrouching()) {
            return InteractionResult.PASS;
        }

        if (!level.getBlockState(pos).is(ModBlocks.SMITHING_ANVIL.get())) {
            return InteractionResult.PASS;
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

        // Server-side handling
        UUID currentUser = occupiedAnvils.get(pos);
        UUID playerId = player.getUUID();

        if (currentUser != null) {
            if (currentUser.equals(playerId)) {
                // Same player trying to use same anvil - allow reopening UI
                return InteractionResult.SUCCESS;
            } else {
                player.sendSystemMessage(Component.literal("This anvil is already in use!").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
        }

        // Claim the anvil for this player
        occupyAnvil(pos, playerId);
        return InteractionResult.SUCCESS;
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
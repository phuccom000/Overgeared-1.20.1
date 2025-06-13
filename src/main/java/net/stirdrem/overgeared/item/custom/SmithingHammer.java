package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;
import net.stirdrem.overgeared.networking.packet.StartMinigameC2SPacket;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SmithingHammer extends DiggerItem {

    public SmithingHammer(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(attackDamage, attackSpeed, tier, ModTags.Blocks.SMITHING, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof SmithingAnvilBlockEntity anvilBE)) return InteractionResult.PASS;
        if (player == null) return InteractionResult.PASS;

        UUID playerUUID = player.getUUID();

        // Server-side logic
        if (!level.isClientSide) {
            return handleServerSideInteraction(anvilBE, pos, (ServerPlayer) player, playerUUID);
        }
        // Client-side logic
        else {
            return handleClientSideInteraction(anvilBE, pos, player, context.getItemInHand());
        }
    }

    private InteractionResult handleServerSideInteraction(SmithingAnvilBlockEntity anvilBE, BlockPos pos, ServerPlayer player, UUID playerUUID) {
        // Server always uses the block entity as source of truth
        UUID currentOwner = anvilBE.getOwnerUUID();

        // Check if anvil is already owned by someone else
        if (currentOwner != null && !currentOwner.equals(playerUUID)) {
            player.sendSystemMessage(Component.translatable("message.overgeared.anvil_in_use_by_another").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        // Claim the anvil if unclaimed
        if (currentOwner == null) {
            anvilBE.setOwner(playerUUID);
            // Sync to client
            CompoundTag syncData = new CompoundTag();
            syncData.putUUID("anvilOwner", playerUUID);
            syncData.putLong("anvilPos", pos.asLong());
            for (ServerPlayer other : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                ModMessages.sendToPlayer(new MinigameSyncS2CPacket(syncData), other);
            }
        }
        if (playerUUID.equals(currentOwner)) {
            if (anvilBE.hasRecipe()) {
                Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                recipeOpt.ifPresent(recipe -> {
                    ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
                    int progress = anvilBE.getRequiredProgress();
                    ModMessages.sendToServer(new StartMinigameC2SPacket(result, progress, pos));
                });
            }
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleClientSideInteraction(SmithingAnvilBlockEntity anvilBE, BlockPos pos, Player player, ItemStack heldItem) {
        if (!anvilBE.hasRecipe()) {
            return InteractionResult.FAIL;
        }

        if (!anvilBE.hasQuality()) {
            player.displayClientMessage(Component.translatable("message.overgeared.item_has_no_quality").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        // Client should trust the server's sync data in ClientAnvilMinigameData
        UUID currentOwner = ClientAnvilMinigameData.getOccupiedAnvil(pos);
        if (currentOwner != null && !currentOwner.equals(player.getUUID())) {
            return InteractionResult.FAIL;
        }

        /*player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
            Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
            recipeOpt.ifPresent(recipe -> {
                ItemStack result = recipe.getResultItem(player.level().registryAccess());
                int progress = anvilBE.getRequiredProgress();
                ModMessages.sendToServer(new StartMinigameC2SPacket(result, progress, pos));
            });
        });*/

        // Set "pending minigame" data if allowed to request
        ClientAnvilMinigameData.setPendingMinigame(pos);

        return InteractionResult.SUCCESS;
    }

    // Add this to your packet handler for MinigameSyncS2CPacket
    public static void handleAnvilOwnershipSync(CompoundTag syncData) {
        UUID owner = null;
        if (syncData.contains("anvilOwner")) {
            owner = syncData.getUUID("anvilOwner");
            if (owner.getMostSignificantBits() == 0 && owner.getLeastSignificantBits() == 0) {
                owner = null;
            }
        }
        BlockPos pos = BlockPos.of(syncData.getLong("anvilPos"));
        ClientAnvilMinigameData.putOccupiedAnvil(pos, owner);

        // ✅ Only start minigame if this client is the new owner and it was waiting
        if (Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.getUUID().equals(owner)
                && pos.equals(ClientAnvilMinigameData.getPendingMinigamePos())) {

            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
            if (be instanceof SmithingAnvilBlockEntity anvilBE && anvilBE.hasRecipe()) {
                Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                recipeOpt.ifPresent(recipe -> {
                    ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
                    int progress = anvilBE.getRequiredProgress();
                    ModMessages.sendToServer(new StartMinigameC2SPacket(result, progress, pos));
                    ClientAnvilMinigameData.clearPendingMinigame(); // ✅ Done
                });
            }
        }
    }


    public static void releaseAnvil(ServerPlayer player, BlockPos pos) {
        // 1. Clear ownership from the block entity (server-side)
        BlockEntity be = player.level().getBlockEntity(pos);
        if (be instanceof SmithingAnvilBlockEntity anvilBE) {
            anvilBE.clearOwner();
        }

        // 2. Clear client-side state (will be synced via packet below)
        ClientAnvilMinigameData.putOccupiedAnvil(pos, null);

        // 3. Reset player’s minigame capability
       /* player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
            minigame.reset(player);
        });*/

        // 4. Sync null ownership to all clients
        CompoundTag syncData = new CompoundTag();
        syncData.putLong("anvilPos", pos.asLong());
        syncData.putUUID("anvilOwner", new UUID(0, 0)); // special "no owner" UUID
        for (ServerPlayer other : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            ModMessages.sendToPlayer(new MinigameSyncS2CPacket(syncData), other);
        }
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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                if (minigame.getVisible()) {
                    minigame.setIsVisible(false, (ServerPlayer) player);
                }
            });
        }
        return super.use(level, player, hand);
    }
}
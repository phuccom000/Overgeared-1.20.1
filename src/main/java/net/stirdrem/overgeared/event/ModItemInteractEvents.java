package net.stirdrem.overgeared.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.stirdrem.overgeared.OvergearedMod;
//import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.custom.StoneSmithingAnvil;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
//import net.stirdrem.overgeared.datapack.GrindingBlacklistReloadListener;
//import net.stirdrem.overgeared.heatedtem.HeatedItemProvider;
import net.stirdrem.overgeared.item.ModItems;
//import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.networking.packet.HideMinigameS2CPacket;
import net.stirdrem.overgeared.networking.packet.MinigameSetStartedC2SPacket;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;
import net.stirdrem.overgeared.networking.packet.SetMinigameVisibleC2SPacket;
import net.stirdrem.overgeared.recipe.CoolingRecipe;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.GrindingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
//import net.stirdrem.overgeared.screen.FletchingStationMenu;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;
//import net.stirdrem.overgeared.util.QualityHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

//import static net.stirdrem.overgeared.OvergearedMod.getCooledItem;


@EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModItemInteractEvents {
    public static final Map<UUID, BlockPos> playerAnvilPositions = new HashMap<>();
    public static final Map<UUID, Boolean> playerMinigameVisibility = new HashMap<>();
    private static final Set<ItemEntity> trackedEntities = ConcurrentHashMap.newKeySet();

    private static final ConcurrentMap<ItemEntity, Long> trackedSinceMs = new ConcurrentHashMap<>();
    private static final long TRACKED_PRUNE_MS = 2 * 60 * 1000L;
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack heldStack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        BlockState state = level.getBlockState(pos);

        // Check if the item is heated either by tag or NBT
//        boolean isHeatedItem = heldStack.is(ModTags.Items.HEATED_METALS)
//                || (heldStack.hasTag() && heldStack.getTag().getBoolean("Heated"));
//
//        if (!isHeatedItem) {
//            return;
//        }
//
//        // Handle water cauldron interaction
//        if (state.is(Blocks.WATER_CAULDRON)) {
//            handleCauldronInteraction(level, pos, player, heldStack, state);
//            // Remove the "Heated" tag if it exists
//
//
//            event.setCancellationResult(InteractionResult.SUCCESS);
//            event.setCanceled(true);
//        }

    }

    @SubscribeEvent
    public static void onUseSmithingHammer(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack heldItem = event.getItemStack();

        if (!heldItem.is(ModTags.Items.SMITHING_HAMMERS)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockEntity be = level.getBlockEntity(pos);
        BlockState clickedState = level.getBlockState(pos);

        // Shift-right-click to convert stone into smithing anvil
        if (!level.isClientSide && player.isCrouching() && clickedState.is(ModTags.Blocks.ANVIL_BASES)
                && ServerConfig.ENABLE_STONE_TO_ANVIL.get()) {
            BlockState newState = ModBlocks.STONE_SMITHING_ANVIL.get()
                    .defaultBlockState()
                    .setValue(StoneSmithingAnvil.FACING, player.getDirection().getClockWise());
            level.setBlock(pos, newState, 3);
            level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (player instanceof ServerPlayer serverPlayer) {
//                ModAdvancementTriggers.MAKE_SMITHING_ANVIL
//                        .trigger(serverPlayer, "stone");
            }

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

//        if (!level.isClientSide && player.isCrouching() && clickedState.is(Blocks.ANVIL)
//                && ServerConfig.ENABLE_ANVIL_TO_SMITHING.get()) {
//            BlockState newState = ModBlocks.SMITHING_ANVIL.get()
//                    .defaultBlockState()
//                    .setValue(StoneSmithingAnvil.FACING, player.getDirection().getClockWise());
//            level.setBlock(pos, newState, 3);
//            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
//            if (player instanceof ServerPlayer serverPlayer) {
//                ModAdvancementTriggers.MAKE_SMITHING_ANVIL
//                        .trigger(serverPlayer, "iron");
//            }
//            event.setCancellationResult(InteractionResult.SUCCESS);
//            event.setCanceled(true);
//            return;
//        }

        if (!level.isClientSide()) {
            if (!(be instanceof AbstractSmithingAnvilBlockEntity)) {
                hideMinigame((ServerPlayer) player);
                ;
            }
        }

        if (!(be instanceof
                AbstractSmithingAnvilBlockEntity anvilBE)) return;
        UUID playerUUID = player.getUUID();

        if (!player.isCrouching()) return;

        if (!level.isClientSide) {
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            // Server-side ownership logic
            if (anvilBE.hasRecipe() && !ServerConfig.ENABLE_MINIGAME.get()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.no_minigame").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (!anvilBE.hasRecipe()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.no_recipe").withStyle(ChatFormatting.RED), true);
                return;
            }

            if (!anvilBE.hasQuality() && !anvilBE.needsMinigame()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.item_has_no_quality").withStyle(ChatFormatting.RED), true);
                return;
            }

            UUID currentOwner = anvilBE.getOwnerUUID();
            if (currentOwner != null && !currentOwner.equals(playerUUID)) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.anvil_in_use_by_another").withStyle(ChatFormatting.RED), true);
                return;
            }

            if (currentOwner == null && !playerAnvilPositions.containsKey(player.getUUID())) {
                anvilBE.setOwner(playerUUID);

                // ADD SERVER-SIDE TRACKING
                playerAnvilPositions.put(playerUUID, pos);
                playerMinigameVisibility.put(playerUUID, true);

                CompoundTag sync = new CompoundTag();
                sync.putUUID("anvilOwner", playerUUID);
                sync.putLong("anvilPos", pos.asLong());
                PacketDistributor.sendToAllPlayers(new MinigameSyncS2CPacket(sync));
                return;
            }

            if (playerAnvilPositions.get(player.getUUID()) != null && !pos.equals(playerAnvilPositions.get(player.getUUID()))) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.another_anvil_in_use").withStyle(ChatFormatting.RED), true);
                return;
            }
        } else {
            if (anvilBE.hasRecipe() && !ServerConfig.ENABLE_MINIGAME.get()) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.no_minigame").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (!anvilBE.hasRecipe()) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.no_recipe").withStyle(ChatFormatting.RED));
                return;
            }

            if (!anvilBE.hasQuality() && !anvilBE.needsMinigame()) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.item_has_no_quality").withStyle(ChatFormatting.RED));
                return;
            }

            // Client should trust the server's sync data in ClientAnvilMinigameData
            UUID currentOwner = ClientAnvilMinigameData.getOccupiedAnvil(pos);
            if (currentOwner != null && !currentOwner.equals(player.getUUID())) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.anvil_in_use_by_another").withStyle(ChatFormatting.RED));
                return;
            }

            if (player.getUUID().equals(currentOwner)
                    || currentOwner == null
                    && ClientAnvilMinigameData.getPendingMinigamePos() == null) {
                BlockPos pos1 = pos;
                BlockPos anvilPos = playerAnvilPositions.get(player.getUUID());
                if (playerAnvilPositions.get(player.getUUID()) != null && !pos.equals(playerAnvilPositions.get(player.getUUID()))) {
                    //player.sendSystemMessage(Component.translatable("message.overgeared.another_anvil_in_use").withStyle(ChatFormatting.RED));
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.PASS);
                    return;
                }
                if (anvilBE.hasRecipe() || anvilBE.needsMinigame()) {
                    anvilBE.setOwner(playerUUID);
                    AtomicReference<String> quality = new AtomicReference<>("perfect");
                    Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                    recipeOpt.ifPresent(recipe -> {
                        //ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
                        //int progress = anvilBE.getRequiredProgress();
                        //ModMessages.sendToServer(new StartMinigameC2SPacket(result, progress, pos));
                        if (AnvilMinigameEvents.minigameStarted) {
                            boolean isVisible = AnvilMinigameEvents.isIsVisible();
                            AnvilMinigameEvents.setIsVisible(pos, !isVisible);
                            PacketDistributor.sendToServer(new SetMinigameVisibleC2SPacket(!isVisible, pos));
                            playerMinigameVisibility.put(player.getUUID(), !isVisible);
                        } else {
                            quality.set(anvilBE.minigameQuality());
                            AnvilMinigameEvents.reset(quality.get());
                            playerAnvilPositions.put(player.getUUID(), pos);
                            playerMinigameVisibility.put(player.getUUID(), true);
                            AnvilMinigameEvents.setMinigameStarted(pos, true);
                            //AnvilMinigameEvents.setIsVisible(pos, true);
                            PacketDistributor.sendToServer(new MinigameSetStartedC2SPacket(pos));
                            //AnvilMinigameEvents.setMinigameStarted(pos, true);
                            PacketDistributor.sendToServer(new SetMinigameVisibleC2SPacket(true, pos));
                            AnvilMinigameEvents.setHitsRemaining(anvilBE.getRequiredProgress());
                        }
                    });
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.PASS);
                return;
            }

            ClientAnvilMinigameData.setPendingMinigame(pos);
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

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
            if (be instanceof AbstractSmithingAnvilBlockEntity anvilBE && anvilBE.hasRecipe()) {
                Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                recipeOpt.ifPresent(recipe -> {
                    ClientAnvilMinigameData.clearPendingMinigame(); // ✅ Done
                });
            }
        }
    }

    public static void releaseAnvil(ServerPlayer player, BlockPos pos) {
        UUID playerId = player.getUUID();
        if (playerMinigameVisibility.get(playerId) != null)
            playerMinigameVisibility.remove(playerId);
        if (playerAnvilPositions.get(playerId) != null
                && pos.equals(playerAnvilPositions.get(playerId))
        ) {
            playerAnvilPositions.remove(playerId);


            // 1. Clear ownership from the block entity (server-side)
            BlockEntity be = player.level().getBlockEntity(pos);
            String quality = "perfect";
            if (be instanceof AbstractSmithingAnvilBlockEntity anvilBE) {
                anvilBE.clearOwner();
                quality = anvilBE.minigameQuality();
            }
            // 3. Clear client-side state
            ClientAnvilMinigameData.putOccupiedAnvil(pos, null);
            AnvilMinigameEvents.reset(quality);
            // 4. Sync null ownership to all clients
            CompoundTag syncData = new CompoundTag();
            syncData.putLong("anvilPos", pos.asLong());
            syncData.putUUID("anvilOwner", new UUID(0, 0)); // special "no owner" UUID
            PacketDistributor.sendToAllPlayers(new MinigameSyncS2CPacket(syncData));
        }
    }

    public static ServerPlayer getUsingPlayer(BlockPos pos) {
        // Get the server instance
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;

        // Iterate through all online players
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();

            // Check if this player is using the specified anvil
            if (playerAnvilPositions.containsKey(playerId) &&
                    playerAnvilPositions.get(playerId).equals(pos)) {
                return player;
            }
        }

        return null;
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        if (world.isClientSide()) return;

        hideMinigame((ServerPlayer) player);
    }

    public static void hideMinigame(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new HideMinigameS2CPacket());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        Level world = event.getLevel();
        if (world.isClientSide()) return;

        // 🔹 Check if the player is holding a heated metal and targeting water
        if (stack.is(ModTags.Items.HEATED_METALS)) {
            HitResult hit = player.pick(5.0D, 0.0F, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                BlockState state = world.getBlockState(pos);
//                if (state.getFluidState().isSource() && state.getBlock() == Blocks.WATER) {
//                    coolItem(player, stack);
//                    event.setCancellationResult(InteractionResult.SUCCESS);
//                    event.setCanceled(true);
//                }
            }
        }
        if (world.isClientSide()) return;

        HitResult hit = player.pick(5.0D, 0.0F, false);
        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = world.getBlockState(pos);
//        if (player.isCrouching() && state.is(ModTags.Blocks.GRINDSTONES)) {
//
//            if (player.getMainHandItem() != stack) {
//                return;
//            }
//
//            if (hasGrindingRecipe(stack.getItem(), event.getLevel())) {
//                grindItem(player, stack);
//                world.playSound(null, player.blockPosition(),
//                        SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS,
//                        1.0f, 1.2f); // Higher pitch for polishing sound
//                spawnGrindParticles(world, pos);
//                event.setCancellationResult(InteractionResult.SUCCESS);
//                event.setCanceled(true);
//                return;
//            }
//            if (stack.hasTag() && stack.getTag().contains("Polished") && !stack.getTag().getBoolean("Polished")) {
//                // Only convert 1 item in the stack
//                if (stack.getCount() > 1) {
//                    // Split 1 item from the stack
//                    ItemStack polishedItem = stack.copy();
//                    polishedItem.setCount(1);
//                    polishedItem.getOrCreateTag().putBoolean("Polished", true);
//
//                    // Reduce held stack by 1
//                    stack.shrink(1);
//
//                    // Try to add the polished item to player's inventory
//                    if (!player.getInventory().add(polishedItem)) {
//                        // If inventory is full, drop the item in the world
//                        player.drop(polishedItem, false);
//                    }
//                } else {
//                    // Only one item in stack, just polish it directly
//                    stack.getOrCreateTag().putBoolean("Polished", true);
//                }
//
//                world.playSound(null, player.blockPosition(),
//                        SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS,
//                        1.0f, 1.2f); // Higher pitch for polishing sound
//                spawnGrindParticles(world, pos);
//                event.setCancellationResult(InteractionResult.SUCCESS);
//                event.setCanceled(true);
//                return;
//            }
//
//            if (stack.isDamageableItem() && stack.getDamageValue() > 0) {
//                if (!ServerConfig.GRINDING_RESTORE_DURABILITY.get()) {
//                    event.setCancellationResult(InteractionResult.SUCCESS);
//                    event.setCanceled(true);
//                    return;
//                }
//                Item item = stack.getItem();
//                // Check blacklist
//                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
//                List<? extends String> blacklist = ServerConfig.GRINDING_BLACKLIST.get();
//
//                for (String entry : blacklist) {
//                    if (entry.startsWith("#")) {
//                        // Handle tag-based blacklist entries
//                        ResourceLocation tagId = ResourceLocation.parse(entry.substring(1));
//                        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
//                        if (stack.is(tag)) {
//                            event.setCancellationResult(InteractionResult.PASS);
//                            event.setCanceled(true);
//                            return;
//                        }
//                    } else {
//                        // Handle direct item blacklist entries
//                        if (itemId.equals(ResourceLocation.parse(entry))) {
//                            event.setCancellationResult(InteractionResult.PASS);
//                            event.setCanceled(true);
//                            return;
//                        }
//                    }
//                }
//                boolean isBlacklisted = GrindingBlacklistReloadListener.isBlacklisted(stack);
//                if (isBlacklisted) {
//                    event.setCancellationResult(InteractionResult.PASS);
//                    event.setCanceled(true);
//                    return;
//                }
//
//                CompoundTag tag = stack.getOrCreateTag();
//                int reducedCount = tag.getInt("ReducedMaxDurability");
//
//                // Base vanilla durability
//                int originalDurability = stack.getItem().getMaxDamage();
//                // Config multipliers
//                float baseMultiplier = ServerConfig.BASE_DURABILITY_MULTIPLIER.get().floatValue();
//                float grindReduction = ServerConfig.DURABILITY_REDUCE_PER_GRIND.get().floatValue();
//
//                // Quality multiplier (if any)
//                float qualityMultiplier = 1.0f;
//                if (tag.contains("ForgingQuality")) {
//                    qualityMultiplier = QualityHelper.getQualityMultiplier(stack);
//                }
//                int newOriginalDurability = (int) (originalDurability * baseMultiplier * qualityMultiplier);
//
//                // Final durability multiplier, clamped to 10% minimum
//                float penaltyMultiplier = Math.max(0.1f, 1.0f - (reducedCount * grindReduction));
//
//                int effectiveMaxDurability = (int) (newOriginalDurability * penaltyMultiplier);
//                effectiveMaxDurability = Math.max(1, effectiveMaxDurability); // Clamp to avoid zero
//
//                int currentDamage = stack.getDamageValue();
//
//                // If already fully repaired relative to reduced max, skip
//                if (currentDamage <= (newOriginalDurability - effectiveMaxDurability)) {
//                    tag.putInt("ReducedMaxDurability", reducedCount + 1);
//                    stack.setDamageValue(0);
//                    event.getLevel().playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
//                    spawnGrindParticles(world, pos);
//                    event.setCancellationResult(InteractionResult.SUCCESS);
//                    event.setCanceled(true);
//                    return;
//                }
//
//                float restorePercent = ServerConfig.DAMAGE_RESTORE_PER_GRIND.get().floatValue(); // e.g., 0.05F for 5%
//                int theoreticalMaxDurability = (int) (originalDurability * baseMultiplier * qualityMultiplier);
//                int repairAmount = Math.max(1, (int) (theoreticalMaxDurability * restorePercent));
//
//                // Respect effective max cap
//                int newDamage = Math.max(theoreticalMaxDurability - effectiveMaxDurability, currentDamage - repairAmount);
//
//                stack.setDamageValue(newDamage);
//                tag.putInt("ReducedMaxDurability", reducedCount + 1);
//                event.getLevel().playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
//                spawnGrindParticles(world, pos);
//                event.setCancellationResult(InteractionResult.SUCCESS);
//                event.setCanceled(true);
//            }
//
//        }
        ItemStack mainHand = player.getMainHandItem();
        // Only hide if MAIN HAND is not a hammer
        if (!mainHand.is(ModTags.Items.SMITHING_HAMMERS) || !state.is(ModTags.Blocks.SMITHING_ANVIL)) {
            hideMinigame((ServerPlayer) player);
        }
    }

    private static void spawnGrindParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.1);
        }
    }

    private static void handleCauldronInteraction(Level level, BlockPos pos, Player player,
                                                  ItemStack heldStack, BlockState state) {
        IntegerProperty levelProperty = LayeredCauldronBlock.LEVEL;
        int waterLevel = state.getValue(levelProperty);

        if (waterLevel > 0) {
            // Cool the ingot
            coolItem(player, heldStack);
        }
    }
//
//    private static ItemStack coolSingleStack(ItemStack stack, Level level) {
//        Item cooled = getCooledItem(stack.getItem(), level);
//        if (cooled == null) return stack;
//
//        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());
//
//        if (stack.hasTag()) {
//            CompoundTag tag = stack.getTag().copy();
//            tag.remove("Heated");
//            tag.remove("HeatedSince");
//            if (tag.isEmpty()) {
//                cooledStack.setTag(null);
//            } else {
//                cooledStack.setTag(tag);
//            }
//        }
//
//        return cooledStack;
//    }
//
    private static void coolItem(Player player, ItemStack stack) {
//        Item cooled = getCooledItem(stack.getItem(), player.level());
//        if (cooled == null) return;
//        if (stack.getCount() <= 0) return;
//
//        // === Tool Cast special handling ===
//        if (stack.getItem() instanceof ToolCastItem && stack.hasTag()) {
//            CompoundTag tag = stack.getTag();
//
//            if (tag != null && tag.contains("Output", Tag.TAG_COMPOUND)) {
//                ItemStack output = ItemStack.of(tag.getCompound("Output"));
//                ItemStack cooledOutput = coolSingleStack(output, player.level());
//                tag.put("Output", cooledOutput.save(new CompoundTag()));
//            }
//        }
//
//        // === Original logic (unchanged) ===
//        ItemStack cooledStack = new ItemStack(cooled, 1);
//        if (stack.hasTag()) {
//            cooledStack.setTag(stack.getTag().copy());
//            cooledStack.removeTagKey("HeatedSince");
//            cooledStack.removeTagKey("Heated");
//        }
//
//        stack.shrink(1);
//
//        if (stack.isEmpty()) {
//            if (player.getMainHandItem() == stack) {
//                player.setItemInHand(InteractionHand.MAIN_HAND, cooledStack);
//            } else if (player.getOffhandItem() == stack) {
//                player.setItemInHand(InteractionHand.OFF_HAND, cooledStack);
//            } else if (!player.getInventory().add(cooledStack)) {
//                player.drop(cooledStack, false);
//            }
//        } else {
//            if (!player.getInventory().add(cooledStack)) {
//                player.drop(cooledStack, false);
//            }
//        }
//
//        player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
    }
//
//
//    private static void coolItemEntity(ItemEntity entity) {
//        ItemStack stack = entity.getItem();
//        Level level = entity.level();
//
//        Item cooled = getCooledItem(stack.getItem(), level);
//        if (cooled == null || stack.getCount() <= 0) return;
//
//        // === Tool Cast special handling ===
//        if (stack.getItem() instanceof ToolCastItem && stack.hasTag()) {
//            CompoundTag tag = stack.getTag();
//
//            if (tag.contains("Output", Tag.TAG_COMPOUND)) {
//                ItemStack output = ItemStack.of(tag.getCompound("Output"));
//                ItemStack cooledOutput = coolSingleStack(output, level);
//                tag.put("Output", cooledOutput.save(new CompoundTag()));
//            }
//        }
//
//        // === Original entity logic ===
//        CompoundTag oldTag = stack.hasTag() ? stack.getTag().copy() : null;
//        ItemStack cooledStack = new ItemStack(cooled, stack.getCount());
//
//        if (oldTag != null) {
//            oldTag.remove("Heated");
//            oldTag.remove("HeatedSince");
//            if (oldTag.isEmpty()) {
//                cooledStack.setTag(null);
//            } else {
//                cooledStack.setTag(oldTag);
//            }
//        }
//
//        entity.setItem(cooledStack);
//        entity.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
//    }
//
//
//    private static void grindItem(Player player, ItemStack heldStack) {
//        Item cooledItem = getGrindable(heldStack.getItem(), player.level());
//        if (cooledItem != null) {
//            ItemStack cooledIngot = new ItemStack(cooledItem);
//            if (heldStack.hasTag()) {
//                cooledIngot.setTag(heldStack.getTag().copy());
//            }
//            cooledIngot.getOrCreateTag().putBoolean("Polished", true);
//            heldStack.shrink(1);
//
//            if (heldStack.isEmpty()) {
//                player.setItemInHand(player.getUsedItemHand(), cooledIngot);
//            } else {
//                if (!player.getInventory().add(cooledIngot)) {
//                    player.drop(cooledIngot, false);
//                }
//            }
//
//          player.playSound(SoundEvents.GRINDSTONE_USE, 1.0F, 1.0F);
//        }
//    }
//
    private static Item getGrindable(Item heatedItem, Level level) {
        if (heatedItem == null || level == null) return null;

        // Wrap the single item in a container for recipe matching
        SingleRecipeInput container = new SingleRecipeInput(new ItemStack(heatedItem));

        // Find the first matching GrindingRecipe
        Optional<RecipeHolder<GrindingRecipe>> recipeOpt = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.GRINDING_RECIPE.get())
                .stream()
                .filter(r -> r.value().matches(container, level))
                .findFirst();

        if (recipeOpt.isEmpty()) {
            return heatedItem; // no grinding recipe found
        }

        // Return the result item from the recipe
        GrindingRecipe recipe = recipeOpt.get().value();
        ItemStack result = recipe.getResultItem(level.registryAccess());
        return result.isEmpty() ? heatedItem : result.getItem();
    }

    public static boolean hasCoolingRecipe(@Nullable Item heatedItem, @NotNull Level level) {
        if (heatedItem == null) return false;

        // Wrap the item in a container for recipe matching
        SingleRecipeInput container = new SingleRecipeInput(new ItemStack(heatedItem));

        // Find the first matching CoolingRecipe
        Optional<RecipeHolder<CoolingRecipe>> recipeOpt = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.COOLING_RECIPE.get())
                .stream()
                .filter(r -> r.value().matches(container, level))
                .findFirst();

        // Return true if a recipe exists and produces a non-empty result
        return recipeOpt.map(recipe -> !recipe.value().getResultItem(level.registryAccess()).isEmpty())
                .orElse(false);
    }

    public static boolean hasGrindingRecipe(@Nullable Item heatedItem, @NotNull Level level) {
        if (heatedItem == null) return false;

        // Wrap the item in a container for recipe matching
        SingleRecipeInput container = new SingleRecipeInput(new ItemStack(heatedItem));

        // Find the first matching GrindingRecipe
        Optional<RecipeHolder<GrindingRecipe>> recipeOpt = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.GRINDING_RECIPE.get())
                .stream()
                .filter(r -> r.value().matches(container, level))
                .findFirst();

        // Return true if a recipe exists and produces a non-empty result
        return recipeOpt.map(recipe -> !recipe.value().getResultItem(level.registryAccess()).isEmpty())
                .orElse(false);
    }

    private static final Map<ServerLevel, List<ItemEntity>> trackedEntitiesPerWorld = new HashMap<>();

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

//        ItemStack stack = itemEntity.getItem();
//        boolean isHeatedItem = stack.hasTag() && stack.getTag().getBoolean("Heated");
//        Item cooled = getCooledItem(stack.getItem(), event.getLevel());
//
//        if (hasCoolingRecipe(stack.getItem(), event.getLevel()) || isHeatedItem) {
//            // Only track if cooled ingot exists or item is heated
//            if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
//
//            // Track per world
//            trackedEntitiesPerWorld
//                    .computeIfAbsent(serverLevel, w -> new ArrayList<>())
//                    .add(itemEntity);
//
//            // Only track items that already have HeatedSince
//            if (stack.hasTag() && stack.getTag().contains("HeatedSince")) {
//                long heatedSince = stack.getTag().getLong("HeatedSince");
//                trackedSinceMs.put(itemEntity, heatedSince);
//            }
//        }
    }

    @SubscribeEvent
    public static void onEntityPickupItem(ItemEntityPickupEvent.Post event) {
        if (!(event.getItemEntity().level() instanceof ServerLevel serverLevel)) return;

        List<ItemEntity> tracked = trackedEntitiesPerWorld.get(serverLevel);
        if (tracked != null) {
            tracked.remove(event.getItemEntity());
            if (tracked.isEmpty()) trackedEntitiesPerWorld.remove(serverLevel);
        }

        trackedSinceMs.remove(event.getItemEntity());
    }

    private static final Map<Item, Boolean> COOLING_CACHE = new HashMap<>();

    private static boolean hasCoolingRecipeCached(Item item, Level level) {
        return COOLING_CACHE.computeIfAbsent(item, (i) ->
                hasCoolingRecipe(i, level)   // your existing function
        );
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        // Iterate over all loaded worlds
//        for (ServerLevel level : server.getAllLevels()) {
//            long now = level.getGameTime();
//
//            // Only run every 10 ticks
//            if (now % 10 != 0) continue;
//
//            List<ItemEntity> tracked = trackedEntitiesPerWorld.get(level);
//            if (tracked == null || tracked.isEmpty()) continue;
//
//            Iterator<ItemEntity> it = tracked.iterator();
//
//            while (it.hasNext()) {
//                ItemEntity entity = it.next();
//                if (!entity.isAlive()) {
//                    it.remove();
//                    trackedSinceMs.remove(entity);
//                    continue;
//                }
//
//                ItemStack stack = entity.getItem();
//
//                // --- Check if heated ---
//                boolean isHeated = (stack.hasTag() && stack.getTag().getBoolean("Heated"))
//                        || hasCoolingRecipeCached(stack.getItem(), level);
//
//                if (!isHeated) {
//                    it.remove();
//                    trackedSinceMs.remove(entity);
//                    continue;
//                }
//
//                Long started = trackedSinceMs.get(entity);
//                boolean cooled = false;
//
//                // --- Time-based cooling ---
//                if (started != null && now - started > ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get()) {
//                    cooled = true;
//                }
//
//                // --- Water-based cooling ---
//                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
//                        entity.getX(), entity.getY(), entity.getZ());
//
//                BlockState state = level.getBlockState(pos);
//                if (state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON)) {
//                    cooled = true;
//                }
//
//                if (cooled) {
//                    // Cool entire stack
//                    coolItemEntity(entity);
//
//                    // Clear timestamp
//                    trackedSinceMs.remove(entity);
//
//                    // Remove entity if stack is empty
//                    if (entity.getItem().isEmpty()) {
//                        it.remove();
//                    }
//                }
//            }
//
//            // Clean up empty lists to avoid memory leaks
//            if (tracked.isEmpty()) {
//                trackedEntitiesPerWorld.remove(level);
//            }
//        }
    }

// TODO: this should be replaced with DataAttachments
//    @SubscribeEvent
//    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
//        if (event.getObject() instanceof ItemEntity) {
//            event.addCapability(ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "heated_item"),
//                    new HeatedItemProvider());
//        }
//    }

    @SubscribeEvent
    public static void onFlintUsedOnStone(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack heldItem = event.getItemStack();
        Player player = event.getEntity();

        // Check: Right-clicked block is stone and holding flint
        if (state.is(Blocks.STONE) && heldItem.is(Items.FLINT) && ServerConfig.GET_ROCK_USING_FLINT.get()) {

            ServerLevel serverLevel = (ServerLevel) level;
            boolean shouldConsumeFlint = false;

            // Chance to drop the item (e.g., 30%)
            if (RANDOM.nextFloat() < ServerConfig.ROCK_DROPPING_CHANCE.get()) {
                ItemStack dropStack = new ItemStack(ModItems.ROCK.get());

                /* spawn at block centre, slightly above */
                double sx = pos.getX() + 0.5;
                double sy = pos.getY() + 0.9;   // a bit above
                double sz = pos.getZ() + 0.5;

                /* vector from block to player */
                double dx = player.getX() - sx;
                double dy = (player.getY() + player.getEyeHeight()) - sy;
                double dz = player.getZ() - sz;

                /* normalise + scale for gentle toss (speed ≈ 0.25) */
                double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len != 0) {
                    dx /= len;
                    dy /= len;
                    dz /= len;
                }
                double speed = 0.25;
                ItemEntity item = new ItemEntity(serverLevel, sx, sy, sz, dropStack);
                item.setDeltaMovement(dx * speed, dy * speed, dz * speed);
                item.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(item);
                level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());

            }
            shouldConsumeFlint = RANDOM.nextFloat() < ServerConfig.FLINT_BREAKING_CHANCE.get();

            // Damage flint (optional)
            // Handle flint consumption
            if (shouldConsumeFlint) {
                heldItem.shrink(1);
                // Play tool breaking sound at player's location
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_BREAK, SoundSource.PLAYERS,
                        0.8F, 0.8F + RANDOM.nextFloat() * 0.4F);

                // Show breaking animation
                player.swing(event.getHand());
            } else {
                // Play regular stone hit sound
                level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS,
                        1.0F, 0.8F + RANDOM.nextFloat() * 0.4F);
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            hideMinigame((ServerPlayer) player);
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            hideMinigame((ServerPlayer) player);
        }
    }

    @SubscribeEvent
    public static void onArrowTipping(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        InteractionHand hand = event.getHand();

        if (level.isClientSide()) return;

        // Get items in both hands
        ItemStack usedHand = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        // Check if we're dealing with arrow + potion combination
        boolean isVanillaArrow = usedHand.is(Items.ARROW) && otherStack.is(Items.POTION);
        boolean isCustomArrow = ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get() && (usedHand.is(ModItems.IRON_UPGRADE_ARROW.get()) ||
                usedHand.is(ModItems.STEEL_UPGRADE_ARROW.get()) ||
                usedHand.is(ModItems.DIAMOND_UPGRADE_ARROW.get())) &&
                otherStack.is(Items.POTION);

        if (!isVanillaArrow && !isCustomArrow) {
            return;
        }

        // Get potion contents from the potion item (1.21 API uses data components)
        PotionContents potionContents = otherStack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) return;

        // Track usage via data component
        int used = otherStack.getOrDefault(ModComponents.TIPPED_USES.get(), 0);
        int maxUse = ServerConfig.MAX_POTION_TIPPING_USE.get();

        // Create the appropriate tipped arrow BEFORE consuming
        ItemStack resultArrow;
        if (isVanillaArrow) {
            resultArrow = new ItemStack(Items.TIPPED_ARROW);
            resultArrow.set(DataComponents.POTION_CONTENTS, potionContents);
        } else {
            resultArrow = usedHand.copy();
            resultArrow.setCount(1);
            // Apply potion contents to custom arrow
            resultArrow.set(DataComponents.POTION_CONTENTS, potionContents);
        }

        // Only consume arrow after we've successfully created the tipped version
        if (usedHand.getCount() == 1) {
            // For single arrow, replace it with the tipped version
            player.setItemInHand(hand, resultArrow);
        } else {
            // For stack, shrink and add new tipped arrow
            usedHand.shrink(1);
            player.setItemInHand(hand, usedHand);
            if (!player.getInventory().add(resultArrow)) {
                player.drop(resultArrow, false);
            }
        }

        // Handle potion usage tracking
        used++;
        if (used >= maxUse) {
            // Potion is exhausted, replace with empty bottle
            player.setItemInHand(otherHand, new ItemStack(Items.GLASS_BOTTLE));
        } else {
            // Update the usage count on the potion using data component
            otherStack.set(ModComponents.TIPPED_USES.get(), used);
        }

        level.playSound(null,
                player.blockPosition(),
                SoundEvents.BREWING_STAND_BREW,
                SoundSource.PLAYERS,
                0.6F,
                1.2F
        );

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onRightClickFletching(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!ServerConfig.ENABLE_FLETCHING_RECIPES.get()) return;
        // Only respond to main-hand to avoid double-open with offhand
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.FLETCHING_TABLE)) return;

        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }
// TODO: implement fletching menu
//        // Server: open our custom menu via a SimpleMenuProvider (vanilla table has no BE)
//        SimpleMenuProvider provider = new SimpleMenuProvider(
//                (windowId, playerInv, p) ->
//                        new FletchingStationMenu(windowId, playerInv, ContainerLevelAccess.create(level, pos)),
//                Component.translatable("container.overgeared.fletching_table")
//        );
//
//        player.openMenu(provider, pos);
        event.setCancellationResult(InteractionResult.sidedSuccess(false));
        event.setCanceled(true);
    }
}
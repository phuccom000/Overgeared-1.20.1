package net.stirdrem.overgeared.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
//import net.stirdrem.overgeared.block.custom.LayeredWaterBarrel;
import net.stirdrem.overgeared.block.custom.StoneSmithingAnvil;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.*;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.FletchingStationMenu;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.QualityHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.stirdrem.overgeared.OvergearedMod.getCooledIngot;


@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModItemInteractEvents {
    private static final Set<ItemEntity> trackedEntities = ConcurrentHashMap.newKeySet();
    public static final Map<UUID, BlockPos> playerAnvilPositions = new HashMap<>();
    public static final Map<UUID, Boolean> playerMinigameVisibility = new HashMap<>();

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack heldStack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        BlockState state = level.getBlockState(pos);

        // Only handle if holding a heated metal
        if (!heldStack.is(ModTags.Items.HEATED_METALS)) {
            return;
        }

        // Handle water cauldron interaction
        if (state.is(Blocks.WATER_CAULDRON)) {
            handleCauldronInteraction(level, pos, player, heldStack, state);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);

        }


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
        if (!level.isClientSide && player.isCrouching() && clickedState.is(Blocks.STONE)
                && ServerConfig.ENABLE_STONE_TO_ANVIL.get()) {
            BlockState newState = ModBlocks.STONE_SMITHING_ANVIL.get()
                    .defaultBlockState()
                    .setValue(StoneSmithingAnvil.FACING, player.getDirection().getClockWise());
            level.setBlock(pos, newState, 3);
            level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (!level.isClientSide && player.isCrouching() && clickedState.is(Blocks.ANVIL)
                && ServerConfig.ENABLE_ANVIL_TO_SMITHING.get()) {
            BlockState newState = ModBlocks.SMITHING_ANVIL.get()
                    .defaultBlockState()
                    .setValue(StoneSmithingAnvil.FACING, player.getDirection().getClockWise());
            level.setBlock(pos, newState, 3);
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

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
                ModMessages.sendToAll(new MinigameSyncS2CPacket(sync));
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

                    Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                    recipeOpt.ifPresent(recipe -> {
                        //ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
                        //int progress = anvilBE.getRequiredProgress();
                        //ModMessages.sendToServer(new StartMinigameC2SPacket(result, progress, pos));
                        if (AnvilMinigameEvents.minigameStarted) {
                            boolean isVisible = AnvilMinigameEvents.isIsVisible();
                            AnvilMinigameEvents.setIsVisible(pos, !isVisible);
                            ModMessages.sendToServer(new SetMinigameVisibleC2SPacket(pos, !isVisible));
                            playerMinigameVisibility.put(player.getUUID(), !isVisible);
                        } else {
                            AnvilMinigameEvents.reset();
                            playerAnvilPositions.put(player.getUUID(), pos);
                            playerMinigameVisibility.put(player.getUUID(), true);
                            AnvilMinigameEvents.setMinigameStarted(pos, true);
                            //AnvilMinigameEvents.setIsVisible(pos, true);
                            ModMessages.sendToServer(new MinigameSetStartedC2SPacket(pos));
                            //AnvilMinigameEvents.setMinigameStarted(pos, true);
                            ModMessages.sendToServer(new SetMinigameVisibleC2SPacket(pos, true));
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

        // 1. Clear ownership from the block entity (server-side)
        BlockEntity be = player.level().getBlockEntity(pos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvilBE) {
            anvilBE.clearOwner();
        }

        // 2. Clear server-side tracking - FIXED: use UUID instead of Player object
        if (playerAnvilPositions.get(playerId) != null && playerAnvilPositions.get(playerId).equals(pos)) {
            playerAnvilPositions.remove(playerId);
            playerMinigameVisibility.remove(playerId);
        }
        // 3. Clear client-side state
        ClientAnvilMinigameData.putOccupiedAnvil(pos, null);
        AnvilMinigameEvents.reset();
        // 4. Sync null ownership to all clients
        CompoundTag syncData = new CompoundTag();
        syncData.putLong("anvilPos", pos.asLong());
        syncData.putUUID("anvilOwner", new UUID(0, 0)); // special "no owner" UUID
        ModMessages.sendToAll(new MinigameSyncS2CPacket(syncData));
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
       /* if (!player.isCrouching()) {
            player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                if (minigame.getVisible()) {
                    minigame.setIsVisible(false, (ServerPlayer) player);
                }
            });
        }*/
        //AnvilMinigameEvents.hideMinigame(player.getUUID());
        ModMessages.sendToPlayer(new HideMinigameS2CPacket(), player);
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
                if (state.getFluidState().isSource() && state.getBlock() == Blocks.WATER) {
                    coolIngot(player, stack);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }


        if (!world.isClientSide()) {
            HitResult hit = player.pick(5.0D, 0.0F, false);
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (player.isCrouching() && state.is(Blocks.GRINDSTONE)) {

                if (player.getMainHandItem() != stack) {
                    return;
                }

                if (stack.is(ModTags.Items.GRINDABLE)) {
                    grindItem(player, stack);
                    world.playSound(null, player.blockPosition(),
                            SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS,
                            1.0f, 1.2f); // Higher pitch for polishing sound
                    spawnGrindParticles(world, pos);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                    return;
                }
                if (stack.hasTag() && stack.getTag().contains("Polished") && !stack.getTag().getBoolean("Polished")) {
                    // Only convert 1 item in the stack
                    if (stack.getCount() > 1) {
                        // Split 1 item from the stack
                        ItemStack polishedItem = stack.copy();
                        polishedItem.setCount(1);
                        polishedItem.getOrCreateTag().putBoolean("Polished", true);

                        // Reduce held stack by 1
                        stack.shrink(1);

                        // Try to add the polished item to player's inventory
                        if (!player.getInventory().add(polishedItem)) {
                            // If inventory is full, drop the item in the world
                            player.drop(polishedItem, false);
                        }
                    } else {
                        // Only one item in stack, just polish it directly
                        stack.getOrCreateTag().putBoolean("Polished", true);
                    }

                    world.playSound(null, player.blockPosition(),
                            SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS,
                            1.0f, 1.2f); // Higher pitch for polishing sound
                    spawnGrindParticles(world, pos);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                    return;
                }

                if (stack.isDamageableItem() && stack.getDamageValue() > 0) {
                    if (!ServerConfig.GRINDING_RESTORE_DURABILITY.get()) {
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                        return;
                    }
                    Item item = stack.getItem();
                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);

// Check blacklist
                    if (itemId != null && ServerConfig.GRINDING_BLACKLIST.get().contains(itemId.toString())) {
                        event.setCancellationResult(InteractionResult.PASS); // Allow vanilla behavior, or SUCCESS if you want to eat the event
                        event.setCanceled(true); // Prevent your custom grind logic
                        return;
                    }

                    CompoundTag tag = stack.getOrCreateTag();
                    int reducedCount = tag.getInt("ReducedMaxDurability");

                    // Base vanilla durability
                    int originalDurability = stack.getItem().getMaxDamage();
                    // Config multipliers
                    float baseMultiplier = ServerConfig.BASE_DURABILITY_MULTIPLIER.get().floatValue();
                    float grindReduction = ServerConfig.DURABILITY_REDUCE_PER_GRIND.get().floatValue();

                    // Quality multiplier (if any)
                    float qualityMultiplier = 1.0f;
                    if (tag.contains("ForgingQuality")) {
                        qualityMultiplier = QualityHelper.getQualityMultiplier(stack);
                    }
                    int newOriginalDurability = (int) (originalDurability * baseMultiplier * qualityMultiplier);

                    // Final durability multiplier, clamped to 10% minimum
                    float penaltyMultiplier = Math.max(0.1f, 1.0f - (reducedCount * grindReduction));

                    int effectiveMaxDurability = (int) (newOriginalDurability * penaltyMultiplier);
                    effectiveMaxDurability = Math.max(1, effectiveMaxDurability); // Clamp to avoid zero

                    int currentDamage = stack.getDamageValue();

                    // If already fully repaired relative to reduced max, skip
                    if (currentDamage <= (newOriginalDurability - effectiveMaxDurability)) {
                        tag.putInt("ReducedMaxDurability", reducedCount + 1);
                        stack.setDamageValue(0);
                        event.getLevel().playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                        spawnGrindParticles(world, pos);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                        return;
                    }

                    float restorePercent = ServerConfig.DAMAGE_RESTORE_PER_GRIND.get().floatValue(); // e.g., 0.05F for 5%
                    int theoreticalMaxDurability = (int) (originalDurability * baseMultiplier * qualityMultiplier);
                    int repairAmount = Math.max(1, (int) (theoreticalMaxDurability * restorePercent));

                    // Respect effective max cap
                    int newDamage = Math.max(theoreticalMaxDurability - effectiveMaxDurability, currentDamage - repairAmount);

                    stack.setDamageValue(newDamage);
                    tag.putInt("ReducedMaxDurability", reducedCount + 1);
                    event.getLevel().playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    spawnGrindParticles(world, pos);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }

            }
        }
        if (!world.isClientSide()) {
            ItemStack mainHand = player.getMainHandItem();

            // Only hide if MAIN HAND is not a hammer
            if (!mainHand.is(ModTags.Items.SMITHING_HAMMERS)) {
                hideMinigame((ServerPlayer) player);
            }
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
            // Update water level
            if (waterLevel == 1) {
                level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            } else {
                level.setBlockAndUpdate(pos, state.setValue(levelProperty, waterLevel - 1));
            }
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

            // Cool the ingot
            coolIngot(player, heldStack);
        }
    }

    private static void coolIngot(Player player, ItemStack stack) {
        Item cooled = getCooledIngot(stack.getItem());
        if (cooled == null) return;

        int count = stack.getCount();
        if (count <= 0) return; // Avoid making 0-stack cooled items

        ItemStack newStack = new ItemStack(cooled, count);
        if (stack.hasTag()) {
            newStack.setTag(stack.getTag().copy());
        }

        // Check where the heated stack is located
        boolean isMain = ItemStack.isSameItemSameTags(stack, player.getMainHandItem());
        boolean isOff = ItemStack.isSameItemSameTags(stack, player.getOffhandItem());

        // Set original stack to empty
        stack.setCount(0);

        if (isMain) {
            player.setItemInHand(InteractionHand.MAIN_HAND, newStack);
        } else if (isOff) {
            player.setItemInHand(InteractionHand.OFF_HAND, newStack);
        } else {
            // Try to replace in inventory first
            if (!player.getInventory().add(newStack)) {
                player.drop(newStack, false); // Drop if inventory is full
            }
        }

        player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
    }


    private static void grindItem(Player player, ItemStack heldStack) {
        Item cooledItem = getGrindable(heldStack.getItem());
        if (cooledItem != null) {
            ItemStack cooledIngot = new ItemStack(cooledItem);
            if (heldStack.hasTag()) {
                cooledIngot.setTag(heldStack.getTag().copy());
            }
            cooledIngot.getOrCreateTag().putBoolean("Polished", true);
            heldStack.shrink(1);

            if (heldStack.isEmpty()) {
                player.setItemInHand(player.getUsedItemHand(), cooledIngot);
            } else {
                if (!player.getInventory().add(cooledIngot)) {
                    player.drop(cooledIngot, false);
                }
            }

            if (player != null) {
                player.playSound(SoundEvents.GRINDSTONE_USE, 1.0F, 1.0F);
            }
        }
    }

    private static Item getGrindable(Item heatedItem) {
        var heatedTag = ForgeRegistries.ITEMS.tags().getTag(ModTags.Items.GRINDABLE);
        var cooledTag = ForgeRegistries.ITEMS.tags().getTag(ModTags.Items.GRINDED);

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

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack.is(ModTags.Items.HEATED_METALS)) {
                trackedEntities.add(itemEntity);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;
        if (event.level.getGameTime() % 20 != 0) return; // Run every second

        List<ItemEntity> toRemove = new ArrayList<>();

        for (ItemEntity entity : new ArrayList<>(trackedEntities)) {
            if (!entity.isAlive()) {
                toRemove.add(entity);
                continue;
            }
            BlockState state = event.level.getBlockState(entity.blockPosition());
            if (state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON)) {
                ItemStack stack = entity.getItem();
                Item cooled = getCooledIngot(stack.getItem());

                if (cooled != null && stack.getCount() > 0) {
                    // Cool only 1 item
                    stack.shrink(1);
                    entity.setItem(stack); // update remaining stack

                    // Handle cauldron water level
                    if (state.is(Blocks.WATER_CAULDRON)) {
                        IntegerProperty levelProperty = LayeredCauldronBlock.LEVEL;
                        int waterLevel = state.getValue(levelProperty);

                        if (waterLevel > 0) {
                            if (waterLevel == 1) {
                                event.level.setBlockAndUpdate(entity.blockPosition(), Blocks.CAULDRON.defaultBlockState());
                            } else {
                                event.level.setBlockAndUpdate(entity.blockPosition(), state.setValue(levelProperty, waterLevel - 1));
                            }
                            event.level.gameEvent(entity, GameEvent.BLOCK_CHANGE, entity.blockPosition());
                        }
                    }

                    // Spawn the cooled ingot
                    ItemStack cooledStack = new ItemStack(cooled, 1);
                    ItemEntity cooledEntity = new ItemEntity(
                            event.level, entity.getX(), entity.getY(), entity.getZ(), cooledStack
                    );
                    event.level.addFreshEntity(cooledEntity);

                    entity.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);

                    // Remove if stack is empty
                    if (stack.isEmpty()) {
                        entity.discard();
                        toRemove.add(entity);
                    }
                } else {
                    toRemove.add(entity); // Not a valid heated item
                }
            }
        }
        trackedEntities.removeAll(toRemove);
    }

    private static final Random RANDOM = new Random();

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

        CompoundTag potionTag = otherStack.getOrCreateTag();
        int used = potionTag.getInt("TippedUsed");
        int maxUse = ServerConfig.MAX_POTION_TIPPING_USE.get();
        Potion basePotion = PotionUtils.getPotion(otherStack);

        // Create the appropriate tipped arrow BEFORE consuming
        ItemStack resultArrow;
        if (isVanillaArrow) {
            resultArrow = PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), basePotion);
        } else {
            resultArrow = usedHand.copy();
            resultArrow.setCount(1);

            CompoundTag arrowTag = resultArrow.getOrCreateTag();
            arrowTag.putString("Potion", basePotion.getName(""));

            if (potionTag.contains("CustomPotionEffects", 9)) {
                arrowTag.put("CustomPotionEffects", potionTag.getList("CustomPotionEffects", 10));
            }
            if (potionTag.contains("CustomPotionColor", 3)) {
                arrowTag.putInt("CustomPotionColor", potionTag.getInt("CustomPotionColor"));
            }
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

        // Handle potion stack
        if (otherStack.getCount() > 1) {
            ItemStack onePotion = otherStack.split(1);
            CompoundTag oneTag = onePotion.getOrCreateTag();
            oneTag.putInt("TippedUsed", used + 1);
            PotionUtils.setPotion(onePotion, basePotion);
            player.setItemInHand(otherHand, otherStack);
        } else {
            used++;
            if (used >= maxUse) {
                player.setItemInHand(otherHand, new ItemStack(Items.GLASS_BOTTLE));
            } else {
                potionTag.putInt("TippedUsed", used);
                PotionUtils.setPotion(otherStack, basePotion);
                player.setItemInHand(otherHand, otherStack);
            }
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
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();

        // Only respond to main-hand to avoid double-open with offhand
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.FLETCHING_TABLE)) return;

        // Optional: sneak to bypass opening (let players interact as normal)
        // if (player.isShiftKeyDown()) return;

        // Client: mark success so the hand anim plays; open screen ONLY on server
        if (level.isClientSide) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        // Server: open our custom menu via a SimpleMenuProvider (vanilla table has no BE)
        SimpleMenuProvider provider = new SimpleMenuProvider(
                (windowId, playerInv, p) ->
                        new FletchingStationMenu(windowId, playerInv, ContainerLevelAccess.create(level, pos)),
                Component.translatable("container.overgeared.fletching_table")
        );

        NetworkHooks.openScreen((ServerPlayer) player, provider, pos);
        event.setCancellationResult(InteractionResult.sidedSuccess(false));
        event.setCanceled(true);
    }
}
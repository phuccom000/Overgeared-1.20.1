package net.stirdrem.overgeared.event;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;
import net.stirdrem.overgeared.networking.packet.StartMinigameC2SPacket;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.QualityHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.stirdrem.overgeared.OvergearedMod.getCooledIngot;


@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModItemInteractEvents {
    private static final Set<ItemEntity> trackedEntities = ConcurrentHashMap.newKeySet();

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

        BlockEntity be = level.getBlockEntity(pos);
        BlockState clickedState = level.getBlockState(pos);

        // Shift-right-click to convert stone into smithing anvil
        if (!level.isClientSide && player.isCrouching() && clickedState.is(Blocks.STONE)) {
            BlockState newState = ModBlocks.STONE_SMITHING_ANVIL.get()
                    .defaultBlockState()
                    .setValue(StoneSmithingAnvil.FACING, player.getDirection().getClockWise());
            level.setBlock(pos, newState, 3);
            level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (!level.isClientSide()) {
            if (!(be instanceof AbstractSmithingAnvilBlockEntity)) {
                hideMinigame(player);
            }
        }

        if (!(be instanceof AbstractSmithingAnvilBlockEntity anvilBE)) return;
        UUID playerUUID = player.getUUID();

        if (!player.isCrouching()) return;

        if (!level.isClientSide) {
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            // Server-side ownership logic
            if (!anvilBE.hasRecipe()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.no_recipe", true));
                return;
            }

            if (!anvilBE.hasQuality()) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.item_has_no_quality", true));
                return;
            }

            UUID currentOwner = anvilBE.getOwnerUUID();
            if (currentOwner != null && !currentOwner.equals(playerUUID)) {
                serverPlayer.sendSystemMessage(Component.translatable("message.overgeared.anvil_in_use_by_another", true));
                return;
            }

            if (currentOwner == null) {
                anvilBE.setOwner(playerUUID);
                CompoundTag sync = new CompoundTag();
                sync.putUUID("anvilOwner", playerUUID);
                sync.putLong("anvilPos", pos.asLong());
                ModMessages.sendToAll(new MinigameSyncS2CPacket(sync));
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

            if (!anvilBE.hasQuality()) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.item_has_no_quality").withStyle(ChatFormatting.RED));
                return;
            }

            // Client should trust the server's sync data in ClientAnvilMinigameData
            UUID currentOwner = ClientAnvilMinigameData.getOccupiedAnvil(pos);
            if (currentOwner != null && !currentOwner.equals(player.getUUID())) {
                //player.sendSystemMessage(Component.translatable("message.overgeared.anvil_in_use_by_another").withStyle(ChatFormatting.RED));
                return;
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

            if (player.getUUID().equals(currentOwner)
                    && ClientAnvilMinigameData.getPendingMinigamePos() == null) {
                if (anvilBE.hasRecipe()) {
                    Optional<ForgingRecipe> recipeOpt = anvilBE.getCurrentRecipe();
                    recipeOpt.ifPresent(recipe -> {
                        ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
                        int progress = anvilBE.getRequiredProgress();
                        ModMessages.sendToServer(new StartMinigameC2SPacket(result, progress, pos));
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
        if (be instanceof AbstractSmithingAnvilBlockEntity anvilBE) {
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

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        if (world.isClientSide()) return;

        hideMinigame(player);
    }

    private static void hideMinigame(Player player) {
        if (!player.isCrouching()) {
            player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                if (minigame.getVisible()) {
                    minigame.setIsVisible(false, (ServerPlayer) player);
                }
            });
        }
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
            if (!event.isCanceled() || event.getCancellationResult() == InteractionResult.PASS) {
                hideMinigame(player); // Any general item use
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

    private static void coolIngot(Player player, ItemStack heldStack) {
        Item cooledItem = getCooledIngot(heldStack.getItem());
        if (cooledItem != null) {
            ItemStack cooledIngot = new ItemStack(cooledItem);
            heldStack.shrink(1);

            if (heldStack.isEmpty()) {
                player.setItemInHand(player.getUsedItemHand(), cooledIngot);
            } else {
                if (!player.getInventory().add(cooledIngot)) {
                    player.drop(cooledIngot, false);
                }
            }

            if (player != null) {
                player.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
            }
        }
    }

    private static void grindItem(Player player, ItemStack heldStack) {
        Item cooledItem = getGrindable(heldStack.getItem());
        if (cooledItem != null) {
            ItemStack cooledIngot = new ItemStack(cooledItem);
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
        if (state.is(Blocks.STONE) && heldItem.is(Items.FLINT)) {

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
            hideMinigame(player);
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            hideMinigame(player);
        }
    }

}
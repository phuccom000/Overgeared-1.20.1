package net.stirdrem.overgeared.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
//import net.stirdrem.overgeared.block.custom.LayeredWaterBarrel;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.QualityHelper;

import java.util.*;


@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModItemInteractEvents {
    private static final Set<ItemEntity> trackedEntities = Collections.newSetFromMap(new WeakHashMap<>());

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
        // Handle water barrel interaction
       /* else if (state.is(ModBlocks.WATER_BARREL_FULL.get())) {
            handleWaterBarrelInteraction(level, pos, player, heldStack, state);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }*/
        // 🔹 Shift-right-clicking a grindstone with a grindable item
        /*if (player.isCrouching() && state.is(Blocks.GRINDSTONE)) {
            if (heldStack.isDamageableItem() && heldStack.getDamageValue() > 0) {
                CompoundTag tag = heldStack.getOrCreateTag();
                int reducedCount = tag.getInt("ReducedMaxDurability");
                tag.putInt("ReducedMaxDurability", reducedCount + 1);

                int maxDamage = heldStack.getMaxDamage();
                int repairAmount = Math.max(1, (int) (maxDamage * 0.05F)); // 5% repair
                heldStack.setDamageValue(Math.max(0, heldStack.getDamageValue() - repairAmount));

                // Optional: play repair sound or particle
                level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            } else grindItem(player, heldStack);

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }*/

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

    /*private static void handleWaterBarrelInteraction(Level level, BlockPos pos, Player player,
                                                     ItemStack heldStack, BlockState state) {
        IntegerProperty levelProperty = LayeredWaterBarrel.LEVEL;
        int waterLevel = state.getValue(levelProperty);

        if (waterLevel > 0) {
            // Update water level
            if (waterLevel == 1) {
                level.setBlockAndUpdate(pos, ModBlocks.WATER_BARREL.get().defaultBlockState());
            } else {
                level.setBlockAndUpdate(pos, state.setValue(levelProperty, waterLevel - 1));
            }
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

            // Cool the ingot
            coolIngot(player, heldStack);
        }
    }*/

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

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack.is(ModTags.Items.HEATED_METALS)) {
                trackedEntities.add(itemEntity);
            }
        }
    }

    private static Item getCooledIngot(Item heatedItem) {
        var heatedTag = ForgeRegistries.ITEMS.tags().getTag(ModTags.Items.HEATED_METALS);
        var cooledTag = ForgeRegistries.ITEMS.tags().getTag(ModTags.Items.HEATABLE_METALS);

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
    public static void onItemDrop(ItemTossEvent event) {
        ItemEntity entity = event.getEntity();
        ItemStack stack = entity.getItem();
        if (stack.is(ModTags.Items.HEATED_METALS)) {
            trackedEntities.add(entity);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;
        if (event.level.getGameTime() % 20 != 0) return; // run every second

        List<ItemEntity> snapshot = new ArrayList<>(trackedEntities);
        for (ItemEntity entity : snapshot) {
            if (!entity.isAlive()) {
                trackedEntities.remove(entity);
                continue;
            }

            BlockState state = event.level.getBlockState(entity.blockPosition());
            if (state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON)) {
                ItemStack stack = entity.getItem();
                Item cooled = getCooledIngot(stack.getItem());
                if (cooled != null) {
                    int count = stack.getCount();
                    stack.shrink(count); // Remove the whole stack
                    double dx = entity.getX(), dy = entity.getY(), dz = entity.getZ();

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

                    ItemStack cooledStack = new ItemStack(cooled, count);
                    ItemEntity cooledEntity = new ItemEntity(event.level, dx, dy, dz, cooledStack);
                    event.level.addFreshEntity(cooledEntity);

                    entity.playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);

                    trackedEntities.remove(entity); // now safe
                } else {
                    trackedEntities.remove(entity); // invalid item, stop tracking
                }
            }
        }
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

}
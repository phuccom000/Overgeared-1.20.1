package net.stirdrem.overgeared.core.waterbarrel;/*
package net.stirdrem.overgeared.core.waterbarrel;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.stirdrem.overgeared.block.custom.LayeredWaterBarrel;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;

public interface BarrelInteraction {
    Map<Item, BarrelInteraction> EMPTY = newInteractionMap();
    Map<Item, BarrelInteraction> WATER = newInteractionMap();
    Map<Item, BarrelInteraction> LAVA = newInteractionMap();
    Map<Item, BarrelInteraction> POWDER_SNOW = newInteractionMap();

    BarrelInteraction FILL_WATER = (p_175683_, p_175684_, p_175685_, p_175686_, p_175687_, p_175688_) -> {
        return emptyBucket(p_175684_, p_175685_, p_175686_, p_175687_, p_175688_, ModBlocks.WATER_BARREL_FULL.get().defaultBlockState().setValue(LayeredWaterBarrel.LEVEL, Integer.valueOf(2)), SoundEvents.BUCKET_EMPTY);
    };

    BarrelInteraction SHULKER_BOX = (p_175662_, p_175663_, p_175664_, p_175665_, p_175666_, p_175667_) -> {
        Block block = Block.byItem(p_175667_.getItem());
        if (!(block instanceof ShulkerBoxBlock)) {
            return InteractionResult.PASS;
        } else {
            if (!p_175663_.isClientSide) {
                ItemStack itemstack = new ItemStack(Blocks.SHULKER_BOX);
                if (p_175667_.hasTag()) {
                    itemstack.setTag(p_175667_.getTag().copy());
                }

                p_175665_.setItemInHand(p_175666_, itemstack);
                p_175665_.awardStat(Stats.CLEAN_SHULKER_BOX);
                LayeredWaterBarrel.lowerFillLevel(p_175662_, p_175663_, p_175664_);
            }

            return InteractionResult.sidedSuccess(p_175663_.isClientSide);
        }
    };

    BarrelInteraction BANNER = (p_278890_, p_278891_, p_278892_, p_278893_, p_278894_, p_278895_) -> {
        if (BannerBlockEntity.getPatternCount(p_278895_) <= 0) {
            return InteractionResult.PASS;
        } else {
            if (!p_278891_.isClientSide) {
                ItemStack itemstack = p_278895_.copyWithCount(1);
                BannerBlockEntity.removeLastPattern(itemstack);
                if (!p_278893_.getAbilities().instabuild) {
                    p_278895_.shrink(1);
                }

                if (p_278895_.isEmpty()) {
                    p_278893_.setItemInHand(p_278894_, itemstack);
                } else if (p_278893_.getInventory().add(itemstack)) {
                    p_278893_.inventoryMenu.sendAllDataToRemote();
                } else {
                    p_278893_.drop(itemstack, false);
                }

                p_278893_.awardStat(Stats.CLEAN_BANNER);
                LayeredWaterBarrel.lowerFillLevel(p_278890_, p_278891_, p_278892_);
            }

            return InteractionResult.sidedSuccess(p_278891_.isClientSide);
        }
    };

    BarrelInteraction DYED_ITEM = (p_175629_, p_175630_, p_175631_, p_175632_, p_175633_, p_175634_) -> {
        Item item = p_175634_.getItem();
        if (!(item instanceof DyeableLeatherItem dyeableleatheritem)) {
            return InteractionResult.PASS;
        } else if (!dyeableleatheritem.hasCustomColor(p_175634_)) {
            return InteractionResult.PASS;
        } else {
            if (!p_175630_.isClientSide) {
                dyeableleatheritem.clearColor(p_175634_);
                p_175632_.awardStat(Stats.CLEAN_ARMOR);
                LayeredWaterBarrel.lowerFillLevel(p_175629_, p_175630_, p_175631_);
            }

            return InteractionResult.sidedSuccess(p_175630_.isClientSide);
        }
    };

    BarrelInteraction FLUID_ITEM = (blockState, level, pos, player, hand, stack) -> {
        LazyOptional<IFluidHandlerItem> fluidHandlerLazy = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        if (!fluidHandlerLazy.isPresent()) return InteractionResult.PASS;
        IFluidHandlerItem fluidHandlerItem = fluidHandlerLazy.orElse(null);

        // Check if the wooden bucket can accept water
        int simulatedFill = fluidHandlerItem.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.SIMULATE);
        if (simulatedFill > 0 && level.getBlockState(pos) == ModBlocks.WATER_BARREL_FULL.get().defaultBlockState().setValue(LayeredWaterBarrel.LEVEL, 2)) {
            // Actually fill the bucket
            int filled = fluidHandlerItem.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                // Reduce water level in barrel
                //LayeredWaterBarrel.emptyBarrel(blockState, level, pos);
                level.setBlockAndUpdate(pos, ModBlocks.WATER_BARREL.get().defaultBlockState());
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.setItemInHand(hand, fluidHandlerItem.getContainer());
                player.awardStat(Stats.USE_CAULDRON);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
        } else if (level.getBlockState(pos) != ModBlocks.WATER_BARREL_FULL.get().defaultBlockState().setValue(LayeredWaterBarrel.LEVEL, 2)) {
            FluidStack drained = fluidHandlerItem.drain(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                // Update barrel state
                level.setBlockAndUpdate(pos, ModBlocks.WATER_BARREL_FULL.get().defaultBlockState().setValue(LayeredWaterBarrel.LEVEL, 2));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.setItemInHand(hand, fluidHandlerItem.getContainer());
                player.awardStat(Stats.FILL_CAULDRON);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    };

    static Object2ObjectOpenHashMap<Item, BarrelInteraction> newInteractionMap() {
        return Util.make(new Object2ObjectOpenHashMap<>(), (p_175646_) -> {
            p_175646_.defaultReturnValue((p_175739_, p_175740_, p_175741_, p_175742_, p_175743_, p_175744_) -> {
                return InteractionResult.PASS;
            });
        });
    }

    InteractionResult interact(BlockState pBlockState, Level pLevel, BlockPos pBlockPos, Player pPlayer, InteractionHand pHand, ItemStack pStack);

    static void bootStrap() {
        addDefaultInteractions(EMPTY);
        EMPTY.put(Items.POTION, (blockState, level, blockPos, player, hand, stack) -> {
            if (PotionUtils.getPotion(stack) != Potions.WATER) {
                return InteractionResult.PASS;
            } else {
                if (!level.isClientSide) {
                    Item item = stack.getItem();
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(item));
                    level.setBlockAndUpdate(blockPos, ModBlocks.WATER_BARREL_FULL.get().defaultBlockState());
                    level.playSound((Player) null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.gameEvent((Entity) null, GameEvent.FLUID_PLACE, blockPos);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        });

        addDefaultInteractions(WATER);
        WATER.put(Items.BUCKET, (p_175725_, p_175726_, p_175727_, p_175728_, p_175729_, p_175730_) -> {
            return fillBucket(p_175725_, p_175726_, p_175727_, p_175728_, p_175729_, p_175730_, new ItemStack(Items.WATER_BUCKET), (p_175660_) -> {
                return p_175660_.getValue(LayeredWaterBarrel.LEVEL) == 2;
            }, SoundEvents.BUCKET_FILL);
        });

        WATER.put(Items.GLASS_BOTTLE, (p_175718_, p_175719_, p_175720_, p_175721_, p_175722_, p_175723_) -> {
            if (!p_175719_.isClientSide) {
                Item item = p_175723_.getItem();
                p_175721_.setItemInHand(p_175722_, ItemUtils.createFilledResult(p_175723_, p_175721_, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
                p_175721_.awardStat(Stats.USE_CAULDRON);
                p_175721_.awardStat(Stats.ITEM_USED.get(item));
                LayeredWaterBarrel.lowerFillLevel(p_175718_, p_175719_, p_175720_);
                p_175719_.playSound((Player) null, p_175720_, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                p_175719_.gameEvent((Entity) null, GameEvent.FLUID_PICKUP, p_175720_);
            }

            return InteractionResult.sidedSuccess(p_175719_.isClientSide);
        });

        WATER.put(Items.POTION, (p_175704_, p_175705_, p_175706_, p_175707_, p_175708_, p_175709_) -> {
            if (p_175704_.getValue(LayeredWaterBarrel.LEVEL) != 2 && PotionUtils.getPotion(p_175709_) == Potions.WATER) {
                if (!p_175705_.isClientSide) {
                    p_175707_.setItemInHand(p_175708_, ItemUtils.createFilledResult(p_175709_, p_175707_, new ItemStack(Items.GLASS_BOTTLE)));
                    p_175707_.awardStat(Stats.USE_CAULDRON);
                    p_175707_.awardStat(Stats.ITEM_USED.get(p_175709_.getItem()));
                    p_175705_.setBlockAndUpdate(p_175706_, p_175704_.cycle(LayeredWaterBarrel.LEVEL));
                    p_175705_.playSound((Player) null, p_175706_, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    p_175705_.gameEvent((Entity) null, GameEvent.FLUID_PLACE, p_175706_);
                }

                return InteractionResult.sidedSuccess(p_175705_.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        });

        // Add all the dyeable items and banners
        WATER.put(Items.LEATHER_BOOTS, DYED_ITEM);
        WATER.put(Items.LEATHER_LEGGINGS, DYED_ITEM);
        WATER.put(Items.LEATHER_CHESTPLATE, DYED_ITEM);
        WATER.put(Items.LEATHER_HELMET, DYED_ITEM);
        WATER.put(Items.LEATHER_HORSE_ARMOR, DYED_ITEM);
        WATER.put(Items.WHITE_BANNER, BANNER);
        WATER.put(Items.GRAY_BANNER, BANNER);
        WATER.put(Items.BLACK_BANNER, BANNER);
        WATER.put(Items.BLUE_BANNER, BANNER);
        WATER.put(Items.BROWN_BANNER, BANNER);
        WATER.put(Items.CYAN_BANNER, BANNER);
        WATER.put(Items.GREEN_BANNER, BANNER);
        WATER.put(Items.LIGHT_BLUE_BANNER, BANNER);
        WATER.put(Items.LIGHT_GRAY_BANNER, BANNER);
        WATER.put(Items.LIME_BANNER, BANNER);
        WATER.put(Items.MAGENTA_BANNER, BANNER);
        WATER.put(Items.ORANGE_BANNER, BANNER);
        WATER.put(Items.PINK_BANNER, BANNER);
        WATER.put(Items.PURPLE_BANNER, BANNER);
        WATER.put(Items.RED_BANNER, BANNER);
        WATER.put(Items.YELLOW_BANNER, BANNER);
        WATER.put(Items.WHITE_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.GRAY_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.BLACK_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.BLUE_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.BROWN_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.CYAN_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.GREEN_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.LIGHT_BLUE_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.LIGHT_GRAY_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.LIME_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.MAGENTA_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.ORANGE_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.PINK_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.PURPLE_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.RED_SHULKER_BOX, SHULKER_BOX);
        WATER.put(Items.YELLOW_SHULKER_BOX, SHULKER_BOX);

        // Wooden Bucket Interactions
        WATER.put(ModItems.WOODEN_BUCKET.get(), FLUID_ITEM);

        EMPTY.put(ModItems.WOODEN_BUCKET.get(), FLUID_ITEM);

        addDefaultInteractions(LAVA);
        addDefaultInteractions(POWDER_SNOW);
    }

    static void addDefaultInteractions(Map<Item, BarrelInteraction> pInteractionsMap) {
        pInteractionsMap.put(Items.WATER_BUCKET, FILL_WATER);
    }

    static InteractionResult fillBucket(BlockState pBlockState, Level pLevel, BlockPos pPos, Player
            pPlayer, InteractionHand pHand, ItemStack pEmptyStack, ItemStack
                                                pFilledStack, Predicate<BlockState> pStatePredicate, SoundEvent pFillSound) {
        if (!pStatePredicate.test(pBlockState)) {
            return InteractionResult.PASS;
        } else {
            if (!pLevel.isClientSide) {
                Item item = pEmptyStack.getItem();
                pPlayer.setItemInHand(pHand, ItemUtils.createFilledResult(pEmptyStack, pPlayer, pFilledStack));
                pPlayer.awardStat(Stats.USE_CAULDRON);
                pPlayer.awardStat(Stats.ITEM_USED.get(item));
                pLevel.setBlockAndUpdate(pPos, ModBlocks.WATER_BARREL.get().defaultBlockState());
                pLevel.playSound((Player) null, pPos, pFillSound, SoundSource.BLOCKS, 1.0F, 1.0F);
                pLevel.gameEvent((Entity) null, GameEvent.FLUID_PICKUP, pPos);
            }

            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
    }

    static InteractionResult emptyBucket(Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand
            pHand, ItemStack pFilledStack, BlockState pState, SoundEvent pEmptySound) {
        if (!pLevel.isClientSide) {
            Item item = pFilledStack.getItem();
            pPlayer.setItemInHand(pHand, ItemUtils.createFilledResult(pFilledStack, pPlayer, new ItemStack(Items.BUCKET)));
            pPlayer.awardStat(Stats.FILL_CAULDRON);
            pPlayer.awardStat(Stats.ITEM_USED.get(item));
            pLevel.setBlockAndUpdate(pPos, pState);
            pLevel.playSound((Player) null, pPos, pEmptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
            pLevel.gameEvent((Entity) null, GameEvent.FLUID_PLACE, pPos);
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }
}*/

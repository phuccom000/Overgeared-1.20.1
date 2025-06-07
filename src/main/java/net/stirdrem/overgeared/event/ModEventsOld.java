/*
package net.stirdrem.overgeared.event;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.custom.SmithingHammer;
import net.stirdrem.overgeared.minigame.AnvilMinigame;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;
import net.stirdrem.overgeared.util.ModTags;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModEventsOld {


    */
/*@SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) return;

        Level level = event.level;

        // Define the area to search for ItemEntities
        AABB searchArea = new AABB(
                level.getWorldBorder().getMinX(), level.getMinBuildHeight(), level.getWorldBorder().getMinZ(),
                level.getWorldBorder().getMaxX(), level.getMaxBuildHeight(), level.getWorldBorder().getMaxZ()
        );

        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, searchArea);

        for (ItemEntity itemEntity : itemEntities) {
            BlockPos pos = itemEntity.blockPosition();
            BlockState state = level.getBlockState(pos);
            ItemStack itemStack = itemEntity.getItem();

            if (!itemStack.is(ModTags.Items.HEATED_METALS)) return;

            // Check if the item is in water or in a water cauldron
            boolean inWater = itemEntity.isInWater();
            boolean inWaterCauldron = state.is(Blocks.WATER_CAULDRON) && state.getValue(LayeredCauldronBlock.LEVEL) > 0;

            if (!inWater && !inWaterCauldron) continue;

            Item cooledItem = getCooledIngot(itemStack.getItem());
            if (cooledItem == null) continue;

            // Create cooled item stack
            ItemStack cooledStack = new ItemStack(cooledItem);

            // Handle item stack reduction
            itemStack.shrink(1);
            if (itemStack.isEmpty()) {
                itemEntity.discard();
            }

            // Spawn new item with proper motion
            Block.popResource(level, pos, cooledStack); // spawn cooled ingot

            // Play sound
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);

            // If in water cauldron, reduce water level by 1
            if (inWaterCauldron) {
                int currentLevel = state.getValue(LayeredCauldronBlock.LEVEL);
                if (currentLevel == 1) {
                    // Replace Water Cauldron with normal (empty) Cauldron
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                } else {
                    // Decrease water level by 1
                    level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, currentLevel - 1));
                }
            }
        }
    }*//*


    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) return;

        Level level = event.level;

    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
            // Run cleanup every minute (1200 ticks)
            if (event.getServer().getTickCount() % 1200 == 0) {
                SmithingHammer.cleanupStaleAnvils(event.getServer().overworld());
            }
        }
    }

    private static int TICK = 0;
    private static final int perTick = 30;


    @SubscribeEvent//(priority = EventPriority.HIGH)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            Level level = player.level();
            TICK = TICK + 1;
            //player.sendSystemMessage(Component.literal(String.valueOf(TICK)));
            boolean hasHeatedIngot = false;
            // Check inventory
            if (level.isClientSide()) return; // Ensure this runs only on the server side

            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && stack.is(ModTags.Items.HEATED_METALS)) {
                    hasHeatedIngot = true;
                    break;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffhandItem();
            if (!offhandStack.isEmpty() && offhandStack.is(ModTags.Items.HEATED_METALS)) {
                hasHeatedIngot = true;
            }

            // Apply effect if player has heated ingot
            if (hasHeatedIngot) {
                boolean hasTongs = false;
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty() && offhand.is(ModTags.Items.TONGS)) {
                    hasTongs = true;
                    if (level.getGameTime() % 40 == 0)
                        offhandStack.hurtAndBreak(1, event.player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                }
                if (!hasTongs)            //player.sendSystemMessage(Component.literal("Player does not have tongs!"));
                    player.hurt(player.damageSources().hotFloor(), 1.0F); // Apply burn damage
            }

            if (event.side == LogicalSide.SERVER) {
                event.player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                    if (minigame.isVisible()) {
                        float arrowPosition = minigame.getArrowPosition();
                        float arrowSpeed = minigame.getArrowSpeed();
                        if (minigame.getMovingRight()) {
                            minigame.setArrowPosition(arrowPosition + arrowSpeed);
                            ;
                            if (arrowPosition >= 100) {
                                minigame.setArrowPosition(100);
                                minigame.setMovingRight(false);
                            }
                        } else {
                            minigame.setArrowPosition(arrowPosition - arrowSpeed);
                            ;
                            if (arrowPosition <= 0) {
                                minigame.setArrowPosition(0);
                                minigame.setMovingRight(true);
                            }
                        }
                        //event.player.sendSystemMessage(Component.literal("minigamemoving"));
                        ModMessages.sendToPlayer(new MinigameSyncS2CPacket(
                                minigame.isVisible(),
                                minigame.isForging(),
                                minigame.getResultItem(),
                                minigame.getHitsRemaining(),
                                minigame.getArrowPosition(),
                                minigame.getArrowSpeed(),
                                ServerConfig.MAX_SPEED.get().floatValue(),
                                ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue(),
                                minigame.getMovingRight(),
                                minigame.getPerfectHits(),
                                minigame.getGoodHits(),
                                minigame.getMissedHits(),
                                minigame.getPerfectZoneStart(),
                                minigame.getPerfectZoneEnd(),
                                minigame.getGoodZoneStart(),
                                minigame.getGoodZoneEnd(),
                                ServerConfig.ZONE_SHRINK_FACTOR.get().floatValue(),
                                15.0f, // zoneShiftAmount
                                minigame.getAnvilPos()
                        ), (ServerPlayer) event.player);
                    }
                });
            }

           */
/* for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && stack.is(ModTags.Items.HEATABLE_METALS)
                        && stack.hasTag() && stack.getTag().contains("heat")) {
                    // Decrease durability over time
                    //if (level.getGameTime() % 20 == 0) { // Every second
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    //}

                    // Send debug message to the player
                    //int currentDurability = stack.getMaxDamage() - stack.getDamageValue();
                    //player.sendSystemMessage(Component.literal("Item: " + stack.getItem().getDescription().getString() +
                    //", Durability: " + currentDurability + "/" + stack.getMaxDamage()));
                }
            }*//*



 */
/*for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && stack.is(ModTags.Items.HEATED_METALS)) {
                    if (stack.getDamageValue() == stack.getMaxDamage() - 1) {
                        Item cooledItem = getCooledIngot(stack.getItem());
                        if (cooledItem != null) {
                            ItemStack cooledIngot = new ItemStack(cooledItem);
                            stack.shrink(1);
                            if (stack.isEmpty()) {
                                player.setItemInHand(player.getUsedItemHand(), cooledIngot);
                                level.playSound(null, player.getOnPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                            } else {
                                if (!player.getInventory().add(cooledIngot)) {
                                    player.drop(cooledIngot, false);
                                }
                            }
                        }
                    } else {
                        if (level.getGameTime() % 20 == 0)
                            stack.hurtAndBreak(1, event.player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    }
                    break;
                }
            }*//*

        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            //AnvilMinigame.tick();
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).isPresent()) {
                event.addCapability(ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "properties"), new AnvilMinigameProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(oldStore -> {
                event.getOriginal().getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                    ModMessages.sendToPlayer(new MinigameSyncS2CPacket(
                            minigame.isVisible(),
                            minigame.isForging(),
                            minigame.getResultItem(),
                            minigame.getHitsRemaining(),
                            minigame.getArrowPosition(),
                            minigame.getArrowSpeed(),
                            ServerConfig.MAX_SPEED.get().floatValue(),
                            ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue(),
                            minigame.getMovingRight(),
                            minigame.getPerfectHits(),
                            minigame.getGoodHits(),
                            minigame.getMissedHits(),
                            minigame.getPerfectZoneStart(),
                            minigame.getPerfectZoneEnd(),
                            minigame.getGoodZoneStart(),
                            minigame.getGoodZoneEnd(),
                            ServerConfig.ZONE_SHRINK_FACTOR.get().floatValue(),
                            15.0f, // zoneShiftAmount
                            minigame.getAnvilPos()
                    ), player);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(AnvilMinigame.class);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            String quality = stack.getTag().getString("ForgingQuality");
            Item item = stack.getItem();

            if (isWeapon(item)) {
                double damageBonus = getDamageBonusForQuality(quality);
                double speedBonus = getSpeedBonusForQuality(quality);
                modifyWeaponAttributes(event, damageBonus, speedBonus);
            }
            if (isArmor(item)) {
                double armorBonus = getArmorBonusForQuality(quality);
                modifyArmorAttribute(event, armorBonus);
            }
        }
    }

    private static boolean isWeapon(Item item) {
        return item instanceof SwordItem ||
                item instanceof DiggerItem ||
                item instanceof ProjectileWeaponItem;
    }

    private static boolean isArmor(Item item) {
        return item instanceof ArmorItem;
    }

    private static void modifyWeaponAttributes(ItemAttributeModifierEvent event, double damageBonus, double speedBonus) {
        Multimap<Attribute, AttributeModifier> originalModifiers = LinkedHashMultimap.create();
        originalModifiers.putAll(event.getOriginalModifiers());

        // Process attack damage modifiers
        for (AttributeModifier modifier : originalModifiers.get(Attributes.ATTACK_DAMAGE)) {
            event.removeModifier(Attributes.ATTACK_DAMAGE, modifier);
            AttributeModifier newModifier = new AttributeModifier(
                    modifier.getId(),
                    modifier.getName() + "_forged",
                    modifier.getAmount() + damageBonus,
                    modifier.getOperation()
            );
            event.addModifier(Attributes.ATTACK_DAMAGE, newModifier);
        }

        // Process attack speed modifiers
        for (AttributeModifier modifier : originalModifiers.get(Attributes.ATTACK_SPEED)) {
            event.removeModifier(Attributes.ATTACK_SPEED, modifier);
            AttributeModifier newModifier = new AttributeModifier(
                    modifier.getId(),
                    modifier.getName() + "_forged",
                    modifier.getAmount() + speedBonus,
                    modifier.getOperation()
            );
            event.addModifier(Attributes.ATTACK_SPEED, newModifier);
        }
    }

    private static void modifyArmorAttribute(ItemAttributeModifierEvent event, double damageBonus) {
        // Create a copy of the original modifiers to avoid concurrent modification
        Multimap<Attribute, AttributeModifier> originalModifiers = LinkedHashMultimap.create();
        originalModifiers.putAll(event.getOriginalModifiers());

        for (AttributeModifier modifier : originalModifiers.get(Attributes.ARMOR)) {
            // Remove original modifier
            event.removeModifier(Attributes.ARMOR, modifier);

            // Create new modifier with bonus damage
            AttributeModifier newModifier = new AttributeModifier(
                    modifier.getId(),
                    modifier.getName() + "_forged",
                    modifier.getAmount() + damageBonus,
                    modifier.getOperation()
            );

            // Add the modified version
            event.addModifier(Attributes.ARMOR, newModifier);
        }


    }

    private static double getDamageBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "perfect" -> ServerConfig.PERFECT_WEAPON_DAMAGE.get();
            case "expert" -> ServerConfig.EXPERT_WEAPON_DAMAGE.get();
            case "well" -> ServerConfig.WELL_WEAPON_DAMAGE.get();
            case "poor" -> ServerConfig.POOR_WEAPON_DAMAGE.get();
            default -> 0.0;
        };
    }

    private static double getSpeedBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "perfect" -> ServerConfig.PERFECT_WEAPON_SPEED.get();
            case "expert" -> ServerConfig.EXPERT_WEAPON_SPEED.get();
            case "well" -> ServerConfig.WELL_WEAPON_SPEED.get();
            case "poor" -> ServerConfig.POOR_WEAPON_SPEED.get();
            default -> 0.0;
        };
    }

    private static double getArmorBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "perfect" -> ServerConfig.PERFECT_ARMOR_BONUS.get();
            case "expert" -> ServerConfig.EXPERT_ARMOR_BONUS.get();
            case "well" -> ServerConfig.WELL_ARMOR_BONUS.get();
            case "poor" -> ServerConfig.POOR_ARMOR_BONUS.get();
            default -> 0.0;
        };
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Free all anvils this player was using
            SmithingHammer.releaseAnvilsForPlayer(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() == ModBlocks.SMITHING_ANVIL.get()) {
            SmithingHammer.releaseAnvil(event.getPos());
        }
    }
}
*/

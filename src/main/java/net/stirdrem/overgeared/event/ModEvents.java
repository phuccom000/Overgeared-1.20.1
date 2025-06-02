package net.stirdrem.overgeared.event;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.heat.HeatCapability;
import net.stirdrem.overgeared.heat.HeatCapabilityProvider;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.HeatableItem;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModEvents {


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
    }*/

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) return;

        Level level = event.level;

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
            }*/


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
            }*/
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            AnvilMinigameOverlay.tick();
        }
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

}

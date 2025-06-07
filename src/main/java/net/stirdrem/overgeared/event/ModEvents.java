package net.stirdrem.overgeared.event;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.minigame.AnvilMinigame;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.custom.SmithingHammer;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;
import net.stirdrem.overgeared.util.ModTags;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModEvents {
    private static final int ANVIL_CLEANUP_INTERVAL = 1200; // 1 minute (60 seconds * 20 ticks)
    private static final int HEATED_ITEM_CHECK_INTERVAL = 20; // 1 second
    private static final float BURN_DAMAGE = 1.0f;
    private static final float ZONE_SHIFT_AMOUNT = 15.0f;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
            // Run cleanup at regular intervals
            if (event.getServer().getTickCount() % ANVIL_CLEANUP_INTERVAL == 0) {
                SmithingHammer.cleanupStaleAnvils(event.getServer().overworld());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            Level level = player.level();

            if (level.isClientSide()) return;

            handleHeatedItems(player, level);
            handleAnvilMinigameSync(event, player);
        }
    }

    private static void handleHeatedItems(Player player, Level level) {
        boolean hasHeatedIngot = player.getInventory().items.stream()
                .anyMatch(stack -> !stack.isEmpty() && stack.is(ModTags.Items.HEATED_METALS))
                || (!player.getOffhandItem().isEmpty() && player.getOffhandItem().is(ModTags.Items.HEATED_METALS));

        if (hasHeatedIngot) {
            boolean hasTongs = !player.getOffhandItem().isEmpty() &&
                    player.getOffhandItem().is(ModTags.Items.TONGS);

            if (hasTongs && level.getGameTime() % HEATED_ITEM_CHECK_INTERVAL == 0) {
                player.getOffhandItem().hurtAndBreak(1, player,
                        p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else if (!hasTongs) {
                player.hurt(player.damageSources().hotFloor(), BURN_DAMAGE);
            }
        }
    }

    private static void handleAnvilMinigameSync(TickEvent.PlayerTickEvent event, Player player) {
        if (event.side == LogicalSide.SERVER) {
            player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                if (minigame.isVisible()) {
                    updateArrowPosition(minigame);
                    syncMinigameData(minigame, (ServerPlayer) player);
                }
            });
        }
    }

    private static void updateArrowPosition(AnvilMinigame minigame) {
        float arrowPosition = minigame.getArrowPosition();
        float arrowSpeed = minigame.getArrowSpeed();
        boolean movingRight = minigame.getMovingRight();

        if (movingRight) {
            minigame.setArrowPosition(Math.min(100, arrowPosition + arrowSpeed));
            if (arrowPosition >= 100) {
                minigame.setMovingRight(false);
            }
        } else {
            minigame.setArrowPosition(Math.max(0, arrowPosition - arrowSpeed));
            if (arrowPosition <= 0) {
                minigame.setMovingRight(true);
            }
        }
    }

    private static void syncMinigameData(AnvilMinigame minigame, ServerPlayer player) {
        try {
            // Create a CompoundTag to hold all minigame data
            CompoundTag minigameData = new CompoundTag();
            minigame.saveNBTData(minigameData); // Use your existing NBT serialization

            // Add server config values that the client needs
            minigameData.putFloat("maxArrowSpeed", ServerConfig.MAX_SPEED.get().floatValue());
            minigameData.putFloat("speedIncreasePerHit", ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue());
            minigameData.putFloat("zoneShrinkFactor", ServerConfig.ZONE_SHRINK_FACTOR.get().floatValue());

            // Send the packet
            ModMessages.sendToPlayer(new MinigameSyncS2CPacket(minigameData), player);

            // Optional debug logging
            OvergearedMod.LOGGER.debug("Sent minigame sync packet to player {}", player.getName().getString());
        } catch (Exception e) {
            OvergearedMod.LOGGER.error("Failed to sync minigame data to player {}", player.getName().getString(), e);
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player &&
                !event.getObject().getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).isPresent()) {
            event.addCapability(
                    ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "properties"),
                    new AnvilMinigameProvider()
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(AnvilMinigameProvider.ANVIL_MINIGAME)
                    .ifPresent(oldStore -> {
                        event.getEntity().getCapability(AnvilMinigameProvider.ANVIL_MINIGAME)
                                .ifPresent(newStore -> newStore.copyFrom(oldStore));
                    });
        }
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME)
                    .ifPresent(minigame -> syncMinigameData(minigame, player));
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
                applyWeaponAttributes(event, quality);
            } else if (isArmor(item)) {
                applyArmorAttributes(event, quality);
            }
        }
    }

    private static void applyWeaponAttributes(ItemAttributeModifierEvent event, String quality) {
        double damageBonus = getDamageBonusForQuality(quality);
        double speedBonus = getSpeedBonusForQuality(quality);
        modifyAttribute(event, Attributes.ATTACK_DAMAGE, damageBonus);
        modifyAttribute(event, Attributes.ATTACK_SPEED, speedBonus);
    }

    private static void applyArmorAttributes(ItemAttributeModifierEvent event, String quality) {
        double armorBonus = getArmorBonusForQuality(quality);
        modifyAttribute(event, Attributes.ARMOR, armorBonus);
    }

    private static void modifyAttribute(ItemAttributeModifierEvent event, Attribute attribute, double bonus) {
        Multimap<Attribute, AttributeModifier> originalModifiers = LinkedHashMultimap.create();
        originalModifiers.putAll(event.getOriginalModifiers());

        originalModifiers.get(attribute).forEach(modifier -> {
            event.removeModifier(attribute, modifier);
            event.addModifier(attribute, createModifiedAttribute(modifier, bonus));
        });
    }

    private static AttributeModifier createModifiedAttribute(AttributeModifier original, double bonus) {
        return new AttributeModifier(
                original.getId(),
                original.getName() + "_forged",
                original.getAmount() + bonus,
                original.getOperation()
        );
    }

    private static boolean isWeapon(Item item) {
        return item instanceof SwordItem ||
                item instanceof DiggerItem ||
                item instanceof ProjectileWeaponItem;
    }

    private static boolean isArmor(Item item) {
        return item instanceof ArmorItem;
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
            player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                if (minigame.hasAnvilPosition()) {
                    BlockPos anvilPos = minigame.getAnvilPosition();
                    SmithingHammer.releaseAnvil(anvilPos);
                }
            });
            ClientAnvilMinigameData.clearData(player.getUUID());
        }
    }


    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() == ModBlocks.SMITHING_ANVIL.get()) {
            SmithingHammer.releaseAnvil(event.getPos());
        }
    }
}
package net.stirdrem.overgeared.event;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvilNew;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.minigame.AnvilMinigame;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;
import net.stirdrem.overgeared.networking.packet.OnlyResetMinigameS2CPacket;
import net.stirdrem.overgeared.networking.packet.ResetMinigameS2CPacket;
import net.stirdrem.overgeared.util.ModTags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModEvents {
    private static final int HEATED_ITEM_CHECK_INTERVAL = 20; // 1 second
    private static final float BURN_DAMAGE = 1.0f;

    //private static final Map<UUID, Integer> playerTimeoutCounters = new HashMap<>();


    private static int serverTick = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        serverTick++;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.side == LogicalSide.CLIENT) return;

        ServerPlayer player = (ServerPlayer) event.player; // Safe cast
        //handleAnvilMinigameSync(event, player);

        // Refresh timeout counter if player is actively in minigame
      /*  if (AnvilMinigameEvents.isIsVisible() && AnvilMinigameEvents.hasAnvilPosition()) {
            playerTimeoutCounters.put(player.getUUID(), ServerConfig.MINIGAME_TIMEOUT_TICKS.get());
        }*/


        if (serverTick % HEATED_ITEM_CHECK_INTERVAL != 0) return;

        Level level = player.level();
        handleAnvilDistance(player, level);

    }

    private static void handleAnvilDistance(ServerPlayer player, Level level) {
        if (AnvilMinigameEvents.hasAnvilPosition(player.getUUID())) {
            BlockPos anvilPos = AnvilMinigameEvents.getAnvilPos(player.getUUID());
            BlockEntity be = level.getBlockEntity(anvilPos);
            if ((be instanceof AbstractSmithingAnvilBlockEntity)) {
                double distSq = player.blockPosition().distSqr(anvilPos);
                int maxDist = ServerConfig.MAX_ANVIL_DISTANCE.get(); // e.g. 7
                if (distSq > maxDist * maxDist) {
                    resetMinigameForPlayer(player);
                }
            }
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
            minigameData.putFloat("maxArrowSpeed", ServerConfig.MAX_ARROW_SPEED.get().floatValue());
            minigameData.putFloat("speedIncreasePerHit", ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue());
            minigameData.putFloat("zoneShrinkFactor", ServerConfig.ZONE_SHRINK_FACTOR.get().floatValue());

            // Send the packet
            ModMessages.sendToPlayer(new MinigameSyncS2CPacket(minigameData), player);

            // Optional debug logging
            // OvergearedMod.LOGGER.debug("Sent minigame sync packet to player {}", player.getName().getString());
        } catch (Exception e) {
            OvergearedMod.LOGGER.error("Failed to sync minigame data to player {}", player.getName().getString(), e);
        }
    }

    /*@SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player &&
                !event.getObject().getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).isPresent()) {
            event.addCapability(
                    ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "properties"),
                    new AnvilMinigameProvider()
            );
        }
    }*/

    /*@SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(AnvilMinigameProvider.ANVIL_MINIGAME)
                    .ifPresent(oldStore -> {
                        event.getEntity().getCapability(AnvilMinigameProvider.ANVIL_MINIGAME)
                                .ifPresent(newStore -> newStore.copyFrom(oldStore));
                    });
        }
    }*/

   /* @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(AnvilMinigame.class);
    }*/

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
        return item instanceof TieredItem ||
                item instanceof ProjectileWeaponItem;
    }

    private static boolean isArmor(Item item) {
        return item instanceof ArmorItem;
    }

    private static double getDamageBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "master" -> ServerConfig.MASTER_WEAPON_DAMAGE.get();
            case "perfect" -> ServerConfig.PERFECT_WEAPON_DAMAGE.get();
            case "expert" -> ServerConfig.EXPERT_WEAPON_DAMAGE.get();
            case "well" -> ServerConfig.WELL_WEAPON_DAMAGE.get();
            case "poor" -> ServerConfig.POOR_WEAPON_DAMAGE.get();
            default -> 0.0;
        };
    }

    private static double getSpeedBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "master" -> ServerConfig.MASTER_WEAPON_SPEED.get();
            case "perfect" -> ServerConfig.PERFECT_WEAPON_SPEED.get();
            case "expert" -> ServerConfig.EXPERT_WEAPON_SPEED.get();
            case "well" -> ServerConfig.WELL_WEAPON_SPEED.get();
            case "poor" -> ServerConfig.POOR_WEAPON_SPEED.get();
            default -> 0.0;
        };
    }

    private static double getArmorBonusForQuality(String quality) {
        return switch (quality.toLowerCase()) {
            case "master" -> ServerConfig.MASTER_ARMOR_BONUS.get();
            case "perfect" -> ServerConfig.PERFECT_ARMOR_BONUS.get();
            case "expert" -> ServerConfig.EXPERT_ARMOR_BONUS.get();
            case "well" -> ServerConfig.WELL_ARMOR_BONUS.get();
            case "poor" -> ServerConfig.POOR_ARMOR_BONUS.get();
            default -> 0.0;
        };
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Reset minigame state when joining any world
            resetMinigameForPlayer(player);

            // Start timeout counter if needed
            //startTimeoutCounter(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resetMinigameForPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resetMinigameForPlayer(player);
            //startTimeoutCounter(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();

        // Iterate over all players and reset their minigame
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            resetMinigameForPlayer(player);
        }

        // Optional: Log for debugging
        OvergearedMod.LOGGER.info("Reset all minigames on server stop.");
    }

    private static void resetMinigameForPlayer(ServerPlayer player) {
        UUID playerId = player.getUUID();
        ModMessages.sendToPlayer(new OnlyResetMinigameS2CPacket(), player);
        if (ModItemInteractEvents.playerAnvilPositions.containsKey(player.getUUID())) {
            BlockPos anvilPos = ModItemInteractEvents.playerAnvilPositions.get(player.getUUID());
            BlockEntity be = player.level().getBlockEntity(anvilPos);

            // Only execute on server side
            if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
                anvil.setProgress(0);
                anvil.setChanged();
            }

            // Send reset packet to the specific player
            ModMessages.sendToPlayer(new ResetMinigameS2CPacket(anvilPos), player);
            ModItemInteractEvents.releaseAnvil(player, anvilPos);

            Block block = player.level().getBlockState(anvilPos).getBlock();
            if (block instanceof AbstractSmithingAnvilNew anvilNew) {
                anvilNew.setMinigameOn(false);
            }

            ModItemInteractEvents.playerAnvilPositions.remove(playerId);
            ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
        }

        AnvilMinigameEvents.reset();
        //playerTimeoutCounters.remove(player.getUUID());
    }

    public static void resetMinigameForPlayer(ServerPlayer player, BlockPos anvilPos) {
        // Only execute on server side
        ModMessages.sendToPlayer(new OnlyResetMinigameS2CPacket(), player);
        BlockEntity be = player.level().getBlockEntity(anvilPos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.setChanged();
        }
        AnvilMinigameEvents.reset();
        Block block = player.level().getBlockState(anvilPos).getBlock();
        if (block instanceof AbstractSmithingAnvilNew anvilNew) {
            anvilNew.setMinigameOn(false);
        }
        // Send reset packet to the specific player
        ModMessages.sendToPlayer(new ResetMinigameS2CPacket(anvilPos), player);
        ModItemInteractEvents.playerAnvilPositions.remove(player.getUUID());
        ModItemInteractEvents.playerMinigameVisibility.remove(player.getUUID());
    }

    // In ModEvents.java
    // In ModEvents.java
    public static void resetMinigameForAnvil(Level level, BlockPos anvilPos) {
        // Only execute on server side

        // Reset the anvil block entity
        BlockEntity be = level.getBlockEntity(anvilPos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.setChanged();
            anvil.clearOwner(); // Clear ownership from the anvil itself
        }
        Block block = level.getBlockState(anvilPos).getBlock();
        if (block instanceof AbstractSmithingAnvilNew anvilNew) {
            anvilNew.setMinigameOn(false);
        }
        AnvilMinigameEvents.reset();
        // Find the specific player using this anvil and reset only them
        if (level instanceof ServerLevel serverLevel) {

            for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                UUID playerId = player.getUUID();
                ModMessages.sendToPlayer(new ResetMinigameS2CPacket(anvilPos), player);
                if (ModItemInteractEvents.playerAnvilPositions.getOrDefault(playerId, BlockPos.ZERO).equals(anvilPos)) {
                    // Send reset packet only to this specific player

                    // Clear server-side tracking for this player
                    ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                    ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
                    break; // Only reset the first player found (should only be one)
                }
            }
        }
    }


    private static void startTimeoutCounter(ServerPlayer player) {
        if (player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME)
                .map(AnvilMinigame::isVisible).orElse(false)) {
            // playerTimeoutCounters.put(player.getUUID(), ServerConfig.MINIGAME_TIMEOUT_TICKS.get());
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        int insertOffset = 1;

        if (stack.is(Items.FLINT)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.flint_flavor")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModItems.DIAMOND_SHARD.get())) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.diamond_shard")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModTags.Items.HEATED_METALS)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.heatedingots.tooltip")
                    .withStyle(ChatFormatting.RED));
        }

        if (stack.is(ModTags.Items.HEATABLE_METALS)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.heatablemetals.tooltip")
                    .withStyle(ChatFormatting.GRAY));
        }

        if (stack.is(ModTags.Items.HOT_ITEMS)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.hotitems.tooltip")
                    .withStyle(ChatFormatting.RED));
        }

        // Add Forging Quality
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            String quality = stack.getTag().getString("ForgingQuality");
            Component qualityComponent = switch (quality) {
                case "poor" -> Component.translatable("tooltip.overgeared.poor").withStyle(ChatFormatting.RED);
                case "well" -> Component.translatable("tooltip.overgeared.well").withStyle(ChatFormatting.YELLOW);
                case "expert" -> Component.translatable("tooltip.overgeared.expert").withStyle(ChatFormatting.BLUE);
                case "perfect" -> Component.translatable("tooltip.overgeared.perfect").withStyle(ChatFormatting.GOLD);
                case "master" ->
                        Component.translatable("tooltip.overgeared.master").withStyle(ChatFormatting.LIGHT_PURPLE);
                default -> null;
            };
            if (qualityComponent != null) {
                tooltip.add(insertOffset++, qualityComponent);
            }
        }

        // Add Polish status
        if (stack.hasTag() && stack.getTag().contains("Polished")) {
            boolean isPolished = stack.getTag().getBoolean("Polished");
            Component polishComponent = isPolished
                    ? Component.translatable("tooltip.overgeared.polished").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)
                    : Component.translatable("tooltip.overgeared.unpolished").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC);
            tooltip.add(insertOffset++, polishComponent);
        }

        if (stack.hasTag() && stack.getTag().contains("failedResult")) {
            tooltip.add(insertOffset, Component.translatable("tooltip.overgeared.failedResult")
                    .withStyle(ChatFormatting.RED));
        }

        if (stack.is(ModTags.Items.GRINDABLE)) {
            tooltip.add(insertOffset, Component.translatable("tooltip.overgeared.grindable")
                    .withStyle(ChatFormatting.GRAY));
        }

        // Smithing Hammer special tooltip
        if (stack.is(ModTags.Items.SMITHING_HAMMERS)) {
            if (!Screen.hasShiftDown()) {
                tooltip.add(insertOffset, Component.translatable("tooltip.overgeared.smithing_hammer.hold_shift")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            } else {
                tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line1")
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line2")
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line3")
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        // 🔽 Add Potion Uses Left Tooltip
        if (stack.is(Items.POTION)) {
            CompoundTag tag = stack.getTag();
            int maxUses = ServerConfig.MAX_POTION_TIPPING_USE.get();
            int used = 0;

            if (tag != null && tag.contains("TippedUsed", Tag.TAG_INT)) {
                used = tag.getInt("TippedUsed");
            }

            int left = Math.max(0, maxUses - used);
            //PotionUtils.addPotionTooltip(stack, tooltip, 1 - (float) used / ServerConfig.MAX_POTION_TIPPING_USE.get());
            tooltip.add(Component.translatable("tooltip.overgeared.potion_uses", left, maxUses).withStyle(ChatFormatting.GRAY));

        }

    }

    @SubscribeEvent
    public static void onHammerDestroyed(PlayerDestroyItemEvent event) {
        ItemStack stack = event.getOriginal();
        if (!(stack.is(ModTags.Items.SMITHING_HAMMERS))) return;

        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (AnvilMinigameEvents.isIsVisible() && AnvilMinigameEvents.hasAnvilPosition(player.getUUID())) {
            BlockPos pos = AnvilMinigameEvents.getAnvilPos(player.getUUID());
            if (pos != null) {
                resetMinigameForPlayer(serverPlayer);
            }
        }
    }


}
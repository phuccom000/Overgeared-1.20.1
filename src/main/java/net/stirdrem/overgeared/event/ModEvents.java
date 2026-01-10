package net.stirdrem.overgeared.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.networking.packet.OnlyResetMinigameS2CPacket;
import net.stirdrem.overgeared.networking.packet.ResetMinigameS2CPacket;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModEvents {
    private static final int HEATED_ITEM_CHECK_INTERVAL = 20; // 1 second

    private static int serverTick = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        serverTick++;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
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


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();

        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY);
        if (quality != null && quality != ForgingQuality.NONE) {
            Item item = stack.getItem();

            if (isWeapon(item)) {
                applyWeaponAttributes(event, quality);
            }
            if (isArmor(item)) {
                applyArmorAttributes(event, quality);
            }
        }
    }

    private static void applyWeaponAttributes(ItemAttributeModifierEvent event, ForgingQuality quality) {
        double damageBonus = getDamageBonusForQuality(quality);
        double speedBonus = getSpeedBonusForQuality(quality);
        modifyAttribute(event, Attributes.ATTACK_DAMAGE.value(), damageBonus);
        modifyAttribute(event, Attributes.ATTACK_SPEED.value(), speedBonus);
    }

    private static void applyArmorAttributes(ItemAttributeModifierEvent event, ForgingQuality quality) {
        double armorBonus = getArmorBonusForQuality(quality);
        modifyAttribute(event, Attributes.ARMOR.value(), armorBonus);
        modifyAttribute(event, Attributes.ARMOR_TOUGHNESS.value(), armorBonus);
    }

    private static void modifyAttribute(ItemAttributeModifierEvent event, Attribute attribute, double bonus) {
        List<ItemAttributeModifiers.Entry> originalModifiers = event.getModifiers();

        Holder<Attribute> attributeHolder = new Holder.Direct<>(attribute);

        List<ItemAttributeModifiers.Entry> matchingEntries = originalModifiers.stream()
                .filter(entry -> entry.attribute().equals(attributeHolder))
                .toList();

        if (matchingEntries.isEmpty()) return;

        for (ItemAttributeModifiers.Entry entry : matchingEntries) {
            AttributeModifier modifier = entry.modifier();
            if (modifier.amount() == 0) continue;

            event.removeModifier(attributeHolder, modifier.id());
            event.addModifier(attributeHolder, createModifiedAttribute(modifier, bonus), entry.slot());
        }
    }

    private static AttributeModifier createModifiedAttribute(AttributeModifier original, double bonus) {
        return new AttributeModifier(
                original.id(),
                original.amount() + bonus,
                original.operation()
        );
    }

    private static boolean isWeapon(Item item) {
        return item instanceof TieredItem ||
                item instanceof ProjectileWeaponItem;
    }

    private static boolean isArmor(Item item) {
        return item instanceof ArmorItem;
    }

    private static double getDamageBonusForQuality(ForgingQuality quality) {
        return switch (quality) {
            case MASTER -> ServerConfig.MASTER_WEAPON_DAMAGE.get();
            case PERFECT -> ServerConfig.PERFECT_WEAPON_DAMAGE.get();
            case EXPERT -> ServerConfig.EXPERT_WEAPON_DAMAGE.get();
            case WELL -> ServerConfig.WELL_WEAPON_DAMAGE.get();
            case POOR -> ServerConfig.POOR_WEAPON_DAMAGE.get();
            case NONE -> 0.0;
        };
    }

    private static double getSpeedBonusForQuality(ForgingQuality quality) {
        return switch (quality) {
            case MASTER -> ServerConfig.MASTER_WEAPON_SPEED.get();
            case PERFECT -> ServerConfig.PERFECT_WEAPON_SPEED.get();
            case EXPERT -> ServerConfig.EXPERT_WEAPON_SPEED.get();
            case WELL -> ServerConfig.WELL_WEAPON_SPEED.get();
            case POOR -> ServerConfig.POOR_WEAPON_SPEED.get();
            case NONE -> 0.0;
        };
    }

    private static double getArmorBonusForQuality(ForgingQuality quality) {
        return switch (quality) {
            case MASTER -> ServerConfig.MASTER_ARMOR_BONUS.get();
            case PERFECT -> ServerConfig.PERFECT_ARMOR_BONUS.get();
            case EXPERT -> ServerConfig.EXPERT_ARMOR_BONUS.get();
            case WELL -> ServerConfig.WELL_ARMOR_BONUS.get();
            case POOR -> ServerConfig.POOR_ARMOR_BONUS.get();
            case NONE -> 0.0;
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

    public static void resetMinigameForPlayer(ServerPlayer player) {
        if (player == null) return;
        UUID playerId = player.getUUID();
        PacketDistributor.sendToPlayer(player, new OnlyResetMinigameS2CPacket());
        String blueprintQuality = BlueprintQuality.PERFECT.getDisplayName();
        if (ModItemInteractEvents.playerAnvilPositions.containsKey(player.getUUID())) {
            BlockPos anvilPos = ModItemInteractEvents.playerAnvilPositions.get(player.getUUID());
            BlockEntity be = player.level().getBlockEntity(anvilPos);

            // Only execute on server side
            if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
                anvil.setProgress(0);
                anvil.setChanged();
                anvil.setMinigameOn(false);

                // Send reset packet to the specific player
                PacketDistributor.sendToPlayer(player, new ResetMinigameS2CPacket(anvilPos));
                ModItemInteractEvents.releaseAnvil(player, anvilPos);
                ModItemInteractEvents.playerAnvilPositions.remove(playerId);
                ModItemInteractEvents.playerMinigameVisibility.remove(playerId);
                Block block = player.level().getBlockState(anvilPos).getBlock();
                blueprintQuality = anvil.minigameQuality();
                /*if (block instanceof AbstractSmithingAnvilNew anvilNew) {
                    anvilNew.setMinigameOn(false);
                }*/
            }


        }

        AnvilMinigameEvents.reset(blueprintQuality);
        //playerTimeoutCounters.remove(player.getUUID());
    }

    public static void resetMinigameForPlayer(ServerPlayer player, BlockPos anvilPos) {
        // Only execute on server side
        if (player == null) return;
        PacketDistributor.sendToPlayer(player, new OnlyResetMinigameS2CPacket());
        BlockEntity be = player.level().getBlockEntity(anvilPos);
        String quality = "perfect";
        if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.setChanged();
            anvil.setMinigameOn(false);
            quality = anvil.minigameQuality();
        }
        AnvilMinigameEvents.reset(quality);
        Block block = player.level().getBlockState(anvilPos).getBlock();

        // Send reset packet to the specific player
        ModItemInteractEvents.playerAnvilPositions.remove(player.getUUID());
        ModItemInteractEvents.playerMinigameVisibility.remove(player.getUUID());
    }

    public static void resetMinigameForAnvil(Level level, BlockPos anvilPos) {
        // Only execute on server side
        String quality = "perfect";
        // Reset the anvil block entity
        BlockEntity be = level.getBlockEntity(anvilPos);
        if (be instanceof AbstractSmithingAnvilBlockEntity anvil) {
            anvil.setProgress(0);
            anvil.setChanged();
            anvil.setMinigameOn(false);
            anvil.clearOwner(); // Clear ownership from the anvil itself
            quality = anvil.minigameQuality();
        }

        AnvilMinigameEvents.reset(quality);
        // Find the specific player using this anvil and reset only them
        if (level instanceof ServerLevel serverLevel) {

            for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
                UUID playerId = player.getUUID();
                PacketDistributor.sendToPlayer(player, new ResetMinigameS2CPacket(anvilPos));
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


    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        int insertOffset = 1;


        // Add Forging Quality
        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY);
        if (quality != null) {
            Component qualityComponent = switch (quality) {
                case MASTER -> Component.translatable("tooltip.overgeared.master").withStyle(ChatFormatting.LIGHT_PURPLE);
                case PERFECT -> Component.translatable("tooltip.overgeared.perfect").withStyle(ChatFormatting.GOLD);
                case EXPERT -> Component.translatable("tooltip.overgeared.expert").withStyle(ChatFormatting.BLUE);
                case WELL -> Component.translatable("tooltip.overgeared.well").withStyle(ChatFormatting.YELLOW);
                case POOR -> Component.translatable("tooltip.overgeared.poor").withStyle(ChatFormatting.RED);
                default -> null;
            };
            if (qualityComponent != null) {
                tooltip.add(insertOffset++, qualityComponent);
            }
        }

        // Add Polish status
        Boolean isPolished = stack.get(ModComponents.POLISHED);
        if (isPolished != null) {
            Component polishComponent = isPolished
                    ? Component.translatable("tooltip.overgeared.polished").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)
                    : Component.translatable("tooltip.overgeared.unpolished").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC);
            tooltip.add(insertOffset++, polishComponent);
        }
        if (Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT))) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.heated").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        }
        if (Boolean.TRUE.equals(stack.get(ModComponents.FAILED_RESULT))) {
            tooltip.add(insertOffset, Component.translatable("tooltip.overgeared.failedResult")
                    .withStyle(ChatFormatting.RED));
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
                if (ServerConfig.ENABLE_STONE_TO_ANVIL.get())
                    tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line3")
                            .withStyle(ChatFormatting.GRAY));
                if (ServerConfig.ENABLE_ANVIL_TO_SMITHING.get())
                    tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.smithing_hammer.advanced_tooltip.line4")
                            .withStyle(ChatFormatting.GRAY));
            }
        }

        // 🔽 Add Potion Uses Left Tooltip
        if (stack.is(Items.POTION)) {
            int maxUses = ServerConfig.MAX_POTION_TIPPING_USE.get();
            Integer used = stack.getOrDefault(ModComponents.TIPPED_USES, 0);

            int left = Math.max(0, maxUses - used);
            tooltip.add(Component.translatable("tooltip.overgeared.potion_uses", left, maxUses).withStyle(ChatFormatting.GRAY));
        }
        if (!ServerConfig.ENABLE_MOD_TOOLTIPS.get()) return;

        if (stack.is(Items.FLINT) && ServerConfig.GET_ROCK_USING_FLINT.get()) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.flint_flavor")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModItems.DIAMOND_SHARD.get())) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.diamond_shard")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModItems.STONE_HAMMER_HEAD.get())) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.stone_hammer_head")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModTags.Items.HEATED_METALS)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.heatedingots.tooltip")
                    .withStyle(ChatFormatting.RED));
        }

      /*  if (stack.is(ModTags.Items.HEATABLE_METALS)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.heatablemetals.tooltip")
                    .withStyle(ChatFormatting.GRAY));
        }*/

        if (stack.is(ModTags.Items.HOT_ITEMS)) {
            tooltip.add(insertOffset++, Component.translatable("tooltip.overgeared.hotitems.tooltip")
                    .withStyle(ChatFormatting.RED));
        }
        /*if (stack.is(ModTags.Items.GRINDABLE)) {
            tooltip.add(insertOffset, Component.translatable("tooltip.overgeared.grindable")
                    .withStyle(ChatFormatting.GRAY));
        }*/

        String creatorName = stack.get(ModComponents.CREATOR);
        if (creatorName != null) {
            Component creatorComponent = Component.translatable("tooltip.overgeared.made_by")
                    .append(" ")
                    .append(creatorName)
                    .withStyle(ChatFormatting.GRAY);
            tooltip.add(insertOffset++, creatorComponent);
        }
    }

    @SubscribeEvent
    public static void onHammerDestroyed(PlayerDestroyItemEvent event) {
        ItemStack stack = event.getOriginal();
        if (!(stack.is(ModTags.Items.SMITHING_HAMMERS))) return;

        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (AnvilMinigameEvents.isVisible() && AnvilMinigameEvents.hasAnvilPosition(player.getUUID())) {
            BlockPos pos = AnvilMinigameEvents.getAnvilPos(player.getUUID());
            if (pos != null) {
                resetMinigameForPlayer(serverPlayer);
            }
        }
    }
}
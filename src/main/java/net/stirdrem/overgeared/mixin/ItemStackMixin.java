package net.stirdrem.overgeared.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.QualityHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(
            method = "getTooltipLines",
            at = @At("RETURN"),
            cancellable = true
    )
    private void insertQualityTooltip(Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        List<Component> tooltip = cir.getReturnValue() != null ? new ArrayList<>(cir.getReturnValue()) : new ArrayList<>();
        boolean modified = false;

        // Handle quality tooltip
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
                int insertPos = Math.min(1, tooltip.size());
                tooltip.add(insertPos, qualityComponent);
                modified = true;
            }
        }

        // Handle polished/unpolished tooltip
        if (stack.hasTag() && stack.getTag().contains("Polished")) {
            boolean isPolished = stack.getTag().getBoolean("Polished");
            Component polishComponent = isPolished
                    ? Component.translatable("tooltip.overgeared.polished").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)
                    : Component.translatable("tooltip.overgeared.unpolished").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

            // Insert after quality tooltip or at position 1 if no quality
            int insertPos = modified ? 2 : Math.min(1, tooltip.size());
            tooltip.add(insertPos, polishComponent);
            modified = true;
        }

        if (modified) {
            cir.setReturnValue(tooltip);
        }
    }


    /*@Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void modifyDurability(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem().canBeDepleted()) { // Only for damageable items
            float multiplier = QualityHelper.getQualityMultiplier(stack);
            int baseDurability = stack.getItem().getMaxDamage();
            cir.setReturnValue((int) (baseDurability * multiplier));
        }
    }*/

    @Inject(
            method = "getDestroySpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        float baseSpeed = cir.getReturnValueF();

        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            float multiplier = QualityHelper.getQualityMultiplier(stack);
            cir.setReturnValue(baseSpeed * multiplier);
        }
    }

    @Inject(
            method = "getMaxDamage()I",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyDurabilityBasedOnQuality(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        // Skip if the item doesn't have durability
        if (!stack.isDamageableItem()) {
            return;
        }

        // Get the original max damage (respects overridden methods)
        int originalDurability = cir.getReturnValue();

        // Skip if durability is already customized (like the flint case)
        if (originalDurability != stack.getItem().getMaxDamage()) {
            return;
        }

        float baseMultiplier = ServerConfig.BASE_DURABILITY_MULTIPLIER.get().floatValue();
        int newBaseDurability = (int) (originalDurability * baseMultiplier);

        // Apply quality multiplier if present
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            float multiplier = QualityHelper.getQualityMultiplier(stack);
            newBaseDurability = (int) (newBaseDurability * multiplier);
        }

        // Reduce max durability by 5% for each 'ReducedMaxDurability'
        if (stack.hasTag() && stack.getTag().contains("ReducedMaxDurability")) {
            int reductions = stack.getTag().getInt("ReducedMaxDurability");
            float durabilityPenaltyMultiplier = 1.0f - (reductions * ServerConfig.DURABILITY_REDUCE_PER_GRIND.get().floatValue());
            durabilityPenaltyMultiplier = Math.max(0.1f, durabilityPenaltyMultiplier); // Prevent zero or negative durability
            newBaseDurability = (int) (originalDurability * baseMultiplier *
                    (stack.getTag().contains("ForgingQuality") ? QualityHelper.getQualityMultiplier(stack) : 1)
                    * durabilityPenaltyMultiplier);
        }

        cir.setReturnValue(newBaseDurability);
    }

    /*@Redirect(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getAttackDamage()F"))
    private float modifyAttackSpeed(Item instance) {
        // This is more complex - you might need to use @Inject instead
        return instance.getAttackDamage() * QualityHelper.getQualityMultiplier(...);
    }*/


    // Per-player last-hit tick
    private static final Map<UUID, Long> lastTongsHit = new WeakHashMap<>();

    private static final String HEATED_TIME_TAG = "HeatedSince";
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 60; // Default: 60 seconds

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void onInventoryTick(Level level, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        if (slot != 0) return; // Only process once per player per tick

        long tick = level.getGameTime();
        int cooldownTicks = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get(); // add to your config

        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            if (!stack.is(ModTags.Items.HEATED_METALS)) continue;

            CompoundTag tag = stack.getOrCreateTag();
            long heatedSince = tag.getLong(HEATED_TIME_TAG);
            if (heatedSince == 0L) {
                tag.putLong(HEATED_TIME_TAG, tick); // Initialize the timestamp
            } else if (tick - heatedSince >= cooldownTicks) {
                Item cooled = getCooledIngot(stack.getItem());
                if (cooled != null) {
                    ItemStack newStack = new ItemStack(cooled, stack.getCount());
                    stack.setCount(0); // Remove old
                    player.getInventory().add(newStack); // Add new cooled item
                    player.playSound(SoundEvents.FIRE_EXTINGUISH, 0.7f, 1.0f);
                }
            }
        }

        // 🔥 Heat Damage or Tongs Handling (unchanged)
        boolean hasHeated = player.getInventory().items.stream()
                .anyMatch(s -> !s.isEmpty() && s.getItem().builtInRegistryHolder().is(ModTags.Items.HEATED_METALS))
                || player.getMainHandItem().is(ModTags.Items.HEATED_METALS)
                || player.getOffhandItem().is(ModTags.Items.HEATED_METALS);

        if (!hasHeated) return;

        UUID uuid = player.getUUID();
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        // Check for tongs in either hand
        ItemStack tongsStack;
        if (!main.isEmpty() && main.getItem().builtInRegistryHolder().is(ModTags.Items.TONGS)) {
            tongsStack = main;
        } else if (!off.isEmpty() && off.getItem().builtInRegistryHolder().is(ModTags.Items.TONGS)) {
            tongsStack = off;
        } else {
            tongsStack = ItemStack.EMPTY;
        }

        if (!tongsStack.isEmpty()) {
            if (tick % 40 != 0) return;
            long last = lastTongsHit.getOrDefault(uuid, -1L);
            if (last != tick) {
                tongsStack.hurtAndBreak(1, player, p -> {
                    // Determine correct hand
                    InteractionHand hand = tongsStack == player.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                    p.broadcastBreakEvent(hand);
                });
                lastTongsHit.put(uuid, tick);
            }
        } else {
            player.hurt(player.damageSources().hotFloor(), 1.0f);
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

}


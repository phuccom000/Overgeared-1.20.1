package net.stirdrem.overgeared.mixin;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ModTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import static net.stirdrem.overgeared.util.ItemUtils.copyComponentsExceptHeated;
import static net.stirdrem.overgeared.util.ItemUtils.getCooledItem;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    // Per-player last-hit tick to prevent multiple tongs damage per tick
    private static final Map<UUID, Long> lastTongsHit = new WeakHashMap<>();

    @Inject(
            method = "getDestroySpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        float baseSpeed = cir.getReturnValueF();

        ForgingQuality quality = stack.get(ModComponents.FORGING_QUALITY.get());
        if (quality != null) {
            float multiplier = quality.getDamageMultiplier();
            cir.setReturnValue(baseSpeed * multiplier);
        }
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void onInventoryTick(Level level, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;

        long tick = level.getGameTime();
        int cooldownTicks = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();

        // Process heated items in inventory - cool them down after time
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            if (!stack.is(ModTags.Items.HEATED_METALS)) continue;

            Long heatedSince = stack.get(ModComponents.HEATED_TIME.get());
            if (heatedSince == null) {
                // Initialize the timestamp
                stack.set(ModComponents.HEATED_TIME.get(), tick);
            } else if (tick - heatedSince >= cooldownTicks) {
                // Time to cool down
                Item cooled = getCooledItem(stack.getItem(), level);
                if (cooled != null) {
                    ItemStack newStack = new ItemStack(cooled, stack.getCount());
                    copyComponentsExceptHeated(stack, newStack);

                    boolean isMain = stack == player.getMainHandItem();
                    boolean isOff = stack == player.getOffhandItem();

                    stack.setCount(0); // Remove old heated item

                    if (isMain) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, newStack);
                    } else if (isOff) {
                        player.setItemInHand(InteractionHand.OFF_HAND, newStack);
                    } else if (!player.getInventory().add(newStack)) {
                        player.drop(newStack, false); // Drop if inventory is full
                    }

                    level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.0f);
                }
            }
        }

        // Check if player has any heated items
        boolean hasHotItem = player.getInventory().items.stream()
                .anyMatch(s -> !s.isEmpty() && (s.is(ModTags.Items.HEATED_METALS) || s.is(ModTags.Items.HOT_ITEMS))
                        || Boolean.TRUE.equals(s.get(ModComponents.HEATED_COMPONENT.get())))
                || player.getMainHandItem().is(ModTags.Items.HEATED_METALS) || player.getMainHandItem().is(ModTags.Items.HOT_ITEMS)
                || player.getOffhandItem().is(ModTags.Items.HEATED_METALS) || player.getOffhandItem().is(ModTags.Items.HOT_ITEMS);

        if (!hasHotItem) return;

        UUID uuid = player.getUUID();
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        // Check for tongs in either hand
        ItemStack tongsStack;
        if (!main.isEmpty() && main.is(ModTags.Items.TONGS)) {
            tongsStack = main;
        } else if (!off.isEmpty() && off.is(ModTags.Items.TONGS)) {
            tongsStack = off;
        } else {
            tongsStack = ItemStack.EMPTY;
        }

        if (!tongsStack.isEmpty()) {
            // Player has tongs - damage them instead of the player
            if (tick % 40 != 0) return; // Only damage every 2 seconds
            long last = lastTongsHit.getOrDefault(uuid, -1L);
            if (last != tick) {
                // Determine correct hand for break animation
                EquipmentSlot equipSlot = tongsStack == player.getMainHandItem() 
                        ? EquipmentSlot.MAINHAND 
                        : EquipmentSlot.OFFHAND;
                tongsStack.hurtAndBreak(1, player, equipSlot);
                lastTongsHit.put(uuid, tick);
            }
        } else {
            // No tongs - damage the player
            if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                player.hurt(player.damageSources().hotFloor(), 1.0f);
            }
        }
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void fixDurabilityBar(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageableItem()) return;

        int maxDamage = stack.getMaxDamage(); // includes quality/durability changes
        int damage = stack.getDamageValue();

        // Clamp to valid range
        if (damage >= maxDamage) {
            cir.setReturnValue(0);
            return;
        }

        int width = Math.round(13.0F - (float) damage * 13.0F / (float) maxDamage);
        cir.setReturnValue(width);
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void fixDurabilityBarColor(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (!stack.isDamageableItem()) return;

        int max = stack.getMaxDamage(); // Includes quality/durability changes
        int damage = stack.getDamageValue();

        if (max <= 0) {
            cir.setReturnValue(0xFFFFFF); // fallback white
            return;
        }

        float ratio = Math.max(0.0F, 1.0F - (float) damage / (float) max);

        // Vanilla bar color: hue from red (0.0) to green (0.333...)
        float hue = ratio / 3.0F; // [0, 0.33]

        int color = Mth.hsvToRgb(hue, 1.0F, 1.0F);

        cir.setReturnValue(color);
    }
}

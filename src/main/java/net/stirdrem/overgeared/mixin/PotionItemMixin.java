package net.stirdrem.overgeared.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.stirdrem.overgeared.components.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {
    @Unique
    private static final float MIN_DURATION_SCALE = 0.1f;

    @Unique
    private static final ThreadLocal<ItemStack> overgeared$currentStack = new ThreadLocal<>();

    /**
     * Modifies the effects applied when drinking a potion with TIPPED_USES component.
     * This intercepts before vanilla applies effects and applies scaled effects instead.
     */
    @Inject(
            method = "finishUsingItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onFinishUsing(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.isEmpty()) return;
        Integer tippedUsed = stack.get(ModComponents.TIPPED_USES.get());
        if (tippedUsed == null) return;

        float scale = overgeared$calculateDurationScale(tippedUsed);
        Player player = entity instanceof Player ? (Player) entity : null;

        // Get potion contents from data component
        PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

        // Apply scaled effects
        if (!level.isClientSide) {
            for (MobEffectInstance effect : potionContents.getAllEffects()) {
                if (effect.getEffect().value().isInstantenous()) {
                    effect.getEffect().value().applyInstantenousEffect(player, player, entity, effect.getAmplifier(), 1.0D);
                } else {
                    entity.addEffect(overgeared$createScaledEffect(effect, scale));
                }
            }
        }

        ItemStack resultStack = stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
        // Handle item consumption and bottle return
        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get((PotionItem) (Object) this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            if (!player.getAbilities().instabuild && !stack.isEmpty()) {
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        entity.gameEvent(GameEvent.DRINK);
        cir.setReturnValue(resultStack);
    }

    @Unique
    private static float overgeared$calculateDurationScale(int tippedUsed) {
        return Math.max(MIN_DURATION_SCALE, 1.0f - (tippedUsed / 8.0f));
    }

    @Unique
    private static MobEffectInstance overgeared$createScaledEffect(MobEffectInstance original, float scale) {
        return new MobEffectInstance(
                original.getEffect(),  // Holder<MobEffect> - passed directly
                Math.max(1, (int) (original.getDuration() * scale)),
                original.getAmplifier(),
                original.isAmbient(),
                original.isVisible(),
                original.showIcon()
        );
    }

    /**
     * Capture the stack at the start of appendHoverText for use in the redirect.
     */
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void overgeared$captureStack(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        overgeared$currentStack.set(stack);
    }

    /**
     * Clear the captured stack after appendHoverText completes.
     */
    @Inject(method = "appendHoverText", at = @At("RETURN"))
    private void overgeared$clearStack(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        overgeared$currentStack.remove();
    }

    /**
     * Redirect the tooltip call to use scaled duration for potions with TIPPED_USES.
     */
    @Redirect(
            method = "appendHoverText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/alchemy/PotionContents;addPotionTooltip(Ljava/util/function/Consumer;FF)V"
            )
    )
    private void overgeared$redirectTooltip(PotionContents contents, Consumer<Component> tooltipAdder, float durationFactor, float tickRate) {
        ItemStack stack = overgeared$currentStack.get();
        float scale = durationFactor;
        if (stack != null) {
            Integer tippedUsed = stack.get(ModComponents.TIPPED_USES);
            if (tippedUsed != null) {
                scale = overgeared$calculateDurationScale(tippedUsed);
            }
        }
        contents.addPotionTooltip(tooltipAdder, scale, tickRate);
    }
}
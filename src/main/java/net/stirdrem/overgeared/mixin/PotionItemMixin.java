package net.stirdrem.overgeared.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {

    @Unique
    private static final String TIPPED_USED_TAG = "TippedUsed";

    @Unique
    private static final float MIN_DURATION_SCALE = 0.1f;

    /**
     * Modifies the effects applied when drinking a potion
     */
    @Inject(
            method = "finishUsingItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/alchemy/PotionUtils;getMobEffects(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"
            ),
            cancellable = true
    )
    private void onFinishUsing(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.isEmpty() || !stack.hasTag() || !stack.getTag().contains(TIPPED_USED_TAG)) {
            return; // Skip if no TippedUsed tag
        }

        Player player = entity instanceof Player ? (Player) entity : null;
        int tippedUsed = stack.getTag().getInt(TIPPED_USED_TAG);
        float scale = calculateDurationScale(tippedUsed);

        // Apply scaled effects
        if (!level.isClientSide) {
            for (MobEffectInstance effect : PotionUtils.getMobEffects(stack)) {
                if (effect.getEffect().isInstantenous()) {
                    effect.getEffect().applyInstantenousEffect(player, player, entity, effect.getAmplifier(), 1.0D);
                } else {
                    entity.addEffect(createScaledEffect(effect, scale));
                }
            }
        }

        // Handle item consumption and bottle return
        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get((PotionItem) (Object) this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        ItemStack resultStack = stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
        if (player != null && !player.getAbilities().instabuild && stack.isEmpty()) {
            player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
        }

        entity.gameEvent(GameEvent.DRINK);
        cir.setReturnValue(resultStack);
    }

    /**
     * Modifies the tooltip to show scaled durations
     */
    @ModifyArg(
            method = "appendHoverText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/alchemy/PotionUtils;addPotionTooltip(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;F)V"
            ),
            index = 2
    )
    private float modifyTooltipDurationScale(ItemStack stack, List<Component> tooltip, float originalScale) {
        if (stack.hasTag() && stack.getTag().contains(TIPPED_USED_TAG)) {
            int tippedUsed = stack.getTag().getInt(TIPPED_USED_TAG);
            return calculateDurationScale(tippedUsed);
        }
        return originalScale;
    }

    @Unique
    private static float calculateDurationScale(int tippedUsed) {
        return Math.max(MIN_DURATION_SCALE, 1.0f - (tippedUsed / 8.0f));
    }

    @Unique
    private static MobEffectInstance createScaledEffect(MobEffectInstance original, float scale) {
        return new MobEffectInstance(
                original.getEffect(),
                Math.max(1, (int) (original.getDuration() * scale)),
                original.getAmplifier(),
                original.isAmbient(),
                original.isVisible(),
                original.showIcon()
        );
    }
}
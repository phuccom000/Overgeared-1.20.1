/*
package net.stirdrem.overgeared.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TippedArrowItem.class)
public abstract class TippedArrowItemMixin {

    @Inject(
            method = "getDescriptionId", // <-- correct method
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGetDescriptionId(ItemStack stack, CallbackInfoReturnable<String> cir) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            List<net.minecraft.world.effect.MobEffectInstance> effects = PotionUtils.getCustomEffects(tag);

            // Multiple custom effects
            if (effects.size() > 1) {
                cir.setReturnValue("item.minecraft.tipped_arrow.effect.mixed");
                return;
            }

            // Multiple base potion effects
            Potion potion = PotionUtils.getPotion(tag);
            if (potion != Potions.EMPTY && potion.getEffects().size() > 1) {
                cir.setReturnValue("item.minecraft.tipped_arrow.mixed");
            }
        }
    }
}
*/

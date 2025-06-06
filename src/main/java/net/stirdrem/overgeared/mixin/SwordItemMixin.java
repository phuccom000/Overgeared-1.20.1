package net.stirdrem.overgeared.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.util.QualityHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TieredItemMixin.java (for tools like PickaxeItem, AxeItem, etc.)
// SwordItemMixin.java (for melee weapons)
@Mixin(SwordItem.class)
public abstract class SwordItemMixin {
    @Inject(method = "getDamage", at = @At("HEAD"), cancellable = true)
    private void modifyAttackDamage(CallbackInfoReturnable<Float> cir) {
        // Only modify if we're not in attribute calculation context
        if (!QualityHelper.isCalculatingAttributes()) {
            ItemStack stack = (ItemStack) (Object) this;
            float multiplier = QualityHelper.getQualityMultiplier(stack);
            cir.setReturnValue(((SwordItem) (Object) this).getDamage() * multiplier);
        }
    }
}

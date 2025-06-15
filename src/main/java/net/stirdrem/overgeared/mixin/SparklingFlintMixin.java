package net.stirdrem.overgeared.mixin;

import com.eruannie_9.lititup.Item.SparklingFlintItem;
import com.eruannie_9.lititup.ModConfiguration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.util.QualityHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SparklingFlintItem.class)
public abstract class SparklingFlintMixin {
    /*@Inject(
            method = "getMaxDamage",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void modifyDamage(CallbackInfoReturnable<Integer> cir) {
        //OvergearedMod.LOGGER.debug("Sparkling Flint Durability: {}", ModConfiguration.SPARKLING_FLINT_DURABILITY.get());
        cir.setReturnValue(ModConfiguration.SPARKLING_FLINT_DURABILITY.get());
    }*/
}



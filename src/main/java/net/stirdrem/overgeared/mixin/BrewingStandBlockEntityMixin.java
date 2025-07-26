package net.stirdrem.overgeared.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    // Thread-local or static to track original TippedUsed values
    @Unique
    private static final int[] overgeared_BlacksmithMod_1_20_1$tippedUsedCache = new int[3];

    @Inject(method = "doBrew", at = @At("HEAD"))
    private static void cacheTippedUsedTags(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        for (int i = 0; i < 3; i++) {
            ItemStack original = items.get(i);
            CompoundTag tag = original.getTag();
            overgeared_BlacksmithMod_1_20_1$tippedUsedCache[i] = (tag != null && tag.contains("TippedUsed", CompoundTag.TAG_INT))
                    ? tag.getInt("TippedUsed") : -1;
        }
    }

    @Inject(method = "doBrew", at = @At("TAIL"))
    private static void restoreTippedUsedTags(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        for (int i = 0; i < 3; i++) {
            if (overgeared_BlacksmithMod_1_20_1$tippedUsedCache[i] != -1) {
                ItemStack brewed = items.get(i);
                if (!brewed.isEmpty()) {
                    brewed.getOrCreateTag().putInt("TippedUsed", overgeared_BlacksmithMod_1_20_1$tippedUsedCache[i]);
                }
            }
        }
    }
}


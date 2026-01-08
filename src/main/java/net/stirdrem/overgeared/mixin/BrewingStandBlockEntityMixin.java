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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    /**
     * Cache TippedUsed per brewing stand position to avoid race conditions
     * when multiple brewing stands brew in the same tick.
     */
    @Unique
    private static final Map<BlockPos, int[]> overgeared$tippedUsedCache = new ConcurrentHashMap<>();

    @Inject(method = "doBrew", at = @At("HEAD"))
    private static void overgeared$cacheTippedUsed(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        int[] cache = new int[3];

        for (int i = 0; i < 3; i++) {
            ItemStack stack = items.get(i);
            CompoundTag tag = stack.getTag();
            cache[i] = (tag != null && tag.contains("TippedUsed", CompoundTag.TAG_INT))
                    ? tag.getInt("TippedUsed")
                    : -1;
        }

        overgeared$tippedUsedCache.put(pos, cache);
    }

    @Inject(method = "doBrew", at = @At("TAIL"))
    private static void overgeared$restoreTippedUsed(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        int[] cache = overgeared$tippedUsedCache.remove(pos);
        if (cache == null) return;

        for (int i = 0; i < 3; i++) {
            if (cache[i] != -1) {
                ItemStack brewed = items.get(i);
                if (!brewed.isEmpty()) {
                    brewed.getOrCreateTag().putInt("TippedUsed", cache[i]);
                }
            }
        }
    }
}

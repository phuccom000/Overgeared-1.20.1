package net.stirdrem.overgeared.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.stirdrem.overgeared.components.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    @Unique
    private static final Map<BlockPos, int[]> overgeared$tippedUsedCache = new ConcurrentHashMap<>();

    @Inject(method = "doBrew", at = @At("HEAD"))
    private static void cacheTippedUsedTags(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        int[] cache = new int[3];
        for (int i = 0; i < 3; i++) {
            ItemStack original = items.get(i);
            Integer tippedUses = original.get(ModComponents.TIPPED_USES);
            cache[i] = (tippedUses != null ? tippedUses : -1);
        }
        overgeared$tippedUsedCache.put(pos.immutable(), cache);
    }

    @Inject(method = "doBrew", at = @At("TAIL"))
    private static void restoreTippedUsedTags(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        int[] cache = overgeared$tippedUsedCache.remove(pos);
        if (cache == null) return;
        
        for (int i = 0; i < 3; i++) {
            if (cache[i] != -1) {
                ItemStack brewed = items.get(i);
                if (brewed.isEmpty()) continue;
                brewed.set(ModComponents.TIPPED_USES, cache[i]);
            }
        }
    }
}


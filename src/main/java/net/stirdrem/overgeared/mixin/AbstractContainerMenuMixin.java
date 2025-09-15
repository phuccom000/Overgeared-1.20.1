package net.stirdrem.overgeared.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.stirdrem.overgeared.OvergearedMod.getCooledIngot;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow
    @Final
    private List<ContainerListener> containerListeners;

    @Inject(method = "broadcastChanges", at = @At("HEAD"))

    private void overgeared$lazyCool(CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

        // Check all listeners for a ServerPlayer
        for (ContainerListener listener : this.containerListeners) {
            if (listener instanceof ServerPlayer player) {
                long now = player.level().getGameTime();
                int cooldown = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();

                for (Slot slot : menu.slots) {
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty() || !stack.hasTag() || !stack.getTag().contains("HeatedSince")) continue;

                    long heatedAt = stack.getTag().getLong("HeatedSince");
                    if (now - heatedAt >= cooldown) {
                        ItemStack cooled = getCooledIngot(stack.getItem()).getDefaultInstance();
                        slot.set(cooled);
                        menu.broadcastChanges(); // sync change
                    }
                }
            }
        }
    }
}


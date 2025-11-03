package net.stirdrem.overgeared.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Shadow
    protected NonNullList<ItemStack> items;

    @Inject(
            method = "burn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"
            ),
            cancellable = true
    )
    private void overgeared_beforeShrink(
            net.minecraft.core.RegistryAccess registryAccess,
            Recipe<?> recipe,
            NonNullList<ItemStack> inventory,
            int maxStackSize,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ItemStack input = inventory.get(0);

        SimpleContainer container = new SimpleContainer(inventory.toArray(ItemStack[]::new));

        if (recipe != null) {
            @SuppressWarnings("unchecked")
            Recipe<net.minecraft.world.Container> typedRecipe = (Recipe<net.minecraft.world.Container>) recipe;
            ItemStack remainder = typedRecipe.getRemainingItems(container).get(0);

            if (!remainder.isEmpty()) {
                // Apply remainder instead of shrinking
                inventory.set(0, remainder.copy());
                cir.setReturnValue(true); // skip shrink
                return;
            }
        }

        // If no remainder: re-do the shrink manually then cancel vanilla
        input.shrink(1);
        cir.setReturnValue(true); // prevents vanilla shrink from running again
    }
}


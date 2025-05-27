package net.stirdrem.overgeared.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.util.QualityHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(
            method = "getTooltipLines",
            at = @At("RETURN"),
            cancellable = true
    )
    private void insertQualityTooltip(Player player, TooltipFlag context, CallbackInfoReturnable<List<Component>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            String quality = stack.getTag().getString("ForgingQuality");
            Component qualityComponent = switch (quality) {
                case "poor" -> Component.translatable("tooltip.overgeared.poor").withStyle(ChatFormatting.RED);
                case "well" -> Component.translatable("tooltip.overgeared.well").withStyle(ChatFormatting.YELLOW);
                case "expert" -> Component.translatable("tooltip.overgeared.expert").withStyle(ChatFormatting.BLUE);
                case "perfect" -> Component.translatable("tooltip.overgeared.perfect").withStyle(ChatFormatting.GOLD);
                default -> null;
            };

            if (qualityComponent != null) {
                List<Component> tooltip = cir.getReturnValue();
                if (tooltip == null) {
                    tooltip = new ArrayList<>();
                } else {
                    tooltip = new ArrayList<>(tooltip);
                }

                // Insert the quality tooltip after the item's name (which is typically at index 0)
                int insertPos = Math.min(1, tooltip.size());
                tooltip.add(insertPos, qualityComponent);
                cir.setReturnValue(tooltip);
            }
        }
    }


    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void modifyDurability(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem().canBeDepleted()) { // Only for damageable items
            float multiplier = QualityHelper.getQualityMultiplier(stack);
            int baseDurability = stack.getItem().getMaxDamage();
            cir.setReturnValue((int) (baseDurability * multiplier));
        }
    }

    @Inject(
            method = "getDestroySpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        float baseSpeed = cir.getReturnValueF();

        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            float multiplier = QualityHelper.getQualityMultiplier(stack);
            cir.setReturnValue(baseSpeed * multiplier);
        }
    }

    @Inject(
            method = "getMaxDamage()I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifyDurabilityBasedOnQuality(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        // Skip if the item doesn't have durability
        if (!stack.isDamageableItem()) {
            return;
        }

        // Apply quality multiplier if the tag exists
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            float multiplier = QualityHelper.getQualityMultiplier(stack);
            int baseDurability = stack.getItem().getMaxDamage();
            int modifiedDurability = (int) (baseDurability * multiplier);
            cir.setReturnValue(modifiedDurability);
        }
    }

    /*@Redirect(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getAttackDamage()F"))
    private float modifyAttackSpeed(Item instance) {
        // This is more complex - you might need to use @Inject instead
        return instance.getAttackDamage() * QualityHelper.getQualityMultiplier(...);
    }*/
}


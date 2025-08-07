/*
package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.ModularArrowEntity;

import javax.annotation.Nullable;
import java.util.List;

public class ModularArrowItem extends ArrowItem {
    public ModularArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        return new ModularArrowEntity(level, shooter, stack);
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, net.minecraft.world.entity.player.Player player) {
        return false; // Infinity doesn't work on lingering arrows
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // First add potion tooltip if present
        PotionUtils.addPotionTooltip(stack, tooltip, 0.25F);

        // Then add component effects
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            // Add spacing if there are potion effects
            if (PotionUtils.getPotion(stack) != Potions.EMPTY) {
                tooltip.add(Component.empty());
            }

            tooltip.add(Component.translatable("tooltip.modular_arrow.component").withStyle(ChatFormatting.GRAY));

            // Tip effects
            if (tag.contains("Tip")) {
                String tip = tag.getString("Tip");
                Component tipEffect = getComponentEffectDescription("Tip", tip);
                tooltip.add(Component.literal(" ").append(Component.translatable("tooltip.modular_arrow.tip").append(tipEffect).withStyle(ChatFormatting.BLUE)));
            }

            // Shaft effects
            if (tag.contains("Shaft")) {
                String shaft = tag.getString("Shaft");
                Component shaftEffect = getComponentEffectDescription("Shaft", shaft);
                tooltip.add(Component.literal(" ").append(Component.translatable("tooltip.modular_arrow.shaft").append(shaftEffect).withStyle(ChatFormatting.BLUE)));
            }

            // Feather effects
            if (tag.contains("Feather")) {
                String feather = tag.getString("Feather");
                Component featherEffect = getComponentEffectDescription("Feather", feather);
                tooltip.add(Component.literal(" ").append(Component.translatable("tooltip.modular_arrow.feather").append(featherEffect).withStyle(ChatFormatting.BLUE)));
            }
        }
    }

    private Component getComponentEffectDescription(String componentType, String material) {
        String translationKey = "tooltip.modular_arrow." + componentType.toLowerCase() + "." + material;
        return Component.literal(" ").append(Component.translatable(translationKey).withStyle(ChatFormatting.BLUE));
    }

    @Override
    public String getDescriptionId(ItemStack pStack) {
        return PotionUtils.getPotion(pStack).getName(this.getDescriptionId() + ".effect.");
    }
}
*/

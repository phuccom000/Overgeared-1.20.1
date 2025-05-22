package net.stirdrem.overgeared.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HeatableItem extends Item {
    public HeatableItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        // Show the bar if the item has taken any damage
        return stack.isDamaged();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        // Calculate the width of the bar based on remaining durability
        return Math.round(13.0F * (1.0F - (float) stack.getDamageValue() / stack.getMaxDamage()));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Calculate color from red (hot) to orange as durability decreases
        float durabilityRatio = 1.0F - (float) stack.getDamageValue() / stack.getMaxDamage();
        float hue = 0.05F * durabilityRatio; // Adjust hue for color transition
        return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 1.0F, 1.0F);
    }
}

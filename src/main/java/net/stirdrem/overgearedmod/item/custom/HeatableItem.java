package net.stirdrem.overgearedmod.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgearedmod.heat.HeatCapabilityProvider;

public class HeatableItem extends Item {
    public HeatableItem(Properties properties) {
        super(properties);
    }

    // Make the item show the durability bar (used here for heat)
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getCapability(HeatCapabilityProvider.ITEM_HEAT)
                .map(cap -> cap.getHeat() > 0)
                .orElse(false);
    }

    // Set how much of the bar is filled (inverted: 1.0 = empty)
    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(HeatCapabilityProvider.ITEM_HEAT)
                .map(cap -> Math.round(13.0F * cap.getHeat() / cap.getMaxHeat()))
                .orElse(0);
    }

    // Set the color of the durability bar (same logic as HeatBarDecorator)
    @Override
    public int getBarColor(ItemStack stack) {
        return stack.getCapability(HeatCapabilityProvider.ITEM_HEAT)
                .map(cap -> {
                    float heatRatio = (float) cap.getHeat() / cap.getMaxHeat();
                    float hue = 0.05f * (1.0f - heatRatio); // red to orange
                    return 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
                })
                .orElse(0xFFFFFFFF); // fallback color
    }
}

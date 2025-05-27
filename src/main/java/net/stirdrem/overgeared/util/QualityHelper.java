package net.stirdrem.overgeared.util;

import net.minecraft.world.item.ItemStack;

public class QualityHelper {
    public static float getQualityMultiplier(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("ForgingQuality")) {
            String quality = stack.getTag().getString("ForgingQuality");
            return switch (quality) {
                case "poor" -> 0.7f;  // 30% worse
                case "well" -> 1.0f;  // 10% better
                case "expert" -> 1.35f; // 30% better
                case "perfect" -> 1.7f; // 50% better
                default -> 1.0f;
            };
        }
        return 1.0f;
    }
}

package net.stirdrem.overgeared.util;

import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;

public class QualityHelper {



    public static float getQualityMultiplier(ItemStack stack) {
        // Get the quality string from the data component
        // Returns null if component is missing
        String quality = stack.get(OvergearedMod.FORGING_QUALITY);

        if (quality == null) {
            return 1.0f;  // No quality = vanilla behavior
        }
            return switch (quality) {
                case "poor" -> ServerConfig.POOR_DURABILITY_BONUS.get().floatValue();  // 30% worse
                case "well" -> ServerConfig.WELL_DURABILITY_BONUS.get().floatValue();  // 10% better
                case "expert" -> ServerConfig.EXPERT_DURABILITY_BONUS.get().floatValue(); // 30% better
                case "perfect" -> ServerConfig.PERFECT_DURABILITY_BONUS.get().floatValue(); // 50% better
                case "master" -> ServerConfig.MASTER_DURABILITY_BONUS.get().floatValue(); // 50% better
                default -> 1.0f;
            };
}

    public static void setQuality(ItemStack stack, String quality) {
        stack.set(OvergearedMod.FORGING_QUALITY, quality.toLowerCase());
    }
    private static boolean calculatingAttributes = false;

    public static boolean isCalculatingAttributes() {
        return calculatingAttributes;
    }

    public static void setCalculatingAttributes(boolean state) {
        calculatingAttributes = state;
    }
}

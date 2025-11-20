package net.stirdrem.overgeared.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.config.ServerConfig;

import javax.annotation.Nullable;
import java.util.Optional;

import static net.stirdrem.overgeared.ForgingQuality.*;

public class QualityHelper {
    //    public static float getQualityMultiplier(ItemStack stack) {
//        if (stack.has(DataComponents.CUSTOM_DATA) && stack.get(DataComponents.CUSTOM_DATA).contains("ForgingQuality")) {
//            String quality = stack.get(DataComponents.CUSTOM_DATA).copyTag().getString("ForgingQuality");
//            return switch (quality) {
//                case "poor" -> ServerConfig.POOR_DURABILITY_BONUS.get().floatValue();  // 30% worse
//                case "well" -> ServerConfig.WELL_DURABILITY_BONUS.get().floatValue();  // 10% better
//                case "expert" -> ServerConfig.EXPERT_DURABILITY_BONUS.get().floatValue(); // 30% better
//                case "perfect" -> ServerConfig.PERFECT_DURABILITY_BONUS.get().floatValue(); // 50% better
//                case "master" -> ServerConfig.MASTER_DURABILITY_BONUS.get().floatValue(); // 50% better
//                default -> 1.0f;
//            };
//        }
//        return 1.0f;
//    }
    // NPE safe but still wth is this
    public static float getQualityMultiplier(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.CUSTOM_DATA))
                .map(data -> data.copyTag().getString("ForgingQuality"))
                .map(ForgingQuality::fromString)
                .map(quality -> switch (quality) {
                    case POOR -> ServerConfig.POOR_DURABILITY_BONUS.get().floatValue();
                    case WELL -> ServerConfig.WELL_DURABILITY_BONUS.get().floatValue();
                    case EXPERT -> ServerConfig.EXPERT_DURABILITY_BONUS.get().floatValue();
                    case PERFECT -> ServerConfig.PERFECT_DURABILITY_BONUS.get().floatValue();
                    case MASTER -> ServerConfig.MASTER_DURABILITY_BONUS.get().floatValue();
                    case NONE -> null;
                })
                .orElse(1.0f);
    }

    private static boolean calculatingAttributes = false;

    public static boolean isCalculatingAttributes() {
        return calculatingAttributes;
    }

    public static void setCalculatingAttributes(boolean state) {
        calculatingAttributes = state;
    }
}

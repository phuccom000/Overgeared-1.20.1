package net.stirdrem.overgeared.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.ForgingQuality;

public class ForgingQualityHelper {

    public static final String QUALITY_TAG = "ForgingQuality";

    public static ForgingQuality rollQuality(RandomSource rand, int villagerLevel) {
        float roll = rand.nextFloat();

        // =========================
        // LEVEL 1–2
        // =========================
        if (villagerLevel <= 2) {
            if (roll < 0.08f) return ForgingQuality.EXPERT;   // 8%
            if (roll < 0.30f) return ForgingQuality.POOR;     // 22%
            return ForgingQuality.WELL;                       // 70%
        }

        // =========================
        // LEVEL 3–4
        // =========================
        if (villagerLevel <= 4) {
            if (roll < 0.06f) return ForgingQuality.PERFECT;  // 6%
            if (roll < 0.36f) return ForgingQuality.EXPERT;   // 30%
            return ForgingQuality.WELL;                       // 64%
        }

        // =========================
        // LEVEL 5
        // =========================
        if (roll < 0.003f) return ForgingQuality.MASTER;      // 0.3%
        if (roll < 0.05f) return ForgingQuality.PERFECT;     // 4.7%
        return ForgingQuality.EXPERT;                        // 95%
    }


    public static int getBasePrice(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).getPath();

        if (id.startsWith("stone_")) return 4;
        if (id.startsWith("copper_")) return 8;
        if (id.startsWith("iron_")) return 14;
        if (id.startsWith("golden_")) return 16;
        if (id.startsWith("steel_")) return 24;

        // vanilla fallback (diamond, netherite, etc)
        if (id.contains("diamond")) return 32;
        if (id.contains("netherite")) return 64;

        return 10; // default fallback
    }

    public static float getQualityMultiplier(ForgingQuality quality) {
        return switch (quality) {
            case POOR -> 0.75f;
            case WELL -> 1.0f;
            case EXPERT -> 1.4f;
            case PERFECT -> 2.0f;
            case MASTER -> 3.0f;
            default -> 1.0f;
        };
    }

    public static void applyQuality(ItemStack stack, ForgingQuality quality) {
        stack.getOrCreateTag().putString(QUALITY_TAG, quality.getDisplayName());
    }

    public static int priceForQuality(ForgingQuality quality) {
        return switch (quality) {
            case POOR -> 4;
            case WELL -> 7;
            case EXPERT -> 12;
            case PERFECT -> 20;
            case MASTER -> 32;
            default -> 5;
        };
    }
}


package net.stirdrem.overgeared;

import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.stirdrem.overgeared.config.ServerConfig;

import java.util.function.Supplier;

public enum BlueprintQuality {
    POOR("poor", 10, ChatFormatting.RED, () -> ServerConfig.POOR_MAX_USE),
    WELL("well", 15, ChatFormatting.YELLOW, () -> ServerConfig.WELL_MAX_USE),
    EXPERT("expert", 20, ChatFormatting.BLUE, () -> ServerConfig.EXPERT_MAX_USE),
    PERFECT("perfect", 25, ChatFormatting.GOLD, () -> ServerConfig.PERFECT_MAX_USE),
    MASTER("master", 30, ChatFormatting.LIGHT_PURPLE, () -> ServerConfig.MASTER_MAX_USE); // Final tier

    private final String id;
    private final int defaultUse;
    private final ChatFormatting color;
    private final java.util.function.Supplier<ModConfigSpec.IntValue> configSupplier;

    BlueprintQuality(String id, int defaultUse, ChatFormatting color, Supplier<ModConfigSpec.IntValue> configSupplier) {
        this.id = id;
        this.defaultUse = defaultUse;
        this.color = color;
        this.configSupplier = configSupplier;
    }

    public static int compare(String q1, String q2) {
        BlueprintQuality a = fromString(q1);
        BlueprintQuality b = fromString(q2);
        return Integer.compare(a.ordinal(), b.ordinal());
    }

    /**
     * Match a quality string safely.
     */
    public static BlueprintQuality fromString(String id) {
        for (BlueprintQuality q : values()) {
            if (q.id.equalsIgnoreCase(id)) return q;
        }
        return POOR; // fallback
    }

    /**
     * Get the next tier of blueprint quality.
     */
    public static BlueprintQuality getNext(BlueprintQuality current) {
        int index = current.ordinal();
        if (index + 1 < values().length) {
            return values()[index + 1];
        }
        return null; // Already at max
    }

    /**
     * Get the previous tier of blueprint quality.
     */
    public static BlueprintQuality getPrevious(BlueprintQuality current) {
        int index = current.ordinal();
        if (index - 1 >= 0) {
            return values()[index - 1];
        }
        return null; // Already at lowest
    }

    public static ChatFormatting getColor(String qualityName) {
        for (BlueprintQuality q : values()) {
            if (q.name().equalsIgnoreCase(qualityName)) {
                return q.color; // assuming the color field exists in your enum
            }
        }
        return ChatFormatting.GRAY;
    }

    public String getDisplayName() {
        return id;
    }

    public int getUse() {
        try {
            return configSupplier.get().get();
        } catch (IllegalStateException e) {
            // Config not loaded yet, return default
            return defaultUse;
        }
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getTranslationKey() {
        return "tooltip.overgeared.blueprint.quality." + name().toLowerCase();
    }

    public String getId() {
        return id;
    }

}

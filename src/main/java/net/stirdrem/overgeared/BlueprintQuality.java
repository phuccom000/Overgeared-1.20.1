package net.stirdrem.overgeared;

import net.minecraft.ChatFormatting;
import net.stirdrem.overgeared.config.ServerConfig;

public enum BlueprintQuality {
    POOR("poor", ServerConfig.POOR_MAX_USE.get(), ChatFormatting.RED),
    WELL("well", ServerConfig.WELL_MAX_USE.get(), ChatFormatting.YELLOW),
    EXPERT("expert", ServerConfig.EXPERT_MAX_USE.get(), ChatFormatting.BLUE),
    PERFECT("perfect", ServerConfig.PERFECT_MAX_USE.get(), ChatFormatting.GOLD),
    MASTER("master", ServerConfig.MASTER_MAX_USE.get(), ChatFormatting.LIGHT_PURPLE); // Final tier

    private final String id;
    private final int use;
    private final ChatFormatting color;

    BlueprintQuality(String id, int use, ChatFormatting color) {
        this.id = id;
        this.use = use;
        this.color = color;
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
        return use;
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

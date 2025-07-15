package net.stirdrem.overgeared;

public enum AnvilTier {
    STONE("stone", "gui.overgeared.tier.stone"),
    STEEL("steel", "gui.overgeared.tier.steel"),
    ABOVE_A("above_a", "gui.overgeared.tier.above_a"),
    ABOVE_B("above_b", "gui.overgeared.tier.above_b");

    private final String displayName;
    private final String lang;

    AnvilTier(String displayName, String lang) {
        this.displayName = displayName;
        this.lang = lang;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLang() {
        return lang;
    }

    public static AnvilTier fromDisplayName(String name) {
        for (AnvilTier tier : values()) {
            if (tier.displayName.equalsIgnoreCase(name)) {
                return tier;
            }
        }
        return null; // or throw IllegalArgumentException
    }

    public boolean isEqualOrLowerThan(AnvilTier other) {
        return this.ordinal() <= other.ordinal();
    }
}


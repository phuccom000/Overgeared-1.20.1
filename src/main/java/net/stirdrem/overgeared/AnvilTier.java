package net.stirdrem.overgeared;

public enum AnvilTier {
    STONE("stone"),
    STEEL("steel");

    private final String displayName;

    AnvilTier(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


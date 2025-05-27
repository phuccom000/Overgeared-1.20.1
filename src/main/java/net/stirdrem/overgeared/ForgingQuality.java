package net.stirdrem.overgeared;

public enum ForgingQuality {
    POOR("poor"),
    WELL("well"),
    EXPERT("expert"),
    PERFECT("perfect");

    private final String displayName;

    ForgingQuality(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


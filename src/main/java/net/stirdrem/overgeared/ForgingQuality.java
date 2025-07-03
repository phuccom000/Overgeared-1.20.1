package net.stirdrem.overgeared;

public enum ForgingQuality {
    POOR("poor"),
    WELL("well"),
    EXPERT("expert"),
    PERFECT("perfect"),
    MASTER("master");

    private final String displayName;

    ForgingQuality(String displayName) {
        this.displayName = displayName;
    }

    public static ForgingQuality fromString(String quality) {
        for (ForgingQuality q : values()) {
            if (q.displayName.equalsIgnoreCase(quality)) return q;
        }
        return POOR; // fallback
    }

    public String getDisplayName() {
        return displayName;
    }
}


package net.stirdrem.overgeared.entity;

public enum ArrowTier {
    FLINT("flint", 1.0f),
    IRON("iron", 1.2f),
    STEEL("steel", 1.5f),
    DIAMOND("diamond", 2.0f);

    private final String name;
    private final float damageBonus;

    ArrowTier(String name, float damageBonus) {
        this.name = name;
        this.damageBonus = damageBonus;
    }

    public float getDamageBonus() {
        return damageBonus;
    }

    public String getSerializedName() {
        return name;
    }
}

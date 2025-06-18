package net.stirdrem.overgeared.client;

import net.minecraft.util.StringRepresentable;

public enum ForgingBookRecipeBookTab {
    TOOLS("tools"),
    ARMORS("armors"),
    MISC("misc");

    public static final StringRepresentable.EnumCodec<net.minecraft.world.item.crafting.CookingBookCategory> CODEC = StringRepresentable.fromEnum(net.minecraft.world.item.crafting.CookingBookCategory::values);
    private final String name;

    ForgingBookRecipeBookTab(String pName) {
        this.name = pName;
    }

    public String getSerializedName() {
        return this.name;
    }

    public static ForgingBookRecipeBookTab findByName(String name) {
        for (ForgingBookRecipeBookTab value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
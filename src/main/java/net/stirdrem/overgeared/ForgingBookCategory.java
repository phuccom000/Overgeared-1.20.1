package net.stirdrem.overgeared;

import net.minecraft.util.StringRepresentable;

public enum ForgingBookCategory implements StringRepresentable {
    TOOLS("tools"),
    ARMORS("armors"),
    MISC("misc");

    public static final EnumCodec<net.minecraft.world.item.crafting.CookingBookCategory> CODEC = StringRepresentable.fromEnum(net.minecraft.world.item.crafting.CookingBookCategory::values);
    private final String name;

    private ForgingBookCategory(String pName) {
        this.name = pName;
    }

    public String getSerializedName() {
        return this.name;
    }
}
package net.stirdrem.overgeared.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public class ToolType {
    private final String id;
    private final String displayName;
    private final boolean isTranslatable;

    public ToolType(String id, String displayName, boolean isTranslatable) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Tool type ID cannot be null or empty");
        }
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be null or empty");
        }
        if (!id.matches("^[A-Z0-9_]+$")) {
            throw new IllegalArgumentException("Tool type ID must be uppercase alphanumeric with underscores");
        }

        this.id = id.toUpperCase(Locale.ROOT);
        this.displayName = displayName;
        this.isTranslatable = isTranslatable;
    }

    public String getId() {
        return id;
    }

    public MutableComponent getDisplayName() {
        return isTranslatable ?
                Component.translatable(displayName) :
                Component.literal(displayName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToolType toolType = (ToolType) o;
        return id.equals(toolType.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public boolean isTranslatable() {
        return isTranslatable;
    }

    // Default vanilla tool types (translatable)
    public static final ToolType SWORD = new ToolType("SWORD", "tooltype.minecraft.sword", true);
    public static final ToolType AXE = new ToolType("AXE", "tooltype.minecraft.axe", true);
    public static final ToolType PICKAXE = new ToolType("PICKAXE", "tooltype.minecraft.pickaxe", true);
    public static final ToolType SHOVEL = new ToolType("SHOVEL", "tooltype.minecraft.shovel", true);
    public static final ToolType HOE = new ToolType("HOE", "tooltype.minecraft.hoe", true);
    //public static final ToolType HAMMER = new ToolType("HAMMER", "tooltype.overgeared.hammer", true);
    public static final ToolType MULTITOOL = new ToolType("MULTITOOL", "tooltype.overgeared.multitool", true);

}
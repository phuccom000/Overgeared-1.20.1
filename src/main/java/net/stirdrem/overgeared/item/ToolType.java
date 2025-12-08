package net.stirdrem.overgeared.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;
import java.util.Set;

public class ToolType {

    // Vanilla known types (minecraft lang keys)


    public static final ToolType SWORD = new ToolType("SWORD");
    public static final ToolType AXE = new ToolType("AXE");
    public static final ToolType PICKAXE = new ToolType("PICKAXE");
    public static final ToolType SHOVEL = new ToolType("SHOVEL");
    public static final ToolType HOE = new ToolType("HOE");

    // Overgeared example
    public static final ToolType MULTITOOL = new ToolType("MULTITOOL");

    private final String id;
    private final String translationKey;

    public ToolType(String id) {
        if (id == null || id.isEmpty())
            throw new IllegalArgumentException("Tool type ID cannot be null or empty");

        if (!id.matches("^[A-Za-z0-9_]+$"))
            throw new IllegalArgumentException("Tool type ID must be alphanumeric with underscores");

        this.id = id.toLowerCase(Locale.ROOT); // ✅ internal canonical form

        // ✅ Translation always uses lowercase
        this.translationKey = "tooltype.overgeared." + this.id;

    }

    public String getId() {
        return id.toLowerCase();
    }

    public MutableComponent getDisplayName() {
        Component trans = Component.translatable(translationKey);
    /*    // If untranslated, fallback to literal ID
        if (trans.getString().equals(translationKey)) {
            return Component.literal(id);
        }
*/
        return trans.copy();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ToolType)) return false;
        return id.equals(((ToolType) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

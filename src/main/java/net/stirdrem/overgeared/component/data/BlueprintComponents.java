package net.stirdrem.overgeared.component.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;

import java.util.List;

public record BlueprintComponents(String quality, int uses, String toolType, boolean required) {
    public static final Codec<BlueprintComponents> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("Quality").orElse("POOR").forGetter(BlueprintComponents::quality),
                    Codec.INT.fieldOf("Uses").orElse(0).forGetter(BlueprintComponents::uses),
                    Codec.STRING.fieldOf("ToolType").orElse("SWORD").forGetter(BlueprintComponents::toolType),
                    Codec.BOOL.fieldOf("Required").orElse(false).forGetter(BlueprintComponents::required)
            ).apply(instance, BlueprintComponents::new)
    );

    // Helper method to create default data
    public static BlueprintComponents defaultValue() {
        List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
        String defaultToolType = !types.isEmpty() ? types.get(0).getId() : "SWORD";
        return new BlueprintComponents("POOR", 0, defaultToolType, false);
    }
}
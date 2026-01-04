package net.stirdrem.overgeared.datapack;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BlueprintTooltypesData {

    private final ResourceLocation id;
    private final List<String> toolTypes;

    public BlueprintTooltypesData(ResourceLocation id, List<String> toolTypes) {
        this.id = id;
        this.toolTypes = toolTypes;
    }

    public ResourceLocation getId() {
        return id;
    }

    public List<String> getToolTypes() {
        return toolTypes;
    }
}

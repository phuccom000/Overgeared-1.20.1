package net.stirdrem.overgeared.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ToolTypeRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlueprintTooltypesReloadListener
        extends SimpleJsonResourceReloadListener {

    public static final Map<ResourceLocation, BlueprintTooltypesData> DATA = new ConcurrentHashMap<>();

    private static final Gson GSON = new Gson();

    public BlueprintTooltypesReloadListener() {
        super(GSON, "blueprint_tooltypes");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> objects,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        DATA.clear();

        Map<ResourceLocation, BlueprintTooltypesData> tempMap = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue().getAsJsonObject();

            try {
                if (!json.has("tooltypes")) {
                    throw new JsonSyntaxException("Missing 'tooltypes' array: " + id);
                }

                JsonArray arr = json.getAsJsonArray("tooltypes");
                List<String> toolTypes = new ArrayList<>();

                for (JsonElement e : arr) {
                    toolTypes.add(e.getAsString().toLowerCase(Locale.ROOT));
                }

                BlueprintTooltypesData data =
                        new BlueprintTooltypesData(id, toolTypes);

                tempMap.put(id, data);

            } catch (Exception e) {
                OvergearedMod.LOGGER.error(
                        "Failed to load blueprint tooltypes: {}", id, e
                );
            }
        }

        DATA.putAll(tempMap);

        OvergearedMod.LOGGER.info(
                "Loaded {} blueprint tooltype packs",
                DATA.size()
        );

        ToolTypeRegistry.init();
    }

    public static Collection<BlueprintTooltypesData> getDataSnapshot() {
        return new ArrayList<>(DATA.values());
    }

    public static class BlueprintTooltypesData {
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
}
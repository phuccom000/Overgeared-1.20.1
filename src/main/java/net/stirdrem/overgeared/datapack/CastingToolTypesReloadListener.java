package net.stirdrem.overgeared.datapack;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CastingToolTypesReloadListener extends SimpleJsonResourceReloadListener {

    public static class CastingToolEntry {
        private final String toolType;
        private final int maxMaterialAmount;

        public CastingToolEntry(String toolType, int maxMaterialAmount) {
            this.toolType = toolType;
            this.maxMaterialAmount = maxMaterialAmount;
        }

        public String getToolType() {
            return toolType;
        }

        public int getMaxMaterialAmount() {
            return maxMaterialAmount;
        }
    }

    private static final Map<ResourceLocation, List<CastingToolEntry>> DATA = new ConcurrentHashMap<>();
    public static final CastingToolTypesReloadListener INSTANCE = new CastingToolTypesReloadListener();
    private static final Gson GSON = new Gson();

    public CastingToolTypesReloadListener() {
        super(GSON, "casting_tooltypes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        DATA.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement jsonElement = entry.getValue();

            try {
                if (jsonElement.isJsonObject()) {
                    JsonObject json = jsonElement.getAsJsonObject();
                    List<CastingToolEntry> toolEntries = parseToolEntries(json);
                    DATA.put(id, toolEntries);
                } else {
                    throw new JsonSyntaxException("Expected JSON object for casting tool types entry: " + id);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse casting tool types entry: " + id + ", error: " + e.getMessage());
            }
        }

        System.out.println("Loaded " + DATA.size() + " casting tool types entries");
    }

    private List<CastingToolEntry> parseToolEntries(JsonObject json) {
        List<CastingToolEntry> entries = new ArrayList<>();

        if (!json.has("tools")) {
            throw new JsonSyntaxException("Missing 'tools' array for casting tool types entry");
        }

        JsonArray toolsArray = json.getAsJsonArray("tools");
        for (JsonElement toolElement : toolsArray) {
            if (!toolElement.isJsonArray()) {
                throw new JsonSyntaxException("Expected array for tool entry");
            }

            JsonArray toolArray = toolElement.getAsJsonArray();
            if (toolArray.size() != 2) {
                throw new JsonSyntaxException("Tool entry must have exactly 2 elements: [tool_type, max_material_amount]");
            }

            String toolType = toolArray.get(0).getAsString();
            int maxMaterialAmount = toolArray.get(1).getAsInt();

            if (maxMaterialAmount <= 0) {
                throw new JsonSyntaxException("Max material amount must be positive: " + maxMaterialAmount);
            }

            entries.add(new CastingToolEntry(toolType, maxMaterialAmount));
        }

        return entries;
    }

    public static Map<ResourceLocation, List<CastingToolEntry>> getData() {
        return Collections.unmodifiableMap(DATA);
    }

    public static List<CastingToolEntry> getAllToolEntries() {
        List<CastingToolEntry> allEntries = new ArrayList<>();
        for (List<CastingToolEntry> entries : DATA.values()) {
            allEntries.addAll(entries);
        }
        return allEntries;
    }

    public static Optional<CastingToolEntry> getEntryForToolType(String toolType) {
        return getAllToolEntries().stream()
                .filter(entry -> entry.getToolType().equalsIgnoreCase(toolType))
                .findFirst();
    }

    public static int getMaxMaterialAmount(String toolType) {
        return getEntryForToolType(toolType)
                .map(CastingToolEntry::getMaxMaterialAmount)
                .orElse(0);
    }

    public static Set<String> getAllToolTypes() {
        Set<String> toolTypes = new HashSet<>();
        for (CastingToolEntry entry : getAllToolEntries()) {
            toolTypes.add(entry.getToolType());
        }
        return toolTypes;
    }

    public static void clear() {
        DATA.clear();
    }
}
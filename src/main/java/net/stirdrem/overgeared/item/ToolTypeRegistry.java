package net.stirdrem.overgeared.item;

import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.BlueprintTooltypesReloadListener;

import java.util.*;
import java.util.stream.Collectors;

public class ToolTypeRegistry {

    private static final List<ToolType> HIDDEN_TYPES = new ArrayList<>();
    private static final Map<String, ToolType> BY_ID = new HashMap<>();

    public static void init() {
        BY_ID.clear();
        HIDDEN_TYPES.clear();

        BlueprintTooltypesReloadListener.DATA.values().forEach(data -> {
            for (String id : data.getToolTypes()) {
                BY_ID.computeIfAbsent(id.toLowerCase(Locale.ROOT), ToolType::new);
            }
        });

        // Load available types from config
        List<? extends String> availableIds = ServerConfig.AVAILABLE_TOOL_TYPES.get();
        if (availableIds != null) {
            for (String id : availableIds) {
                BY_ID.computeIfAbsent(id.toLowerCase(Locale.ROOT), ToolType::new);
            }
        }

        // Load hidden types from config (same format: just IDs)
        List<? extends String> hiddenIds = ServerConfig.HIDDEN_TOOL_TYPES.get();
        if (hiddenIds != null) {
            for (String id : hiddenIds) {
                BY_ID.computeIfPresent(id.toLowerCase(Locale.ROOT), (k, t) -> {
                    HIDDEN_TYPES.add(t);
                    return t;
                });
            }
        }

        System.out.println("Registered tool types: " +
                BY_ID.keySet().stream().collect(Collectors.joining(", "))
        );
        OvergearedMod.LOGGER.info("Tool types initialized: {}",
                ToolTypeRegistry.getRegisteredTypes().size());
        //}
    }

    public static List<ToolType> getRegisteredTypes() {
        List<ToolType> result = new ArrayList<>();
        List<? extends String> allowed = ServerConfig.AVAILABLE_TOOL_TYPES.get();

        // If config list is EMPTY → allow everything (defaults + datapack)
        if (allowed == null || allowed.isEmpty()) {
            for (ToolType type : BY_ID.values()) {
                if (!HIDDEN_TYPES.contains(type)) {
                    result.add(type);
                }
            }
            return result;
        }

        // If config list is present → include ALL registered types, but filter out:
        // 1. Types not in the allowed list (unless allowed list is empty)
        // 2. Hidden types
        for (ToolType type : BY_ID.values()) {
            boolean isHidden = HIDDEN_TYPES.contains(type);

            if (!isHidden) {
                result.add(type);
            }
        }

        return result;
    }


    public static List<ToolType> getRegisteredTypesAll() {
        List<ToolType> list = getRegisteredTypes();
        list.addAll(HIDDEN_TYPES);
        return list;
    }

    public static Optional<ToolType> byId(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(BY_ID.get(id.toLowerCase(Locale.ROOT)));
    }
}

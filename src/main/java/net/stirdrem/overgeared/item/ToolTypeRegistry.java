package net.stirdrem.overgeared.item;

import net.stirdrem.overgeared.config.ServerConfig;

import java.util.*;
import java.util.stream.Collectors;

public class ToolTypeRegistry {

    private static final List<ToolType> DEFAULT_TYPES = List.of(
            ToolType.SWORD,
            ToolType.AXE,
            ToolType.PICKAXE,
            ToolType.SHOVEL,
            ToolType.HOE,
            ToolType.MULTITOOL
    );

    private static final List<ToolType> HIDDEN_TYPES = new ArrayList<>();
    private static final Map<String, ToolType> BY_ID = new HashMap<>();

    public static void init() {
        BY_ID.clear();
        HIDDEN_TYPES.clear();

        // Register default types
        DEFAULT_TYPES.forEach(type ->
                BY_ID.put(type.getId(), type)
        );

        // Load available types from config
        List<? extends String> availableIds = ServerConfig.AVAILABLE_TOOL_TYPES.get();
        if (availableIds != null) {
            for (String id : availableIds) {
                ToolTypeRegistry.byId(id).ifPresent(type -> {
                }); // ensure reference
            }
        }

        // Load hidden types from config (same format: just IDs)
        List<? extends String> hiddenIds = ServerConfig.HIDDEN_TOOL_TYPES.get();
        if (hiddenIds != null) {
            for (String id : hiddenIds) {
                BY_ID.computeIfPresent(id.toUpperCase(Locale.ROOT), (k, t) -> {
                    HIDDEN_TYPES.add(t);
                    return t;
                });
            }
        }

        System.out.println("Registered tool types: " +
                BY_ID.keySet().stream().collect(Collectors.joining(", "))
        );
    }

    public static List<ToolType> getRegisteredTypes() {
        List<ToolType> list = new ArrayList<>();
        List<? extends String> allowed = ServerConfig.AVAILABLE_TOOL_TYPES.get();
        if (allowed == null) return list;

        for (String id : allowed) {
            String key = id.toUpperCase(Locale.ROOT);

            // If exists in registry (default or already created)
            Optional<ToolType> existing = byId(key);
            if (existing.isPresent()) {
                list.add(existing.get());
            } else {
                // Create NEW ToolType dynamically
                ToolType newType = new ToolType(key);
                BY_ID.put(key, newType);
                list.add(newType);
            }
        }

        return list;
    }


    public static List<ToolType> getRegisteredTypesAll() {
        List<ToolType> list = getRegisteredTypes();
        list.addAll(HIDDEN_TYPES);
        return list;
    }

    public static Optional<ToolType> byId(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(BY_ID.get(id.toUpperCase(Locale.ROOT)));
    }
}

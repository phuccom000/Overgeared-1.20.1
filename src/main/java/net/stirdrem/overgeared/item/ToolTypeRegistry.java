package net.stirdrem.overgeared.item;

import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.BlueprintTooltypesReloadListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ToolTypeRegistry {

    private static final List<ToolType> HIDDEN_TYPES = new ArrayList<>();
    public static final Map<String, ToolType> BY_ID = new ConcurrentHashMap<>();

    public static void init() {
        synchronized (BY_ID) {
            BY_ID.clear();
        }
        synchronized (HIDDEN_TYPES) {
            HIDDEN_TYPES.clear();
        }

        for (BlueprintTooltypesReloadListener.BlueprintTooltypesData data :
                BlueprintTooltypesReloadListener.getDataSnapshot()) {
            for (String id : data.getToolTypes()) {
                BY_ID.computeIfAbsent(id.toLowerCase(Locale.ROOT), ToolType::new);
            }
        }

        List<? extends String> availableIds = ServerConfig.AVAILABLE_TOOL_TYPES.get();
        if (availableIds != null) {
            for (String id : availableIds) {
                BY_ID.computeIfAbsent(id.toLowerCase(Locale.ROOT), ToolType::new);
            }
        }

        List<? extends String> hiddenIds = ServerConfig.HIDDEN_TOOL_TYPES.get();
        if (hiddenIds != null) {
            for (String id : hiddenIds) {
                BY_ID.computeIfPresent(id.toLowerCase(Locale.ROOT), (k, t) -> {
                    synchronized (HIDDEN_TYPES) {
                        HIDDEN_TYPES.add(t);
                    }
                    return t;
                });
            }
        }

        System.out.println("Registered tool types: " +
                BY_ID.keySet().stream().collect(Collectors.joining(", "))
        );
        OvergearedMod.LOGGER.info("Tool types initialized: {}",
                ToolTypeRegistry.getRegisteredTypes().size());
    }

    public static List<ToolType> getRegisteredTypes() {
        List<ToolType> result = new ArrayList<>();
        List<? extends String> allowed = ServerConfig.AVAILABLE_TOOL_TYPES.get();

        Map<String, ToolType> snapshot;
        synchronized (BY_ID) {
            snapshot = new HashMap<>(BY_ID);
        }

        List<ToolType> hiddenSnapshot;
        synchronized (HIDDEN_TYPES) {
            hiddenSnapshot = new ArrayList<>(HIDDEN_TYPES);
        }

        if (allowed == null || allowed.isEmpty()) {
            for (ToolType type : snapshot.values()) {
                if (!hiddenSnapshot.contains(type)) {
                    result.add(type);
                }
            }
            return result;
        }

        for (ToolType type : snapshot.values()) {
            boolean isHidden = hiddenSnapshot.contains(type);

            if (!isHidden) {
                result.add(type);
            }
        }

        return result;
    }

    public static List<ToolType> getRegisteredTypesAll() {
        List<ToolType> list = getRegisteredTypes();
        synchronized (HIDDEN_TYPES) {
            list.addAll(HIDDEN_TYPES);
        }
        return list;
    }

    public static Optional<ToolType> byId(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(BY_ID.get(id.toLowerCase(Locale.ROOT)));
    }
}
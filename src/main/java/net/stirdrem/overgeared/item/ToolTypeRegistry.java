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

    private static final List<ToolType> CUSTOM_TYPES = new ArrayList<>();
    private static final List<ToolType> HIDDEN_CUSTOM_TYPES = new ArrayList<>();
    private static final Map<String, ToolType> BY_ID = new HashMap<>();

    public static void init() {
        CUSTOM_TYPES.clear();
        BY_ID.clear();

        // Register default types with consistent case
        DEFAULT_TYPES.forEach(type -> {
            BY_ID.put(type.getId().toUpperCase(Locale.ROOT), type);
            System.out.println("Registered default tool type: " + type.getId()); // Debug
        });

        // Parse and register custom types
        List<? extends String> customPairs = ServerConfig.CUSTOM_TOOL_TYPES.get();
        for (int i = 0; i < customPairs.size(); i += 2) {
            if (i + 1 >= customPairs.size()) break;

            String typeId = customPairs.get(i);
            String displayName = customPairs.get(i + 1);

            try {
                ToolType newType = new ToolType(typeId, displayName, false);
                register(newType);
                System.out.println("Registered custom tool type: " + newType.getId()); // Debug
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid tool type definition: " + typeId + " - " + e.getMessage());
            }
        }
        List<? extends String> hiddenPairs = ServerConfig.HIDDEN_TOOL_TYPES.get();
        for (int i = 0; i < hiddenPairs.size(); i += 2) {
            if (i + 1 >= hiddenPairs.size()) break;

            String typeId = hiddenPairs.get(i);
            String displayName = hiddenPairs.get(i + 1);

            try {
                ToolType hiddenType = new ToolType(typeId, displayName, false);
                registerHidden(hiddenType);
                System.out.println("Registered hidden tool type: " + hiddenType.getId()); // Debug
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid hidden tool type definition: " + typeId + " - " + e.getMessage());
            }
        }
        // Debug output
        System.out.println("Currently registered tool types: " +
                BY_ID.keySet().stream().collect(Collectors.joining(", ")));
    }

    public static void register(ToolType type) {
        String upperId = type.getId().toUpperCase(Locale.ROOT);
        if (!BY_ID.containsKey(upperId)) {
            BY_ID.put(upperId, type);
            CUSTOM_TYPES.add(type);
        }
    }

    public static void registerHidden(ToolType type) {
        String upperId = type.getId().toUpperCase(Locale.ROOT);
        if (!BY_ID.containsKey(upperId)) {
            BY_ID.put(upperId, type);
            HIDDEN_CUSTOM_TYPES.add(type);
        }
    }

    public static List<ToolType> getRegisteredTypes() {
        List<ToolType> allTypes = new ArrayList<>();

        // Get enabled default types

        List<? extends String> availableTypes = ServerConfig.AVAILABLE_TOOL_TYPES.get();
        DEFAULT_TYPES.stream()
                .filter(type -> availableTypes.contains(type.getId()))
                .forEach(allTypes::add);

        // Add all custom types
        allTypes.addAll(CUSTOM_TYPES);

        return allTypes;
    }

    public static List<ToolType> getRegisteredTypesAll() {
        List<ToolType> allTypes = new ArrayList<>();

        // Get enabled default types

        List<? extends String> availableTypes = ServerConfig.AVAILABLE_TOOL_TYPES.get();
        DEFAULT_TYPES.stream()
                .filter(type -> availableTypes.contains(type.getId()))
                .forEach(allTypes::add);

        // Add all custom types
        allTypes.addAll(CUSTOM_TYPES);
        allTypes.addAll(HIDDEN_CUSTOM_TYPES);

        return allTypes;
    }


    public static Optional<ToolType> byId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        ToolType type = BY_ID.get(id.toUpperCase(Locale.ROOT));
        //System.out.println("Looking up tool type '" + id + "' -> " +                (type != null ? "FOUND: " + type.getId() : "NOT FOUND")); // Debug
        return Optional.ofNullable(type);
    }
}
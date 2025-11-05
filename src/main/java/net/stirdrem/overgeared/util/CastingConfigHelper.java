package net.stirdrem.overgeared.util;

import net.stirdrem.overgeared.config.ServerConfig;

import java.util.List;

public class CastingConfigHelper {

    // -----------------------
    // Tool Types
    // -----------------------

    /**
     * Return the translation key for a tool type ID.
     * Example: "SWORD" -> "tooltype.overgeared.sword"
     */
    public static String getToolTypeDisplayName(String toolType) {
        return "tooltype.overgeared." + toolType.toLowerCase();
    }

    /**
     * Get max material amount for a tool type
     * config row: [tool_id, max_amount]
     */
    public static int getMaxMaterialAmount(String toolType) {
        for (var e : ServerConfig.CASTING_TOOL_TYPES.get()) {
            List<?> row = (List<?>) e;
            if (row.get(0).equals(toolType)) {
                return ((Number) row.get(1)).intValue();
            }
        }
        return 0;
    }

    // -----------------------
    // Material Types
    // -----------------------

    /**
     * Return the translation key for a material ID.
     * Example: "IRON" -> "material.overgeared.iron"
     */
    public static String getMaterialDisplayName(String materialId) {
        return "material.overgeared." + materialId.toLowerCase();
    }

    // -----------------------
    // Tool Head → Tool Type
    // -----------------------

    public static String getToolTypeForItem(String itemId) {
        for (var e : ServerConfig.TOOL_HEAD_SETTING.get()) {
            List<?> row = (List<?>) e;
            if (row.get(0).equals(itemId)) {
                return (String) row.get(1);
            }
        }
        return "none";
    }

    // -----------------------
    // Material from Item
    // -----------------------

    public static String getMaterialForItem(String itemId) {
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            if (row.get(0).equals(itemId)) {
                return (String) row.get(1);
            }
        }
        return "none";
    }

    public static int getMaterialValue(String itemId) {
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            if (row.get(0).equals(itemId)) {
                return ((Number) row.get(2)).intValue();
            }
        }
        return 0;
    }

    public static boolean isValidMaterial(String itemId) {
        // Check if this item exists in MATERIAL_SETTING table
        for (var entry : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) entry;

            // Row format: [item_id, material_name, value]
            if (row.size() >= 3 && row.get(0).equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all item IDs that belong to a given tool type.
     * Example: "SWORD" -> ["minecraft:iron_sword_head", "minecraft:diamond_sword_head"]
     */
    public static List<String> getAllItemsWithToolType(String toolType) {
        List<String> items = new java.util.ArrayList<>();

        for (var e : ServerConfig.TOOL_HEAD_SETTING.get()) {
            List<?> row = (List<?>) e;

            // Row format: [itemId, toolType]
            if (row.size() >= 2 && row.get(1).equals(toolType)) {
                items.add((String) row.get(0));
            }
        }

        return items;
    }

}

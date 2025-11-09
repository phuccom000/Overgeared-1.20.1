package net.stirdrem.overgeared.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.recipe.ItemToToolTypeRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

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
    public static String getToolTypeForItem(Level level, ItemStack stack) {
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ITEM_TO_TOOLTYPE.get())
                .stream()
                .filter(r -> r.getInput().test(stack))
                .map(ItemToToolTypeRecipe::getToolType)
                .findFirst()
                .orElse("none");
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

}

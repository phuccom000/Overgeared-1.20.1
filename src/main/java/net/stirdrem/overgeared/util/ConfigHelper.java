package net.stirdrem.overgeared.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.datapack.MaterialSettingsReloadListener;

import java.util.List;
import java.util.Optional;

public class ConfigHelper {

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
          if (e.get(0).equals(toolType)) {
                return ((Number) e.get(1)).intValue();
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
//    public static String getToolTypeForItem(Level level, ItemStack stack) {
//        return level.getRecipeManager()
//                .getAllRecipesFor(ModRecipeTypes.ITEM_TO_TOOLTYPE.get())
//                .stream()
//                .filter(r -> r.getInput().test(stack))
//                .map(ItemToToolTypeRecipe::getToolType)
//                .findFirst()
//                .orElse("none");
//    }

    // -----------------------
    // Material from Item (UPDATED - uses datapack)
    // -----------------------

    /**
     * Returns the material name for the given item/stack
     */
    public static String getMaterialForItem(ItemStack stack) {
        return getMaterialForItem(stack.getItem());
    }

    public static String getMaterialForItem(Item item) {
        // First check datapack entries
        Optional<MaterialSettingsReloadListener.MaterialEntry> datapackEntry =
                MaterialSettingsReloadListener.getAllMaterialEntries().stream()
                        .filter(entry -> matchesItemOrTag(item, entry.getItemOrTag()))
                        .findFirst();

        if (datapackEntry.isPresent()) {
            return datapackEntry.get().getMaterialId();
        }

        // Fallback to config for backward compatibility
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
          String key = (String) e.get(0);
            if (matchesItemOrTag(item, key)) {
                return (String) e.get(1);
            }
        }
        return "none";
    }

    /**
     * Returns the material value for the given item/stack
     */
    public static int getMaterialValue(ItemStack stack) {
        return getMaterialValue(stack.getItem());
    }

    public static int getMaterialValue(Item item) {
        // First check datapack entries
        Optional<MaterialSettingsReloadListener.MaterialEntry> datapackEntry =
                MaterialSettingsReloadListener.getAllMaterialEntries().stream()
                        .filter(entry -> matchesItemOrTag(item, entry.getItemOrTag()))
                        .findFirst();

        if (datapackEntry.isPresent()) {
            return datapackEntry.get().getMaterialValue();
        }

        // Fallback to config for backward compatibility
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
          String key = (String) e.get(0);
            if (matchesItemOrTag(item, key)) {
                return ((Number) e.get(2)).intValue();
            }
        }
        return 0;
    }

    /**
     * Checks if the item/stack is a valid material
     */
    public static boolean isValidMaterial(ItemStack stack) {
        return isValidMaterial(stack.getItem());
    }

    public static boolean isValidMaterial(Item item) {
        // First check datapack entries
        boolean isValidInDatapack = MaterialSettingsReloadListener.getAllMaterialEntries().stream()
                .anyMatch(entry -> matchesItemOrTag(item, entry.getItemOrTag()));

        if (isValidInDatapack) {
            return true;
        }

        // Fallback to config for backward compatibility
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
          String key = (String) e.getFirst();
            if (matchesItemOrTag(item, key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all material values for an item (useful when an item belongs to multiple materials)
     */
    public static java.util.Map<String, Integer> getMaterialValuesForItem(ItemStack stack) {
        return getMaterialValuesForItem(stack.getItem());
    }

    public static java.util.Map<String, Integer> getMaterialValuesForItem(Item item) {
        java.util.Map<String, Integer> result = new java.util.HashMap<>();

        // Add datapack entries
        MaterialSettingsReloadListener.getAllMaterialEntries().stream()
                .filter(entry -> matchesItemOrTag(item, entry.getItemOrTag()))
                .forEach(entry -> result.put(entry.getMaterialId(), entry.getMaterialValue()));

        // Add config entries (will override datapack if same material ID)
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
          String key = (String) e.getFirst();
            if (matchesItemOrTag(item, key)) {
                String materialId = (String) e.get(1);
                int value = ((Number) e.get(2)).intValue();
                result.put(materialId, value);
            }
        }

        return result;
    }

    /**
     * Helper: matches an item against an item ID or tag key
     */
    private static boolean matchesItemOrTag(Item item, String key) {
        // Exact match by registry name
        if (BuiltInRegistries.ITEM.getKey(item).toString().equals(key)) return true;

        // Tag check if the key starts with "#"
        if (key.startsWith("#")) {
            String tagId = key.substring(1); // Remove #
            TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(tagId));

            return new ItemStack(item).is(tag);
        }

        return false;
    }

    // -----------------------
    // New utility methods for datapack integration
    // -----------------------

    /**
     * Get all available material IDs from both datapack and config
     */
    public static java.util.Set<String> getAllMaterialIds() {
        java.util.Set<String> materialIds = new java.util.HashSet<>();

        // From datapack
        MaterialSettingsReloadListener.getAllMaterialEntries().stream()
                .map(MaterialSettingsReloadListener.MaterialEntry::getMaterialId)
                .forEach(materialIds::add);

        // From config
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
          materialIds.add((String) e.get(1));
        }

        return materialIds;
    }

    /**
     * Get all items for a specific material
     */
    public static List<String> getItemsForMaterial(String materialId) {
        List<String> items = new java.util.ArrayList<>();

        // From datapack
        MaterialSettingsReloadListener.getEntriesForMaterial(materialId).stream()
                .map(MaterialSettingsReloadListener.MaterialEntry::getItemOrTag)
                .forEach(items::add);

        // From config
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
          if (e.get(1).equals(materialId)) {
                items.add((String) e.getFirst());
            }
        }

        return items;
    }
}
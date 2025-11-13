package net.stirdrem.overgeared.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
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

    /**
     * Returns the material name for the given item/stack
     */
    public static String getMaterialForItem(ItemStack stack) {
        return getMaterialForItem(stack.getItem());
    }

    public static String getMaterialForItem(Item item) {
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            String key = (String) row.get(0);
            if (matchesItemOrTag(item, key)) {
                return (String) row.get(1);
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
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            String key = (String) row.get(0);
            if (matchesItemOrTag(item, key)) {
                return ((Number) row.get(2)).intValue();
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
        for (var e : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) e;
            String key = (String) row.get(0);
            if (matchesItemOrTag(item, key)) {
                return true;
            }
        }
        return false;
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
            TagKey<Item> tag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(tagId));
            return item.builtInRegistryHolder().is(tag);
        }

        return false;
    }
}

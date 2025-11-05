package net.stirdrem.overgeared.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.CastingConfigHelper;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class DynamicToolCastRecipe extends CustomRecipe {

    public DynamicToolCastRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    private static String getItemIdStringSafe(ItemStack stack) {
        if (stack.isEmpty()) return "NONE";
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) return "NONE";
        return key.toString();
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        ItemStack cast = ItemStack.EMPTY;
        boolean foundMaterial = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.isEmpty()) continue;

            if (stack.is(ModItems.FIRED_TOOL_CAST.get()) || stack.is(ModItems.NETHER_TOOL_CAST.get())) {
                if (!cast.isEmpty()) {
                    return false;
                }
                cast = stack;
                continue;
            }

            String itemId = getItemIdStringSafe(stack);
            String material = CastingConfigHelper.getMaterialForItem(itemId);

            if (!material.equals("NONE")) {
                foundMaterial = true;
                continue;
            }

            return false;
        }

        return !cast.isEmpty() && foundMaterial;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack cast = ItemStack.EMPTY;
        HashMap<String, Integer> materialTotals = new HashMap<>();
        List<ItemStack> inputItems = new ArrayList<>(); // Store the actual ItemStacks for comparison
        int newAmount = 0;
        int maxAmount = 0;
        String toolType = "NONE";

        // Scan grid
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            // Find cast
            if (stack.is(ModItems.FIRED_TOOL_CAST.get()) || stack.is(ModItems.NETHER_TOOL_CAST.get())) {
                cast = stack.copy();
                CompoundTag tag = cast.getOrCreateTag();
                toolType = tag.getString("ToolType");
                maxAmount = CastingConfigHelper.getMaxMaterialAmount(toolType);
            }
            // Process materials
            else if (!stack.isEmpty()) {
                String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                String material = CastingConfigHelper.getMaterialForItem(itemId);
                if (!material.equals("NONE")) {
                    int value = CastingConfigHelper.getMaterialValue(itemId);
                    materialTotals.put(material, materialTotals.getOrDefault(material, 0) + value);
                    newAmount += value;

                    // Store the actual ItemStack for comparison
                    ItemStack singleItem = stack.copy();
                    singleItem.setCount(1);
                    inputItems.add(singleItem);
                }
            }
        }

        if (cast.isEmpty()) return ItemStack.EMPTY;

        // === Load existing values ===
        CompoundTag castTag = cast.getOrCreateTag();
        CompoundTag existingMatTag = castTag.getCompound("Materials");
        int existingAmount = castTag.getInt("Amount");

        // Sum total after adding
        int totalAmount = existingAmount + newAmount;

        // ❌ Block if exceeds
        if (maxAmount > 0 && totalAmount > maxAmount) return ItemStack.EMPTY;

        // ✅ Merge materials
        for (String mat : existingMatTag.getAllKeys()) {
            int oldVal = existingMatTag.getInt(mat);
            materialTotals.put(mat, materialTotals.getOrDefault(mat, 0) + oldVal);
        }

        // ✅ Write merged data back
        CompoundTag newMatTag = new CompoundTag();
        for (var entry : materialTotals.entrySet()) {
            newMatTag.putInt(entry.getKey(), entry.getValue());
        }

        // ✅ Add complete item data to the "input" list, merging duplicates
        addItemStacksToInputList(castTag, inputItems);

        castTag.put("Materials", newMatTag);
        castTag.putInt("Amount", totalAmount);
        castTag.putString("ToolType", toolType);

        return cast;
    }

    /**
     * Adds complete item stack data to the "input" NBT list, merging duplicates
     */
    private void addItemStacksToInputList(CompoundTag castTag, List<ItemStack> newInputItems) {
        // Get or create the "input" list
        ListTag inputList;
        if (castTag.contains("input", Tag.TAG_LIST)) {
            inputList = castTag.getList("input", Tag.TAG_COMPOUND);
        } else {
            inputList = new ListTag();
        }

        // Convert existing input list to a list of ItemStacks for comparison
        List<ItemStack> existingItems = new ArrayList<>();
        for (Tag inputTag : inputList) {
            if (inputTag instanceof CompoundTag) {
                ItemStack existingItem = ItemStack.of((CompoundTag) inputTag);
                if (!existingItem.isEmpty()) {
                    existingItems.add(existingItem);
                }
            }
        }

        // Merge new items with existing items
        for (ItemStack newItem : newInputItems) {
            boolean merged = false;

            // Try to merge with existing items
            for (int i = 0; i < existingItems.size(); i++) {
                ItemStack existingItem = existingItems.get(i);

                // Check if items are identical (same item, same NBT)
                if (areItemStacksIdentical(existingItem, newItem)) {
                    // Increase count of existing item
                    existingItem.setCount(existingItem.getCount() + newItem.getCount());
                    merged = true;
                    break;
                }
            }

            // If not merged, add as new entry
            if (!merged) {
                existingItems.add(newItem.copy());
            }
        }

        // Convert back to NBT list
        ListTag mergedInputList = new ListTag();
        for (ItemStack item : existingItems) {
            CompoundTag itemTag = new CompoundTag();
            item.save(itemTag); // Save with updated count
            mergedInputList.add(itemTag);
        }

        // Save the updated list back to the cast
        castTag.put("input", mergedInputList);
    }

    /**
     * Checks if two ItemStacks are identical (same item and same NBT)
     */
    private boolean areItemStacksIdentical(ItemStack stack1, ItemStack stack2) {
        // Check if items are the same
        if (!ItemStack.isSameItem(stack1, stack2)) {
            return false;
        }

        // Check if NBT tags are the same
        CompoundTag tag1 = stack1.getTag();
        CompoundTag tag2 = stack2.getTag();

        if (tag1 == null && tag2 == null) {
            return true;
        }

        if (tag1 == null || tag2 == null) {
            return false;
        }

        return tag1.equals(tag2);
    }

    /**
     * Alternative: More efficient version that works directly with NBT
     */
    private void addItemStacksToInputListEfficient(CompoundTag castTag, List<ItemStack> newInputItems) {
        // Get or create the "input" list
        ListTag inputList;
        if (castTag.contains("input", Tag.TAG_LIST)) {
            inputList = castTag.getList("input", Tag.TAG_COMPOUND);
        } else {
            inputList = new ListTag();
        }

        // Create a map to track items by their NBT signature
        HashMap<String, CompoundTag> itemMap = new HashMap<>();

        // Process existing items
        for (Tag inputTag : inputList) {
            if (inputTag instanceof CompoundTag) {
                CompoundTag itemTag = (CompoundTag) inputTag;
                String signature = getItemSignature(itemTag);
                itemMap.put(signature, itemTag);
            }
        }

        // Process new items
        for (ItemStack newItem : newInputItems) {
            CompoundTag newItemTag = new CompoundTag();
            newItem.save(newItemTag);
            String signature = getItemSignature(newItemTag);

            if (itemMap.containsKey(signature)) {
                // Merge with existing item
                CompoundTag existingTag = itemMap.get(signature);
                int existingCount = existingTag.getByte("Count") & 0xFF; // Minecraft stores count as byte
                int newCount = newItemTag.getByte("Count") & 0xFF;
                existingTag.putByte("Count", (byte) Math.min(existingCount + newCount, 64)); // Cap at stack size
            } else {
                // Add as new item
                itemMap.put(signature, newItemTag);
            }
        }

        // Convert map back to list
        ListTag mergedInputList = new ListTag();
        mergedInputList.addAll(itemMap.values());

        // Save the updated list back to the cast
        castTag.put("input", mergedInputList);
    }

    /**
     * Creates a unique signature for an item based on its ID and NBT
     */
    private String getItemSignature(CompoundTag itemTag) {
        String itemId = itemTag.getString("id");
        CompoundTag tag = itemTag.getCompound("tag");
        return itemId + "#" + tag.toString(); // Use NBT string as part of signature
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRAFTING_DYNAMIC_TOOL_CAST.get();
    }
}
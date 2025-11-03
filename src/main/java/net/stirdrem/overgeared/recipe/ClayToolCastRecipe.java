package net.stirdrem.overgeared.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.CastingConfigHelper;

public class ClayToolCastRecipe extends CustomRecipe {

    private static final int[] CLAY_SLOTS = {1, 3, 5, 7}; // N,E,S,W around center

    public ClayToolCastRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        if (inv.getContainerSize() != 9) return false;

        ItemStack center = inv.getItem(4);
        if (center.isEmpty()) return false;

        String itemId = center.getItem().toString();

        // Must be mapped to a tool type in config
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(center.getItem());
        String toolType = CastingConfigHelper.getToolTypeForItem(id.toString().toLowerCase());
        if ("none".equals(toolType)) return false;

        // Check 4 clay at N/E/S/W
        for (int slot : CLAY_SLOTS) {
            if (!inv.getItem(slot).is(Items.CLAY_BALL)) return false;
        }

        // Other slots must be empty
        for (int i = 0; i < 9; i++) {
            if (i == 4 || i == 1 || i == 3 || i == 5 || i == 7) continue;
            if (!inv.getItem(i).isEmpty()) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack center = inv.getItem(4);
        if (center.isEmpty()) return ItemStack.EMPTY;

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(center.getItem());

        // Get tool type & max molten amount from config
        String toolType = CastingConfigHelper.getToolTypeForItem(id.toString().toLowerCase());
        if ("none".equals(toolType)) return ItemStack.EMPTY;

        int maxAmount = CastingConfigHelper.getMaxMaterialAmount(toolType);
        if (maxAmount <= 0) maxAmount = 9; // fallback if config broken
        // ✅ Extract forging quality from item NBT

        CompoundTag centerTag = center.getTag();
        String quality = "none";
        if (centerTag != null && centerTag.contains("ForgingQuality")) {
            quality = centerTag.getString("ForgingQuality");
            if (quality.isEmpty()) quality = "none";
        }

        // Create new tool cast item
        ItemStack result = new ItemStack(ModItems.UNFIRED_TOOL_CAST.get());
        CompoundTag tag = result.getOrCreateTag();

        tag.putString("ToolType", toolType);
        tag.putString("Quality", quality);
        tag.putInt("Amount", 0);
        tag.putInt("MaxAmount", maxAmount);

        // Empty material map -> stored as CompoundTag
        tag.put("Materials", new CompoundTag());

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        // Keep the center item (slot 4)
        ItemStack centerItem = inv.getItem(4);
        if (!centerItem.isEmpty()) {
            // Create a copy of the center item to return it
            remaining.set(4, centerItem.copy());
        }

        // Clay balls will be consumed (return EMPTY for those slots)
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 9;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CLAY_TOOL_CAST.get();
    }
}

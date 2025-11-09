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
import net.minecraftforge.server.ServerLifecycleHooks;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.CastingConfigHelper;

public class ClayToolCastRecipe extends CustomRecipe {

    private static final int[] CLAY_SLOTS = {1, 3, 5, 7}; // N, E, S, W around center

    // store the level between matches() and assemble()
    private Level lastLevel = null;

    public ClayToolCastRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        if (inv.getContainerSize() != 9) return false;

        // store level reference for assemble()
        this.lastLevel = level;

        ItemStack center = inv.getItem(4);
        if (center.isEmpty()) return false;

        // Must be mapped to a tool type in config
        String toolType = CastingConfigHelper.getToolTypeForItem(level, center);
        if ("none".equals(toolType)) return false;

        boolean clayPattern = true;
        boolean netherPattern = true;

        for (int slot : CLAY_SLOTS) {
            ItemStack stack = inv.getItem(slot);
            clayPattern &= stack.is(Items.CLAY_BALL);
            netherPattern &= stack.is(Items.NETHER_BRICK);
        }

        // Must be exclusively clay or exclusively nether bricks
        if (!clayPattern && !netherPattern) return false;

        // Other slots must be empty
        for (int i = 0; i < 9; i++) {
            if (i == 4 || i == 1 || i == 3 || i == 5 || i == 7) continue;
            if (!inv.getItem(i).isEmpty()) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        if (!ServerConfig.ENABLE_CASTING.get()) return ItemStack.EMPTY;

        ItemStack center = inv.getItem(4);
        if (center.isEmpty()) return ItemStack.EMPTY;

        // use the last known level from matches(), fallback to overworld if null
        Level level = (lastLevel != null)
                ? lastLevel
                : ServerLifecycleHooks.getCurrentServer().overworld();

        String toolType = CastingConfigHelper.getToolTypeForItem(level, center);
        if (toolType == null) return ItemStack.EMPTY;
        if ("none".equals(toolType) || toolType.isBlank()) return ItemStack.EMPTY;

        // detect if nether bricks were used
        boolean netherPattern = true;
        for (int slot : CLAY_SLOTS) {
            ItemStack stack = inv.getItem(slot);
            netherPattern &= stack.is(Items.NETHER_BRICK);
        }

        // Determine which cast item to create
        ItemStack result = netherPattern
                ? new ItemStack(ModItems.NETHER_TOOL_CAST.get())
                : new ItemStack(ModItems.UNFIRED_TOOL_CAST.get());

        // Extract forging quality from the center item
        CompoundTag centerTag = center.getTag();
        String quality = "none";
        if (centerTag != null && centerTag.contains("ForgingQuality")) {
            quality = centerTag.getString("ForgingQuality");
            if (quality.isEmpty()) quality = "none";
        }

        int maxAmount = CastingConfigHelper.getMaxMaterialAmount(toolType);
        if (maxAmount <= 0) maxAmount = 9;

        // downgrade the quality one level
        if (!quality.equals("none"))
            quality = BlueprintQuality.getPrevious(BlueprintQuality.fromString(quality)).getId();

        // Attach all relevant NBT data
        CompoundTag tag = result.getOrCreateTag();
        tag.putString("ToolType", toolType);
        tag.putString("Quality", quality);
        tag.putInt("Amount", 0);
        tag.putInt("MaxAmount", maxAmount);
        tag.put("Materials", new CompoundTag());

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        // Keep the center item (slot 4)
        ItemStack centerItem = inv.getItem(4);
        if (!centerItem.isEmpty()) {
            remaining.set(4, centerItem.copy());
        }

        // Clay balls / nether bricks are consumed
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

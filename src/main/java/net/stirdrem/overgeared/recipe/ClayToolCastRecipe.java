package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.Map;

public class ClayToolCastRecipe extends CustomRecipe {

    private static final int[] CLAY_SLOTS = {1, 3, 5, 7}; // N, E, S, W around center

    public ClayToolCastRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.size() != 9) return false;

        ItemStack center = input.getItem(4);
        if (center.isEmpty()) return false;

        // Must be mapped to a tool type in config
        String toolType = ConfigHelper.getToolTypeForItem(level, center);
        if ("none".equals(toolType)) return false;

        boolean clayPattern = true;
        boolean netherPattern = true;

        for (int slot : CLAY_SLOTS) {
            ItemStack stack = input.getItem(slot);
            clayPattern &= stack.is(Items.CLAY_BALL);
            netherPattern &= stack.is(Items.NETHER_BRICK);
        }

        // Must be exclusively clay or exclusively nether bricks
        if (!clayPattern && !netherPattern) return false;

        // Other slots must be empty
        for (int i = 0; i < 9; i++) {
            if (i == 4 || i == 1 || i == 3 || i == 5 || i == 7) continue;
            if (!input.getItem(i).isEmpty()) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        if (!ServerConfig.ENABLE_CASTING.get()) return ItemStack.EMPTY;

        ItemStack center = input.getItem(4);
        if (center.isEmpty()) return ItemStack.EMPTY;

        // Get the tool type from config - we pass null for level but ConfigHelper should handle it
        // In 1.21.1, we need to get level from somewhere else or make ConfigHelper work without it
        String toolType = ConfigHelper.getToolTypeForItem(null, center);
        if ("none".equals(toolType) || toolType.isBlank()) return ItemStack.EMPTY;

        // detect if nether bricks were used
        boolean netherPattern = true;
        for (int slot : CLAY_SLOTS) {
            ItemStack stack = input.getItem(slot);
            netherPattern &= stack.is(Items.NETHER_BRICK);
        }

        // Determine which cast item to create
        ItemStack result = netherPattern
                ? new ItemStack(ModItems.NETHER_TOOL_CAST.get())
                : new ItemStack(ModItems.UNFIRED_TOOL_CAST.get());

        // Extract forging quality from the center item using data components
        ForgingQuality forgingQuality = center.get(ModComponents.FORGING_QUALITY);
        String quality = "";
        
        if (forgingQuality != null && forgingQuality != ForgingQuality.NONE) {
            // Convert forging quality to blueprint quality and downgrade one level
            BlueprintQuality blueprintQuality = BlueprintQuality.fromString(forgingQuality.getDisplayName());
            BlueprintQuality downgraded = BlueprintQuality.getPrevious(blueprintQuality);
            if (downgraded != null) {
                quality = downgraded.getId();
            }
        }

        int maxAmount = ConfigHelper.getMaxMaterialAmount(toolType);
        if (maxAmount <= 0) maxAmount = 9;

        // Create CastData with initial values
        CastData castData = new CastData(
                quality,
                toolType,
                Map.of(),  // Empty materials map
                0,         // No amount yet
                maxAmount, // Max amount from config
                java.util.List.of(),  // Empty input list
                ItemStack.EMPTY,      // No output yet
                false      // Not heated
        );

        // Set the cast data component
        result.set(ModComponents.CAST_DATA, castData);

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        // Keep the center item (slot 4)
        ItemStack centerItem = input.getItem(4);
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
        return ModRecipeSerializers.CLAY_TOOL_CAST.get();
    }

    public static class Serializer implements RecipeSerializer<ClayToolCastRecipe> {
        private static final MapCodec<ClayToolCastRecipe> CODEC = 
            CraftingBookCategory.CODEC.fieldOf("category")
                .xmap(ClayToolCastRecipe::new, CustomRecipe::category);

        private static final StreamCodec<RegistryFriendlyByteBuf, ClayToolCastRecipe> STREAM_CODEC = 
            StreamCodec.composite(
                net.minecraft.network.codec.ByteBufCodecs.fromCodec(CraftingBookCategory.CODEC),
                CustomRecipe::category,
                ClayToolCastRecipe::new
            );

        @Override
        public MapCodec<ClayToolCastRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ClayToolCastRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

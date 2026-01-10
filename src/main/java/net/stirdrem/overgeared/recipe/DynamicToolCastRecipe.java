package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.HashMap;
import java.util.Map;

public class DynamicToolCastRecipe extends CustomRecipe {

    public DynamicToolCastRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack cast = ItemStack.EMPTY;
        CastData castData = null;
        int existingAmount = 0;
        int addedAmount = 0;
        int maxAmount = 0;
        boolean foundMaterial = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            // === CAST ===
            if (stack.is(ModItems.CLAY_TOOL_CAST.get()) || stack.is(ModItems.NETHER_TOOL_CAST.get())) {
                if (!cast.isEmpty()) return false; // only one cast allowed

                castData = stack.get(ModComponents.CAST_DATA);
                if (castData == null) return false;

                String toolType = castData.toolType();
                if (toolType.isBlank()) return false;

                cast = stack;
                existingAmount = castData.amount();
                maxAmount = ConfigHelper.getMaxMaterialAmount(toolType);
                continue;
            }

            // === MATERIAL ===
            String material = ConfigHelper.getMaterialForItem(stack);
            if (!material.equals("none")) {
                foundMaterial = true;
                addedAmount += ConfigHelper.getMaterialValue(stack);
                continue;
            }

            // === INVALID ITEM ===
            return false;
        }

        if (cast.isEmpty() || !foundMaterial) return false;

        // ❌ Overflow check
        if (maxAmount > 0 && existingAmount + addedAmount > maxAmount) {
            return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        if (!ServerConfig.ENABLE_CASTING.get()) return ItemStack.EMPTY;

        ItemStack cast = ItemStack.EMPTY;
        CastData castData = null;
        Map<String, Integer> materialTotals = new HashMap<>();
        int newAmount = 0;
        int maxAmount = 0;
        String toolType = "none";

        // Scan grid
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            // Find cast
            if (stack.is(ModItems.CLAY_TOOL_CAST.get()) || stack.is(ModItems.NETHER_TOOL_CAST.get())) {
                cast = stack.copy();
                castData = cast.get(ModComponents.CAST_DATA);
                if (castData == null) return ItemStack.EMPTY;

                toolType = castData.toolType();
                maxAmount = ConfigHelper.getMaxMaterialAmount(toolType);
                
                // Copy existing materials
                materialTotals.putAll(castData.materials());
            }
            // Process materials
            else if (!stack.isEmpty()) {
                String material = ConfigHelper.getMaterialForItem(stack);
                if (!material.equals("none")) {
                    int value = ConfigHelper.getMaterialValue(stack);
                    materialTotals.put(material, materialTotals.getOrDefault(material, 0) + value);
                    newAmount += value;

                    // Add the input item to cast data (for tracking)
                    ItemStack singleItem = stack.copyWithCount(1);
                    castData = castData.withAddedInput(singleItem);
                }
            }
        }

        if (cast.isEmpty() || castData == null) return ItemStack.EMPTY;

        int existingAmount = castData.amount();
        int totalAmount = existingAmount + newAmount;

        // ❌ Block if exceeds
        if (maxAmount > 0 && totalAmount > maxAmount) return ItemStack.EMPTY;

        // ✅ Create updated cast data with new materials and amount
        CastData newCastData = new CastData(
                castData.quality(),
                castData.toolType(),
                materialTotals,
                totalAmount,
                castData.maxAmount(),
                castData.input(),
                castData.output(),
                castData.heated()
        );

        cast.set(ModComponents.CAST_DATA, newCastData);
        return cast;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_DYNAMIC_TOOL_CAST.get();
    }

    public static class Serializer implements RecipeSerializer<DynamicToolCastRecipe> {
        private static final MapCodec<DynamicToolCastRecipe> CODEC = 
            CraftingBookCategory.CODEC.fieldOf("category")
                .xmap(DynamicToolCastRecipe::new, CustomRecipe::category);

        private static final StreamCodec<RegistryFriendlyByteBuf, DynamicToolCastRecipe> STREAM_CODEC = 
            StreamCodec.composite(
                net.minecraft.network.codec.ByteBufCodecs.fromCodec(CraftingBookCategory.CODEC),
                CustomRecipe::category,
                DynamicToolCastRecipe::new
            );

        @Override
        public MapCodec<DynamicToolCastRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DynamicToolCastRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

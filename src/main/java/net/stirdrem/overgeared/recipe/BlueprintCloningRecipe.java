package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;

public class BlueprintCloningRecipe extends CustomRecipe {
    public BlueprintCloningRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int blueprintCount = 0;
        ItemStack emptyBlueprint = ItemStack.EMPTY;

        for (int j = 0; j < input.size(); ++j) {
            ItemStack stack = input.getItem(j);
            if (!stack.isEmpty()) {
                if (stack.is(ModItems.EMPTY_BLUEPRINT.get())) {
                    if (!emptyBlueprint.isEmpty()) {
                        return false; // Only 1 empty blueprint allowed
                    }
                    emptyBlueprint = stack;
                } else {
                    if (!stack.is(ModItems.BLUEPRINT.get())) {
                        return false;
                    }

                    ++blueprintCount;
                }
            }
        }

        return !emptyBlueprint.isEmpty() && blueprintCount > 0;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack source = ItemStack.EMPTY;

        for (int j = 0; j < input.size(); ++j) {
            ItemStack stack = input.getItem(j);
            if (!stack.isEmpty() && stack.is(ModItems.BLUEPRINT.get())) {
                if (!source.isEmpty()) return ItemStack.EMPTY; // only 1 blueprint source allowed
                source = stack;
            }
        }

        if (source.isEmpty()) return ItemStack.EMPTY;
        
        ItemStack result = source.copyWithCount(2);

        // Reduce quality using data components
        BlueprintData blueprintData = source.get(ModComponents.BLUEPRINT_DATA);
        if (blueprintData != null) {
            BlueprintQuality current = BlueprintQuality.fromString(blueprintData.quality());
            BlueprintQuality downgraded = BlueprintQuality.getPrevious(current);

            if (downgraded != null) {
                // Update the blueprint data with the new quality
                BlueprintData newData = blueprintData.withQuality(downgraded.getId());
                result.set(ModComponents.BLUEPRINT_DATA, newData);
            }
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_BLUEPRINTCLONING.get();
    }

    public static class Serializer implements RecipeSerializer<BlueprintCloningRecipe> {
        private static final MapCodec<BlueprintCloningRecipe> CODEC = 
            CraftingBookCategory.CODEC.fieldOf("category")
                .xmap(BlueprintCloningRecipe::new, CustomRecipe::category);

        private static final StreamCodec<RegistryFriendlyByteBuf, BlueprintCloningRecipe> STREAM_CODEC = 
            StreamCodec.composite(
                net.minecraft.network.codec.ByteBufCodecs.fromCodec(CraftingBookCategory.CODEC),
                CustomRecipe::category,
                BlueprintCloningRecipe::new
            );

        @Override
        public MapCodec<BlueprintCloningRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BlueprintCloningRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
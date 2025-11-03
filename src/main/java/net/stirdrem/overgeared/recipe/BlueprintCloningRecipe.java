package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.item.ModItems;

public class BlueprintCloningRecipe extends CustomRecipe {
    public BlueprintCloningRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
        super(pId, pCategory);
    }

    @Override
    public boolean matches(CraftingContainer pInv, Level pLevel) {
        int blueprintCount = 0;
        ItemStack emptyBlueprint = ItemStack.EMPTY;

        for (int j = 0; j < pInv.getContainerSize(); ++j) {
            ItemStack stack = pInv.getItem(j);
            if (!stack.isEmpty()) {
                if (stack.is(ModItems.EMPTY_BLUEPRINT.get())) {
                    if (!emptyBlueprint.isEmpty()) {
                        return false; // Only 1 empty blueprint allowed
                    }
                    emptyBlueprint = stack;
                /*} else if (stack.is(ModItems.BLUEPRINT.get())) {
                    // Reject if it contains master quality
                    CompoundTag tag = stack.getTag();
                    if (tag != null && "master".equalsIgnoreCase(tag.getString("Quality"))) {
                        return false;
                    }
                    blueprintCount++;
                } else {
                    return false; // Invalid item in recipe
                }*/
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
    public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
        ItemStack source = ItemStack.EMPTY;

        for (int j = 0; j < pContainer.getContainerSize(); ++j) {
            ItemStack stack = pContainer.getItem(j);
            if (!stack.isEmpty() && stack.is(ModItems.BLUEPRINT.get())) {
                if (!source.isEmpty()) return ItemStack.EMPTY; // only 1 blueprint source allowed
                source = stack;
            }
        }

        if (source.isEmpty()) return ItemStack.EMPTY;
        
        ItemStack result = source.copyWithCount(2);

        // Reduce quality
        if (source.hasTag() && source.getTag().contains("Quality")) {
            String currentId = source.getTag().getString("Quality");
            BlueprintQuality current = BlueprintQuality.fromString(currentId);
            BlueprintQuality downgraded = BlueprintQuality.getPrevious(current);

            if (downgraded != null) {
                result.getOrCreateTag().putString("Quality", downgraded.getId());
            }
        }

        return result;
    }


    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth >= 3 && pHeight >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRAFTING_BLUEPRINTCLONING.get();
    }

}
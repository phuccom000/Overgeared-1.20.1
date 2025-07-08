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
        int i = 0;
        ItemStack itemstack = ItemStack.EMPTY;

        for (int j = 0; j < pContainer.getContainerSize(); ++j) {
            ItemStack itemstack1 = pContainer.getItem(j);
            if (!itemstack1.isEmpty()) {
                if (itemstack1.is(ModItems.BLUEPRINT.get())) {
                    if (!itemstack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }

                    itemstack = itemstack1;
                } else {
                    if (!itemstack1.is(ModItems.EMPTY_BLUEPRINT.get())) {
                        return ItemStack.EMPTY;
                    }

                    ++i;
                }
            }
        }

        if (!itemstack.isEmpty() && i >= 1) {
            ItemStack result = itemstack.copyWithCount(i);

            // Reduce quality
            if (itemstack.hasTag() && itemstack.getTag().contains("Quality")) {
                String currentId = itemstack.getTag().getString("Quality");
                BlueprintQuality currentQuality = BlueprintQuality.fromString(currentId);
                BlueprintQuality downgraded = BlueprintQuality.getPrevious(currentQuality);

                if (downgraded != null) {
                    result.getOrCreateTag().putString("Quality", downgraded.getId());
                }
            }

            return result;
        }

        return ItemStack.EMPTY;
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
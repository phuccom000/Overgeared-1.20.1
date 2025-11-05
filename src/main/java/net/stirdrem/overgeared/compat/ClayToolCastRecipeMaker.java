package net.stirdrem.overgeared.compat;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.core.registries.BuiltInRegistries;

import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.CastingConfigHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class ClayToolCastRecipeMaker {

    private ClayToolCastRecipeMaker() {
    }

    public static List<CraftingRecipe> createRecipes() {

        return ServerConfig.TOOL_HEAD_SETTING.get().stream()
                .filter(row -> row instanceof List<?> && row.size() >= 2)
                .map(row -> {
                    String itemId = (String) row.get(0);
                    String toolType = (String) row.get(1);

                    Item toolHead = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(itemId));
                    if (toolHead == Items.AIR) return null;

                    return createRecipe(toolHead, toolType);
                })
                .filter(Objects::nonNull)
                .toList();

    }

    private static CraftingRecipe createRecipe(Item toolHead, String toolType) {
        NonNullList<Ingredient> inputs = NonNullList.of(
                Ingredient.EMPTY,
                Ingredient.EMPTY, Ingredient.of(Items.CLAY_BALL), Ingredient.EMPTY,
                Ingredient.of(Items.CLAY_BALL), Ingredient.of(toolHead), Ingredient.of(Items.CLAY_BALL),
                Ingredient.EMPTY, Ingredient.of(Items.CLAY_BALL), Ingredient.EMPTY
        );

        ItemStack output = createOutput(toolType);

        ResourceLocation id = ResourceLocation.tryBuild(
                OvergearedMod.MOD_ID,
                "jei.clay_cast." + toolType
        );

        return new ShapedRecipe(id, "jei.clay_cast", CraftingBookCategory.MISC, 3, 3, inputs, output);
    }

    private static ItemStack createOutput(String toolType) {
        ItemStack result = new ItemStack(ModItems.UNFIRED_TOOL_CAST.get());
        CompoundTag tag = result.getOrCreateTag();

        tag.putString("ToolType", toolType);
        tag.putInt("Amount", 0);
        tag.putInt("MaxAmount", CastingConfigHelper.getMaxMaterialAmount(toolType));

        return result;
    }
}

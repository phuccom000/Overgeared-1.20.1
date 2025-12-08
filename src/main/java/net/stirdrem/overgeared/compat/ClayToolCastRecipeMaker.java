package net.stirdrem.overgeared.compat;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ItemToToolTypeRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.*;
import java.util.stream.Collectors;

public final class ClayToolCastRecipeMaker {

    private ClayToolCastRecipeMaker() {
    }

    public static List<CraftingRecipe> createRecipes(RecipeManager recipeManager) {
        // ✅ Get all registered item_to_tooltype recipes
        List<ItemToToolTypeRecipe> itemToToolTypeRecipes =
                recipeManager.getAllRecipesFor(ModRecipeTypes.ITEM_TO_TOOLTYPE.get());

        // ✅ Group recipes by tool type (key = tooltype string)
        Map<String, List<ItemToToolTypeRecipe>> grouped =
                itemToToolTypeRecipes.stream()
                        .collect(Collectors.groupingBy(ItemToToolTypeRecipe::getToolType));

        // ✅ Convert each tooltype group into a synthetic JEI recipe
        return grouped.entrySet().stream()
                .map(entry -> {
                    String toolType = entry.getKey();

                    // Collect all input items from the recipes - FIXED
                    List<Item> toolHeads = entry.getValue().stream()
                            .flatMap(r -> r.getItems().stream()) // Now returns List<ItemStack>
                            .map(ItemStack::getItem) // Get the Item from ItemStack
                            .filter(item -> item != Items.AIR)
                            .distinct() // Remove duplicates
                            .toList();

                    if (toolHeads.isEmpty()) return null;
                    return createRecipe(toolHeads, toolType);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private static CraftingRecipe createRecipe(List<Item> toolHeads, String toolType) {
        // Center ingredient must accept ANY head item
        Ingredient headIngredient = Ingredient.of(toolHeads.stream().map(ItemStack::new));

        NonNullList<Ingredient> inputs = NonNullList.of(
                Ingredient.EMPTY,
                Ingredient.EMPTY, Ingredient.of(Items.CLAY_BALL), Ingredient.EMPTY,
                Ingredient.of(Items.CLAY_BALL), headIngredient, Ingredient.of(Items.CLAY_BALL),
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
        tag.putInt("MaxAmount", ConfigHelper.getMaxMaterialAmount(toolType));

        return result;
    }
}

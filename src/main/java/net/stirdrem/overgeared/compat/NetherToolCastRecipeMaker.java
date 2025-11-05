package net.stirdrem.overgeared.compat;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.CastingConfigHelper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class NetherToolCastRecipeMaker {

    private NetherToolCastRecipeMaker() {
    }

    public static List<CraftingRecipe> createRecipes() {

        // Group by tool type
        var grouped = ServerConfig.TOOL_HEAD_SETTING.get().stream()
                .filter(row -> row instanceof List<?> && row.size() >= 2)
                .collect(Collectors.groupingBy(row -> (String) row.get(1))); // key = toolType

        return grouped.entrySet().stream()
                .map(entry -> {
                    String toolType = entry.getKey();

                    // Collect all item heads for this tool type
                    List<Item> toolHeads = entry.getValue().stream()
                            .map(row -> (String) row.get(0))
                            .map(ResourceLocation::tryParse)
                            .map(BuiltInRegistries.ITEM::get)
                            .filter(item -> item != Items.AIR)
                            .toList();

                    if (toolHeads.isEmpty()) return null;

                    // Create one multi-input recipe
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
                Ingredient.EMPTY, Ingredient.of(Items.NETHER_BRICK), Ingredient.EMPTY,
                Ingredient.of(Items.NETHER_BRICK), headIngredient, Ingredient.of(Items.NETHER_BRICK),
                Ingredient.EMPTY, Ingredient.of(Items.NETHER_BRICK), Ingredient.EMPTY
        );

        ItemStack output = createOutput(toolType);

        ResourceLocation id = ResourceLocation.tryBuild(
                OvergearedMod.MOD_ID,
                "jei.clay_cast." + toolType
        );

        return new ShapedRecipe(id, "jei.clay_cast", CraftingBookCategory.MISC, 3, 3, inputs, output);
    }


    private static ItemStack createOutput(String toolType) {
        ItemStack result = new ItemStack(ModItems.NETHER_TOOL_CAST.get());
        CompoundTag tag = result.getOrCreateTag();

        tag.putString("ToolType", toolType);
        tag.putInt("Amount", 0);
        tag.putInt("MaxAmount", CastingConfigHelper.getMaxMaterialAmount(toolType));

        return result;
    }
}

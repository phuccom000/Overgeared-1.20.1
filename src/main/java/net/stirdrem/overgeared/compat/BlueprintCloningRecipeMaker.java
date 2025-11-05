package net.stirdrem.overgeared.compat;

import mezz.jei.api.helpers.IStackHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public class BlueprintCloningRecipeMaker {

    public static List<CraftingRecipe> createRecipes() {
        List<CraftingRecipe> recipes = new ArrayList<>();

        List<BlueprintQuality> qualities = new ArrayList<>(List.of(BlueprintQuality.values()));
        qualities.sort((a, b) -> Integer.compare(b.ordinal(), a.ordinal())); // descending order

        for (BlueprintQuality quality : qualities) {
            // Optional: skip cloning master if undesired
            // if (quality == BlueprintQuality.MASTER) continue;

            BlueprintQuality outputQuality = BlueprintQuality.getPrevious(quality);
            if (outputQuality == null) {
                outputQuality = quality; // lowest stays the same
            }

            ItemStack input = new ItemStack(ModItems.BLUEPRINT.get());
            input.getOrCreateTag().putString("Quality", quality.getId());

            ItemStack output = new ItemStack(ModItems.BLUEPRINT.get(), 1);
            output.getOrCreateTag().putString("Quality", outputQuality.getId());

            Ingredient blueprintIngredient = Ingredient.of(input);
            Ingredient emptyIngredient = Ingredient.of(ModItems.EMPTY_BLUEPRINT.get());

            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
                    blueprintIngredient,
                    emptyIngredient
            );

            ResourceLocation id = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "blueprint_cloning/" + quality.getId());
            ShapelessRecipe fakeRecipe = new ShapelessRecipe(id, "jei.blueprint.clone", CraftingBookCategory.MISC, output, inputs);
            recipes.add(fakeRecipe);
        }

        return recipes;
    }

}

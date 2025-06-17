package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class ForgingQualityShapelessRecipe extends ShapelessRecipe {

    public ForgingQualityShapelessRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                         ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack result = super.assemble(container, registryAccess);
        CompoundTag resultTag = result.getOrCreateTag();

        // Find first ingredient with quality data
        String foundQuality = null;
        boolean isPolished = false;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack ingredient = container.getItem(i);
            if (ingredient.hasTag()) {
                CompoundTag tag = ingredient.getTag();
                if (tag.contains("ForgingQuality")) {
                    foundQuality = tag.getString("ForgingQuality");
                }
                if (tag.contains("Polished") && tag.getBoolean("Polished")) {
                    isPolished = true;
                }
            }
        }

        // Apply quality modification rules
        if (foundQuality != null) {
            String resultQuality = foundQuality;

            if (!isPolished) {
                // Downgrade quality if not polished
                resultQuality = switch (foundQuality) {
                    case "perfect" -> "expert";
                    case "expert" -> "well";
                    case "well" -> "poor";
                    default -> foundQuality; // "poor" stays as "poor"
                };
            }

            resultTag.putString("ForgingQuality", resultQuality);
            result.setTag(resultTag);
        }

        return result;
    }

    /*@Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CRAFTING_SHAPELESS.get();
    }*/

    public static class Type implements RecipeType<ForgingQualityShapelessRecipe> {
        public static final ForgingQualityShapelessRecipe.Type INSTANCE = new ForgingQualityShapelessRecipe.Type();
        public static final String ID = "crafting_shapeless";
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<ForgingQualityShapelessRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public ForgingQualityShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapelessRecipe baseRecipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromJson(recipeId, json);
            return new ForgingQualityShapelessRecipe(
                    recipeId,
                    baseRecipe.getGroup(),
                    baseRecipe.category(),
                    baseRecipe.getResultItem(null),
                    baseRecipe.getIngredients()
            );
        }

        @Override
        public ForgingQualityShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ShapelessRecipe baseRecipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromNetwork(recipeId, buffer);
            return new ForgingQualityShapelessRecipe(
                    recipeId,
                    baseRecipe.getGroup(),
                    baseRecipe.category(),
                    baseRecipe.getResultItem(null),
                    baseRecipe.getIngredients()
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ForgingQualityShapelessRecipe recipe) {
            ShapelessRecipe.Serializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
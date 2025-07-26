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
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;

public class OvergearedShapelessRecipe extends ShapelessRecipe {

    public OvergearedShapelessRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                     ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
    }


    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack result = super.assemble(container, registryAccess);

        if (!ServerConfig.ENABLE_MINIGAME.get()) {
            // When minigame is disabled
            boolean hasUnpolishedQualityItem = false;

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack ingredient = container.getItem(i);
                if (ingredient.hasTag()) {
                    CompoundTag tag = ingredient.getTag();
                    // Check if item has quality but isn't polished
                    if (!tag.contains("Polished") || !tag.getBoolean("Polished")) {
                        hasUnpolishedQualityItem = true;
                        break; // No need to check further if we found one
                    }
                }
            }

            // Prevent crafting if any unpolished quality items exist
            if (hasUnpolishedQualityItem) {
                return ItemStack.EMPTY;
            }

            // Remove any ForgingQuality tag from result if present
            if (result.hasTag() && result.getTag().contains("ForgingQuality")) {
                result.getTag().remove("ForgingQuality");
                if (result.getTag().isEmpty()) {
                    result.setTag(null); // Remove empty tag
                }
            }

            return result;
        }

        // Original minigame-enabled logic
        CompoundTag resultTag = result.getOrCreateTag();
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

        if (foundQuality != null) {
            String resultQuality = foundQuality;
            if (!isPolished) {
                resultQuality = switch (foundQuality) {
                    case "perfect" -> "expert";
                    case "expert" -> "well";
                    case "well" -> "poor";
                    default -> foundQuality;
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

    public static class Type implements RecipeType<OvergearedShapelessRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_shapeless";
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<OvergearedShapelessRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public OvergearedShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapelessRecipe baseRecipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromJson(recipeId, json);
            return new OvergearedShapelessRecipe(
                    recipeId,
                    baseRecipe.getGroup(),
                    baseRecipe.category(),
                    baseRecipe.getResultItem(null),
                    baseRecipe.getIngredients()
            );
        }

        @Override
        public OvergearedShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ShapelessRecipe baseRecipe = ShapelessRecipe.Serializer.SHAPELESS_RECIPE.fromNetwork(recipeId, buffer);
            return new OvergearedShapelessRecipe(
                    recipeId,
                    baseRecipe.getGroup(),
                    baseRecipe.category(),
                    baseRecipe.getResultItem(null),
                    baseRecipe.getIngredients()
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, OvergearedShapelessRecipe recipe) {
            ShapelessRecipe.Serializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
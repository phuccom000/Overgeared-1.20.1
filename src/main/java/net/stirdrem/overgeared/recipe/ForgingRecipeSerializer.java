/*
package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.Map;

public class ForgingRecipeSerializer implements RecipeSerializer<ForgingRecipe> {
    @Override
    public ForgingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String group = GsonHelper.getAsString(json, "group", "");
        Map<String, Ingredient> key = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
        String[] pattern = ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern"));
        int width = pattern[0].length();
        int height = pattern.length;
        NonNullList<Ingredient> ingredients = ShapedRecipe.dissolvePattern(pattern, key, width, height);
        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
        int hammering = GsonHelper.getAsInt(json, "hammering", 0);
        boolean showNotification = GsonHelper.getAsBoolean(json, "show_notification", false);

        return new ForgingRecipe(recipeId, group, ingredients, result, hammering, showNotification, width, height);
    }

    @Override
    public ForgingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        String group = buffer.readUtf();
        int width = buffer.readVarInt();
        int height = buffer.readVarInt();
        NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
        for (int i = 0; i < ingredients.size(); i++) {
            ingredients.set(i, Ingredient.fromNetwork(buffer));
        }
        ItemStack result = buffer.readItem();
        int hammering = buffer.readVarInt();
        boolean showNotification = buffer.readBoolean();

        return new ForgingRecipe(recipeId, group, ingredients, result, hammering, showNotification, width, height);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ForgingRecipe recipe) {
        buffer.writeUtf(recipe.group);
        buffer.writeVarInt(recipe.width);
        buffer.writeVarInt(recipe.height);
        for (Ingredient ingredient : recipe.ingredients) {
            ingredient.toNetwork(buffer);
        }
        buffer.writeItem(recipe.result);
        buffer.writeVarInt(recipe.hammering);
        buffer.writeBoolean(recipe.showNotification);
    }
}

*/

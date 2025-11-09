package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;

public class ItemToToolTypeRecipe implements Recipe<SimpleContainer> {

    private final ResourceLocation id;
    private final Ingredient input;
    private final String toolType;

    public ItemToToolTypeRecipe(ResourceLocation id, Ingredient input, String toolType) {
        this.id = id;
        this.input = input;
        this.toolType = toolType;
    }

    public Ingredient getInput() {
        return input;
    }

    public String getToolType() {
        return toolType;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(SimpleContainer container, net.minecraft.core.RegistryAccess registryAccess) {
        return ItemStack.EMPTY; // purely data-driven recipe
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ITEM_TO_TOOLTYPE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ITEM_TO_TOOLTYPE.get();
    }

    public List<ItemStack> getItems() {
        return List.of(input.getItems());
    }

    // ----------------------------------------------------
    // Serializer
    // ----------------------------------------------------
    public static class Serializer implements RecipeSerializer<ItemToToolTypeRecipe> {

        @Override
        public ItemToToolTypeRecipe fromJson(ResourceLocation id, JsonObject json) {
            // Allow "item" to be either an object or an array
            if (!json.has("item")) {
                throw new JsonSyntaxException("Missing 'item' for item_to_tooltype recipe");
            }

            JsonElement itemElement = json.get("item");
            Ingredient input = Ingredient.fromJson(itemElement);
            String toolType = json.get("tooltype").getAsString();

            return new ItemToToolTypeRecipe(id, input, toolType);
        }

        @Override
        public ItemToToolTypeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            String toolType = buf.readUtf();
            return new ItemToToolTypeRecipe(id, input, toolType);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ItemToToolTypeRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeUtf(recipe.toolType);
        }
    }
}

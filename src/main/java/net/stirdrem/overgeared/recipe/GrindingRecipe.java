package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class GrindingRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;

    public GrindingRecipe(ResourceLocation id, Ingredient input, ItemStack output) {
        this.id = id;
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(SimpleContainer container, net.minecraft.core.RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.GRINDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.GRINDING_RECIPE.get();
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public static class Type implements RecipeType<GrindingRecipe> {
        public static final GrindingRecipe.Type INSTANCE = new GrindingRecipe.Type();
        public static final String ID = "grinding";
    }

    public static class Serializer implements RecipeSerializer<GrindingRecipe> {
        @Override
        public GrindingRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            ItemStack output = net.minecraft.world.item.crafting.ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            return new GrindingRecipe(id, input, output);
        }

        @Override
        public GrindingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            ItemStack output = buffer.readItem();
            return new GrindingRecipe(id, input, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GrindingRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.output);
        }
    }
}

package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;

public class NBTKeepingBlastingRecipe extends BlastingRecipe {

    public NBTKeepingBlastingRecipe(ResourceLocation pId, String pGroup, CookingBookCategory pCategory, Ingredient pIngredient, ItemStack pResult, float pExperience, int pCookingTime) {
        super(pId, pGroup, pCategory, pIngredient, pResult, pExperience, pCookingTime);
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess pRegistryAccesss) {
        ItemStack input = ItemStack.EMPTY;

        // Get input item
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                input = stack.copy();
                break;
            }
        }

        ItemStack output = result.copy();

        // Copy NBT data
        if (input.hasTag()) {
            output.setTag(input.getTag().copy());
        }

        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.NBT_BLASTING.get();
    }

    public static class Serializer implements RecipeSerializer<NBTKeepingBlastingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public NBTKeepingBlastingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CookingBookCategory category = CookingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", null),
                    CookingBookCategory.MISC
            );

            Ingredient ingredient = Ingredient.fromJson(
                    GsonHelper.isArrayNode(json, "ingredient")
                            ? GsonHelper.getAsJsonArray(json, "ingredient")
                            : GsonHelper.getAsJsonObject(json, "ingredient")
            );

            ItemStack result = json.get("result").isJsonObject()
                    ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"))
                    : new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(json, "result"))));

            float xp = GsonHelper.getAsFloat(json, "experience", 0.0F);
            int cookTime = GsonHelper.getAsInt(json, "cookingtime", 200);

            return new NBTKeepingBlastingRecipe(id, group, category, ingredient, result, xp, cookTime);
        }

        @Override
        public @Nullable NBTKeepingBlastingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CookingBookCategory category = buf.readEnum(CookingBookCategory.class);
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            float xp = buf.readFloat();
            int cookTime = buf.readVarInt();

            return new NBTKeepingBlastingRecipe(id, group, category, ingredient, result, xp, cookTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, NBTKeepingBlastingRecipe recipe) {
            buf.writeUtf(recipe.getGroup());
            buf.writeEnum(recipe.category());
            recipe.ingredient.toNetwork(buf);
            buf.writeItem(recipe.result);
            buf.writeFloat(recipe.experience);
            buf.writeVarInt(recipe.cookingTime);
        }
    }

}

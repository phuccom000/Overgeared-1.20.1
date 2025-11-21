package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public class OvergearedShapelessRecipe extends ShapelessRecipe {

    private final NonNullList<IngredientWithRemainder> ingredientsWithRemainder;

    public OvergearedShapelessRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                     ItemStack result, NonNullList<IngredientWithRemainder> ingredientsWithRemainder) {
        super(id, group, category, result, convertToBaseIngredients(ingredientsWithRemainder));
        this.ingredientsWithRemainder = ingredientsWithRemainder;
    }

    // Convert our custom ingredients to base Minecraft ingredients for parent class
    private static NonNullList<Ingredient> convertToBaseIngredients(NonNullList<IngredientWithRemainder> customIngredients) {
        NonNullList<Ingredient> baseIngredients = NonNullList.create();
        for (IngredientWithRemainder ingredient : customIngredients) {
            baseIngredients.add(ingredient.getIngredient());
        }
        return baseIngredients;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        // Track which ingredients have been processed
        boolean[] ingredientProcessed = new boolean[ingredientsWithRemainder.size()];

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack slotStack = container.getItem(slot);
            if (slotStack.isEmpty()) continue;

            // Find matching ingredient with remainder
            for (int ingIndex = 0; ingIndex < ingredientsWithRemainder.size(); ingIndex++) {
                if (!ingredientProcessed[ingIndex] && ingredientsWithRemainder.get(ingIndex).getIngredient().test(slotStack)) {
                    IngredientWithRemainder ingredient = ingredientsWithRemainder.get(ingIndex);

                    if (ingredient.hasRemainder()) {
                        ItemStack remainder = ingredient.getRemainder(slotStack);
                        if (!remainder.isEmpty()) {
                            remainingItems.set(slot, remainder);
                        }
                    }

                    ingredientProcessed[ingIndex] = true;
                    break;
                }
            }
        }

        return remainingItems;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack result = super.assemble(container, registryAccess);

        if (!ServerConfig.ENABLE_MINIGAME.get()) {
            // When minigame is disabled
            boolean hasUnpolishedQualityItem = false;
            boolean unquenched = false;
            String foundQuality = null;
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack ingredient = container.getItem(i);
                if (ingredient.hasTag()) {
                    CompoundTag tag = ingredient.getTag();

                    if (!tag.contains("Polished") || !tag.getBoolean("Polished")) {
                        hasUnpolishedQualityItem = true;
                        break; // No need to check further if we found one
                    }
                    if (tag.contains("Heated") && tag.getBoolean("Heated")) {
                        unquenched = true;
                        break;
                    }
                    if (tag.contains("ForgingQuality")) {
                        if (!tag.getString("ForgingQuality").equals("none"))
                            foundQuality = tag.getString("ForgingQuality");
                    }
                }
            }

            // Prevent crafting if any unpolished quality items exist
            if (hasUnpolishedQualityItem || unquenched) {
                return ItemStack.EMPTY;
            }
            CompoundTag resultTag = result.getOrCreateTag();
            ForgingQuality quality = ForgingQuality.fromString(foundQuality);
            resultTag.putString("ForgingQuality", quality.getDisplayName());
            result.setTag(resultTag);
            return result;
        }

        // Original minigame-enabled logic
        CompoundTag resultTag = result.getOrCreateTag();
        String foundQuality = null;
        boolean isPolished = true;
        boolean unquenched = false;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack ingredient = container.getItem(i);
            if (ingredient.hasTag()) {
                CompoundTag tag = ingredient.getTag();
                if (tag.contains("ForgingQuality")) {
                    if (!tag.getString("ForgingQuality").equals("none"))
                        foundQuality = tag.getString("ForgingQuality");
                }
                if (!tag.contains("Polished") || !tag.getBoolean("Polished")) {
                    isPolished = false;
                }
                if (tag.contains("Heated") && tag.getBoolean("Heated")) {
                    unquenched = true;
                }
            }
        }
        if (foundQuality == null || foundQuality.equals("none")) {
            // If no quality found
            if (!isPolished || unquenched) {
                // Either polished OR unquenched (or both) → set to POOR
                resultTag.putString("ForgingQuality", ForgingQuality.POOR.getDisplayName());
                result.setTag(resultTag);
            }
            return result;
        } else {
            ForgingQuality quality = ForgingQuality.fromString(foundQuality);

            if (!isPolished) {
                quality = quality.getLowerQuality();
            }
            if (unquenched) {
                quality = quality.getLowerQuality();
            }

            resultTag.putString("ForgingQuality", quality.getDisplayName());
            result.setTag(resultTag);
            return result;
        }
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    // Custom ingredient class with remainder support
    public static class IngredientWithRemainder {
        private final Ingredient ingredient;
        private final boolean remainder;
        private final int durabilityDecrease;

        public IngredientWithRemainder(Ingredient ingredient, boolean remainder, int durabilityDecrease) {
            this.ingredient = ingredient;
            this.remainder = remainder;
            this.durabilityDecrease = durabilityDecrease;
        }

        public static IngredientWithRemainder fromNetwork(FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            boolean remainder = buffer.readBoolean();
            int durabilityDecrease = buffer.readInt();
            return new IngredientWithRemainder(ingredient, remainder, durabilityDecrease);
        }

        public Ingredient getIngredient() {
            return ingredient;
        }

        public boolean hasRemainder() {
            return remainder;
        }

        public int getDurabilityDecrease() {
            return durabilityDecrease;
        }

        public ItemStack getRemainder(ItemStack original) {
            if (!remainder) {
                return ItemStack.EMPTY;
            }

            ItemStack remainderStack = original.copy();
            remainderStack.setCount(1);

            // Handle durability decrease for damageable items
            if (durabilityDecrease > 0 && remainderStack.isDamageableItem()) {
                int newDamage = remainderStack.getDamageValue() + durabilityDecrease;
                if (newDamage >= remainderStack.getMaxDamage()) {
                    return ItemStack.EMPTY; // Item breaks
                }
                remainderStack.setDamageValue(newDamage);
            }

            return remainderStack;
        }

        public void toNetwork(FriendlyByteBuf buffer) {
            ingredient.toNetwork(buffer);
            buffer.writeBoolean(remainder);
            buffer.writeInt(durabilityDecrease);
        }
    }

    public static class Type implements RecipeType<OvergearedShapelessRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_shapeless";
    }

    public static class Serializer implements RecipeSerializer<OvergearedShapelessRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public OvergearedShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = json.has("group") ? json.get("group").getAsString() : "";
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", "misc"), CraftingBookCategory.MISC);
            // Parse result
            JsonObject resultJson = json.getAsJsonObject("result");
            ItemStack result = ShapedRecipe.itemStackFromJson(resultJson);

            // Parse ingredients with remainder support
            JsonArray ingredientsJson = json.getAsJsonArray("ingredients");
            NonNullList<IngredientWithRemainder> ingredients = NonNullList.create();

            for (JsonElement element : ingredientsJson) {
                JsonObject ingredientJson = element.getAsJsonObject();

                // Parse base ingredient
                Ingredient ingredient = Ingredient.fromJson(ingredientJson);

                // Parse remainder properties
                boolean remainder = ingredientJson.has("remainder") && ingredientJson.get("remainder").getAsBoolean();
                int durabilityDecrease = ingredientJson.has("durability_decrease") ? ingredientJson.get("durability_decrease").getAsInt() : 0;

                ingredients.add(new IngredientWithRemainder(ingredient, remainder, durabilityDecrease));
            }

            return new OvergearedShapelessRecipe(recipeId, group, category, result, ingredients);
        }

        @Override
        public OvergearedShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            ItemStack result = buffer.readItem();

            int ingredientCount = buffer.readVarInt();
            NonNullList<IngredientWithRemainder> ingredients = NonNullList.create();
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.add(IngredientWithRemainder.fromNetwork(buffer));
            }

            return new OvergearedShapelessRecipe(recipeId, group, category, result, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, OvergearedShapelessRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeItem(recipe.getResultItem(null));

            buffer.writeVarInt(recipe.ingredientsWithRemainder.size());
            for (IngredientWithRemainder ingredient : recipe.ingredientsWithRemainder) {
                ingredient.toNetwork(buffer);
            }
        }
    }
}
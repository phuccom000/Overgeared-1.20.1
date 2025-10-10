package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

public class OvergearedShapelessRecipe extends ShapelessRecipe {

    private final NonNullList<ExtraIngredient> extraIngredients;

    public OvergearedShapelessRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                     ItemStack result, NonNullList<ExtraIngredient> extraIngredients) {
        super(id, group, category, result, toIngredientList(extraIngredients));
        this.extraIngredients = extraIngredients;
    }

    private static NonNullList<Ingredient> toIngredientList(NonNullList<ExtraIngredient> extras) {
        NonNullList<Ingredient> list = NonNullList.create();
        for (ExtraIngredient ex : extras) {
            list.add(ex.ingredient);
        }
        return list;
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
                        break;
                    }
                    if (tag.contains("Heated") && tag.getBoolean("Heated")) {
                        unquenched = true;
                        break;
                    }
                    if (tag.contains("ForgingQuality")) {
                        foundQuality = tag.getString("ForgingQuality");
                    }
                }
            }

            // Prevent crafting if any unpolished quality items exist
            if (hasUnpolishedQualityItem || unquenched) {
                return ItemStack.EMPTY;
            }

            if (foundQuality != null) {
                // Only set a tag if we actually have quality info
                CompoundTag resultTag = new CompoundTag();
                ForgingQuality quality = ForgingQuality.fromString(foundQuality);
                resultTag.putString("ForgingQuality", quality.getDisplayName());
                result.setTag(resultTag);
            } else {
                result.setTag(null); // ensure no empty tag
            }
            return result;
        }

        // Original minigame-enabled logic
        String foundQuality = null;
        boolean isPolished = false;
        boolean unquenched = false;

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
                if (tag.contains("Heated") && tag.getBoolean("Heated")) {
                    unquenched = true;
                }
            }
        }

        if (foundQuality != null) {
            ForgingQuality quality = ForgingQuality.fromString(foundQuality);
            if (!isPolished) {
                quality = quality.getLowerQuality();
            }
            if (unquenched) {
                quality = quality.getLowerQuality();
            }
            CompoundTag resultTag = new CompoundTag();
            resultTag.putString("ForgingQuality", quality.getDisplayName());
            result.setTag(resultTag);
        } else {
            if (unquenched) return ItemStack.EMPTY;
            result.setTag(null); // ensure no empty tag
        }

        return result;
    }


    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remains = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int slot = 0; slot < container.getContainerSize(); ++slot) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) continue;

            // Find matching extra ingredient definition
            for (ExtraIngredient ex : extraIngredients) {
                if (ex.ingredient.test(stack)) {
                    if (ex.durabilityDecrease > 0) {
                        ItemStack copy = stack.copy();
                        // Instead of hurtAndBreak(...)
                        int newDamage = copy.getDamageValue() + ex.durabilityDecrease;
                        if (newDamage >= copy.getMaxDamage()) {
                            remains.set(slot, ItemStack.EMPTY);   // tool broke
                        } else {
                            copy.setDamageValue(newDamage);
                            remains.set(slot, copy);              // put damaged tool back
                        }

                    } else if (ex.remainder) {
                        remains.set(slot, stack.copy()); // unchanged remainder
                    } else if (stack.hasCraftingRemainingItem()) {
                        remains.set(slot, stack.getCraftingRemainingItem());
                    }
                    break;
                }
            }
        }
        return remains;
    }

    public static class Type implements RecipeType<OvergearedShapelessRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_shapeless";
    }

    public static class ExtraIngredient {
        public final Ingredient ingredient;
        public final boolean remainder;
        public final int durabilityDecrease;

        public ExtraIngredient(Ingredient ingredient, boolean remainder, int durabilityDecrease) {
            this.ingredient = ingredient;
            this.remainder = remainder;
            this.durabilityDecrease = durabilityDecrease;
        }
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements RecipeSerializer<OvergearedShapelessRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public OvergearedShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            JsonArray ingArray = GsonHelper.getAsJsonArray(json, "ingredients");
            NonNullList<ExtraIngredient> extraList = NonNullList.create();

            for (JsonElement el : ingArray) {
                JsonObject obj = el.getAsJsonObject();
                Ingredient ingredient = Ingredient.fromJson(obj);

                boolean remainder = obj.has("remainder") && obj.get("remainder").getAsBoolean();
                int durability = obj.has("durability_decrease") ? obj.get("durability_decrease").getAsInt() : 0;

                extraList.add(new ExtraIngredient(ingredient, remainder, durability));
            }

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            String group = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory cat = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", "misc"), CraftingBookCategory.MISC);

            return new OvergearedShapelessRecipe(recipeId, group, cat, result, extraList);
        }


        @Override
        public OvergearedShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CraftingBookCategory cat = buf.readEnum(CraftingBookCategory.class);
            ItemStack result = buf.readItem();

            int count = buf.readVarInt();
            NonNullList<ExtraIngredient> extraList = NonNullList.withSize(count, null);

            for (int i = 0; i < count; i++) {
                Ingredient ing = Ingredient.fromNetwork(buf);
                boolean rem = buf.readBoolean();
                int dur = buf.readVarInt();
                extraList.set(i, new ExtraIngredient(ing, rem, dur));
            }

            return new OvergearedShapelessRecipe(recipeId, group, cat, result, extraList);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, OvergearedShapelessRecipe recipe) {
            buf.writeUtf(recipe.getGroup());
            buf.writeEnum(recipe.category());
            buf.writeItem(recipe.getResultItem(null));

            buf.writeVarInt(recipe.extraIngredients.size());
            for (ExtraIngredient ex : recipe.extraIngredients) {
                ex.ingredient.toNetwork(buf);
                buf.writeBoolean(ex.remainder);
                buf.writeVarInt(ex.durabilityDecrease);
            }
        }
    }


}
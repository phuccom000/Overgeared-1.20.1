package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.OvergearedMod;

public class FletchingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient tip, shaft, feather, potion; // <-- added potion
    private final ItemStack result;
    private final ItemStack resultTipped;
    private final ItemStack resultLingering;
    private final String tippedTag;
    private final String lingeringTag;

    public FletchingRecipe(ResourceLocation id, Ingredient tip, Ingredient shaft, Ingredient feather, Ingredient potion,
                           ItemStack result, ItemStack resultTipped, ItemStack resultLingering,
                           String tippedTag, String lingeringTag) {
        this.id = id;
        this.tip = tip;
        this.shaft = shaft;
        this.feather = feather;
        this.potion = potion != null ? potion : Ingredient.EMPTY;
        this.result = result;
        this.resultTipped = resultTipped;
        this.resultLingering = resultLingering;
        this.tippedTag = tippedTag != null ? tippedTag : "Potion";
        this.lingeringTag = lingeringTag != null ? lingeringTag : "LingeringPotion";
    }

    @Override
    public boolean matches(Container container, Level level) {
        return (tip == Ingredient.EMPTY || tip.test(container.getItem(0))) &&
                (shaft == Ingredient.EMPTY || shaft.test(container.getItem(1))) &&
                (feather == Ingredient.EMPTY || feather.test(container.getItem(2))) &&
                (potion == Ingredient.EMPTY || potion.test(container.getItem(3)));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return getDefaultResult();
    }

    public Ingredient getTip() {
        return tip;
    }

    public Ingredient getShaft() {
        return shaft;
    }

    public Ingredient getFeather() {
        return feather;
    }

    public Ingredient getPotion() {
        return potion;
    } // <-- getter for potion

    public boolean hasPotion() {
        return potion != null && !potion.isEmpty();
    }

    public ItemStack getDefaultResult() {
        return result.copy();
    }

    public ItemStack getTippedResult() {
        return resultTipped.copy();
    }

    public ItemStack getLingeringResult() {
        return resultLingering.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return getDefaultResult();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FLETCHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FLETCHING.get();
    }

    public boolean hasTippedResult() {
        return !resultTipped.isEmpty();
    }

    public boolean hasLingeringResult() {
        return !resultLingering.isEmpty();
    }

    public String getTippedTag() {
        return tippedTag;
    }

    public String getLingeringTag() {
        return lingeringTag;
    }

    public static class Type implements RecipeType<FletchingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "fletching";
    }

    public static class Serializer implements RecipeSerializer<FletchingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "fletching");

        @Override
        public FletchingRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonObject material = GsonHelper.getAsJsonObject(json, "material");

            // Allow tip, shaft, feather to be optional
            Ingredient tip = material.has("tip") ? Ingredient.fromJson(material.get("tip")) : Ingredient.EMPTY;
            Ingredient shaft = material.has("shaft") ? Ingredient.fromJson(material.get("shaft")) : Ingredient.EMPTY;
            Ingredient feather = material.has("feather") ? Ingredient.fromJson(material.get("feather")) : Ingredient.EMPTY;

            // Optional potion
            Ingredient potion = json.has("potion") ? Ingredient.fromJson(json.get("potion")) : Ingredient.EMPTY;

            // Base result
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            // Optional tipped result
            ItemStack resultTipped = ItemStack.EMPTY;
            String tippedTag = null;
            if (json.has("result_tipped")) {
                JsonObject tippedJson = GsonHelper.getAsJsonObject(json, "result_tipped");
                resultTipped = ShapedRecipe.itemStackFromJson(tippedJson);
                if (tippedJson.has("tag")) {
                    tippedTag = GsonHelper.getAsString(tippedJson, "tag");
                }
            }

            // Optional lingering result
            ItemStack resultLingering = ItemStack.EMPTY;
            String lingeringTag = null;
            if (json.has("result_lingering")) {
                JsonObject lingeringJson = GsonHelper.getAsJsonObject(json, "result_lingering");
                resultLingering = ShapedRecipe.itemStackFromJson(lingeringJson);
                if (lingeringJson.has("tag")) {
                    lingeringTag = GsonHelper.getAsString(lingeringJson, "tag");
                }
            }

            return new FletchingRecipe(id, tip, shaft, feather, potion, result,
                    resultTipped, resultLingering,
                    tippedTag, lingeringTag);
        }

        @Override
        public FletchingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            // Read ingredients (can be EMPTY)
            Ingredient tip = Ingredient.fromNetwork(buffer);
            Ingredient shaft = Ingredient.fromNetwork(buffer);
            Ingredient feather = Ingredient.fromNetwork(buffer);
            Ingredient potion = Ingredient.fromNetwork(buffer);

            // Read result stacks
            ItemStack result = buffer.readItem();
            ItemStack resultTipped = buffer.readItem();
            ItemStack resultLingering = buffer.readItem();

            // Read optional tags
            String tippedTag = buffer.readBoolean() ? buffer.readUtf() : null;
            String lingeringTag = buffer.readBoolean() ? buffer.readUtf() : null;

            return new FletchingRecipe(id, tip, shaft, feather, potion, result,
                    resultTipped, resultLingering,
                    tippedTag, lingeringTag);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FletchingRecipe recipe) {
            // Write ingredients (Ingredient.EMPTY is supported by vanilla serializer)
            recipe.tip.toNetwork(buffer);
            recipe.shaft.toNetwork(buffer);
            recipe.feather.toNetwork(buffer);
            recipe.potion.toNetwork(buffer);

            // Write result stacks
            buffer.writeItem(recipe.result);
            buffer.writeItem(recipe.resultTipped);
            buffer.writeItem(recipe.resultLingering);

            // Write optional tags
            if (recipe.tippedTag != null) {
                buffer.writeBoolean(true);
                buffer.writeUtf(recipe.tippedTag);
            } else {
                buffer.writeBoolean(false);
            }

            if (recipe.lingeringTag != null) {
                buffer.writeBoolean(true);
                buffer.writeUtf(recipe.lingeringTag);
            } else {
                buffer.writeBoolean(false);
            }
        }

    }
}

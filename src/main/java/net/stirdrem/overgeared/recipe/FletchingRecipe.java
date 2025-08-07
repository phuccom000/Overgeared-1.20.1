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

import javax.annotation.Nullable;

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
        return tip.test(container.getItem(0)) &&
                shaft.test(container.getItem(1)) &&
                feather.test(container.getItem(2)) &&
                (potion == Ingredient.EMPTY || potion.test(container.getItem(3))); // slot 3 is potion
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
        public static final FletchingRecipe.Type INSTANCE = new FletchingRecipe.Type();
        public static final String ID = "fletching";
    }

    public static class Serializer implements RecipeSerializer<FletchingRecipe> {
        public static final FletchingRecipe.Serializer INSTANCE = new FletchingRecipe.Serializer();
        public static final ResourceLocation ID = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "fletching");

        @Override
        public FletchingRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonObject material = GsonHelper.getAsJsonObject(json, "material");
            Ingredient tip = Ingredient.fromJson(material.get("tip"));
            Ingredient shaft = Ingredient.fromJson(material.get("shaft"));
            Ingredient feather = Ingredient.fromJson(material.get("feather"));
            Ingredient potion = json.has("potion") ? Ingredient.fromJson(json.get("potion")) : Ingredient.EMPTY;

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            ItemStack resultTipped = ItemStack.EMPTY;
            String tippedTag = null;
            if (json.has("result_tipped")) {
                JsonObject tippedJson = GsonHelper.getAsJsonObject(json, "result_tipped");
                resultTipped = ShapedRecipe.itemStackFromJson(tippedJson);
                if (tippedJson.has("tag")) {
                    tippedTag = GsonHelper.getAsString(tippedJson, "tag");
                }
            }

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
        public @Nullable FletchingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient tip = Ingredient.fromNetwork(buf);
            Ingredient shaft = Ingredient.fromNetwork(buf);
            Ingredient feather = Ingredient.fromNetwork(buf);
            Ingredient potion = Ingredient.fromNetwork(buf);

            ItemStack result = buf.readItem();
            ItemStack resultTipped = buf.readItem();
            ItemStack resultLingering = buf.readItem();
            String tippedTag = buf.readUtf();
            String lingeringTag = buf.readUtf();

            return new FletchingRecipe(id, tip, shaft, feather, potion, result,
                    resultTipped, resultLingering,
                    tippedTag, lingeringTag);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FletchingRecipe recipe) {
            recipe.tip.toNetwork(buf);
            recipe.shaft.toNetwork(buf);
            recipe.feather.toNetwork(buf);
            recipe.potion.toNetwork(buf);

            buf.writeItem(recipe.result);
            buf.writeItem(recipe.resultTipped);
            buf.writeItem(recipe.resultLingering);
            buf.writeUtf(recipe.tippedTag);
            buf.writeUtf(recipe.lingeringTag);
        }
    }
}

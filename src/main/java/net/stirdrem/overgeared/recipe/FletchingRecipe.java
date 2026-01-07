package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class FletchingRecipe implements Recipe<RecipeInput> {
    private final Ingredient tip, shaft, feather, potion;
    private final ItemStack result;
    private final ItemStack resultTipped;
    private final ItemStack resultLingering;
    private final String tippedTag;
    private final String lingeringTag;

    public FletchingRecipe(Ingredient tip, Ingredient shaft, Ingredient feather, Ingredient potion,
                           ItemStack result, ItemStack resultTipped, ItemStack resultLingering,
                           String tippedTag, String lingeringTag) {
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
    public boolean matches(RecipeInput recipeInput, Level level) {
        return (tip == Ingredient.EMPTY || tip.test(recipeInput.getItem(0))) &&
                (shaft == Ingredient.EMPTY || shaft.test(recipeInput.getItem(1))) &&
                (feather == Ingredient.EMPTY || feather.test(recipeInput.getItem(2))) &&
                (potion == Ingredient.EMPTY || potion.test(recipeInput.getItem(3)));
    }

    @Override
    public ItemStack assemble(RecipeInput container, HolderLookup.Provider provider) {
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
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return getDefaultResult();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FLETCHING_SERIALIZER.get();
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

    public static class Serializer implements RecipeSerializer<FletchingRecipe> {
        public static final MapCodec<FletchingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC.optionalFieldOf("tip", Ingredient.EMPTY).forGetter(r -> r.tip),
                Ingredient.CODEC.optionalFieldOf("shaft", Ingredient.EMPTY).forGetter(r -> r.shaft),
                Ingredient.CODEC.optionalFieldOf("feather", Ingredient.EMPTY).forGetter(r -> r.feather),
                Ingredient.CODEC.optionalFieldOf("potion", Ingredient.EMPTY).forGetter(r -> r.potion),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                ItemStack.CODEC.optionalFieldOf("result_tipped", ItemStack.EMPTY).forGetter(r -> r.resultTipped),
                ItemStack.CODEC.optionalFieldOf("result_lingering", ItemStack.EMPTY).forGetter(r -> r.resultLingering),
                com.mojang.serialization.Codec.STRING.optionalFieldOf("tipped_tag", "Potion").forGetter(r -> r.tippedTag),
                com.mojang.serialization.Codec.STRING.optionalFieldOf("lingering_tag", "LingeringPotion").forGetter(r -> r.lingeringTag)
        ).apply(i, FletchingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public FletchingRecipe decode(RegistryFriendlyByteBuf buffer) {
                Ingredient tip = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                Ingredient shaft = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                Ingredient feather = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                Ingredient potion = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
                ItemStack resultTipped = ItemStack.STREAM_CODEC.decode(buffer);
                ItemStack resultLingering = ItemStack.STREAM_CODEC.decode(buffer);
                String tippedTag = buffer.readUtf();
                String lingeringTag = buffer.readUtf();
                return new FletchingRecipe(tip, shaft, feather, potion, result, resultTipped, resultLingering, tippedTag, lingeringTag);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, FletchingRecipe recipe) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.tip);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.shaft);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.feather);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.potion);
                ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
                ItemStack.STREAM_CODEC.encode(buffer, recipe.resultTipped);
                ItemStack.STREAM_CODEC.encode(buffer, recipe.resultLingering);
                buffer.writeUtf(recipe.tippedTag);
                buffer.writeUtf(recipe.lingeringTag);
            }
        };

        @Override
        public MapCodec<FletchingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

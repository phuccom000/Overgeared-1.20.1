package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;

public class OvergearedShapelessRecipe extends ShapelessRecipe {

    private final NonNullList<IngredientWithRemainder> ingredientsWithRemainder;

    public OvergearedShapelessRecipe(String group, CraftingBookCategory category,
                                     ItemStack result, NonNullList<IngredientWithRemainder> ingredientsWithRemainder) {
        super(group, category, result, convertToBaseIngredients(ingredientsWithRemainder));
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
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        // Track which ingredients have been processed
        boolean[] ingredientProcessed = new boolean[ingredientsWithRemainder.size()];

        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack slotStack = input.getItem(slot);
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
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        ItemStack result = super.assemble(input, provider);

        if (!ServerConfig.ENABLE_MINIGAME.get()) {
            // When minigame is disabled
            boolean hasUnpolishedQualityItem = false;
            boolean unquenched = false;
            ForgingQuality foundQuality = null;
            String creator = null;
            
            for (int i = 0; i < input.size(); i++) {
                ItemStack ingredient = input.getItem(i);

                // Check if item is heated (unquenched)
                if (ingredient.getOrDefault(ModComponents.HEATED_COMPONENT, false)) {
                    unquenched = true;
                    break;
                }

                // Check if item is polished
                Boolean polished = ingredient.get(ModComponents.POLISHED);
                if (polished != null && !polished) {
                    hasUnpolishedQualityItem = true;
                    break;
                }

                // Get forging quality
                ForgingQuality quality = ingredient.get(ModComponents.FORGING_QUALITY);
                if (quality != null && quality != ForgingQuality.NONE) {
                    foundQuality = quality;
                }

                // Get creator
                String itemCreator = ingredient.get(ModComponents.CREATOR);
                if (itemCreator != null) {
                    creator = itemCreator;
                }
            }

            // Prevent crafting if any unpolished quality items exist or item is unquenched
            if (hasUnpolishedQualityItem || unquenched) {
                return ItemStack.EMPTY;
            }

            // Set quality on result
            if (foundQuality == null) {
                foundQuality = ForgingQuality.NONE;
            }
            result.set(ModComponents.FORGING_QUALITY, foundQuality);
            
            if (creator != null) {
                result.set(ModComponents.CREATOR, creator);
            }
            
            return result;
        }

        // Original minigame-enabled logic
        ForgingQuality foundQuality = null;
        boolean isPolished = true;
        boolean unquenched = false;
        String creator = null;
        
        for (int i = 0; i < input.size(); i++) {
            ItemStack ingredient = input.getItem(i);
            
            // Get forging quality from component
            ForgingQuality quality = ingredient.get(ModComponents.FORGING_QUALITY);
            if (quality != null && quality != ForgingQuality.NONE) {
                foundQuality = quality;
            }
            
            // Check if polished
            Boolean polished = ingredient.get(ModComponents.POLISHED);
            if (polished != null && !polished) {
                isPolished = false;
            }
            
            // Check if heated (unquenched)
            if (ingredient.getOrDefault(ModComponents.HEATED_COMPONENT, false)) {
                unquenched = true;
            }
            
            // Get creator
            String itemCreator = ingredient.get(ModComponents.CREATOR);
            if (itemCreator != null) {
                creator = itemCreator;
            }
        }
        
        if (foundQuality == null || foundQuality == ForgingQuality.NONE) {
            // If no quality found
            if (!isPolished || unquenched) {
                // Either not polished OR unquenched (or both) → set to POOR
                result.set(ModComponents.FORGING_QUALITY, ForgingQuality.POOR);
            }
            return result;
        } else {
            ForgingQuality quality = foundQuality;

            if (!isPolished) {
                quality = quality.getLowerQuality();
            }
            if (unquenched) {
                quality = quality.getLowerQuality();
            }

            result.set(ModComponents.FORGING_QUALITY, quality);
            if (creator != null) {
                result.set(ModComponents.CREATOR, creator);
            }
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

        // Codec for serialization
        public static final Codec<IngredientWithRemainder> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Ingredient.CODEC.fieldOf("ingredient").forGetter(IngredientWithRemainder::getIngredient),
                        Codec.BOOL.optionalFieldOf("remainder", false).forGetter(IngredientWithRemainder::hasRemainder),
                        Codec.INT.optionalFieldOf("durability_decrease", 0).forGetter(IngredientWithRemainder::getDurabilityDecrease)
                ).apply(instance, IngredientWithRemainder::new)
        );

        // StreamCodec for network serialization
        public static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithRemainder> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC,
                        IngredientWithRemainder::getIngredient,
                        net.minecraft.network.codec.ByteBufCodecs.BOOL,
                        IngredientWithRemainder::hasRemainder,
                        net.minecraft.network.codec.ByteBufCodecs.VAR_INT,
                        IngredientWithRemainder::getDurabilityDecrease,
                        IngredientWithRemainder::new
                );
    }

    public static class Type implements RecipeType<OvergearedShapelessRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "crafting_shapeless";
    }

    public static class Serializer implements RecipeSerializer<OvergearedShapelessRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        // Codec for list of IngredientWithRemainder
        private static final Codec<NonNullList<IngredientWithRemainder>> INGREDIENTS_CODEC =
                IngredientWithRemainder.CODEC.listOf().xmap(
                        list -> {
                            NonNullList<IngredientWithRemainder> nonNullList = NonNullList.create();
                            nonNullList.addAll(list);
                            return nonNullList;
                        },
                        list -> list
                );

        @Override
        public MapCodec<OvergearedShapelessRecipe> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.getGroup()),
                    CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
                            .forGetter(r -> r.category()),
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
                    INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(r -> r.ingredientsWithRemainder)
            ).apply(instance, OvergearedShapelessRecipe::new));
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, OvergearedShapelessRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public OvergearedShapelessRecipe decode(RegistryFriendlyByteBuf buffer) {
                    String group = buffer.readUtf();
                    CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
                    ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);

                    int ingredientCount = buffer.readVarInt();
                    NonNullList<IngredientWithRemainder> ingredients = NonNullList.create();
                    for (int i = 0; i < ingredientCount; i++) {
                        ingredients.add(IngredientWithRemainder.STREAM_CODEC.decode(buffer));
                    }

                    return new OvergearedShapelessRecipe(group, category, result, ingredients);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, OvergearedShapelessRecipe recipe) {
                    buffer.writeUtf(recipe.getGroup());
                    buffer.writeEnum(recipe.category());
                    ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(null));

                    buffer.writeVarInt(recipe.ingredientsWithRemainder.size());
                    for (IngredientWithRemainder ingredient : recipe.ingredientsWithRemainder) {
                        IngredientWithRemainder.STREAM_CODEC.encode(buffer, ingredient);
                    }
                }
            };
        }
    }
}
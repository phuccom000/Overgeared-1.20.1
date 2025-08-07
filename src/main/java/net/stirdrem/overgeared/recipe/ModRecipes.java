package net.stirdrem.overgeared.recipe;


import net.minecraft.world.item.crafting.MapCloningRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.OvergearedMod;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, OvergearedMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<ForgingRecipe>> FORGING_SERIALIZER =
            SERIALIZERS.register("forging", () -> ForgingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<RockKnappingRecipe>> ROCK_KNAPPING_SERIALIZER =
            SERIALIZERS.register("rock_knapping", () -> RockKnappingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<OvergearedShapelessRecipe>> CRAFTING_SHAPELESS =
            SERIALIZERS.register("crafting_shapeless", () -> OvergearedShapelessRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<BlueprintCloningRecipe>> CRAFTING_BLUEPRINTCLONING =
            SERIALIZERS.register("crafting_cloning", () -> new SimpleCraftingRecipeSerializer<>(BlueprintCloningRecipe::new));
    public static final RegistryObject<RecipeSerializer<FletchingRecipe>> FLETCHING_SERIALIZER =
            SERIALIZERS.register("fletching", () -> FletchingRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}


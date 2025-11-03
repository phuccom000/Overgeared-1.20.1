package net.stirdrem.overgeared.recipe;


import net.minecraft.world.item.crafting.*;
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
    public static final RegistryObject<RecipeSerializer<DynamicToolCastRecipe>> CRAFTING_DYNAMIC_TOOL_CAST =
            SERIALIZERS.register("crafting_cast", () -> new SimpleCraftingRecipeSerializer<>(DynamicToolCastRecipe::new));
    public static final RegistryObject<RecipeSerializer<ClayToolCastRecipe>> CLAY_TOOL_CAST =
            SERIALIZERS.register("crafting_initial_cast", () -> new SimpleCraftingRecipeSerializer<>(ClayToolCastRecipe::new));
    public static final RegistryObject<RecipeSerializer<FletchingRecipe>> FLETCHING_SERIALIZER =
            SERIALIZERS.register("fletching", () -> FletchingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<NBTKeepingSmeltingRecipe>> NBT_SMELTING =
            SERIALIZERS.register("nbt_smelting", () -> NBTKeepingSmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<NBTKeepingBlastingRecipe>> NBT_BLASTING =
            SERIALIZERS.register("nbt_blasting", () -> NBTKeepingBlastingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<CastSmeltingRecipe>> CAST_SMELTING =
            SERIALIZERS.register("cast_smelting", () -> CastSmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<CastBlastingRecipe>> CAST_BLASTING =
            SERIALIZERS.register("cast_blasting", () -> CastBlastingRecipe.Serializer.INSTANCE);


    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}


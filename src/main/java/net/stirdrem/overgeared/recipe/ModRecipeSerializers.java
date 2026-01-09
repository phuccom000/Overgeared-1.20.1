package net.stirdrem.overgeared.recipe;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, OvergearedMod.MOD_ID);

    public static final Supplier<RecipeSerializer<ForgingRecipe>> FORGING_SERIALIZER =
            RECIPE_SERIALIZERS.register("forging", () -> ForgingRecipe.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<RockKnappingRecipe>> ROCK_KNAPPING_SERIALIZER =
            RECIPE_SERIALIZERS.register("rock_knapping", RockKnappingRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<OvergearedShapelessRecipe>> CRAFTING_SHAPELESS =
            RECIPE_SERIALIZERS.register("crafting_shapeless", OvergearedShapelessRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<BlueprintCloningRecipe>> CRAFTING_BLUEPRINTCLONING =
            RECIPE_SERIALIZERS.register("crafting_cloning", BlueprintCloningRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<DynamicToolCastRecipe>> CRAFTING_DYNAMIC_TOOL_CAST =
            RECIPE_SERIALIZERS.register("crafting_cast", DynamicToolCastRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<ClayToolCastRecipe>> CLAY_TOOL_CAST =
            RECIPE_SERIALIZERS.register("crafting_initial_cast", ClayToolCastRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<FletchingRecipe>> FLETCHING_SERIALIZER =
            RECIPE_SERIALIZERS.register("fletching", FletchingRecipe.Serializer::new);
//    public static final RegistryObject<RecipeSerializer<NBTKeepingSmeltingRecipe>> NBT_SMELTING =
//            SERIALIZERS.register("nbt_smelting", () -> NBTKeepingSmeltingRecipe.Serializer.INSTANCE);
//    public static final RegistryObject<RecipeSerializer<NBTKeepingBlastingRecipe>> NBT_BLASTING =
//            SERIALIZERS.register("nbt_blasting", () -> NBTKeepingBlastingRecipe.Serializer.INSTANCE);
//    public static final RegistryObject<RecipeSerializer<CastSmeltingRecipe>> CAST_SMELTING =
//            SERIALIZERS.register("cast_smelting", () -> CastSmeltingRecipe.Serializer.INSTANCE);
//    public static final RegistryObject<RecipeSerializer<CastBlastingRecipe>> CAST_BLASTING =
//            SERIALIZERS.register("cast_blasting", () -> CastBlastingRecipe.Serializer.INSTANCE);
    public static final Supplier<RecipeSerializer<AlloySmeltingRecipe>> ALLOY_SMELTING =
            RECIPE_SERIALIZERS.register("alloy_smelting", AlloySmeltingRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<ShapedAlloySmeltingRecipe>> SHAPED_ALLOY_SMELTING =
            RECIPE_SERIALIZERS.register("shaped_alloy_smelting", ShapedAlloySmeltingRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<NetherAlloySmeltingRecipe>> NETHER_ALLOY_SMELTING =
        RECIPE_SERIALIZERS.register("nether_alloy_smelting", NetherAlloySmeltingRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<ShapedNetherAlloySmeltingRecipe>> SHAPED_NETHER_ALLOY_SMELTING =
            RECIPE_SERIALIZERS.register("shaped_nether_alloy_smelting", ShapedNetherAlloySmeltingRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<ItemToToolTypeRecipe>> ITEM_TO_TOOLTYPE =
        RECIPE_SERIALIZERS.register("item_to_tooltype", ItemToToolTypeRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<CoolingRecipe>> COOLING_SERIALIZER =
        RECIPE_SERIALIZERS.register("cooling", CoolingRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<GrindingRecipe>> GRINDING_SERIALIZER =
        RECIPE_SERIALIZERS.register("grinding", GrindingRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}


package net.stirdrem.overgeared.recipe;


import net.minecraft.resources.ResourceLocation;
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
    public static final RegistryObject<RecipeSerializer<AlloySmeltingRecipe>> ALLOY_SMELTING =
            SERIALIZERS.register("alloy_smelting", () -> AlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<ShapedAlloySmeltingRecipe>> SHAPED_ALLOY_SMELTING =
            SERIALIZERS.register("shaped_alloy_smelting", () -> ShapedAlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<NetherAlloySmeltingRecipe>> NETHER_ALLOY_SMELTING =
            SERIALIZERS.register("nether_alloy_smelting", () -> NetherAlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<ShapedNetherAlloySmeltingRecipe>> SHAPED_NETHER_ALLOY_SMELTING =
            SERIALIZERS.register("shaped_nether_alloy_smelting", () -> ShapedNetherAlloySmeltingRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<ItemToToolTypeRecipe>> ITEM_TO_TOOLTYPE =
            SERIALIZERS.register("item_to_tooltype", ItemToToolTypeRecipe.Serializer::new);
    public static final RegistryObject<RecipeSerializer<CoolingRecipe>> COOLING_SERIALIZER =
            SERIALIZERS.register("cooling", CoolingRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}


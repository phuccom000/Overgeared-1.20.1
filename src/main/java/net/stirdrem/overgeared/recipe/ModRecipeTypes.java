package net.stirdrem.overgeared.recipe;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.crafting.RecipeType;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.function.Supplier;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, OvergearedMod.MOD_ID);

//    public static final RegistryObject<RecipeType<ForgingRecipe>> FORGING =
//            RECIPE_TYPES.register(ForgingRecipe.Type.ID, () -> ForgingRecipe.Type.INSTANCE);
    public static final Supplier<RecipeType<RockKnappingRecipe>> KNAPPING =
            RECIPE_TYPES.register("rock_knapping", () -> RecipeType.simple(OvergearedMod.loc("rock_knapping")));
//    public static final RegistryObject<RecipeType<FletchingRecipe>> FLETCHING =
//            RECIPE_TYPES.register(FletchingRecipe.Type.ID, () -> FletchingRecipe.Type.INSTANCE);
    public static final Supplier<RecipeType<AlloySmeltingRecipe>> ALLOY_SMELTING =
            RECIPE_TYPES.register("alloy_smelting", () -> RecipeType.simple(OvergearedMod.loc("alloy_smelting")));
    public static final Supplier<RecipeType<NetherAlloySmeltingRecipe>> NETHER_ALLOY_SMELTING =
            RECIPE_TYPES.register("nether_alloy_smelting", () -> RecipeType.simple(OvergearedMod.loc("nether_alloy_smelting")));
    public static final Supplier<RecipeType<ShapedAlloySmeltingRecipe>> SHAPED_ALLOY_SMELTING =
            RECIPE_TYPES.register(ShapedAlloySmeltingRecipe.Type.ID, () -> RecipeType.simple(OvergearedMod.loc(ShapedAlloySmeltingRecipe.Type.ID)));
//    public static final RegistryObject<RecipeType<ShapedAlloySmeltingRecipe>> SHAPED_ALLOY_SMELTING =
//            RECIPE_TYPES.register(ShapedAlloySmeltingRecipe.Type.ID, () -> ShapedAlloySmeltingRecipe.Type.INSTANCE);
//    public static final RegistryObject<RecipeType<NetherAlloySmeltingRecipe>> NETHER_ALLOY_SMELTING =
//            RECIPE_TYPES.register(NetherAlloySmeltingRecipe.Type.ID, () -> NetherAlloySmeltingRecipe.Type.INSTANCE);
//    public static final RegistryObject<RecipeType<ShapedNetherAlloySmeltingRecipe>> SHAPED_NETHER_ALLOY_SMELTING =
//            RECIPE_TYPES.register(ShapedNetherAlloySmeltingRecipe.Type.ID, () -> ShapedNetherAlloySmeltingRecipe.Type.INSTANCE);
//    public static final RegistryObject<RecipeType<ItemToToolTypeRecipe>> ITEM_TO_TOOLTYPE =
//            RECIPE_TYPES.register("item_to_tooltype", () -> RecipeType.simple(new ResourceLocation(OvergearedMod.MOD_ID, "item_to_tooltype")));
    public static final Supplier<RecipeType<CoolingRecipe>> COOLING_RECIPE =
            RECIPE_TYPES.register("cooling", () -> RecipeType.simple(OvergearedMod.loc("cooling")));
    public static final Supplier<RecipeType<GrindingRecipe>> GRINDING_RECIPE =
            RECIPE_TYPES.register("grinding", () -> RecipeType.simple(OvergearedMod.loc("grinding")));

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
    }

}

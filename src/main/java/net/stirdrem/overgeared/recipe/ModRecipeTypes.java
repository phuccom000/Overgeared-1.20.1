package net.stirdrem.overgeared.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.OvergearedMod;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, OvergearedMod.MOD_ID);

    public static final RegistryObject<RecipeType<ForgingRecipe>> FORGING =
            RECIPE_TYPES.register(ForgingRecipe.Type.ID, () -> ForgingRecipe.Type.INSTANCE);
    public static final RegistryObject<RecipeType<RockKnappingRecipe>> KNAPPING =
            RECIPE_TYPES.register(RockKnappingRecipe.Type.ID, () -> RockKnappingRecipe.Type.INSTANCE);

   /* public static final RegistryObject<RecipeType<ForgingQualityShapelessRecipe>> CRAFTING_SHAPELESS =
            RECIPE_TYPES.register(ForgingQualityShapelessRecipe.Type.ID, () -> ForgingQualityShapelessRecipe.Type.INSTANCE);*/

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
    }

}

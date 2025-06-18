package net.stirdrem.overgeared.recipe;

import net.minecraft.world.item.crafting.Recipe;
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
            RECIPE_TYPES.register("forging", () -> registerRecipeType("forging"));
   /* public static final RegistryObject<RecipeType<ForgingQualityShapelessRecipe>> CRAFTING_SHAPELESS =
            RECIPE_TYPES.register(ForgingQualityShapelessRecipe.Type.ID, () -> ForgingQualityShapelessRecipe.Type.INSTANCE);*/

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
    }

    public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(final String identifier) {
        return new RecipeType<>() {
            public String toString() {
                return OvergearedMod.MOD_ID + ":" + identifier;
            }
        };
    }

}

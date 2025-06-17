package net.stirdrem.overgeared.recipe;


import com.google.gson.JsonArray;
import net.minecraft.world.item.crafting.RecipeSerializer;
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
    public static final RegistryObject<RecipeSerializer<ForgingQualityShapelessRecipe>> CRAFTING_SHAPELESS =
            SERIALIZERS.register("crafting_shapeless", () -> ForgingQualityShapelessRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}


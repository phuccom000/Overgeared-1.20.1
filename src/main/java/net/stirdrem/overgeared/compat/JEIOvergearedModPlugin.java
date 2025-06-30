package net.stirdrem.overgeared.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;
import net.stirdrem.overgeared.screen.RockKnappingScreen;
import net.stirdrem.overgeared.screen.SteelSmithingAnvilScreen;
import net.stirdrem.overgeared.screen.StoneSmithingAnvilScreen;

import java.util.List;

@JeiPlugin
public class JEIOvergearedModPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ForgingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new KnappingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) return;
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<ForgingRecipe> forgingRecipes = recipeManager.getAllRecipesFor(ForgingRecipe.Type.INSTANCE);
        registration.addRecipes(ForgingRecipeCategory.FORGING_RECIPE_TYPE, forgingRecipes);
        List<RockKnappingRecipe> knappingRecipes = recipeManager.getAllRecipesFor(RockKnappingRecipe.Type.INSTANCE);
        registration.addRecipes(KnappingRecipeCategory.KNAPPING_RECIPE_TYPE, knappingRecipes);

    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(SteelSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(StoneSmithingAnvilScreen.class, 90, 35, 22, 15,
                ForgingRecipeCategory.FORGING_RECIPE_TYPE);

        registration.addRecipeClickArea(RockKnappingScreen.class, 90, 35, 22, 15,
                KnappingRecipeCategory.KNAPPING_RECIPE_TYPE);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.SMITHING_ANVIL.get()), // or your custom source block
                ForgingRecipeCategory.FORGING_RECIPE_TYPE
        );
    }

}

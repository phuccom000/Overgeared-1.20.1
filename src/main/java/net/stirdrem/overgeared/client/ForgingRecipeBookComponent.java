package net.stirdrem.overgeared.client;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.stirdrem.overgeared.recipe.ModRecipeBookTypes;

import java.util.List;

public class ForgingRecipeBookComponent extends RecipeBookComponent {
    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(152, 182, 28, 18, RECIPE_BOOK_LOCATION);
    }

    @Override
    public void setupGhostRecipe(Recipe<?> pRecipe, List<Slot> pSlots) {
        super.setupGhostRecipe(pRecipe, pSlots);
    }
}
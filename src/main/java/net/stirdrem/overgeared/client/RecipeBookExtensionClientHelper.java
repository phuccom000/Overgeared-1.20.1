package net.stirdrem.overgeared.client;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeBookTypes;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

import java.util.function.Supplier;

public class RecipeBookExtensionClientHelper {

    public static final Supplier<RecipeBookCategories> FORGING_SEARCH = Suppliers
            .memoize(() -> RecipeBookCategories.create("FORGING_SEARCH", new ItemStack(Items.COMPASS)));
    public static final Supplier<RecipeBookCategories> FORGING_TOOLS = Suppliers
            .memoize(() -> RecipeBookCategories.create("FORGING_TOOLS", new ItemStack(Items.IRON_PICKAXE)));
    public static final Supplier<RecipeBookCategories> FORGING_ARMORS = Suppliers
            .memoize(() -> RecipeBookCategories.create("FORGING_ARMORS", new ItemStack(Items.IRON_CHESTPLATE)));
    public static final Supplier<RecipeBookCategories> FORGING_MISC = Suppliers.memoize(() -> RecipeBookCategories
            .create("FORGING_MISC", new ItemStack(Items.BUCKET), new ItemStack(Items.SHEARS)));

    public static void init(RegisterRecipeBookCategoriesEvent event) {
        // Register the full category list
        event.registerBookCategories(OvergearedMod.RECIPE_TYPE_FORGING,
                ImmutableList.of(FORGING_SEARCH.get(), FORGING_TOOLS.get(), FORGING_ARMORS.get(), FORGING_MISC.get()));

        // Register aggregate category (for search tab)
        event.registerAggregateCategory(FORGING_SEARCH.get(),
                ImmutableList.of(FORGING_TOOLS.get(), FORGING_ARMORS.get(), FORGING_MISC.get()));

        // Register how to determine category per recipe
        event.registerRecipeCategoryFinder(ModRecipeTypes.FORGING.get(), recipe -> {
            if (recipe instanceof ForgingRecipe forgingRecipe) {
                ForgingBookRecipeBookTab tab = forgingRecipe.getRecipeBookTab();
                if (tab != null) {
                    return switch (tab) {
                        case TOOLS -> FORGING_TOOLS.get();
                        case ARMORS -> FORGING_ARMORS.get();
                        case MISC -> FORGING_MISC.get();
                    };
                }
            }
            return FORGING_MISC.get();
        });
    }
}

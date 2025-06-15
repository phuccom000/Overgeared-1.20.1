package net.stirdrem.overgeared.client;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.stirdrem.overgeared.recipe.ModRecipeBookTypes;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

import java.util.function.Supplier;

import net.stirdrem.overgeared.ForgingBookCategory;

import java.util.EnumMap;
import java.util.Map;


public class RecipeBookExtensionClientHelper {

    private static final Supplier<RecipeBookCategories> SEARCH_CATEGORY = Suppliers.memoize(
            () -> RecipeBookCategories.create("FORGING_SEARCH", new ItemStack(Items.COMPASS))
    );

    private static final Map<ForgingBookCategory, Supplier<RecipeBookCategories>> CATEGORY_MAP = new EnumMap<>(ForgingBookCategory.class);

    static {
        CATEGORY_MAP.put(ForgingBookCategory.TOOLS, Suppliers.memoize(() ->
                RecipeBookCategories.create("FORGING_TOOLS", new ItemStack(Items.IRON_PICKAXE))));
        CATEGORY_MAP.put(ForgingBookCategory.ARMORS, Suppliers.memoize(() ->
                RecipeBookCategories.create("FORGING_ARMORS", new ItemStack(Items.IRON_CHESTPLATE))));
        CATEGORY_MAP.put(ForgingBookCategory.MISC, Suppliers.memoize(() ->
                RecipeBookCategories.create("FORGING_MISC", new ItemStack(Items.ANVIL))));
    }

    public static void init(RegisterRecipeBookCategoriesEvent event) {
        // Register the full category list
        ImmutableList<RecipeBookCategories> categories = ImmutableList.of(
                SEARCH_CATEGORY.get(),
                CATEGORY_MAP.get(ForgingBookCategory.TOOLS).get(),
                CATEGORY_MAP.get(ForgingBookCategory.ARMORS).get(),
                CATEGORY_MAP.get(ForgingBookCategory.MISC).get()
        );

        event.registerBookCategories(ModRecipeBookTypes.FORGING, categories);

        // Register aggregate category (for search tab)
        event.registerAggregateCategory(SEARCH_CATEGORY.get(), ImmutableList.copyOf(CATEGORY_MAP.values().stream().map(Supplier::get).toList()));

        // Register how to determine category per recipe
        event.registerRecipeCategoryFinder(ModRecipeTypes.FORGING.get(), recipe -> {
            ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
            if (result.getItem() == Items.IRON_SWORD) {
                return CATEGORY_MAP.get(ForgingBookCategory.TOOLS).get();
            } else if (result.getItem() == Items.IRON_HELMET) {
                return CATEGORY_MAP.get(ForgingBookCategory.ARMORS).get();
            } else {
                return CATEGORY_MAP.get(ForgingBookCategory.MISC).get();
            }
        });
    }
}

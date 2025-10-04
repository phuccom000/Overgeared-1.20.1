package net.stirdrem.overgeared.compat;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilMenu;
import net.stirdrem.overgeared.screen.ModMenuTypes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ForgingRecipeTransferHandler implements IRecipeTransferHandler<AbstractSmithingAnvilMenu, ForgingRecipe> {
    private final IRecipeTransferHandlerHelper handlerHelper;
    private final MenuType<? extends AbstractSmithingAnvilMenu> menuType;

    public ForgingRecipeTransferHandler(IRecipeTransferHandlerHelper helper,
                                        MenuType<? extends AbstractSmithingAnvilMenu> menuType) {
        this.handlerHelper = helper;
        this.menuType = menuType;
    }

    @Override
    public Class<AbstractSmithingAnvilMenu> getContainerClass() {
        return AbstractSmithingAnvilMenu.class;
    }

    @Override
    public Optional<MenuType<AbstractSmithingAnvilMenu>> getMenuType() {
        return Optional.of((MenuType<AbstractSmithingAnvilMenu>) menuType);
    }


    @Override
    public RecipeType<ForgingRecipe> getRecipeType() {
        return ForgingRecipeCategory.FORGING_RECIPE_TYPE;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(
            AbstractSmithingAnvilMenu container,
            ForgingRecipe recipe,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer
    ) {
        if (!doTransfer) return null;                   // JEI is only checking
        if (player.level().isClientSide()) return null; // Run only on server

        // Get the input slots of your smithing anvil
        List<Integer> inputSlots = container.getInputSlots(); // <- make sure this exists in your menu
        if (inputSlots == null || inputSlots.isEmpty()) {
            return handlerHelper.createInternalError();
        }

        // Collect required ingredients (choose the first option from each ingredient slot)
        List<ItemStack> ingredients = new ArrayList<>();
        recipe.getIngredients().forEach(ing -> {
            ItemStack[] matching = ing.getItems();
            if (matching.length > 0) ingredients.add(matching[0]);
            else ingredients.add(ItemStack.EMPTY);
        });

        if (ingredients.size() > inputSlots.size()) {
            return handlerHelper.createUserErrorWithTooltip(
                    Component.literal("Not enough input slots for this recipe!")
            );
        }

        // Try to fill each input slot with the required items
        for (int i = 0; i < ingredients.size(); i++) {
            ItemStack needed = ingredients.get(i);
            if (needed.isEmpty()) continue;

            int slotIndex = inputSlots.get(i);
            ItemStack slotStack = container.getSlot(slotIndex).getItem();

            // Skip if the slot already has the right item
            if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(slotStack, needed)) continue;

            boolean moved = moveItemFromPlayerInventory(player, needed, container, slotIndex);
            if (!moved) {
                return handlerHelper.createUserErrorWithTooltip(
                        Component.literal("Missing required item: " + needed.getHoverName().getString())
                );
            }
        }

        return null; // Success
    }

    /**
     * Moves the required item from the player's inventory into the specified slot of the container.
     */
    private boolean moveItemFromPlayerInventory(Player player, ItemStack needed,
                                                AbstractSmithingAnvilMenu container, int targetSlot) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameTags(invStack, needed) && invStack.getCount() >= needed.getCount()) {
                // Move the exact amount needed
                ItemStack toMove = invStack.copy();
                toMove.setCount(needed.getCount());
                container.getSlot(targetSlot).set(toMove);
                invStack.shrink(needed.getCount());
                return true;
            }
        }
        return false;
    }
}

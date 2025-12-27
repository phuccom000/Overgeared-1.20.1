package net.stirdrem.overgeared.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
//import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.item.custom.KnappableRockItem;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;

import java.util.ArrayList;
import java.util.List;

public class RockKnappingMenu extends AbstractContainerMenu {
    private final Container craftingGrid = new SimpleContainer(9); // 3x3 grid
    private final Container resultContainer = new SimpleContainer(1); // Output slot
    private final Level level;
    private final RecipeManager recipeManager;
    private final Player player;
    private ItemStack inputRock; // The rock being knapped
    private boolean knappingFinished = false;
    private boolean resultCollected = false;
    private boolean rockConsumed = false; // Track if rock has been consumed

    // Slot indices constants
    private static final int PLAYER_INVENTORY_SLOT_COUNT = 36; // 27 main + 9 hotbar
    private static final int PLAYER_FIRST_SLOT_INDEX = 0;
    private static final int PLAYER_LAST_SLOT_INDEX = PLAYER_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT - 1;
    private static final int GRID_FIRST_SLOT_INDEX = PLAYER_LAST_SLOT_INDEX + 1;
    private static final int GRID_LAST_SLOT_INDEX = GRID_FIRST_SLOT_INDEX + 8;
    private static final int RESULT_SLOT_INDEX = GRID_LAST_SLOT_INDEX + 1;

    public RockKnappingMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, playerInv.player.level().getRecipeManager());
    }

    public RockKnappingMenu(int id, Inventory playerInv, RecipeManager recipeManager) {
        super(ModMenuTypes.ROCK_KNAPPING_MENU.get(), id);
        this.level = playerInv.player.level();
        this.recipeManager = recipeManager;
        this.player = playerInv.player;

        // Check if player has a knappable rock in either hand
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();

        if (mainHandItem.getItem() instanceof KnappableRockItem) {
            this.inputRock = mainHandItem.copy();
        } else if (offHandItem.getItem() instanceof KnappableRockItem) {
            this.inputRock = offHandItem.copy();
        } else {
            // No knappable rock found - close the menu
            playerInv.player.closeContainer();
            return;
        }

        // Add player inventory slots
        addPlayerHotbar(playerInv);
        addPlayerInventory(playerInv);

        // Add knapping grid slots (36-44) - virtual slots for the buttons
        // These are placed off-screen since they're just for tracking state
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(craftingGrid, i, -1000, -1000) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }

        // Add result/output slot (slot 45)
        this.addSlot(new Slot(resultContainer, 0, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                knappingFinished = true;
                resultCollected = true;
                if (player instanceof ServerPlayer serverPlayer) {
                    //ModAdvancementTriggers.KNAPPING.trigger(serverPlayer);
                }
            }

            @Override
            public boolean mayPickup(Player player) {
                return !getItem().isEmpty() && !knappingFinished;
            }
        });
    }

    public boolean isResultCollected() {
        return resultCollected;
    }

    public void markResultCollected() {
        this.resultCollected = true;
        this.knappingFinished = true;
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        // Only close if the input rock disappears before being consumed
        if (!rockConsumed) {
            return hasInputRock(player);
        }

        // After rock is consumed, keep menu open until player closes it manually
        return true;
    }

    private boolean hasInputRock(Player player) {
        // Check if player still has the rock in either hand
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean hasRock = (ItemStack.isSameItemSameComponents(mainHand, inputRock) && mainHand.getCount() > 0) ||
                (ItemStack.isSameItemSameComponents(offHand, inputRock) && offHand.getCount() > 0);

        if (!hasRock && !player.level().isClientSide) {
            player.closeContainer();
        }

        return hasRock;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Result slot (slot 45)
            if (index == RESULT_SLOT_INDEX) {
                if (!this.moveItemStackTo(itemstack1, PLAYER_FIRST_SLOT_INDEX, PLAYER_LAST_SLOT_INDEX + 1, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);

                // Handle result collection
                if (itemstack1.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                    knappingFinished = true;
                    resultCollected = true;

                    // Trigger advancement when taking result via shift-click
                    if (player instanceof ServerPlayer serverPlayer) {
                        //ModAdvancementTriggers.KNAPPING.trigger(serverPlayer);
                    }
                } else {
                    slot.setChanged();
                }

                return itemstack;
            }
            // Player inventory slots (0-35)
            else if (index >= PLAYER_FIRST_SLOT_INDEX && index <= PLAYER_LAST_SLOT_INDEX) {
                // Just reorganize within player inventory
                if (index < PLAYER_FIRST_SLOT_INDEX + 27) { // Main inventory to hotbar
                    if (!this.moveItemStackTo(itemstack1, PLAYER_FIRST_SLOT_INDEX + 27, PLAYER_LAST_SLOT_INDEX + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else { // Hotbar to main inventory
                    if (!this.moveItemStackTo(itemstack1, PLAYER_FIRST_SLOT_INDEX, PLAYER_FIRST_SLOT_INDEX + 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            // Grid slots (36-44) - these are virtual, shouldn't be accessible for transfer
            else if (index >= GRID_FIRST_SLOT_INDEX && index <= GRID_LAST_SLOT_INDEX) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < 9) {
            setChip(id);
            return true;
        }
        return false;
    }

    public void setChip(int index) {
        if (knappingFinished || resultCollected) return;

        // Consume rock on first chip
        if (!rockConsumed) {
            consumeInputRock();
            rockConsumed = true;
        }

        // Toggle the chip state
        if (!craftingGrid.getItem(index).isEmpty()) {
            // Remove chip (make unchipped)
            craftingGrid.setItem(index, ItemStack.EMPTY);
        } else {
            // Add chip (make chipped) - using a marker item
            craftingGrid.setItem(index, new ItemStack(net.minecraft.world.item.Items.FLINT));
        }

        updateResult();
    }

    private void consumeInputRock() {
        if (level.isClientSide) return;

        // Check both hands and consume from whichever has the rock
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (ItemStack.isSameItemSameComponents(mainHand, inputRock) && mainHand.getCount() > 0) {
            mainHand.shrink(1);
            player.getInventory().setChanged();
        } else if (ItemStack.isSameItemSameComponents(offHand, inputRock) && offHand.getCount() > 0) {
            offHand.shrink(1);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.inventoryMenu.broadcastChanges();
            }
        }
    }

    private void updateResult() {
        if (level == null || knappingFinished || resultCollected) return;

        // Create RecipeInput from container items
        List<ItemStack> items = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            items.add(craftingGrid.getItem(i));
        }
        CraftingInput recipeInput = CraftingInput.of(3, 3, items);

        RecipeHolder<RockKnappingRecipe> recipeHolder = recipeManager
                .getRecipeFor(ModRecipeTypes.KNAPPING.get(), recipeInput, level)
                .orElse(null);

        if (recipeHolder != null) {
            RockKnappingRecipe matchingRecipe = recipeHolder.value();
            resultContainer.setItem(0, matchingRecipe.getResultItem(level.registryAccess()).copy());
        } else {
            resultContainer.setItem(0, ItemStack.EMPTY);
        }

        broadcastChanges();
    }

    public boolean isChipped(int index) {
        // Returns true if this position is chipped (has a marker item)
        return !craftingGrid.getItem(index).isEmpty();
    }

    public void clearGrid() {
        for (int i = 0; i < 9; i++) {
            craftingGrid.setItem(i, ItemStack.EMPTY);
        }
        resultContainer.setItem(0, ItemStack.EMPTY);
        broadcastChanges();
    }

    public boolean isKnappingFinished() {
        return knappingFinished;
    }

    public boolean hasAnyChippedSpots() {
        for (int i = 0; i < 9; i++) {
            if (isChipped(i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        // Only on server side
        // If the player hasn't taken the result, but it's there, give it to them
        ItemStack result = resultContainer.getItem(0);
        if (!result.isEmpty() && !resultCollected) {
            if (player instanceof ServerPlayer serverPlayer) {
                //ModAdvancementTriggers.KNAPPING.trigger(serverPlayer);
            }
            if (!player.getInventory().add(result.copy())) {
                // Drop if inventory is full
                player.drop(result.copy(), false);
            }
            resultContainer.setItem(0, ItemStack.EMPTY);
        }

    }

    // Helper method to get the current grid state as a boolean array
    public boolean[][] getGridState() {
        boolean[][] grid = new boolean[3][3];
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            grid[row][col] = isChipped(i);
        }
        return grid;
    }
}
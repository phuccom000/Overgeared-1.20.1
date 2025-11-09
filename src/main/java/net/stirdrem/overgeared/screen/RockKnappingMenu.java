package net.stirdrem.overgeared.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.KnappableRockItem;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;

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
    private boolean advancementTriggered = false; // Track if advancement has been triggered

    // Slot indices constants
    private static final int PLAYER_INVENTORY_SLOT_COUNT = 36; // 27 main + 9 hotbar
    private static final int PLAYER_FIRST_SLOT_INDEX = 0;
    private static final int PLAYER_LAST_SLOT_INDEX = PLAYER_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT - 1;
    private static final int GRID_FIRST_SLOT_INDEX = PLAYER_LAST_SLOT_INDEX + 1;
    private static final int GRID_LAST_SLOT_INDEX = GRID_FIRST_SLOT_INDEX + 8;
    private static final int RESULT_SLOT_INDEX = GRID_LAST_SLOT_INDEX + 1;

    public RockKnappingMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, playerInv.player.level().getRecipeManager(),
                playerInv.player.getMainHandItem(), playerInv.player.getOffhandItem()); // Use held item by default
    }

    public RockKnappingMenu(int id, Inventory playerInv, RecipeManager recipeManager, ItemStack mainHandItem, ItemStack offHandItem) {
        super(ModMenuTypes.ROCK_KNAPPING_MENU.get(), id);
        // Verify both items are knappable rocks
        if (!(mainHandItem.getItem() instanceof KnappableRockItem) ||
                !(offHandItem.getItem() instanceof KnappableRockItem)) {
            playerInv.player.closeContainer();
        }
        this.level = playerInv.player.level();
        this.recipeManager = recipeManager;
        this.player = playerInv.player;
// Determine which hand has the rock (main hand takes priority)
        // Add player inventory slots
        addPlayerHotbar(playerInv);
        addPlayerInventory(playerInv);

        this.inputRock = mainHandItem.getItem() instanceof KnappableRockItem ? mainHandItem.copy() :
                offHandItem.getItem() instanceof KnappableRockItem ? offHandItem.copy() :
                        ItemStack.EMPTY;

        // Verify we actually have a rock
        if (this.inputRock.isEmpty()) {
            playerInv.player.closeContainer();
            return;
        }

        // Add knapping grid slots (0-8) - these are now virtual slots for the buttons
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(craftingGrid, i, 10000, 10000) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        // Add result/output slot (slot 9)
        this.addSlot(new Slot(resultContainer, 0, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                knappingFinished = true;
                markResultCollected();
                if (!advancementTriggered && player instanceof ServerPlayer serverPlayer) {
                    triggerKnappingAdvancement(serverPlayer);
                    advancementTriggered = true;
                }
                super.onTake(player, stack);
            }

            @Override
            public boolean mayPickup(Player player) {
                return !getItem().isEmpty();
            }
        });


    }

    // Method to trigger the knapping advancement
    private void triggerKnappingAdvancement(ServerPlayer player) {
        // Use Minecraft's advancement system to grant the advancement
        var advancement = player.server.getAdvancements().getAdvancement(
                ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "rock_knapping")
        );

        if (advancement != null) {
            var progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                // Grant all criteria to complete the advancement
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
            }
        }
    }

    public boolean isResultCollected() {
        return resultCollected;
    }

    // Call this when result is taken
    public void markResultCollected() {
        this.resultCollected = true;
        //clearGrid(); // Optional: clear grid when result is taken
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

    public void handleResultCollection(Player player, ItemStack result) {
        if (player instanceof ServerPlayer) {
            knappingFinished = true; // ✅ Disable further interaction
        }
    }

    @Override
    public boolean stillValid(Player player) {
        //hasInputRock(player);
        if (!rockConsumed) {
            return hasInputRock(player);
        }
        return true;
    }


    private boolean hasInputRock(Player player) {
        // Check main hand and offhand first
        if (ItemStack.isSameItemSameTags(player.getMainHandItem(), inputRock) &&
                ItemStack.isSameItemSameTags(player.getOffhandItem(), inputRock)) {
            //OvergearedMod.LOGGER.debug("has input rock");
            return true;
        }

        // Rock not found - close menu
        if (!player.level().isClientSide) {
            player.closeContainer();
            //OvergearedMod.LOGGER.debug("has no input rock");
        }
        return false;
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
                knappingFinished = true;
                // Try to move the result to player inventory (slots 0-35)
                if (!this.moveItemStackTo(itemstack1, PLAYER_FIRST_SLOT_INDEX, PLAYER_LAST_SLOT_INDEX + 1, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);

                // Handle result collection
                if (itemstack1.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }

                // Trigger advancement when taking result via shift-click
                if (!advancementTriggered && player instanceof ServerPlayer serverPlayer) {
                    triggerKnappingAdvancement(serverPlayer);
                    advancementTriggered = true;
                }

                // Update menu state
                this.markResultCollected();
                return itemstack;
            }
            // Player inventory slots (0-35)
            else if (index >= PLAYER_FIRST_SLOT_INDEX && index <= PLAYER_LAST_SLOT_INDEX) {
                // Try to move items from player inventory to appropriate slots
                // Since grid slots are virtual, we don't allow moving items into them
                // Just reorganize within player inventory
                if (index < 27) { // Main inventory to hotbar
                    if (!this.moveItemStackTo(itemstack1, 27, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                } else { // Hotbar to main inventory
                    if (!this.moveItemStackTo(itemstack1, 0, 27, false)) {
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

    public void setChip(int index) {
        if (knappingFinished || resultCollected) return;

        if (!rockConsumed) {
            consumeInputRock();
            rockConsumed = true;
        }
        if (isChipped(index)) {
            craftingGrid.setItem(index, ItemStack.EMPTY);
        } else {
            craftingGrid.setItem(index, new ItemStack(net.minecraft.world.item.Items.FLINT));
        }
        updateResult();
    }

    private void consumeInputRock() {
        if (level.isClientSide) return;

        // Check both hands first
        ItemStack mainHand = player.getMainHandItem();

        if (ItemStack.isSameItemSameTags(mainHand, inputRock)) {
            mainHand.shrink(1);
        }
    }

    private void updateResult() {
        if (level == null || knappingFinished || resultCollected) return;

        RockKnappingRecipe matchingRecipe = recipeManager
                .getRecipeFor(ModRecipeTypes.KNAPPING.get(), craftingGrid, level)
                .orElse(null);

        if (matchingRecipe != null && !knappingFinished) {
            resultContainer.setItem(0, matchingRecipe.getResultItem(level.registryAccess()).copy());
        } else {
            resultContainer.setItem(0, ItemStack.EMPTY);
        }

        broadcastChanges();
    }

    public boolean isChipped(int index) {
        if (level == null || knappingFinished || resultCollected) return true;
        else return !craftingGrid.getItem(index).isEmpty();
    }

    public void clearGrid() {
        craftingGrid.clearContent();
        resultContainer.clearContent();
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

        // If the player hasn't taken the result, but it's there
        ItemStack result = resultContainer.getItem(0);
        if (!result.isEmpty() && !resultCollected) {
            if (!player.getInventory().add(result.copy())) {
                // Drop if inventory is full
                player.drop(result.copy(), false);
            }
        }

        // Optionally: clear the grid
        //this.markResultCollected();
    }

}
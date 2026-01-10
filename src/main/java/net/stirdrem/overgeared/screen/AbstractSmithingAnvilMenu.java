package net.stirdrem.overgeared.screen;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.util.ModTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbstractSmithingAnvilMenu extends AbstractContainerMenu {
    private final Container container = new SimpleContainer();
    public final AbstractSmithingAnvilBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private final ResultContainer resultContainer = new ResultContainer();
    private Slot resultSlot;
    private final Player player;
    private final boolean hasBlueprint;
    private final List<Integer> craftingSlotIndices = new ArrayList<>();

    public AbstractSmithingAnvilMenu(MenuType<?> pMenuType, int pContainerId, Inventory inv, AbstractSmithingAnvilBlockEntity entity, ContainerData data, boolean hasBlueprint) {
        super(pMenuType, pContainerId);
        checkContainerSize(inv, 12);
        blockEntity = entity;
        this.level = inv.player.level();
        this.data = data;
        this.player = inv.player;
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        this.hasBlueprint = hasBlueprint;

        IItemHandler iItemHandler = this.blockEntity.getItemHandler();
        
        this.addSlot(new SlotItemHandler(iItemHandler, 9, 152, 61) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.Items.SMITHING_HAMMERS);
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }
        }); //hammer

        if (hasBlueprint)
            this.addSlot(new SlotItemHandler(iItemHandler, 11, 111, 53) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModItems.BLUEPRINT.get()) || stack.getItem() instanceof SmithingTemplateItem;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return true; // This is crucial for JEI transfers
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }

                @Override
                public void set(ItemStack stack) {
                    // Always limit to a single item
                    super.set(stack.copyWithCount(1));
                }
            });

        //crafting slot
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Slot slotIndex = this.addSlot(new SlotItemHandler(iItemHandler, j + i * 3, 30 + j * 18, 17 + i * 18) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return true; // This is crucial for JEI transfers
                    }
                });
                craftingSlotIndices.add(slotIndex.index); // Store the index, not the Slot object
            }
        }

        //output slot
        this.resultSlot = new SlotItemHandler(iItemHandler, 10, 124, 35) {
            private int removeCount;

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                this.checkTakeAchievements(stack);
                Container craftingContainer = AbstractSmithingAnvilMenu.this.container;
                
                // Create a RecipeInput wrapper for the block entity's item handler
                IItemHandler handler = AbstractSmithingAnvilMenu.this.blockEntity.getItemHandler();
                ItemStackHandler tempHandler = new ItemStackHandler(handler.getSlots());
                for (int i = 0; i < handler.getSlots(); i++) {
                    tempHandler.setStackInSlot(i, handler.getStackInSlot(i));
                }
                RecipeInput recipeInput = new RecipeWrapper(tempHandler);
                
                NonNullList<ItemStack> remainders = player.level()
                        .getRecipeManager().getRemainingItemsFor(ModRecipeTypes.FORGING.get(), recipeInput, player.level());
                for (int i = 0; i < remainders.size(); ++i) {
                    ItemStack toRemove = craftingContainer.getItem(i);
                    ItemStack toReplace = remainders.get(i);
                    if (!toRemove.isEmpty()) {
                        craftingContainer.removeItem(i, 1);
                        toRemove = craftingContainer.getItem(i);
                    }

                    if (!toReplace.isEmpty()) {
                        if (toRemove.isEmpty())
                            craftingContainer.setItem(i, toRemove);
                        else if (ItemStack.isSameItemSameComponents(toRemove, toReplace)) {
                            toReplace.grow(toRemove.getCount());
                            craftingContainer.setItem(i, toReplace);
                        } else if (!player.getInventory().add(toReplace))
                            player.drop(toReplace, false);
                    }
                }
            }

            @Override
            public ItemStack remove(int amount) {
                if (this.hasItem())
                    this.removeCount += Math.min(amount, this.getItem().getCount());
                return super.remove(amount);
            }

            @Override
            public void onQuickCraft(ItemStack output, int amount) {
                this.removeCount += amount;
                this.checkTakeAchievements(output);
            }

            @Override
            protected void onSwapCraft(int amount) {
                this.removeCount = amount;
            }

            @Override
            protected void checkTakeAchievements(ItemStack stack) {
                if (this.removeCount > 0)
                    stack.onCraftedBy(AbstractSmithingAnvilMenu.this.player.level(), AbstractSmithingAnvilMenu.this.player, this.removeCount);
                // Award recipe to player for recipe book integration
                AbstractSmithingAnvilMenu.this.blockEntity.getCurrentRecipeHolder().ifPresent(holder ->
                    AbstractSmithingAnvilMenu.this.player.awardRecipes(List.of(holder))
                );
                this.removeCount = 0;
            }
        };
        this.addSlot(this.resultSlot);

        addDataSlots(data);
    }

    public List<Integer> getInputSlots() {
        return new ArrayList<>(craftingSlotIndices);
    }


    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 12;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT - 1, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);  // Max Progress
        int progressArrowSize = 24; // This is the height in pixels of your arrow

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        // get block at the container’s position
        BlockState state = player.level().getBlockState(blockEntity.getBlockPos());
        Block block = state.getBlock();

        // check if the block at that position is in your tag
        boolean isValid = state.is(ModTags.Blocks.SMITHING_ANVIL);

        return AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player,
                block  // only passed here for distance check
        ) && isValid;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public int getRemainingHits() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        return maxProgress - progress;
    }

    public ItemStack getResultItem() {
        // Check if the block entity exists and has an item handler
        if (blockEntity != null) {
            IItemHandler handler = blockEntity.getItemHandler();
            // Slot 10 is the output slot based on your menu setup
            ItemStack result = handler.getStackInSlot(10);
            // Return a copy to prevent modification of the original stack
            return result.copy();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getGhostResult() {
        // Return the expected result based on current inputs
        // This could be from a recipe match or your custom logic
        Optional<ForgingRecipe> recipeOptional = blockEntity.getCurrentRecipe();
        if (recipeOptional.isPresent()) {
            ForgingRecipe recipe = recipeOptional.get();
            if (blockEntity.hasRecipe()) {
                return recipe.getResultItem(level.registryAccess()).copy();
            }
        }
        return ItemStack.EMPTY;
    }

    /*@Override
    public void fillCraftSlotsStackedContents(StackedContents contents) {

    }

    @Override
    public void clearCraftingContent() {

    }


    @Override
    public boolean recipeMatches(Recipe<? super Container> pRecipe) {
        return false;
    }

    @Override
    public int getResultSlotIndex() {
        return 11;
    }

    @Override
    public int getGridWidth() {
        return 3;
    }

    @Override
    public int getGridHeight() {
        return 3;
    }

    @Override
    public int getSize() {
        return 9;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return ModRecipeBookTypes.FORGING;
    }

    @Override
    public boolean shouldMoveToInventory(int pSlotIndex) {
        return false;
    }*/

}

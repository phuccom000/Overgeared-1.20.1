/*
package net.stirdrem.overgeared.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SmithingAnvilMenu2 extends RecipeBookMenu<CraftingContainer> {
    public final SmithingAnvilBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    private final RecipeBookType bookType = RecipeBookType.create("FORGING_ANVIL");
    private final RecipeType<ForgingRecipe> recipeType = ModRecipeTypes.FORGING.get();
    private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final Player player;


    @Override
    public void fillCraftSlotsStackedContents(StackedContents helper) {
        this.craftSlots.fillStackedContents(helper);

    }

    @Override
    public void clearCraftingContent() {
        this.craftSlots.clearContent();
        this.resultSlots.clearContent();
    }

    @Override
    public boolean recipeMatches(Recipe<? super CraftingContainer> pRecipe) {
        return pRecipe.matches(this.craftSlots, this.player.level());
    }


    @Override
    public int getResultSlotIndex() {
        return TE_INVENTORY_FIRST_SLOT_INDEX + 10;
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


    public RecipeBookType getRecipeBookType() {
        return this.bookType;
    }

    @Override
    public boolean shouldMoveToInventory(int slotIndex) {
        return slotIndex < TE_INVENTORY_FIRST_SLOT_INDEX;
    }

    public RecipeType<?> getRecipeType() {
        return this.recipeType;
    }

    public SmithingAnvilMenu2(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(11));
    }


    public SmithingAnvilMenu2(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.SMITHING_ANVIL_MENU.get(), pContainerId);
        checkContainerSize(inv, 11);
        blockEntity = (SmithingAnvilBlockEntity) entity;
        this.player = inv.player;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 9, 152, 61) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.is(ModTags.Items.SMITHING_HAMMERS);
                }
            }); //hammer
            //crafting slot
            */
/*for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    this.addSlot(new SlotItemHandler(iItemHandler, j + i * 3, 30 + j * 18, 17 + i * 18));
                }
            }
            //output slot
            this.addSlot(new SlotItemHandler(iItemHandler, 10, 124, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false; // Prevent inserting any item
                }
            });*//*


            // Output slot at index 10 in your TE

// 3×3 crafting grid
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    this.addSlot(new Slot(this.craftSlots, col + row * 3, 30 + col * 18, 17 + row * 18));
                }
            }
            this.addSlot(new ResultSlot(inv.player, this.craftSlots, this.resultSlots, 0, 124, 35));

// Cosmetic hammer slot: doesn't affect crafting logic

        });

        addDataSlots(data);
    }

    @Override
    public void slotsChanged(Container inventoryIn) {
        if (inventoryIn == this.craftSlots) {
            slotChangedForgingGrid(this, this.level, this.player, craftSlots, resultSlots);
        }
        super.slotsChanged(inventoryIn);
    }

    protected static void slotChangedForgingGrid(AbstractContainerMenu pMenu, Level pLevel, Player pPlayer, CraftingContainer pContainer, ResultContainer pResult) {
        if (!pLevel.isClientSide) {
            ServerPlayer serverplayer = (ServerPlayer) pPlayer;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<ForgingRecipe> optional = pLevel.getServer().getRecipeManager().getRecipeFor(ModRecipeTypes.FORGING.get(), pContainer, pLevel);
            if (optional.isPresent()) {
                ForgingRecipe forgingRecipe = optional.get();
                if (pResult.setRecipeUsed(pLevel, serverplayer, forgingRecipe)) {
                    ItemStack itemstack1 = forgingRecipe.assemble(pContainer, pLevel.registryAccess());
                    if (itemstack1.isItemEnabled(pLevel.enabledFeatures())) {
                        itemstack = itemstack1;
                    }
                }
            }

            pResult.setItem(0, itemstack);
            pMenu.setRemoteSlot(0, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(pMenu.containerId, pMenu.incrementStateId(), 0, itemstack));
        }
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
    private static final int TE_INVENTORY_SLOT_COUNT = 11;  // must be the number of slots you have!

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
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.SMITHING_ANVIL.get());
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
        //ModMessages.sendToServer(new UpdateAnvilProgressC2SPacket(maxProgress - progress));
        return maxProgress - progress;
    }

    public SmithingAnvilBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ItemStack getResultItem() {
        // Check if the block entity exists and has an item handler
        if (blockEntity != null) {
            return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                    .map(handler -> {
                        // Slot 10 is the output slot based on your menu setup
                        ItemStack result = handler.getStackInSlot(10);
                        // Return a copy to prevent modification of the original stack
                        return result.copy();
                    })
                    .orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }
}
*/

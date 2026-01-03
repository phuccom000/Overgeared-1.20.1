package net.stirdrem.overgeared.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.AlloySmelterBlockEntity;

public class AlloySmelterMenu extends AbstractContainerMenu {

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 6;  // must be the number of slots you have!
    private final AlloySmelterBlockEntity blockEntity;
    private final ContainerData data;
    private final Level level;


    // Default constructor for client-side (read from packet)
    public AlloySmelterMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public AlloySmelterMenu(int id, Inventory playerInv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.ALLOY_SMELTER_MENU.get(), id);
        checkContainerSize(playerInv, 6);
        this.blockEntity = (AlloySmelterBlockEntity) blockEntity;
        this.data = data;
        this.level = playerInv.player.level();
        this.addDataSlots(data);
        // Player inventory slots
        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);

        IItemHandler iItemHandler = this.blockEntity.getItemHandler();

        this.addSlot(new SlotItemHandler(iItemHandler, 0, 39, 26)); // Input 1
        this.addSlot(new SlotItemHandler(iItemHandler, 1, 57, 26)); // Input 2
        this.addSlot(new SlotItemHandler(iItemHandler, 2, 39, 44)); // Input 3
        this.addSlot(new SlotItemHandler(iItemHandler, 3, 57, 44)); // Input 4
        this.addSlot(new SlotItemHandler(iItemHandler, 4, 8, 53)); // Fuel
        this.addSlot(new SlotItemHandler(iItemHandler, 5, 124, 35) { // Output
            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                this.checkTakeAchievements(stack);
                ((AlloySmelterBlockEntity) blockEntity).awardStoredExperience(player);
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }
        });
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (int col = 0; col < 9; ++col)
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.ALLOY_FURNACE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSource = sourceStack.copy();

        // --- Slot index ranges ---
        int startPlayer = VANILLA_FIRST_SLOT_INDEX;                           // 0
        int endPlayer = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;        // 36 (0–35)
        int startTE = TE_INVENTORY_FIRST_SLOT_INDEX;                          // 36
        int endTE = TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT;  // 42 (36–41)

      // slots 36–39
      int inputEnd = startTE + 4;       // exclusive (40)
        int fuelSlot = startTE + 4;       // slot 40
        int outputSlot = startTE + 5;     // slot 41

        // --- CASE 1: From TE (machine) to player inventory ---
        if (index >= startTE && index < endTE) {
            // If output slot → move to player inventory
            if (index == outputSlot) {
                if (!moveItemStackTo(sourceStack, startPlayer, endPlayer, true)) {
                    return ItemStack.EMPTY;
                }
                sourceSlot.onQuickCraft(sourceStack, copyOfSource);
            }
            // If fuel or input slot → move to player inventory
            else {
                if (!moveItemStackTo(sourceStack, startPlayer, endPlayer, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        // --- CASE 2: From player inventory to TE ---
        else if (index >= startPlayer && index < endPlayer) {
            // Try to move to fuel slot if it's fuel
            if (sourceStack.getBurnTime(RecipeType.SMELTING) > 0) {
                // First try fuel slot
                if (!moveItemStackTo(sourceStack, fuelSlot, fuelSlot + 1, false)) {
                    // If fuel slot is occupied, try input slots as fallback
                    if (!moveItemStackTo(sourceStack, startTE, inputEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            // Otherwise try to move to input slots
            else if (!moveItemStackTo(sourceStack, startTE, inputEnd, false)) {
                return ItemStack.EMPTY;
            }
        }
        // --- CASE 3: Invalid index ---
        else {
            return ItemStack.EMPTY;
        }

        // --- Final cleanup ---
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copyOfSource;
    }


    public boolean isLit() {
        return data.get(0) > 0;
    }

    public int getLitProgress() {
        int i = this.data.get(1);
        if (i == 0) i = 200;
        return this.data.get(0) * 13 / i;
    }

    public int getCookProgress() {
        int cookTime = this.data.get(2);
        int cookTimeTotal = this.data.get(3);
        return cookTimeTotal != 0 && cookTime != 0 ? cookTime * 24 / cookTimeTotal : 0;
    }
}

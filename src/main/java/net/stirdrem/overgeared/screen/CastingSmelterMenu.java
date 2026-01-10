package net.stirdrem.overgeared.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.stirdrem.overgeared.block.entity.CastingSmelterBlockEntity;
import net.stirdrem.overgeared.util.ConfigHelper;
import net.stirdrem.overgeared.util.ModTags;

public class CastingSmelterMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;

    public CastingSmelterMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, (CastingSmelterBlockEntity) playerInv.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(4));
    }


    public CastingSmelterMenu(int id, Inventory playerInv, CastingSmelterBlockEntity be, ContainerData data) {
        super(ModMenuTypes.CASTING_SMELTER_MENU.get(), id);
        this.container = be;
        this.data = data;

        checkContainerSize(container, 4);
        addDataSlots(data);

        /* ---------- Furnace slots ---------- */
        // Input
        this.addSlot(new Slot(container, CastingSmelterBlockEntity.SLOT_INPUT, 56, 24) {
            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }
        });

        // Fuel
        this.addSlot(new Slot(container, CastingSmelterBlockEntity.SLOT_FUEL, 8, 53));

        // Output
        this.addSlot(new FurnaceResultSlot(
                playerInv.player,
                container,
                CastingSmelterBlockEntity.SLOT_OUTPUT,
                116,
                35
        ) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                this.checkTakeAchievements(stack);
                be.awardStoredExperience(player);

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

        // Cast
        this.addSlot(new Slot(container, CastingSmelterBlockEntity.SLOT_CAST, 56, 46));

        /* ---------- Player inventory ---------- */
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInv,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }


    /* ---------- Progress helpers ---------- */

    public int getBurnProgress() {
        int burn = data.get(0);
        int total = data.get(1);
        return total == 0 ? 0 : burn * 13 / total;
    }

    public int getCookProgress() {
        int cook = data.get(2);
        int total = data.get(3);
        return total == 0 ? 0 : cook * 24 / total;
    }

    public boolean isBurning() {
        return data.get(0) > 0;
    }

    /* ---------- Shift-click ---------- */

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original;
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        original = stack.copy();

        /* ================= OUTPUT SLOT ================= */
        if (index == CastingSmelterBlockEntity.SLOT_OUTPUT) {
            if (!this.moveItemStackTo(stack, 4, 40, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        }

        /* ================= PLAYER INVENTORY → BLOCK ================= */
        else if (index >= 4) {
            // TOOL CAST → CAST SLOT (try this first)
            if (stack.is(ModTags.Items.TOOL_CAST)) {
                if (!this.moveItemStackTo(
                        stack,
                        CastingSmelterBlockEntity.SLOT_CAST,
                        CastingSmelterBlockEntity.SLOT_CAST + 1,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            }
            // FUEL → FUEL SLOT
            else if (stack.getBurnTime(RecipeType.SMELTING) > 0) {
                if (!this.moveItemStackTo(
                        stack,
                        CastingSmelterBlockEntity.SLOT_FUEL,
                        CastingSmelterBlockEntity.SLOT_FUEL + 1,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            }
            // MATERIAL → INPUT SLOT
            else if (ConfigHelper.isValidMaterial(stack)) {
                if (!this.moveItemStackTo(
                        stack,
                        CastingSmelterBlockEntity.SLOT_INPUT,
                        CastingSmelterBlockEntity.SLOT_INPUT + 1,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            }
            // HOTBAR → PLAYER INVENTORY
            else if (index >= 31 && index < 40) {
                if (!this.moveItemStackTo(stack, 4, 31, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // PLAYER INVENTORY → HOTBAR
            else {
                if (!this.moveItemStackTo(stack, 31, 40, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        /* ================= BLOCK → PLAYER ================= */
        else {
            // Handle moving items from furnace slots to player inventory
            if (!this.moveItemStackTo(stack, 4, 40, false)) {
                return ItemStack.EMPTY;
            }
        }

        /* ================= CLEANUP ================= */
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }


    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}

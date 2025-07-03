package net.stirdrem.overgeared.screen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.item.custom.BlueprintItem;
import net.stirdrem.overgeared.screen.ModMenuTypes;
import org.jetbrains.annotations.NotNull;

public class BlueprintWorkbenchMenu extends AbstractContainerMenu {
    private final Container inputContainer;
    private final Container outputContainer;
    private final ContainerData data;

    public BlueprintWorkbenchMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(1), new SimpleContainer(1), new SimpleContainerData(2));
    }

    public BlueprintWorkbenchMenu(int containerId, Inventory playerInventory,
                                  Container inputContainer, Container outputContainer,
                                  ContainerData data) {
        super(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU.get(), containerId);
        this.inputContainer = inputContainer;
        this.outputContainer = outputContainer;
        this.data = data;

        // Input slot (slot 0)
        this.addSlot(new Slot(inputContainer, 0, 48, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.EMPTY_BLUEPRINT.get());
            }
        });

        // Output slot (slot 1) - not player interactable
        this.addSlot(new Slot(outputContainer, 0, 106, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Players can't put items in output
            }
        });

        // Player inventory (slots 2-37)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar (slots 38-46)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    public void createBlueprint(ToolType toolType) {
        ItemStack input = this.inputContainer.getItem(0);
        if (!input.is(ModItems.EMPTY_BLUEPRINT.get()) || input.isEmpty()) {
            return;
        }

        ItemStack output = this.outputContainer.getItem(0);

        // Create new blueprint if output is empty or doesn't match
        if (output.isEmpty()) {
            ItemStack newOutput = new ItemStack(ModItems.BLUEPRINT.get());
            CompoundTag tag = newOutput.getOrCreateTag();
            BlueprintQuality quality = BlueprintQuality.WELL;
            tag.putString("ToolType", toolType.getId());
            tag.putString("Quality", quality.getDisplayName());
            tag.putInt("Uses", 0);
            tag.putInt("UsesToLevel", quality.getUse());

            // Set output and consume 1 input item
            this.outputContainer.setItem(0, newOutput);
            input.shrink(1);
            if (input.isEmpty()) {
                this.inputContainer.setItem(0, ItemStack.EMPTY);
            } else {
                this.inputContainer.setItem(0, input);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Output slot behavior
            if (index == 1) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            }
            // Input slot behavior
            else if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Player inventory -> input slot
            else if (itemstack1.is(ModItems.EMPTY_BLUEPRINT.get())) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Hotbar <-> inventory
            else if (index >= 2 && index < 29) {
                if (!this.moveItemStackTo(itemstack1, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 29 && index < 38) {
                if (!this.moveItemStackTo(itemstack1, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inputContainer.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, inputContainer);
        this.clearContainer(player, outputContainer);
    }
}
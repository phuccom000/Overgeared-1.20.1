package net.stirdrem.overgeared.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;

public class SteelSmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    private static final int OUTPUT_SLOT_INDEX = 10;
    private static final int TE_INVENTORY_SLOT_COUNT = 11;

    public SteelSmithingAnvilMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (SteelSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(11));
    }

    public SteelSmithingAnvilMenu(int containerId, Inventory inv, SteelSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU.get(), containerId, inv, entity, data);
        addCustomSlots();
    }

    @Override
    protected void addCustomSlots() {
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // Hammer slot
            this.addSlot(new SlotItemHandler(handler, 9, 152, 61) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.is(ModTags.Items.SMITHING_HAMMERS);
                }
            });

            // Crafting grid slots
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    this.addSlot(new SlotItemHandler(handler, j + i * 3, 30 + j * 18, 17 + i * 18));
                }
            }

            // Output slot
            this.resultSlot = new ResultSlot(handler, OUTPUT_SLOT_INDEX, 124, 35);
            this.addSlot(this.resultSlot);
        });
    }

    @Override
    protected int getCustomSlotCount() {
        return TE_INVENTORY_SLOT_COUNT;
    }

    @Override
    protected int getOutputSlotIndex() {
        return OUTPUT_SLOT_INDEX;
    }

    private class ResultSlot extends SlotItemHandler {
        private int removeCount;

        public ResultSlot(IItemHandler itemHandler, int index, int x, int y) {
            super(itemHandler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            this.checkTakeAchievements(stack);
            super.onTake(player, stack);
        }

        @Override
        protected void checkTakeAchievements(ItemStack stack) {
            if (this.removeCount > 0) {
                stack.onCraftedBy(player.level(), player, this.removeCount);
            }
            this.removeCount = 0;
        }
    }
}
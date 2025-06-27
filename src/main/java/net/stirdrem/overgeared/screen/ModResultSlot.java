/*
package net.stirdrem.overgeared.screen;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;

public class ModResultSlot extends SlotItemHandler {
    private final Player player;
    private final SteelSmithingAnvilBlockEntity blockEntity; // to access input slots
    private int removeCount;

    public ModResultSlot(Player player, IItemHandler itemHandler, SteelSmithingAnvilBlockEntity blockEntity, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.player = player;
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false; // no inserting items into result slot
    }

    @Override
    public ItemStack remove(int amount) {
        if (this.hasItem()) {
            this.removeCount += Math.min(amount, this.getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        this.removeCount += amount;
        this.checkTakeAchievements(stack);
    }

    @Override
    protected void onSwapCraft(int numItemsCrafted) {
        this.removeCount += numItemsCrafted;
    }

    protected void checkTakeAchievements(ItemStack stack) {
        if (this.removeCount > 0) {
            stack.onCraftedBy(this.player.level(), this.player, this.removeCount);
            // If you want to fire a custom event, you can do it here.
        }
        this.removeCount = 0;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        this.checkTakeAchievements(stack);

        */
/*//*
/ Example logic for decrementing input slots
        for (int i = 0; i < 9; ++i) { // assuming slots 0-8 are inputs
            ItemStack inputStack = blockEntity.getItemHandler().getStackInSlot(i);
            if (!inputStack.isEmpty()) {
                inputStack.shrink(1);
                blockEntity.getItemHandler().setStackInSlot(i, inputStack);
            }
        }*//*


        // If you want to handle remainder items like the vanilla `ResultSlot` (e.g. buckets returned from crafting),
        // you can add your logic here to handle them properly.

        super.onTake(player, stack);
    }
}
*/

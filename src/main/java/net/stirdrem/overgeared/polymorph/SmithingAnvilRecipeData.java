/*
package net.stirdrem.overgeared.polymorph;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;

public class SmithingAnvilRecipeData extends AbstractBlockEntityRecipeData<SmithingAnvilBlockEntity> {

    public SmithingAnvilRecipeData(SmithingAnvilBlockEntity owner) {
        super(owner);
    }

    @Override
    protected NonNullList<ItemStack> getInput() {
        SimpleContainer craftingInventory = this.getOwner().getForgingInv();

        if (craftingInventory != null) {
            NonNullList<ItemStack> stacks =
                    NonNullList.withSize(craftingInventory.getContainerSize(), ItemStack.EMPTY);

            for (int i = 0; i < craftingInventory.getContainerSize(); i++) {
                stacks.set(i, craftingInventory.getItem(i));
            }
            return stacks;
        }
        return NonNullList.create();
    }

}
*/

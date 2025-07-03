package net.stirdrem.overgeared.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.stirdrem.overgeared.block.entity.StoneSmithingAnvilBlockEntity;

public class StoneSmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    // Create a constructor that matches the parent class
    public StoneSmithingAnvilMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (StoneSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(11));
    }

    public StoneSmithingAnvilMenu(int containerId, Inventory inv, StoneSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(), containerId, inv, entity, data, false);
    }

    // You can override any methods from the parent class here if you need custom behavior
    // specific to the steel smithing anvil
}
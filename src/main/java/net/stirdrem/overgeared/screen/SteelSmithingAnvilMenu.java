package net.stirdrem.overgeared.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;

public class SteelSmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    // Create a constructor that matches the parent class
    public SteelSmithingAnvilMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (SteelSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(12));
    }

    public SteelSmithingAnvilMenu(int containerId, Inventory inv, SteelSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU.get(), containerId, inv, entity, data, true);
    }

    // You can override any methods from the parent class here if you need custom behavior
    // specific to the steel smithing anvil
}
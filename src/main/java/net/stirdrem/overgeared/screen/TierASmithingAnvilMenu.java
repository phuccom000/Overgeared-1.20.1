package net.stirdrem.overgeared.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;

public class TierASmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    // Create a constructor that matches the parent class
    public TierASmithingAnvilMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (TierASmithingAnvilBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(12));
    }

    public TierASmithingAnvilMenu(int containerId, Inventory inv, TierASmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU.get(), containerId, inv, entity, data, true);
    }

    // You can override any methods from the parent class here if you need custom behavior
    // specific to the steel smithing anvil
}
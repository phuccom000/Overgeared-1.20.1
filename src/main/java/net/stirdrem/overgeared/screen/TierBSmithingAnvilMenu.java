package net.stirdrem.overgeared.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;
import net.stirdrem.overgeared.block.entity.TierBSmithingAnvilBlockEntity;

public class TierBSmithingAnvilMenu extends AbstractSmithingAnvilMenu {
    // Create a constructor that matches the parent class
    public TierBSmithingAnvilMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (TierBSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(12));
    }

    public TierBSmithingAnvilMenu(int containerId, Inventory inv, TierBSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU.get(), containerId, inv, entity, data, true);
    }

    // You can override any methods from the parent class here if you need custom behavior
    // specific to the steel smithing anvil
}
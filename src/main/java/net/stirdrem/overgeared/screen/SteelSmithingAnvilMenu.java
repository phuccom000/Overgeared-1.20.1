package net.stirdrem.overgeared.screen;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.stirdrem.overgeared.block.entity.SteelSmithingAnvilBlockEntity;

public class SteelSmithingAnvilMenu extends AbstractSmithingAnvilMenu {

    public SteelSmithingAnvilMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(containerId, inv, (SteelSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(12));
    }

    public SteelSmithingAnvilMenu(int containerId, Inventory inv, SteelSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU.get(), containerId, inv, entity, data, true);
    }
}

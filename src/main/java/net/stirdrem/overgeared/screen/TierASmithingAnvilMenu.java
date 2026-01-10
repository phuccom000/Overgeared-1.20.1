package net.stirdrem.overgeared.screen;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.stirdrem.overgeared.block.entity.TierASmithingAnvilBlockEntity;

public class TierASmithingAnvilMenu extends AbstractSmithingAnvilMenu {

    public TierASmithingAnvilMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(containerId, inv, (TierASmithingAnvilBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(12));
    }

    public TierASmithingAnvilMenu(int containerId, Inventory inv, TierASmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU.get(), containerId, inv, entity, data, true);
    }
}

package net.stirdrem.overgeared.screen;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.stirdrem.overgeared.block.entity.TierBSmithingAnvilBlockEntity;

public class TierBSmithingAnvilMenu extends AbstractSmithingAnvilMenu {

    public TierBSmithingAnvilMenu(int containerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(containerId, inv, (TierBSmithingAnvilBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(12));
    }

    public TierBSmithingAnvilMenu(int containerId, Inventory inv, TierBSmithingAnvilBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU.get(), containerId, inv, entity, data, true);
    }
}

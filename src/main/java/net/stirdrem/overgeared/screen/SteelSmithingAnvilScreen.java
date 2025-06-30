package net.stirdrem.overgeared.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SteelSmithingAnvilScreen extends AbstractSmithingAnvilScreen<SteelSmithingAnvilMenu> {

    public SteelSmithingAnvilScreen(SteelSmithingAnvilMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }
}

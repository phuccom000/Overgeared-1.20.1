package net.stirdrem.overgeared.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

public class RockKnappingMenuProvider implements MenuProvider {
    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.rock_knapping");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new RockKnappingMenu(id, inv,
                player.level().getRecipeManager(),
                player.getMainHandItem(), player.getOffhandItem()); // Use the passed items
    }
}

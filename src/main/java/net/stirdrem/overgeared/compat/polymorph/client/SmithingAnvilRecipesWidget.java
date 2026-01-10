package net.stirdrem.overgeared.compat.polymorph.client;

import com.illusivesoulworks.polymorph.api.client.base.PersistentRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilMenu;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilScreen;

/**
 * Client-side widget for displaying Polymorph recipe selection button on smithing anvil screens.
 */
public class SmithingAnvilRecipesWidget extends PersistentRecipesWidget {
    
    private final AbstractSmithingAnvilMenu menu;

    public SmithingAnvilRecipesWidget(AbstractContainerScreen<?> containerScreen) {
        super(containerScreen);
        this.menu = (AbstractSmithingAnvilMenu) containerScreen.getMenu();
    }

    @Override
    public Slot getOutputSlot() {
        return this.menu.getResultSlot();
    }
    
    /**
     * Factory method to create a widget for smithing anvil screens.
     */
    public static SmithingAnvilRecipesWidget create(AbstractContainerScreen<?> screen) {
        if (screen instanceof AbstractSmithingAnvilScreen<?>) {
            return new SmithingAnvilRecipesWidget(screen);
        }
        return null;
    }
}

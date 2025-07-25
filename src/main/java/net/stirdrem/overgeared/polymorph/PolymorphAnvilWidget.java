package net.stirdrem.overgeared.polymorph;/*
package net.stirdrem.overgeared.polymorph;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.client.recipe.widget.PersistentRecipesWidget;
import net.stirdrem.overgeared.screen.SmithingAnvilMenu;
import net.stirdrem.overgeared.screen.SmithingAnvilScreen;


public class PolymorphAnvilWidget extends PersistentRecipesWidget {
    protected final SmithingAnvilMenu menu;
    private final Slot outputSlot;


    public PolymorphAnvilWidget(SmithingAnvilScreen containerScreen) {
        super(containerScreen);
        outputSlot = containerScreen.getMenu().getResultSlot();
        menu = containerScreen.getMenu();
    }

    @Override
    public void selectRecipe(ResourceLocation resourceLocation) {
        super.selectRecipe(resourceLocation);
        var mc = Minecraft.getInstance();
        mc.level.getRecipeManager().byKey(resourceLocation).ifPresent(recipe -> {
            mc.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
        });
    }

    @Override
    public Slot getOutputSlot() {
        return outputSlot;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float renderPartialTicks) {
        resetWidgetOffsets();

        super.render(guiGraphics, mouseX, mouseY, renderPartialTicks);
    }

    public static void register() {
        PolymorphApi.client().registerWidget(screen -> {
            if (screen instanceof CraftingTerminalScreen s) {
                return new PolymorphTerminalWidget(s);
            }

            return null;
        });
    }
}*/

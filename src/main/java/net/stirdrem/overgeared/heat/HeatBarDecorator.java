package net.stirdrem.overgeared.heat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

import java.awt.*;

public class HeatBarDecorator implements IItemDecorator {

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!stack.isEmpty()) {
            stack.getCapability(HeatCapabilityProvider.ITEM_HEAT).ifPresent(heat -> {
                if (heat.getHeat() > 0) {
                    int width = Math.round(13 * ((float) heat.getHeat() / heat.getMaxHeat()));
                    float hue = 0.05f * (1 - ((float) heat.getHeat() / heat.getMaxHeat()));
                    int color = Color.HSBtoRGB(hue, 1.0f, 1.0f);

                    RenderSystem.disableDepthTest();
                    RenderSystem.disableBlend();
                    guiGraphics.fill(xOffset + 2, yOffset + 13, xOffset + 15, yOffset + 15, 0xFF000000);

                    if (width > 0) {
                        guiGraphics.fill(xOffset + 2, yOffset + 13, xOffset + 2 + width, yOffset + 15, color | 0xFF000000);
                    }

                    RenderSystem.enableBlend();
                    RenderSystem.enableDepthTest();
                }
            });
        }
        return false;
    }
}

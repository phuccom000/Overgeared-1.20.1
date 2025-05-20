package net.stirdrem.overgearedmod.util;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.GuiGraphics;

public class TooltipButton extends Button {
    private final Component tooltip;

    public TooltipButton(int x, int y, int width, int height, Component tooltip, OnPress onPress) {
        super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);
        this.tooltip = tooltip;
    }

    public Component getTooltipComponent() {
        return tooltip;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        // Do not render any background or text to make the button invisible
        // Optionally, you can add hover effects here
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Provide narration for accessibility if needed
    }

}

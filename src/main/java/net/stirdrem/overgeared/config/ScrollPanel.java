package net.stirdrem.overgeared.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.util.ArrayList;
import java.util.List;

public class ScrollPanel implements Renderable, GuiEventListener, NarratableEntry {

    private final Minecraft client;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private int scrollY;
    private boolean scrolling;
    private final int scrollbarWidth = 6;

    private final List<AbstractWidget> children = new ArrayList<>();
    private final int contentHeight;

    private boolean focused = false;

    public ScrollPanel(Minecraft client, int x, int y, int width, int height, int contentHeight) {
        this.client = client;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.contentHeight = contentHeight;
    }

    public void addWidget(AbstractWidget widget) {
        children.add(widget);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {

        // --- ENABLE SCISSOR (Correct for 1.20.1: Y is from bottom) ---
        int screenHeight = client.getWindow().getGuiScaledHeight();

        gui.enableScissor(
                x,
                screenHeight - (y + height),
                x + width,
                screenHeight - y
        );

        gui.pose().pushPose();
        gui.pose().translate(0, -scrollY, 0);

        for (AbstractWidget child : children) {
            child.render(gui, mouseX, mouseY - scrollY, delta);
        }

        gui.pose().popPose();
        gui.disableScissor();

        // Draw scrollbar
        if (contentHeight > height) {
            int scrollbarHeight = (int) ((float) height / contentHeight * height);
            int scrollbarY = y + (int) ((float) scrollY / (contentHeight - height) * (height - scrollbarHeight));

            gui.fill(
                    x + width - scrollbarWidth,
                    scrollbarY,
                    x + width,
                    scrollbarY + scrollbarHeight,
                    0xFFCCCCCC
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean inBounds = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        if (!inBounds) return false;

        // Scrollbar
        if (mouseX >= x + width - scrollbarWidth) {
            scrolling = true;
            setFocused(true);
            return true;
        }

        double adjustedY = mouseY - scrollY;

        for (AbstractWidget child : children) {
            if (child.mouseClicked(mouseX, adjustedY, button)) {
                setFocused(true);
                return true;
            }
        }

        setFocused(true);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrolling) {
            double rel = mouseY - y;
            double pct = rel / height;

            scrollY = (int) (pct * (contentHeight - height));
            scrollY = Math.max(0, Math.min(scrollY, contentHeight - height));
            return true;
        }

        double adjustedY = mouseY - scrollY;
        for (AbstractWidget child : children) {
            if (child.mouseDragged(mouseX, adjustedY, button, dragX, dragY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scrollY -= amount * 20;  // natural scroll direction
        scrollY = Math.max(0, Math.min(scrollY, contentHeight - height));
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrolling = false;

        double adjustedY = mouseY - scrollY;
        for (AbstractWidget child : children) {
            if (child.mouseReleased(mouseX, adjustedY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setFocused(boolean isFocused) {
        this.focused = isFocused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }

}

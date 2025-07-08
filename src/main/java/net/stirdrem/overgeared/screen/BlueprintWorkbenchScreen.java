package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.SelectToolTypeC2SPacket;
import net.stirdrem.overgeared.screen.BlueprintWorkbenchMenu;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.ArrayList;
import java.util.List;


public class BlueprintWorkbenchScreen extends AbstractContainerScreen<BlueprintWorkbenchMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/blueprint_workbench.png");

    private List<ToolType> toolTypes;
    private int selectedIndex = 0;
    private Button prevButton;
    private Button nextButton;
    private Button selectButton;
    private Component currentToolName;

    public BlueprintWorkbenchScreen(BlueprintWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.toolTypes = ToolTypeRegistry.getRegisteredTypes();
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // Initialize buttons first
        createButtons(x, y);
        selectButton.active = false;

        // Check if there are any tool types available
        if (toolTypes.isEmpty()) {
            handleNoToolsAvailable(x, y);
        } else {
            updateToolDisplay();
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean hasItem = !menu.getSlot(0).getItem().isEmpty();
        selectButton.active = hasItem && !toolTypes.isEmpty();
    }

    private void createButtons(int x, int y) {
        int centerX = x + imageWidth / 2;
        int buttonRowY = y + 15;
        int buttonWidth = 10;
        int buttonPosFromCenter = 40;

        // Previous tool button (left arrow)
        prevButton = Button.builder(Component.literal("<"), btn -> {
                    selectedIndex = (selectedIndex - 1 + toolTypes.size()) % toolTypes.size();
                    updateToolDisplay();
                })
                .pos(centerX - buttonWidth / 2 - buttonPosFromCenter, buttonRowY)
                .size(buttonWidth, 12)
                .tooltip(Tooltip.create(Component.translatable("tooltip.overgeared.previous_tool")))
                .build();

        // Next tool button (right arrow)
        nextButton = Button.builder(Component.literal(">"), btn -> {
                    selectedIndex = (selectedIndex + 1) % toolTypes.size();
                    updateToolDisplay();
                })
                .pos(centerX - buttonWidth / 2 + buttonPosFromCenter, buttonRowY)
                .size(buttonWidth, 12)
                .tooltip(Tooltip.create(Component.translatable("tooltip.overgeared.next_tool")))
                .build();

        int selectButtonWidth = 60;
        int selectButtonY = y + 58;
        // Select tool button
        selectButton = Button.builder(Component.translatable("button.overgeared.select"), btn -> {
                    if (!toolTypes.isEmpty()) {
                        ModMessages.sendToServer(new SelectToolTypeC2SPacket(toolTypes.get(selectedIndex).getId()));
                    }
                })
                .pos(x + imageWidth / 2 - selectButtonWidth / 2, selectButtonY)
                .size(selectButtonWidth, 14)
                .build();

        this.addRenderableWidget(prevButton);
        this.addRenderableWidget(nextButton);
        this.addRenderableWidget(selectButton);
    }

    private void handleNoToolsAvailable(int x, int y) {
        // Disable all buttons
        prevButton.active = false;
        nextButton.active = false;
        //selectButton.active = false;

        // Set appropriate tooltips
        prevButton.setTooltip(Tooltip.create(Component.translatable("tooltip.overgeared.no_tools_available")));
        nextButton.setTooltip(Tooltip.create(Component.translatable("tooltip.overgeared.no_tools_available")));
        selectButton.setTooltip(Tooltip.create(Component.translatable("tooltip.overgeared.no_tools_available")));

        // Set the no tools message
        currentToolName = Component.literal("Null");
    }

    private void updateToolDisplay() {
        if (!toolTypes.isEmpty()) {
            ToolType currentTool = toolTypes.get(selectedIndex);
            currentToolName = currentTool.getDisplayName();
            selectButton.setTooltip(Tooltip.create(Component.translatable("tooltip.overgeared.select_tool", currentToolName)));

            // Enable all buttons
            prevButton.active = true;
            nextButton.active = true;
            //selectButton.active = true;
        } else {
            handleNoToolsAvailable((this.width - this.imageWidth) / 2, (this.height - this.imageHeight) / 2);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Render current tool name or error message
        if (currentToolName != null) {
            int textWidth = this.font.width(currentToolName);
            int textColor = toolTypes.isEmpty() ? 0xFF0000 : 0x404040; // Red for error, dark gray for normal
            guiGraphics.drawString(this.font, currentToolName,
                    x + imageWidth / 2 - textWidth / 2, y + 18, textColor, false);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
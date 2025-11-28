package net.stirdrem.overgeared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class BlacklistEditorScreen extends Screen {
    private final String blacklistName;
    private final List<String> originalItems;
    private final Consumer<List<? extends String>> setter;
    private final double savedScrollPosition;
    private final Screen parentScreen;

    private BlacklistEditorList itemList;
    private EditBox addItemField;
    private Button addButton;
    private Button doneButton;
    private List<String> items;

    public BlacklistEditorScreen(String blacklistName, List<? extends String> currentItems,
                                 Consumer<List<? extends String>> setter,
                                 double savedScrollPosition, Screen parentScreen) {
        super(Component.literal("Edit " + blacklistName));
        this.blacklistName = blacklistName;
        this.originalItems = new ArrayList<>();
        for (String item : currentItems) {
            this.originalItems.add(item);
        }
        this.setter = setter;
        this.savedScrollPosition = savedScrollPosition;
        this.parentScreen = parentScreen;
        this.items = new ArrayList<>(originalItems);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int listWidth = this.width; // Reasonable max width
        int listHeight = this.height - 100;

        // Create the item list
        this.itemList = new BlacklistEditorList(
                Minecraft.getInstance(),
                listWidth, listHeight,
                40, 40 + listHeight,
                20
        );
        this.itemList.setLeftPos((this.width - listWidth) / 2);
        this.addWidget(itemList);

        // Populate the list with current items
        refreshItemList();

        // Add item input field
        this.addItemField = new EditBox(font, centerX - 120, this.height - 50, 200, 20,
                Component.literal("Enter item ID or tag..."));
        this.addItemField.setMaxLength(256);
        this.addRenderableWidget(addItemField);

        // Add button
        this.addButton = Button.builder(Component.literal("Add"), btn -> {
            String newItem = addItemField.getValue().trim();
            if (!newItem.isEmpty() && !items.contains(newItem)) {
                items.add(newItem);
                addItemField.setValue("");
                refreshItemList();
            }
        }).bounds(centerX + 90, this.height - 50, 50, 20).build();
        this.addRenderableWidget(addButton);

        // Done button
        this.doneButton = Button.builder(Component.literal("Done"), btn -> {
            // Save the changes back to the config
            setter.accept(items);
            if (parentScreen instanceof OvergearedConfigScreen) {
                OvergearedConfigScreen configScreen = (OvergearedConfigScreen) parentScreen;
                configScreen.setScrollPosition(savedScrollPosition);
                configScreen.restoreScrollPosition(); // Add this line
            }
            this.minecraft.setScreen(parentScreen);
        }).bounds(centerX - 105, this.height - 25, 100, 20).build();
        this.addRenderableWidget(doneButton);

        // Cancel button
        Button cancelButton = Button.builder(Component.literal("Cancel"), btn -> {
            if (parentScreen instanceof OvergearedConfigScreen) {
                OvergearedConfigScreen configScreen = (OvergearedConfigScreen) parentScreen;
                configScreen.setScrollPosition(savedScrollPosition);
                configScreen.restoreScrollPosition(); // Add this line
            }
            this.minecraft.setScreen(parentScreen);
        }).bounds(centerX + 5, this.height - 25, 100, 20).build();
        this.addRenderableWidget(cancelButton);
    }

    private void refreshItemList() {
        itemList.children().clear();
        for (String item : items) {
            itemList.addNewEntry(new BlacklistItemEntry(item));
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui);
        this.itemList.render(gui, mouseX, mouseY, partialTicks);
        super.render(gui, mouseX, mouseY, partialTicks);

        int centerX = this.width / 2;

        // Draw title
        gui.drawCenteredString(font, "Edit " + blacklistName, centerX, 15, 0xFFFFFF);
        gui.drawCenteredString(font, "Add item IDs or tags (prefix with # for tags)", centerX, 25, 0xAAAAAA);
        gui.drawCenteredString(font, "Current items: " + items.size(), centerX, this.height - 70, 0xAAAAAA);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            if (parentScreen instanceof OvergearedConfigScreen configScreen) {
                configScreen.setScrollPosition(savedScrollPosition);
                configScreen.restoreScrollPosition(); // Add this line
            }
            this.minecraft.setScreen(parentScreen);
            return true;
        } else if (keyCode == 257 && addItemField.isFocused()) { // Enter key in add field
            String newItem = addItemField.getValue().trim();
            if (!newItem.isEmpty() && !items.contains(newItem)) {
                items.add(newItem);
                addItemField.setValue("");
                refreshItemList();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // Inner class for individual blacklist item entries
    private class BlacklistItemEntry extends ObjectSelectionList.Entry<BlacklistItemEntry> {
        private final String item;
        private final Button removeButton;

        public BlacklistItemEntry(String item) {
            this.item = item;
            this.removeButton = Button.builder(Component.literal("Remove"), btn -> {
                items.remove(item);
                refreshItemList();
            }).bounds(0, 0, 60, 16).build();
        }

        @Override
        public void render(GuiGraphics gui, int idx, int y, int x, int listWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float pt) {
            int color = item.startsWith("#") ? 0x55FF55 : 0xFFFFFF; // Green for tags, white for items
            gui.drawString(font, item, x + 5, y + 2, color);

            removeButton.setX(x + listWidth - 90);
            removeButton.setY(y);
            removeButton.render(gui, mouseX, mouseY, pt);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (removeButton.isMouseOver(mouseX, mouseY)) {
                return removeButton.mouseClicked(mouseX, mouseY, button);
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return removeButton.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public Component getNarration() {
            return Component.literal(item);
        }
    }

    // Custom list for blacklist items
    private class BlacklistEditorList extends ObjectSelectionList<BlacklistItemEntry> {
        private final int entryPadding = 30; // Padding on left and right of each entry

        public BlacklistEditorList(Minecraft mc, int width, int height, int top, int bottom, int itemHeight) {
            super(mc, width, height, top, bottom, itemHeight);
        }

        @Override
        public int getRowWidth() {
            return this.width - (entryPadding * 2); // Reduce available width by padding
        }

        @Override
        public int getRowLeft() {
            return this.x0 + entryPadding; // Start drawing content after padding
        }

        @Override
        public int getScrollbarPosition() {
            return this.x1 - 25; // Ensure scrollbar doesn't overlap buttons
        }

        public void addNewEntry(BlacklistItemEntry entry) {
            super.addEntry(entry);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            this.setSelected(null);
            return false;
        }
    }
}
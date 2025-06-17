package net.stirdrem.overgeared.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.KnappingChipC2SPacket;

import java.util.HashSet;
import java.util.Set;

public class RockKnappingScreen extends AbstractContainerScreen<RockKnappingMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/rock_knapping_gui.png");
    private static final ResourceLocation CHIPPED_TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/blank.png");
    private static final ResourceLocation UNCHIPPED_TEXTURE =
            ResourceLocation.tryParse("textures/block/stone.png");

    private static final int GRID_ORIGIN_X = 32;
    private static final int GRID_ORIGIN_Y = 19;
    private static final int SLOT_SIZE = 16;

    private final Set<Integer> chippedSpots = new HashSet<>();

    public RockKnappingScreen(RockKnappingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        // Clear any existing chipped spots when initializing
        chippedSpots.clear();

        addKnappingButtons(); // Build initial button states

    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!menu.isKnappingFinished()) {
            // Recipe just finished - update buttons
            addKnappingButtons();
        } else this.clearWidgets(); // Clears old buttons

    }

    private void addKnappingButtons() {
        this.clearWidgets(); // Clears old buttons

        if (menu.isKnappingFinished()) return;

        boolean hasResult = !menu.getSlot(9).getItem().isEmpty();
        boolean resultCollected = menu.isResultCollected(); // Need to track this in menu

        // Knapping is only finished when result is collected
        // Allow continuing knapping if there's a result but it hasn't been collected
        boolean canContinueKnapping = hasResult && !resultCollected;

        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = this.leftPos + GRID_ORIGIN_X + col * SLOT_SIZE;
            int y = this.topPos + GRID_ORIGIN_Y + row * SLOT_SIZE;

            final int index = i;
            ResourceLocation texture = menu.isChipped(i) || resultCollected ? CHIPPED_TEXTURE : UNCHIPPED_TEXTURE;
            boolean isChipped = menu.isChipped(i);

            ImageButton button = new ImageButton(
                    x, y,
                    SLOT_SIZE, SLOT_SIZE,
                    0, 0, 0,
                    texture,
                    SLOT_SIZE, SLOT_SIZE,
                    btn -> {
                        if ((!hasResult || canContinueKnapping) && !isChipped) {
                            menu.setChip(index);
                            chippedSpots.add(index);
                            if (!resultCollected) {
                                ModMessages.sendToServer(new KnappingChipC2SPacket(index));
                                minecraft.player.playSound(net.minecraft.sounds.SoundEvents.STONE_BREAK, 1.0F, 1.0F);
                            }
                            addKnappingButtons(); // Refresh visuals
                        }
                    }
            ) {
                @Override
                public boolean mouseClicked(double mouseX, double mouseY, int button) {
                    if (!menu.isKnappingFinished()) {
                        return super.mouseClicked(mouseX, mouseY, button);
                    }
                    return false;
                }

                @Override
                public void playDownSound(SoundManager pHandler) {
                }

            };

            // ❗ Disable the button if knapping is done or spot is already chipped
            button.active = !menu.isKnappingFinished();

            this.addRenderableWidget(button);
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = this.leftPos;
        int y = this.topPos;

        // Draw main background
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        /*// Draw input rock icon if needed
        if (!menu.getInputRock().isEmpty()) {
            graphics.renderItem(menu.getInputRock(), x + 8, y + 35);
        }*/
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);
    }

    private void handleKnappingDrag(double mouseX, double mouseY) {
        for (int i = 0; i < 9; i++) {
            int col = i % 3;
            int row = i / 3;
            int x = this.leftPos + GRID_ORIGIN_X + col * SLOT_SIZE;
            int y = this.topPos + GRID_ORIGIN_Y + row * SLOT_SIZE;

            if (mouseX >= x && mouseX < x + SLOT_SIZE &&
                    mouseY >= y && mouseY < y + SLOT_SIZE &&
                    !menu.isKnappingFinished() &&
                    !menu.isChipped(i)) {

                menu.setChip(i);
                chippedSpots.add(i);
                if (!menu.isResultCollected()) {
                    ModMessages.sendToServer(new KnappingChipC2SPacket(i));
                    minecraft.player.playSound(net.minecraft.sounds.SoundEvents.STONE_BREAK, 1.0F, 1.0F);
                }

                addKnappingButtons(); // Refresh visuals
                break; // Avoid double-processing
            }
        }
    }

}
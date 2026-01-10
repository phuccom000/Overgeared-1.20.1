package net.stirdrem.overgeared.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.screen.CastingSmelterMenu;

public class CastingSmelterScreen extends AbstractContainerScreen<CastingSmelterMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/cast_furnace.png");

    public CastingSmelterScreen(CastingSmelterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        gfx.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Flame
        if (menu.isBurning()) {
            int flame = menu.getBurnProgress();
            gfx.blit(TEXTURE, x + 8, y + 36 + 12 - flame,
                    176, 12 - flame, 14, flame + 1);
        }

        // Arrow
        int progress = menu.getCookProgress();
        gfx.blit(TEXTURE, x + 79, y + 34,
                176, 14, progress + 1, 16);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);
        renderTooltip(gfx, mouseX, mouseY);
    }
}

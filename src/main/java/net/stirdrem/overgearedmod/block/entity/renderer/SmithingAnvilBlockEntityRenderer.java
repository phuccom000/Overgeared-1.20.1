package net.stirdrem.overgearedmod.block.entity.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.stirdrem.overgearedmod.block.entity.SmithingAnvilBlockEntity;

public class SmithingAnvilBlockEntityRenderer implements BlockEntityRenderer<SmithingAnvilBlockEntity> {
    public SmithingAnvilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SmithingAnvilBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack itemStack1 = ItemStack.EMPTY, itemStack2 = ItemStack.EMPTY, itemStack3 = ItemStack.EMPTY;
        int num1 = -1, num2 = -1, num3 = -1;
        boolean hasOutput = !pBlockEntity.getRenderStack(9).isEmpty();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = pBlockEntity.getRenderStack(i);
            if (!stack.isEmpty()) {
                if (num1 == -1) {
                    itemStack1 = stack;
                    num1 = i;
                } else if (num2 == -1) {
                    itemStack2 = stack;
                    num2 = i;
                } else if (num3 == -1) {
                    itemStack3 = stack;
                    num3 = i;
                    break;
                }
            }
        }

        renderStack(pPoseStack, pBuffer, itemRenderer, itemStack1, pBlockEntity, 1.025f, 96f);
        if (!hasOutput) {// flat
            renderStack(pPoseStack, pBuffer, itemRenderer, itemStack2, pBlockEntity, 1.05f, 110f);  // rotated 45°
            renderStack(pPoseStack, pBuffer, itemRenderer, itemStack3, pBlockEntity, 1.075f, 125f);
        }// rotated 90°
    }

    private void renderStack(PoseStack poseStack, MultiBufferSource buffer, ItemRenderer itemRenderer,
                             ItemStack itemStack, SmithingAnvilBlockEntity blockEntity,
                             float yOffset, float rotationDegrees) {
        if (itemStack == null || itemStack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5f, yOffset, 0.5f);

        // Check if the item is a block
        boolean isBlock = itemStack.getItem() instanceof BlockItem;

        // Rotate and scale differently based on item type
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));
        poseStack.mulPose(Axis.XP.rotationDegrees(isBlock ? 180 : 90)); // flip block upright
        poseStack.scale(
                isBlock ? 0.35f : 0.5f, // blocks might be taller
                isBlock ? 0.35f : 0.5f,
                isBlock ? 0.35f : 0.5f
        );

        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED,
                getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, poseStack, buffer, blockEntity.getLevel(), 1);

        poseStack.popPose();
    }


    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}

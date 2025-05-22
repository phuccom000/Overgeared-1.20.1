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
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.stirdrem.overgearedmod.block.entity.SmithingAnvilBlockEntity;

public class SmithingAnvilBlockEntityRenderer implements BlockEntityRenderer<SmithingAnvilBlockEntity> {
    public SmithingAnvilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SmithingAnvilBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        // Render the output item from slot 10
        ItemStack output = pBlockEntity.getRenderStack(10);
        if (!output.isEmpty()) {
            float yOffset = isBlockItem(output) ? 1.05f : 1.025f;
            renderStack(pPoseStack, pBuffer, itemRenderer, output, pBlockEntity, 0.0f, yOffset, 0f, 120f, 0.5f);
        }

        // Render up to three input items from slots 0 to 8
        int rendered = 0;
        for (int i = 0; i < 9 && rendered < 3; i++) {
            ItemStack stack = pBlockEntity.getRenderStack(i);
            if (!stack.isEmpty()) {
                float baseYOffset = 1.025f + (rendered * 0.025f);
                float yOffset = isBlockItem(stack) ? baseYOffset + 0.02f : baseYOffset;
                float rotation = 96f - (rendered * 14f);
                renderStack(pPoseStack, pBuffer, itemRenderer, stack, pBlockEntity, 0.0f, yOffset, 0.43f, rotation, 0.35f);
                rendered++;
            }
        }

        // Render the hammer from slot 9
        ItemStack hammer = pBlockEntity.getRenderStack(9);
        renderStack(pPoseStack, pBuffer, itemRenderer, hammer, pBlockEntity, 0f, 1.025f, -0.43f, 135f, 0.5f);
    }

    // Helper method to determine if an ItemStack is a block item
    private boolean isBlockItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        Block block = Block.byItem(item);
        return block != Blocks.AIR;
    }


    private void renderStack(PoseStack poseStack, MultiBufferSource buffer, ItemRenderer itemRenderer,
                             ItemStack itemStack, SmithingAnvilBlockEntity blockEntity,
                             float xOffset, float yOffset, float zOffset, float rotationDegrees, float scale) {

        if (itemStack == null || itemStack.isEmpty()) return;

        poseStack.pushPose();

        // Get block facing direction
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH; // default fallback

        // Determine rotation based on facing
        float facingRotationDegrees = switch (facing) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST -> 270f;
            case EAST -> 90f;
            default -> 0f;
        };

        // Calculate rotation matrix components
        double radians = Math.toRadians(facingRotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        // Apply rotation to xOffset and zOffset
        float rotatedX = (float) (xOffset * cos - zOffset * sin);
        float rotatedZ = (float) (xOffset * sin + zOffset * cos);

        // Translate to center plus rotated offset, keep yOffset as is
        poseStack.translate(0.5f - rotatedX, yOffset, 0.5f + rotatedZ);

        // Apply facing rotation
        poseStack.mulPose(Axis.YP.rotationDegrees(facingRotationDegrees));

        // Check if item is a block
        boolean isBlock = itemStack.getItem() instanceof BlockItem;

        // Additional rotation (e.g., 45°) if needed
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));

        // Flip non-block items upright
        poseStack.mulPose(Axis.XP.rotationDegrees(isBlock ? 0 : 90));

        // Scale depending on type
        poseStack.scale(scale, scale, scale);

        // Render the item
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

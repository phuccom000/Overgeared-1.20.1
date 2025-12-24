package net.stirdrem.overgeared.block.entity.renderer;

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
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;

import java.util.HashSet;
import java.util.Set;

public class SmithingAnvilBlockEntityRenderer implements BlockEntityRenderer<AbstractSmithingAnvilBlockEntity> {
    public SmithingAnvilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static final float BASE_Y = 1.01f;
    private static final float ITEM_HEIGHT = 0.02f;
    private static final float BLOCK_HEIGHT = 0.2f;
    private static final float BLOCK_BASE_Y_OFFSET = 0.09f;

    @Override
    public void render(AbstractSmithingAnvilBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        // Render the output item from slot 10
        ItemStack output = pBlockEntity.getRenderStack(10);
        boolean inputsEmpty = areInputSlotsEmpty(pBlockEntity);
        float zOffset = inputsEmpty ? 0f : -0.43f;

        float heightScale;
        int progress = pBlockEntity.getContainerData().get(0);
        int max = pBlockEntity.getContainerData().get(1);

        if (max <= 0) {
            heightScale = 1.0f; // default when no recipe / not started
        } else {
            heightScale = 1.0f - ((float) progress / max);
        }
        if (!output.isEmpty()) {
            float yOffset = isBlockItem(output) ? 1.05f : 1.02f;
            renderStack(pPoseStack, pBuffer, itemRenderer, output, pBlockEntity, 0.0f, yOffset, zOffset, 110f, 0.4f, 1.0f);
        }

        // 1️⃣ First pass: render up to three unique input items
        Set<Item> renderedItems = new HashSet<>();
        Set<Integer> renderedSlots = new HashSet<>(); // Track which slots we've rendered from
        int rendered = 0;

        // First pass: render unique items
        rendered = renderPass(pPoseStack, pBuffer, itemRenderer, pBlockEntity,
                renderedItems, renderedSlots, 0f, rendered, true, heightScale);

        // Second pass: fill remaining slots with any items
        if (rendered < 3) {
            renderPass(pPoseStack, pBuffer, itemRenderer, pBlockEntity,
                    renderedItems, renderedSlots, 0f, rendered, false, heightScale);
        }

        // Render the hammer from slot 9
        ItemStack hammer = pBlockEntity.getRenderStack(9);
        renderStack(pPoseStack, pBuffer, itemRenderer, hammer, pBlockEntity, 0f, 1.025f, 0.43f, 135f, 0.5f, 1.0f);
    }

    private boolean areInputSlotsEmpty(AbstractSmithingAnvilBlockEntity be) {
        for (int i = 0; i < 9; i++) {  // assuming slots 0–8 are inputs
            if (!be.getRenderStack(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private int renderPass(PoseStack pPoseStack, MultiBufferSource pBuffer,
                           ItemRenderer itemRenderer, AbstractSmithingAnvilBlockEntity pBlockEntity,
                           Set<Item> renderedItems, Set<Integer> renderedSlots,
                           float zOffset, int renderedCount, boolean checkUniqueness, float heightScale) {

        // Use an accumulated Y offset instead of rendered index
        float currentHeight = BASE_Y;

        // Rebuild currentHeight based on previously rendered slots
        for (int i : renderedSlots) {
            ItemStack prev = pBlockEntity.getRenderStack(i);
            currentHeight += isBlockItem(prev) ? BLOCK_HEIGHT : ITEM_HEIGHT;
        }

        int rendered = renderedCount;

        for (int i = 0; i < 9 && rendered < 3; i++) {

            if (renderedSlots.contains(i)) {
                continue;
            }

            ItemStack stack = pBlockEntity.getRenderStack(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();

            if (checkUniqueness && renderedItems.contains(item)) {
                continue;
            }

            float scale = isBlockItem(stack) ? 0.4f : 0.35f;
            float rotation = 96f + (rendered * 14f);

            // Use accumulated height
            float yOffset = currentHeight;

            // If this is the first rendered item AND it's a block → raise it
            if (isBlockItem(stack)) {
                yOffset += BLOCK_BASE_Y_OFFSET;
            }
            renderStack(
                    pPoseStack, pBuffer, itemRenderer, stack, pBlockEntity,
                    0.0f, yOffset, zOffset, rotation, scale, heightScale
            );

            // Add height for next item
            currentHeight += isBlockItem(stack) ? BLOCK_HEIGHT : ITEM_HEIGHT;

            renderedItems.add(item);
            renderedSlots.add(i);
            rendered++;
        }

        return rendered;
    }


    // Helper method to determine if an ItemStack is a block item
    private boolean isBlockItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        Block block = Block.byItem(item);
        return block != Blocks.AIR;
    }

    private void renderStack(PoseStack poseStack, MultiBufferSource buffer, ItemRenderer itemRenderer,
                             ItemStack itemStack, AbstractSmithingAnvilBlockEntity blockEntity,
                             float xOffset, float yOffset, float zOffset,
                             float rotationDegrees, float scale, float heightScale) {

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

        // Translate to center plus rotated offset, apply height scale to yOffset
        poseStack.translate(0.5f - rotatedX, yOffset - (0.01 * (1 - heightScale)), 0.5f + rotatedZ);

        // Apply facing rotation
        poseStack.mulPose(Axis.YP.rotationDegrees(facingRotationDegrees));

        // Check if item is a block
        boolean isBlock = itemStack.getItem() instanceof BlockItem;

        // Additional rotation (e.g., 45°) if needed
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));

        // Flip non-block items upright
        poseStack.mulPose(Axis.XP.rotationDegrees(isBlock ? 0 : 90));

        poseStack.scale(scale, scale, scale * heightScale);

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

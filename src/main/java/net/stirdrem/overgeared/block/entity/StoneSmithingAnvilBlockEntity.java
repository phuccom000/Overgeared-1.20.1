package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.custom.StoneSmithingAnvil;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.screen.StoneSmithingAnvilMenu;
import org.jetbrains.annotations.Nullable;

public class StoneSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {
    private int craftCount = 0;

    public StoneSmithingAnvilBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super((StoneSmithingAnvil) pBlockState.getBlock(), AnvilTier.STONE, ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(), pPos, pBlockState);
    }

  @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        if (!pPlayer.isCrouching()) {
            return new StoneSmithingAnvilMenu(pContainerId, pPlayerInventory, this, this.data);
        } else return null;

    }


    @Override
    protected String determineForgingQuality() {
        // Get quality from anvil or use default if null
        String quality = anvilBlock.getQuality();
        if (quality == null) {
            return "poor"; // Default quality
        }

        // Use switch expression for better null safety
        return switch (quality.toLowerCase()) {
            case "poor" -> ForgingQuality.POOR.getDisplayName();
            default -> "well";// Fallback
        };
    }

    @Override
    public String blueprintQuality() {
        return "well";
    }

    @Override
    protected void craftItem() {
        super.craftItem(); // Perform regular crafting logic
        if (ServerConfig.STONE_ANVIL_MAX_USES.get() == 0) return;
        craftCount++;

        if (craftCount >= ServerConfig.STONE_ANVIL_MAX_USES.get()) {
            breakAnvil();
        }
    }

    private void breakAnvil() {
        if (level != null && !level.isClientSide) {
            level.destroyBlock(worldPosition, true);
        }
    }
}

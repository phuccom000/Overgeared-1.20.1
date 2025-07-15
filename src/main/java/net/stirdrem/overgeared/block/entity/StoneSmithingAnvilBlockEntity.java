package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.custom.StoneSmithingAnvil;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.StoneSmithingAnvilMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class StoneSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {
    private int craftCount = 0;

    public StoneSmithingAnvilBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(AnvilTier.STONE, ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(), pPos, pBlockState);
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.smithing_anvil");
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
        String quality = StoneSmithingAnvil.getQuality();
        if (quality == null) {
            return "no_quality"; // Default quality
        }

        // Use switch expression for better null safety
        return switch (quality.toLowerCase()) {
            case "poor" -> ForgingQuality.POOR.getDisplayName();
            default -> "well";// Fallback
        };
    }

    /*@Override
    public boolean hasRecipe() {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (recipe.isPresent() && AnvilTier.STONE.getDisplayName().equals(recipe.get().getAnvilTier())) {
            ItemStack resultStack = recipe.get().getResultItem(level.registryAccess());
            return canInsertItemIntoOutputSlot(resultStack)
                    && canInsertAmountIntoOutputSlot(resultStack.getCount());
        }
        return false;
    }*/

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

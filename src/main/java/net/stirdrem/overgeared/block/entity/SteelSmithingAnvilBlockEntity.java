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
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;
import net.stirdrem.overgeared.block.custom.SteelSmithingAnvil;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.SteelSmithingAnvilMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SteelSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {

    public SteelSmithingAnvilBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get(), pPos, pBlockState);
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        if (!pPlayer.isCrouching()) {
            return new SteelSmithingAnvilMenu(pContainerId, pPlayerInventory, this, this.data);
        } else return null;

    }


    @Override
    protected String determineForgingQuality() {
        // Get quality from anvil or use default if null
        String quality = SteelSmithingAnvil.getQuality();
        if (quality == null) {
            return "no_quality"; // Default quality
        }

        // Use switch expression for better null safety
        return switch (quality.toLowerCase()) {
            case "poor" -> ForgingQuality.POOR.getDisplayName();
            case "expert" -> ForgingQuality.EXPERT.getDisplayName();
            case "perfect" -> ForgingQuality.PERFECT.getDisplayName();
            case "well" -> ForgingQuality.WELL.getDisplayName();
            default -> "no_quality";// Fallback
        };
    }

    @Override
    public boolean hasRecipe() {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (recipe.isPresent()
            //&& AnvilTier.STEEL.getDisplayName().equals(recipe.get().getAnvilTier())
        ) {
            ItemStack resultStack = recipe.get().getResultItem(level.registryAccess());
            return canInsertItemIntoOutputSlot(resultStack)
                    && canInsertAmountIntoOutputSlot(resultStack.getCount());
        }
        return false;
    }

}

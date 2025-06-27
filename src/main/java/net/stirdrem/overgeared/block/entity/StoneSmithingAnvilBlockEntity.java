package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.block.AnvilTier;
import net.stirdrem.overgeared.block.custom.StoneSmithingAnvil;
import net.stirdrem.overgeared.screen.StoneSmithingAnvilMenu;
import org.jetbrains.annotations.Nullable;


public class StoneSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {

    public StoneSmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(), pos, state, 11, AnvilTier.STONE);
    }

    @Override
    public String getQuality() {
        return StoneSmithingAnvil.getQuality();
    }

    @Override
    public int getSlotCount() {
        return 11;
    }

    @Override
    protected void applySpecialForgingEffects(ItemStack result) {
        /*CompoundTag tag = result.getOrCreateTag();
        tag.putBoolean("SteelForged", true);
        tag.putFloat("DurabilityBonus", 1.15f);*/
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        if (!pPlayer.isCrouching()) {
            return new StoneSmithingAnvilMenu(pContainerId, pPlayerInventory, this, this.data);
        } else return null;
    }
    /*@Override
    protected String determineForgingQuality() {
        String baseQuality = super.determineForgingQuality();
        if (!baseQuality.equals("no_quality")) {
            return "steel_" + baseQuality;
        }
        return baseQuality;
    }*/


}
package net.stirdrem.overgeared.item.custom;

import net.minecraft.core.BlockPos;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockState;

import net.stirdrem.overgeared.util.ModTags;

public class SmithingHammer extends DiggerItem {

    public SmithingHammer(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(attackDamage, attackSpeed, tier, ModTags.Blocks.SMITHING, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, entity, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

}
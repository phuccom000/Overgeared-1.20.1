package net.stirdrem.overgeared.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.util.ModTags;

public class SmithingHammer extends DiggerItem {

    public SmithingHammer(Tier tier, int attackDamage, float attackSpeed, Item.Properties properties) {
        // Attach tool attributes manually using DataComponents
        super(tier, ModTags.Blocks.SMITHING,
                properties.component(
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        DiggerItem.createAttributes(tier, attackDamage, attackSpeed)
                )
        );
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

}

/*
package net.stirdrem.overgeared.block;

//import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;
import net.stirdrem.overgeared.item.custom.LingeringArrowItem;
import net.stirdrem.overgeared.item.custom.UpgradeArrowItem;

public class UpgradeArrowDispenseBehavior extends DefaultDispenseItemBehavior {
    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        Level level = source.level();
        Position position = DispenserBlock.getDispensePosition(source);
        Direction direction = source.blockEntity().getBlockState().getValue(DispenserBlock.FACING);

        if (stack.getItem() instanceof UpgradeArrowItem arrowItem) {
            createAndShootArrow(arrowItem.getTier(), level, position, direction, stack);
        } else if (stack.getItem() instanceof LingeringArrowItem lingeringArrowItem) {
            createAndShootArrow(lingeringArrowItem.getTier(), level, position, direction, stack);
        }

        stack.shrink(1);
        return stack;
    }

    private void createAndShootArrow(ArrowTier tier, Level level, Position position, Direction direction, ItemStack stack) {
        UpgradeArrowEntity arrow = new UpgradeArrowEntity(
                tier,
                level,
                position.x(),
                position.y(),
                position.z(),
                stack.copy()
        );

        arrow.shoot(
                direction.getStepX(),
                direction.getStepY() + 0.1F, // Added slight upward bias like vanilla arrows
                direction.getStepZ(),
                1.1F, // Power
                6.0F  // Spread/inaccuracy
        );
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        level.addFreshEntity(arrow);
    }

    @Override
    protected void playSound(BlockSource source) {
        source.level().levelEvent(1002, source.pos(), 0);
    }
}*/

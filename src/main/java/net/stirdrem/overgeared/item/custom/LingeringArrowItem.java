package net.stirdrem.overgeared.item.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LingeringArrowItem extends ArrowItem {
    private final ArrowTier tier;

    public LingeringArrowItem(Properties properties, ArrowTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter, @Nullable ItemStack firedFromWeapon) {
        return new LingeringArrowEntity(ModEntities.LINGERING_ARROW.get(), level, shooter, stack, firedFromWeapon);
    }

    public ArrowTier getTier() {
        return tier;
    }

    @Override
    public boolean isInfinite(ItemStack ammo, ItemStack bow, LivingEntity livingEntity) {
        return false; // Infinity doesn't work on lingering arrows
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (!potionContents.equals(PotionContents.EMPTY)) {
            potionContents.addPotionTooltip(tooltip::add, 0.125F, context.tickRate());
        }
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            // Only return color if there's actually a potion or effects
            if (potionContents.potion().isPresent() || potionContents.hasEffects()) {
                return potionContents.getColor();
            }
        }
        return -1;
    }

    @Override
    public Component getName(ItemStack stack) {
        PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

        if (!potionContents.equals(PotionContents.EMPTY)) {
            // Check if there are any effects
            boolean hasEffects = potionContents.getAllEffects().iterator().hasNext();

            if (hasEffects && potionContents.potion().isPresent()) {
                // Get the potion registry key
                var potionHolder = potionContents.potion().get();
                var potionKey = BuiltInRegistries.POTION.getKey(potionHolder.value());

                if (potionKey != null) {
                    String potionId = potionKey.getPath();

                    // Check if it's a "no effect" potion
                    boolean isNoEffectPotion = potionId.equals("mundane") ||
                            potionId.equals("awkward") ||
                            potionId.equals("thick");

                    if (!isNoEffectPotion) {
                        String effectKey = "item.overgeared.arrow.effect." + potionId;
                        Component effectComponent = Component.translatable(effectKey);

                        return Component.translatable(getDescriptionId(stack), effectComponent);
                    }
                }
            }

            return Component.translatable(getDescriptionId(stack) + ".no_effect");
        }

        return Component.translatable(getDescriptionId(stack));
    }
}
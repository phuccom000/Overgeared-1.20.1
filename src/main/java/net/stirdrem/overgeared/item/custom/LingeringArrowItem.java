package net.stirdrem.overgeared.item.custom;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

import javax.annotation.Nullable;
import java.util.List;

public class LingeringArrowItem extends ArrowItem {
    private final ArrowTier tier;

    public LingeringArrowItem(Properties properties, ArrowTier tier) {
        super(properties);
        this.tier = tier;
    }

    public ItemStack getDefaultInstance() {
        return PotionUtils.setPotion(super.getDefaultInstance(), Potions.POISON);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        return new UpgradeArrowEntity(tier, level, shooter, stack);
    }

    public ArrowTier getTier() {
        return tier;
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, net.minecraft.world.entity.player.Player player) {
        return false; // Infinity doesn't work on lingering arrows
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        CompoundTag tag = pStack.getTag();
        if (tag != null && (tag.contains("Potion") || tag.contains("CustomPotionEffects"))) {
            PotionUtils.addPotionTooltip(pStack, pTooltip, 0.125F);
        }
    }

    public static List<MobEffectInstance> getMobEffects(ItemStack pStack) {
        return getAllEffects(pStack.getTag());
    }

    public static Potion getPotion(@Nullable CompoundTag tag) {
        if (tag == null) return Potions.EMPTY;

        if (tag.contains("Potion", 8)) {
            return Potion.byName(tag.getString("Potion"));
        }

        return Potions.EMPTY;
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag pCompoundTag) {
        List<MobEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(pCompoundTag).getEffects());
        getCustomEffects(pCompoundTag, list);
        return list;
    }

    public static void getCustomEffects(@Nullable CompoundTag pCompoundTag, List<MobEffectInstance> pEffectList) {
        if (pCompoundTag != null && pCompoundTag.contains("CustomPotionEffects", 9)) {
            ListTag listtag = pCompoundTag.getList("CustomPotionEffects", 10);

            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag);
                if (mobeffectinstance != null) {
                    pEffectList.add(mobeffectinstance);
                }
            }
        }

    }

    /*@Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            Potion potion = getPotion(tag);
            if (potion != Potions.EMPTY) {
                // Build your own key: "item.overgeared.arrow.effect.swiftness"
                String effectKey = "item.overgeared.arrow.effect." + potion.getName("").replace("effect.minecraft.", "");

                Component effectComponent = Component.translatable(effectKey); // Translatable potion name

                return Component.translatable(getDescriptionId(stack), effectComponent);
            }
        }

        return super.getName(stack);
    }*/

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            // Use getAllEffects to check for both regular potions and custom effects
            List<MobEffectInstance> effects = getAllEffects(tag);
            boolean hasEffects = !effects.isEmpty();

            if (hasEffects) {
                Potion potion = getPotion(tag);

                if (potion != Potions.EMPTY) {
                    String potionId = potion.getName("").replace("effect.minecraft.", "");

                    boolean isNoEffectPotion = potionId.equals("mundane") || potionId.equals("awkward") || potionId.equals("thick");

                    if (!isNoEffectPotion) {
                        String effectKey = "item.overgeared.arrow.effect." + potionId;
                        Component effectComponent = Component.translatable(effectKey);

                        // Determine if it's a Lingering or regular tipped arrow
                        return Component.translatable(getDescriptionId(stack), effectComponent);
                    }
                }
            }
            return Component.translatable(getDescriptionId(stack) + ".no_effect");
        }
        return Component.translatable(getDescriptionId(stack));
    }
}

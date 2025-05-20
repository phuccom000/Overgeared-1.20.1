package net.stirdrem.overgearedmod.item.custom;

import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CustomShovelItem extends ShovelItem {
    public CustomShovelItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float baseSpeed = super.getDestroySpeed(stack, state);
        String quality = stack.getOrCreateTag().getString("ForgingQuality");

        switch (quality) {
            case "expert":
                return baseSpeed * 1.15f;
            case "perfect":
                return baseSpeed * 1.25f;
            case "poor":
                return baseSpeed * 0.9f;
            default:
                return baseSpeed;
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        int durabilityLoss = 1;
        String quality = stack.getOrCreateTag().getString("ForgingQuality");

        Random random = new Random();
        switch (quality) {
            case "poor":
                durabilityLoss = 2;
                break;
            case "expert":
                durabilityLoss = 1;
                break;
            case "perfect":
                durabilityLoss = random.nextFloat() < 0.25f ? 1 : 0; // 25% chance to lose 1 durability
                break;
            default:
                durabilityLoss = 1;
                break;
        }

        stack.hurtAndBreak(durabilityLoss, attacker, (e) -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = super.getAttributeModifiers(slot, stack);
        if (slot == EquipmentSlot.MAINHAND) {
            String quality = stack.getOrCreateTag().getString("ForgingQuality");
            double extraDamage = switch (quality) {
                case "poor" -> -1.0;
                case "expert" -> 1.0;
                case "perfect" -> 2.0;
                default -> 0.0;
            };
            if (extraDamage != 0.0) {
                modifiers.put(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(UUID.randomUUID(), "forging_bonus", extraDamage, AttributeModifier.Operation.ADDITION));
            }
        }
        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        String quality = stack.getOrCreateTag().getString("ForgingQuality");
        if (!quality.isEmpty()) {
            tooltip.add(Component.literal(getDisplayQuality(quality)).withStyle(ChatFormatting.GRAY));
        }
    }

    private String getDisplayQuality(String key) {
        return switch (key) {
            case "poor" -> "Poorly Forged";
            case "well" -> "Well Forged";
            case "expert" -> "Expertly Forged";
            case "perfect" -> "Perfectly Forged";
            default -> "";
        };
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
        int durabilityLoss = 1;
        String quality = stack.getOrCreateTag().getString("ForgingQuality");
        Random random = new Random();

        switch (quality) {
            case "poor":
                durabilityLoss = 2;
                break;
            case "expert":
                durabilityLoss = 1;
                break;
            case "perfect":
                durabilityLoss = random.nextFloat() < 0.25f ? 1 : 0; // 25% chance to lose 1 durability
                break;
        }

        stack.hurtAndBreak(durabilityLoss, pEntityLiving, (e) -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

}

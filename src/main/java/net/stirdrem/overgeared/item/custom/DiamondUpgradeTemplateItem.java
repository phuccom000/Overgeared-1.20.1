package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;

public class DiamondUpgradeTemplateItem extends SmithingTemplateItem {
    private static final Component DIAMOND_UPGRADE = Component.translatable("upgrade.overgeared.diamond_upgrade").withStyle(ChatFormatting.GRAY);
    private static final Component DIAMOND_UPGRADE_APPLIES_TO = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.applies_to").withStyle(ChatFormatting.BLUE);
    private static final Component DIAMOND_UPGRADE_INGREDIENTS = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.ingredients").withStyle(ChatFormatting.BLUE);
    private static final Component DIAMOND_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.base_slot_description");
    private static final Component DIAMOND_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable("item.overgeared.smithing_template.diamond_upgrade.additions_slot_description");
    private static final ResourceLocation EMPTY_SLOT_HELMET = ResourceLocation.tryParse("item/empty_armor_slot_helmet");
    private static final ResourceLocation EMPTY_SLOT_CHESTPLATE = ResourceLocation.tryParse("item/empty_armor_slot_chestplate");
    private static final ResourceLocation EMPTY_SLOT_LEGGINGS = ResourceLocation.tryParse("item/empty_armor_slot_leggings");
    private static final ResourceLocation EMPTY_SLOT_BOOTS = ResourceLocation.tryParse("item/empty_armor_slot_boots");
    private static final ResourceLocation EMPTY_SLOT_HOE = ResourceLocation.tryParse("item/empty_slot_hoe");
    private static final ResourceLocation EMPTY_SLOT_AXE = ResourceLocation.tryParse("item/empty_slot_axe");
    private static final ResourceLocation EMPTY_SLOT_SWORD = ResourceLocation.tryParse("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_SHOVEL = ResourceLocation.tryParse("item/empty_slot_shovel");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE = ResourceLocation.tryParse("item/empty_slot_pickaxe");
    private static final ResourceLocation EMPTY_SLOT_DIAMOND = ResourceLocation.tryParse("item/empty_slot_diamond");


    public DiamondUpgradeTemplateItem(Component pAppliesTo, Component pIngredients, Component pUpdradeDescription, Component pBaseSlotDescription, Component pAdditionsSlotDescription, List<ResourceLocation> pBaseSlotEmptyIcons, List<ResourceLocation> pAdditonalSlotEmptyIcons) {
        super(pAppliesTo, pIngredients, pUpdradeDescription, pBaseSlotDescription, pAdditionsSlotDescription, pBaseSlotEmptyIcons, pAdditonalSlotEmptyIcons);
    }

    public static SmithingTemplateItem createDiamondUpgradeTemplate() {
        return new SmithingTemplateItem(DIAMOND_UPGRADE_APPLIES_TO, DIAMOND_UPGRADE_INGREDIENTS, DIAMOND_UPGRADE, DIAMOND_UPGRADE_BASE_SLOT_DESCRIPTION, DIAMOND_UPGRADE_ADDITIONS_SLOT_DESCRIPTION, createDiamondUpgradeIconList(), createDiamondUpgradeMaterialList());
    }

    private static List<ResourceLocation> createDiamondUpgradeIconList() {
        return List.of(EMPTY_SLOT_HELMET, EMPTY_SLOT_SWORD, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_AXE, EMPTY_SLOT_BOOTS, EMPTY_SLOT_HOE, EMPTY_SLOT_SHOVEL);
    }

    private static List<ResourceLocation> createDiamondUpgradeMaterialList() {
        return List.of(EMPTY_SLOT_DIAMOND);
    }
}

package net.stirdrem.overgeared.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.TierSortingRegistry;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;

public class ModToolTiers {
    public static final Tier STEEL = TierSortingRegistry.registerTier(
            new ForgeTier(2, 500, 7.0F, 3.0F, 12,
                    ModTags.Blocks.NEEDS_STEEL_TOOL, () -> Ingredient.of(ModItems.STEEL_INGOT.get())),
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_ingot"), List.of(Tiers.IRON), List.of(Tiers.DIAMOND));

    public static final Tier COPPER = TierSortingRegistry.registerTier(
            new ForgeTier(1, 190, 5.0F, 1.0F, 12,
                    ModTags.Blocks.NEEDS_COPPER_TOOL, () -> Ingredient.of(Items.COPPER_INGOT)),
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "copper_ingot"), List.of(Tiers.STONE), List.of(Tiers.IRON));
}
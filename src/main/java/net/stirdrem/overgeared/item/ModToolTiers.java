package net.stirdrem.overgeared.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraftforge.common.ForgeTier;
//import net.minecraftforge.common.Tags;
//import net.minecraftforge.common.TierSortingRegistry;
import net.neoforged.neoforge.common.SimpleTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.util.ModTags;

import java.util.List;

public class ModToolTiers {

    public static final Tier STEEL = new SimpleTier(
            ModTags.Blocks.NEEDS_STEEL_TOOL,
            500,
            7f,
            3f,
            12,
            () -> Ingredient.of(ModItems.STEEL_INGOT));
    public static final Tier COPPER = new SimpleTier(
            ModTags.Blocks.NEEDS_STEEL_TOOL,
            500,
            7f,
            3f,
            12,
            () -> Ingredient.of(ModItems.STEEL_INGOT));
}
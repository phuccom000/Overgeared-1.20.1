package net.stirdrem.overgearedmod.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import net.stirdrem.overgearedmod.OvergearedMod;
import net.stirdrem.overgearedmod.util.ModTags;

import java.util.List;

public class ModToolTiers {
    public static final Tier STEEL = TierSortingRegistry.registerTier(
            new ForgeTier(2, 500, 7.0F, 3.0F, 12,
                    ModTags.Blocks.NEEDS_STEEL_TOOL, () -> Ingredient.of(ModItems.STEEL_INGOT.get())),
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_ingot"), List.of(Tiers.IRON), List.of(Tiers.DIAMOND));

}
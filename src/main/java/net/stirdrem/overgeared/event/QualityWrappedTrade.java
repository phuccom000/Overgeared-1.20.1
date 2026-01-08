package net.stirdrem.overgeared.event;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.MerchantOffer;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.util.ForgingQualityHelper;
import net.stirdrem.overgeared.util.ModTags;

public class QualityWrappedTrade implements VillagerTrades.ItemListing {

    private final VillagerTrades.ItemListing original;
    private final int villagerLevel;

    public QualityWrappedTrade(VillagerTrades.ItemListing original, int villagerLevel) {
        this.original = original;
        this.villagerLevel = villagerLevel;
    }

    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource rand) {
        MerchantOffer offer = original.getOffer(trader, rand);
        if (offer == null) return null;

        ItemStack result = offer.getResult().copy();

        // Only tools, armor, or forgeable tool heads
        if (!(result.getItem() instanceof TieredItem
                || result.getItem() instanceof ArmorItem
                || result.is(ModTags.Items.TOOL_PARTS))) {
            return offer;
        }
        int maxUses = offer.getMaxUses();


        ForgingQuality quality =
                ForgingQualityHelper.rollQuality(rand, villagerLevel);
        // Masterwork = only 1 available
        if (quality == ForgingQuality.MASTER) {
            maxUses = 1;
        }
        ForgingQualityHelper.applyQuality(result, quality);

        // Tier base price
        int basePrice = ForgingQualityHelper.getBasePrice(result.getItem());

        // Quality multiplier
        float mult = ForgingQualityHelper.getQualityMultiplier(quality);

        int finalPrice = Math.max(1, Math.round(basePrice * mult));

        ItemStack emeraldCost = new ItemStack(Items.EMERALD, finalPrice);

        return new MerchantOffer(
                emeraldCost,
                offer.getCostB(),
                result,
                maxUses,
                offer.getXp(),
                offer.getPriceMultiplier()
        );
    }

}


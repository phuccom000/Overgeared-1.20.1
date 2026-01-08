package net.stirdrem.overgeared.event;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.util.ForgingQualityHelper;

public class ForgedItemTrade implements VillagerTrades.ItemListing {

    private final Item item;
    private final int maxUses;
    private final int xp;
    private final int villagerLevel;

    public ForgedItemTrade(Item item, int villagerLevel, int maxUses, int xp) {
        this.item = item;
        this.villagerLevel = villagerLevel;
        this.maxUses = maxUses;
        this.xp = xp;
    }

    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource rand) {
        ItemStack result = new ItemStack(item);

        ForgingQuality quality =
                ForgingQualityHelper.rollQuality(rand, villagerLevel);

        ForgingQualityHelper.applyQuality(result, quality);

        int emeralds = ForgingQualityHelper.priceForQuality(quality);

        return new MerchantOffer(
                new ItemStack(Items.EMERALD, emeralds),
                result,
                maxUses,
                xp,
                0.05f
        );
    }
}

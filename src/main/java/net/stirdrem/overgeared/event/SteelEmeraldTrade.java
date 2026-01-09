package net.stirdrem.overgeared.event;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.stirdrem.overgeared.item.ModItems;

public class SteelEmeraldTrade implements VillagerTrades.ItemListing {

    private final boolean steelForEmerald; // true = 1 steel → 2 emerald, false = 2 steel → 1 emerald

    public SteelEmeraldTrade(boolean steelForEmerald) {
        this.steelForEmerald = steelForEmerald;
    }

    @Override
    public MerchantOffer getOffer(Entity trader, RandomSource rand) {

        if (steelForEmerald) {
            // Player gives steel → gets emeralds
            ItemStack result = new ItemStack(ModItems.STEEL_INGOT.get(), 1);
            ItemStack cost = new ItemStack(Items.EMERALD, 2);

            return new MerchantOffer(cost, ItemStack.EMPTY, result, 12, 5, 0.05f);
        } else {
            // Player gives emerald → gets steel
            ItemStack result = new ItemStack(Items.EMERALD, 1);
            ItemStack cost = new ItemStack(ModItems.STEEL_INGOT.get(), 2);

            return new MerchantOffer(cost, ItemStack.EMPTY, result, 12, 5, 0.05f);
        }
    }
}

package net.stirdrem.overgeared.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.item.custom.BlueprintItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlueprintWanderingTrade implements VillagerTrades.ItemListing {

    private final ItemStack blueprintItem;
    private final int emeraldCost;
    private final int maxUses;
    private final int traderXp;

    public BlueprintWanderingTrade(ItemStack blueprintItem, int emeraldCost, int maxUses, int traderXp) {
        this.blueprintItem = blueprintItem;
        this.emeraldCost = emeraldCost;
        this.maxUses = maxUses;
        this.traderXp = traderXp;
    }

    @Override
    public @Nullable MerchantOffer getOffer(Entity entity, RandomSource random) {

        ItemStack result = blueprintItem.copy();
        CompoundTag tag = result.getOrCreateTag();

        // ---------- QUALITY ROLL ----------
        BlueprintQuality quality = rollQuality(random);

        tag.putString("Quality", quality.name());
        tag.putInt("Uses", 0);
        tag.putInt("UsesToLevel", quality.getUse());

        // ---------- RANDOM TOOL TYPE ----------
        List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
        if (!types.isEmpty()) {
            ToolType type = types.get(random.nextInt(types.size()));
            tag.putString("ToolType", type.getId());
        }

        return new MerchantOffer(
                new ItemStack(Items.EMERALD, emeraldCost),
                result,
                maxUses,
                traderXp,
                0.05F
        );
    }

    private BlueprintQuality rollQuality(RandomSource random) {
        int roll = random.nextInt(1000);

        // ---- RATES ----
        // 0–9     → MASTER (1%)
        // 10–249  → PERFECT (24%)
        // 250–999 → EXPERT (75%)

        if (roll < 10) return BlueprintQuality.MASTER;
        if (roll < 250) return BlueprintQuality.PERFECT;
        return BlueprintQuality.EXPERT;
    }
}

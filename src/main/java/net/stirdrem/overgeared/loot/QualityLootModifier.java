package net.stirdrem.overgeared.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.config.ServerConfig;
import org.jetbrains.annotations.NotNull;

public class QualityLootModifier extends LootModifier {
    public static final Codec<QualityLootModifier> CODEC = RecordCodecBuilder.create(inst ->
            codecStart(inst).apply(inst, QualityLootModifier::new));

    public QualityLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!ServerConfig.ENABLE_LOOT_QUALITY.get()) return generatedLoot;

        // Compute enabled qualities and weights
        int wPoor = ServerConfig.QUALITY_WEIGHT_POOR.get();
        int wWell = ServerConfig.QUALITY_WEIGHT_WELL.get();
        int wExpert = ServerConfig.QUALITY_WEIGHT_EXPERT.get();
        int wPerfect = ServerConfig.QUALITY_WEIGHT_PERFECT.get();
        int wMaster = ServerConfig.QUALITY_WEIGHT_MASTER.get();

        // total weight
        int total = 0;
        if (wPoor > 0) total += wPoor;
        if (wWell > 0) total += wWell;
        if (wExpert > 0) total += wExpert;
        if (wPerfect > 0) total += wPerfect;
        if (wMaster > 0) total += wMaster;
        if (total == 0) {
            // no qualities enabled -> default behavior: force "Poor"
            for (ItemStack stack : generatedLoot) {
                if (stack.getItem() instanceof TieredItem || stack.getItem() instanceof ArmorItem) {
                    stack.getOrCreateTag().putString("ForgingQuality", ForgingQuality.POOR.getDisplayName());
                }
            }
            return generatedLoot;
        }

        for (ItemStack stack : generatedLoot) {
            if (stack.getItem() instanceof TieredItem || stack.getItem() instanceof ArmorItem) {
                // choose random quality by weight
                int r = context.getRandom().nextInt(total); // 0 .. total-1
                ForgingQuality chosen;
                int accum = 0;
                accum += wPoor;
                if (r < accum) {
                    chosen = ForgingQuality.POOR;
                } else {
                    accum += wWell;
                    if (r < accum) {
                        chosen = ForgingQuality.WELL;
                    } else {
                        accum += wExpert;
                        if (r < accum) {
                            chosen = ForgingQuality.EXPERT;
                        } else {
                            accum += wPerfect;
                            if (r < accum) {
                                chosen = ForgingQuality.PERFECT;
                            } else {
                                chosen = ForgingQuality.MASTER;
                            }
                        }
                    }
                }
                stack.getOrCreateTag().putString("ForgingQuality", chosen.getDisplayName());
            }
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends LootModifier> codec() {
        return CODEC;
    }
}


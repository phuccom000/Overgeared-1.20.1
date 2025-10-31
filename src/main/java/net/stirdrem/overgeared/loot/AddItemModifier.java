package net.stirdrem.overgeared.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class AddItemModifier extends LootModifier {
//    public static final Supplier<Codec<AddItemModifier>> CODEC = Suppliers.memoize(()
//            -> RecordCodecBuilder.create(inst -> codecStart(inst).and(NeoForgeRegistries.ITEMS.getCodec()
//            .fieldOf("item").forGetter(m -> m.item)).apply(inst, AddItemModifier::new)));
//    private final Item item;

    // ~ What even is this???

    public static Supplier<MapCodec<AddItemModifier>> CODEC_SUPPLIER = Suppliers.memoize(() -> RecordCodecBuilder
            .mapCodec(instance -> AddItemModifier.codecStart(instance)
                    .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item")
                            .forGetter(addItemModifierInstance -> addItemModifierInstance.item))
                    .apply(instance, AddItemModifier::new)));

    private final Item item;

    public AddItemModifier(LootItemCondition[] conditionsIn, Item item) {
        super(conditionsIn);
        this.item = item;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for (LootItemCondition condition : this.conditions) {
            if (!condition.test(context)) {
                return generatedLoot;
            }
        }

        generatedLoot.add(new ItemStack(this.item));

        return generatedLoot;
    }


    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC_SUPPLIER.get();
    }
}
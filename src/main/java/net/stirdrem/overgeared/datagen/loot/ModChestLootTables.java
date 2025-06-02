package net.stirdrem.overgeared.datagen.loot;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;

import java.util.Collections;
import java.util.function.BiConsumer;

public class ModChestLootTables implements LootTableSubProvider {
    // Vanilla loot table locations
    private static final ResourceLocation SIMPLE_DUNGEON = ResourceLocation.tryBuild("minecraft", "chests/simple_dungeon");
    private static final ResourceLocation ABANDONED_MINESHAFT = ResourceLocation.tryBuild("minecraft", "chests/abandoned_mineshaft");
    private static final ResourceLocation STRONGHOLD_CORRIDOR = ResourceLocation.tryBuild("minecraft", "chests/stronghold_corridor");
    private static final ResourceLocation DESERT_PIRAMID = ResourceLocation.tryBuild("minecraft", "chests/desert_piramid");
    private static final ResourceLocation JUNGLE_TEMPLE = ResourceLocation.tryBuild("minecraft", "chests/jungle_temple");
    private static final ResourceLocation SHIPWRECK_TREASURE = ResourceLocation.tryBuild("minecraft", "chests/shipwreck_treasure");
    private static final ResourceLocation WOODLAND_MANSION = ResourceLocation.tryBuild("minecraft", "chests/woodland_mansion");
    private static final ResourceLocation UNDERWATER_RUIN_BIG = ResourceLocation.tryBuild("minecraft", "chests/underwater_ruin_big");

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> pOutput) {
        // Add to simple dungeon (overworld dungeon)
        pOutput.accept(SIMPLE_DUNGEON, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2.0F, 4.0F))
                        .add(createLootEntry(ModItems.STEEL_INGOT.get(), 1, 3, 15)) // 15 weight = moderately rare
                        .add(createLootEntry(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get(), 1, 1, 5)) // 5 weight = very rare
                )
        );

        // Optional: Add to other dungeon types
        pOutput.accept(ABANDONED_MINESHAFT, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 3.0F))
                        .add(createLootEntry(ModItems.STEEL_INGOT.get(), 3, 7, 20))
                        .add(createLootEntry(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get(), 1, 1, 5)) // 5 weight = very rare
                )
        );

        pOutput.accept(STRONGHOLD_CORRIDOR, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 3.0F))
                        .add(createLootEntry(ModItems.STEEL_INGOT.get(), 3, 7, 20))
                        .add(createLootEntry(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get(), 1, 1, 5)) // 5 weight = very rare
                )
        );

        pOutput.accept(DESERT_PIRAMID, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 3.0F))
                        .add(createLootEntry(ModItems.STEEL_INGOT.get(), 3, 7, 20))
                        .add(createLootEntry(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get(), 1, 1, 5)) // 5 weight = very rare
                )
        );

        pOutput.accept(JUNGLE_TEMPLE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 3.0F))
                        .add(createLootEntry(ModItems.STEEL_INGOT.get(), 3, 7, 20))
                        .add(createLootEntry(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get(), 1, 1, 5)) // 5 weight = very rare
                )
        );

        pOutput.accept(SHIPWRECK_TREASURE, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 3.0F))
                        .add(createLootEntry(ModItems.STEEL_INGOT.get(), 3, 7, 20))
                        .add(createLootEntry(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get(), 1, 1, 5)) // 5 weight = very rare
                )
        );

        pOutput.accept(WOODLAND_MANSION, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 3.0F))
                        .add(createLootEntry(ModItems.STEEL_INGOT.get(), 3, 7, 20))
                        .add(createLootEntry(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get(), 1, 1, 5)) // 5 weight = very rare
                )
        );

        pOutput.accept(UNDERWATER_RUIN_BIG, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 3.0F))
                        .add(createLootEntry(ModItems.STEEL_INGOT.get(), 3, 7, 20))
                        .add(createLootEntry(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get(), 1, 1, 5)) // 5 weight = very rare
                )
        );
    }

    // Helper method to create consistent loot entries
    private LootPoolEntryContainer.Builder<?> createLootEntry(Item item, int minCount, int maxCount, int weight) {
        return LootItem.lootTableItem(item)
                .setWeight(weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(minCount, maxCount)));
    }

    // For enchanted items
    private LootPoolEntryContainer.Builder<?> createEnchantedEntry(Item item, int weight) {
        return LootItem.lootTableItem(item)
                .setWeight(weight)
                .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 1));
    }
}
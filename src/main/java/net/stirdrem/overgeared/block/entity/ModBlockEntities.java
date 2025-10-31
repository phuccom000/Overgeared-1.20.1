package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, OvergearedMod.MOD_ID);

    public static final Supplier<BlockEntityType<SteelSmithingAnvilBlockEntity>> STEEL_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("smithing_table_be", () ->
                    BlockEntityType.Builder.of(SteelSmithingAnvilBlockEntity::new,
                                    ModBlocks.SMITHING_ANVIL.get()).
                            build(null));

    public static final Supplier<BlockEntityType<TierASmithingAnvilBlockEntity>> TIER_A_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("tier_a_smithing_table_be", () ->
                    BlockEntityType.Builder.of(TierASmithingAnvilBlockEntity::new,
                                    ModBlocks.TIER_A_SMITHING_ANVIL.get()).
                            build(null));

    public static final Supplier<BlockEntityType<TierBSmithingAnvilBlockEntity>> TIER_B_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("tier_b_smithing_table_be", () ->
                    BlockEntityType.Builder.of(TierBSmithingAnvilBlockEntity::new,
                                    ModBlocks.TIER_B_SMITHING_ANVIL.get()).
                            build(null));

    public static final Supplier<BlockEntityType<StoneSmithingAnvilBlockEntity>> STONE_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("stone_smithing_table_be", () ->
                    BlockEntityType.Builder.of(StoneSmithingAnvilBlockEntity::new,
                                    ModBlocks.STONE_SMITHING_ANVIL.get()).
                            build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

}

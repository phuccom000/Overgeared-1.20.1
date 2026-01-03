package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OvergearedMod.MOD_ID);

//    public static final RegistryObject<BlockEntityType<SteelSmithingAnvilBlockEntity>> STEEL_SMITHING_ANVIL_BE =
//            BLOCK_ENTITIES.register("smithing_table_be", () ->
//                    BlockEntityType.Builder.of(SteelSmithingAnvilBlockEntity::new,
//                                    ModBlocks.SMITHING_ANVIL.get()).
//                            build(null));
//
//    public static final RegistryObject<BlockEntityType<TierASmithingAnvilBlockEntity>> TIER_A_SMITHING_ANVIL_BE =
//            BLOCK_ENTITIES.register("tier_a_smithing_table_be", () ->
//                    BlockEntityType.Builder.of(TierASmithingAnvilBlockEntity::new,
//                                    ModBlocks.TIER_A_SMITHING_ANVIL.get()).
//                            build(null));
//
//    public static final RegistryObject<BlockEntityType<TierBSmithingAnvilBlockEntity>> TIER_B_SMITHING_ANVIL_BE =
//            BLOCK_ENTITIES.register("tier_b_smithing_table_be", () ->
//                    BlockEntityType.Builder.of(TierBSmithingAnvilBlockEntity::new,
//                                    ModBlocks.TIER_B_SMITHING_ANVIL.get()).
//                            build(null));
//
//    public static final RegistryObject<BlockEntityType<StoneSmithingAnvilBlockEntity>> STONE_SMITHING_ANVIL_BE =
//            BLOCK_ENTITIES.register("stone_smithing_table_be", () ->
//                    BlockEntityType.Builder.of(StoneSmithingAnvilBlockEntity::new,
//                                    ModBlocks.STONE_SMITHING_ANVIL.get()).
//                            build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlloySmelterBlockEntity>> ALLOY_FURNACE_BE =
            BLOCK_ENTITIES.register("alloy_furnace_be", () ->
                    BlockEntityType.Builder.of(AlloySmelterBlockEntity::new,
                            ModBlocks.ALLOY_FURNACE.get()).build(null));

//    public static final RegistryObject<BlockEntityType<NetherAlloySmelterBlockEntity>> NETHER_ALLOY_FURNACE_BE =
//            BLOCK_ENTITIES.register("nether_alloy_furnace_be", () ->
//                    BlockEntityType.Builder.of(NetherAlloySmelterBlockEntity::new,
//                            ModBlocks.NETHER_ALLOY_FURNACE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

package net.stirdrem.overgeared.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, OvergearedMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<SteelSmithingAnvilBlockEntity>> STEEL_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("steel_smithing_table_be", () ->
                    BlockEntityType.Builder.of(SteelSmithingAnvilBlockEntity::new,
                                    ModBlocks.SMITHING_ANVIL.get()).
                            build(null));
    public static final RegistryObject<BlockEntityType<StoneSmithingAnvilBlockEntity>> STONE_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("stone_smithing_table_be", () ->
                    BlockEntityType.Builder.of(StoneSmithingAnvilBlockEntity::new,
                                    ModBlocks.STONE_SMITHING_ANVIL.get()).
                            build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

}

package net.stirdrem.overgeared.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.custom.*;
//import net.stirdrem.overgeared.block.custom.LayeredWaterBarrel;
//import net.stirdrem.overgeared.block.custom.WaterBarrel;
//import net.stirdrem.overgeared.core.waterbarrel.BarrelInteraction;
import net.stirdrem.overgeared.item.ModItems;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, OvergearedMod.MOD_ID);
    public static final RegistryObject<Block> SMITHING_ANVIL = registerBlock("smithing_anvil",
            () -> new SteelSmithingAnvil(AnvilTier.IRON, BlockBehaviour.Properties.copy(Blocks.ANVIL).noOcclusion()));
    public static final RegistryObject<Block> TIER_A_SMITHING_ANVIL = registerBlock("tier_a_smithing_anvil",
            () -> new TierASmithingAnvil(AnvilTier.ABOVE_A, BlockBehaviour.Properties.copy(Blocks.ANVIL).noOcclusion()));
    public static final RegistryObject<Block> TIER_B_SMITHING_ANVIL = registerBlock("tier_b_smithing_anvil",
            () -> new TierBSmithingAnvil(AnvilTier.ABOVE_B, BlockBehaviour.Properties.copy(Blocks.ANVIL).noOcclusion()));
    public static final RegistryObject<Block> STONE_SMITHING_ANVIL = registerBlock("stone_anvil",
            () -> new StoneSmithingAnvil(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion()));
    public static final RegistryObject<Block> STEEL_BLOCK = registerBlock("steel_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<Block> DRAFTING_TABLE = registerBlock("drafting_table",
            () -> new BlueprintWorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE)));
    public static final RegistryObject<Block> ALLOY_FURNACE = registerBlock("alloy_furnace",
            () -> new AlloySmelterBlock(BlockBehaviour.Properties.copy(Blocks.BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13))));
    public static final RegistryObject<Block> NETHER_ALLOY_FURNACE = registerBlock("nether_alloy_furnace",
            () -> new NetherAlloySmelterBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13))));
    public static final RegistryObject<Block> CAST_FURNACE = registerBlock("casting_furnace",
            () -> new CastFurnaceBlock(BlockBehaviour.Properties.copy(Blocks.RED_NETHER_BRICKS).noOcclusion().requiresCorrectToolForDrops().strength(3.5F, 6.0F).lightLevel(litBlockEmission(13))));

    private static ToIntFunction<BlockState> litBlockEmission(int pLightValue) {
        return (p_50763_) -> {
            return p_50763_.getValue(BlockStateProperties.LIT) ? pLightValue : 0;
        };
    }

    private BlockState defaultBlockState;

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public final BlockState defaultBlockState() {
        return this.defaultBlockState;
    }
}

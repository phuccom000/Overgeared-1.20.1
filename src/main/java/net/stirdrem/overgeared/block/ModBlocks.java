package net.stirdrem.overgeared.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
//import net.minecraftforge.eventbus.api.IEventBus;
//import net.minecraftforge.registries.DeferredRegister;
//import net.minecraftforge.registries.ForgeRegistries;
//import net.minecraftforge.registries.DeferredBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.custom.*;
//import net.stirdrem.overgeared.block.custom.LayeredWaterBarrel;
//import net.stirdrem.overgeared.block.custom.WaterBarrel;
//import net.stirdrem.overgeared.core.waterbarrel.BarrelInteraction;
import net.stirdrem.overgeared.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    private BlockState defaultBlockState;

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(OvergearedMod.MOD_ID);

    public static final DeferredBlock<Block> SMITHING_ANVIL = registerBlock("smithing_anvil",
            () -> new SteelSmithingAnvil(AnvilTier.IRON, BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion()));
    public static final DeferredBlock<Block> TIER_A_SMITHING_ANVIL = registerBlock("tier_a_smithing_anvil",
            () -> new TierASmithingAnvil(AnvilTier.ABOVE_A, BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion()));
    public static final DeferredBlock<Block> TIER_B_SMITHING_ANVIL = registerBlock("tier_b_smithing_anvil",
            () -> new TierBSmithingAnvil(AnvilTier.ABOVE_B, BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion()));

    public static final DeferredBlock<Block> STONE_SMITHING_ANVIL = registerBlock("stone_anvil",
            () -> new StoneSmithingAnvil(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion()));

    public static final DeferredBlock<Block> STEEL_BLOCK = registerBlock("steel_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)));

    public static final DeferredBlock<Block> DRAFTING_TABLE = registerBlock("drafting_table",
            () -> new BlueprintWorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)));

    /*public static final DeferredBlock<Block> SMITHING_ANVIL_TEST = registerBlock("smithing_anvil_test",
            () -> new CounterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion()));*/

    /*public static final DeferredBlock<Block> ROCK = registerBlock("rock",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEAD_BUSH).sound(SoundType.STONE)));*/

    /* public static final DeferredBlock<Block> WATER_BARREL = registerBlock("water_barrel",
             () -> new WaterBarrel(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).noOcclusion()));
     public static final DeferredBlock<Block> WATER_BARREL_FULL = registerBlock("water_barrel_full",
             () -> new LayeredWaterBarrel(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).noOcclusion(), LayeredCauldronBlock.RAIN, BarrelInteraction.WATER));
     *//*public static final DeferredBlock<Block> WATER_BARREL_FULL = registerBlock("water_barrel_full",
            () -> new WaterBarrel(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).requiresCorrectToolForDrops().strength(2.0F).noOcclusion()));*//*
     */
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public final BlockState defaultBlockState() {
        return this.defaultBlockState;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

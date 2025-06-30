package net.stirdrem.overgeared.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, OvergearedMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.STEEL_BLOCK);
        horizontalBlock(ModBlocks.SMITHING_ANVIL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/smithing_anvil")));
        //simpleBlockWithItem(ModBlocks.STONE_SMITHING_ANVIL.get(), new ModelFile.UncheckedModelFile(modLoc("block/stone_anvil")));
        horizontalBlock(ModBlocks.STONE_SMITHING_ANVIL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/stone_anvil")));

        /*simpleBlock(ModBlocks.WATER_BARREL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/water_barrel")));*/
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void blockWithItemDirectional(RegistryObject<Block> blockRegistryObject) {
    }
}
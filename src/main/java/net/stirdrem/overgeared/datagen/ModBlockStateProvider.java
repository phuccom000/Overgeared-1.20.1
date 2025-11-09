package net.stirdrem.overgeared.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
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
        //blockWithItem(ModBlocks.DRAFTING_TABLE);
        horizontalBlock(ModBlocks.SMITHING_ANVIL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/smithing_anvil")));
        horizontalBlock(ModBlocks.TIER_A_SMITHING_ANVIL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/tier_a_smithing_anvil")));
        horizontalBlock(ModBlocks.TIER_B_SMITHING_ANVIL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/tier_b_smithing_anvil")));
        //simpleBlockWithItem(ModBlocks.STONE_SMITHING_ANVIL.get(), new ModelFile.UncheckedModelFile(modLoc("block/stone_anvil")));
        horizontalBlock(ModBlocks.STONE_SMITHING_ANVIL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/stone_anvil")));
        facingLitBlock(ModBlocks.ALLOY_FURNACE.get(), "alloy_furnace", "alloy_furnace_on");
        facingLitBlock(ModBlocks.NETHER_ALLOY_FURNACE.get(), "nether_alloy_furnace", "nether_alloy_furnace_on");

        /*simpleBlock(ModBlocks.WATER_BARREL.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/water_barrel")));*/
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    protected void facingLitBlock(Block block, String baseModelName, String litModelName) {
        DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
        BooleanProperty LIT = BlockStateProperties.LIT;

        ModelFile baseModel = models().getExistingFile(modLoc("block/" + baseModelName));
        ModelFile litModel = models().getExistingFile(modLoc("block/" + litModelName));

        getVariantBuilder(block).forAllStates(state -> {
            Direction dir = state.getValue(FACING);
            boolean lit = state.getValue(LIT);

            return ConfiguredModel.builder()
                    .modelFile(lit ? litModel : baseModel)
                    .rotationY(((int) dir.toYRot()) % 360)
                    .build();
        });

        simpleBlockItem(block, baseModel);
    }


    private void blockWithItemDirectional(RegistryObject<Block> blockRegistryObject) {
    }
}
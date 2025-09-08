package net.stirdrem.overgeared.datagen;

import com.mojang.patchy.BlockedServers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, OvergearedMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
       /* this.tag(ModTags.Blocks.METAL_DETECTOR_VALUABLES)
                .add(ModBlocks.SAPPHIRE_ORE.get()).addTag(Tags.Blocks.ORES);
*/
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.STEEL_BLOCK.get(),
                        ModBlocks.SMITHING_ANVIL.get(),
                        ModBlocks.STONE_SMITHING_ANVIL.get()
                );

        this.tag(ModTags.Blocks.SMITHING_ANVIL)
                .add(
                        ModBlocks.SMITHING_ANVIL.get(),
                        ModBlocks.STONE_SMITHING_ANVIL.get(),
                        ModBlocks.TIER_A_SMITHING_ANVIL.get(),
                        ModBlocks.TIER_B_SMITHING_ANVIL.get()
                );
       /* this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.WATER_BARREL.get(),
                        ModBlocks.WATER_BARREL_FULL.get()
                );
*/
        this.tag(ModTags.Blocks.NEEDS_STEEL_TOOL)
                .add(
                        Blocks.OBSIDIAN,
                        Blocks.CRYING_OBSIDIAN
                );
        this.tag(ModTags.Blocks.NEEDS_COPPER_TOOL)
                .add(
                        Blocks.RAW_IRON_BLOCK,
                        Blocks.IRON_ORE,
                        Blocks.DEEPSLATE_IRON_ORE,
                        Blocks.IRON_BLOCK
                );
        this.tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(Blocks.NETHERITE_BLOCK,
                        Blocks.RESPAWN_ANCHOR,
                        Blocks.ANCIENT_DEBRIS)
                .replace(true);

        this.tag(BlockTags.create(ResourceLocation.tryBuild("forge", "storage_blocks/steel")))
                .add(ModBlocks.STEEL_BLOCK.get());
        this.tag(ModTags.Blocks.ANVIL_BASES)
                .add(Blocks.STONE
                );
       /* this.tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.RAW_SAPPHIRE_BLOCK.get());

        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.NETHER_SAPPHIRE_ORE.get());

        this.tag(Tags.Blocks.NEEDS_NETHERITE_TOOL)
                .add(ModBlocks.END_STONE_SAPPHIRE_ORE.get());*/


    }
}
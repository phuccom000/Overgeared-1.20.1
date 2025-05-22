package net.stirdrem.overgeared.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagGenerator extends ItemTagsProvider {
    public ModItemTagGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagLookup<Block>> future, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, completableFuture, future, OvergearedMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(ModTags.Items.TONGS)
                .add(ModItems.IRON_TONGS.get(),
                        ModItems.STEEL_TONGS.get()
                );
        this.tag(ModTags.Items.TOOL_PARTS)
                .add(
                        // Sword Blades
                        ModItems.STONE_SWORD_BLADE.get(),
                        ModItems.IRON_SWORD_BLADE.get(),
                        ModItems.GOLDEN_SWORD_BLADE.get(),
                        ModItems.STEEL_SWORD_BLADE.get(),
                        ModItems.DIAMOND_SWORD_BLADE.get(),

                        // Pickaxe Heads
                        ModItems.STONE_PICKAXE_HEAD.get(),
                        ModItems.IRON_PICKAXE_HEAD.get(),
                        ModItems.GOLDEN_PICKAXE_HEAD.get(),
                        ModItems.STEEL_PICKAXE_HEAD.get(),
                        ModItems.DIAMOND_PICKAXE_HEAD.get(),

                        // Axe Heads
                        ModItems.STONE_AXE_HEAD.get(),
                        ModItems.IRON_AXE_HEAD.get(),
                        ModItems.GOLDEN_AXE_HEAD.get(),
                        ModItems.STEEL_AXE_HEAD.get(),
                        ModItems.DIAMOND_AXE_HEAD.get(),

                        // Shovel Heads
                        ModItems.STONE_SHOVEL_HEAD.get(),
                        ModItems.IRON_SHOVEL_HEAD.get(),
                        ModItems.GOLDEN_SHOVEL_HEAD.get(),
                        ModItems.STEEL_SHOVEL_HEAD.get(),
                        ModItems.DIAMOND_SHOVEL_HEAD.get(),

                        // Hoe Heads
                        ModItems.STONE_HOE_HEAD.get(),
                        ModItems.IRON_HOE_HEAD.get(),
                        ModItems.GOLDEN_HOE_HEAD.get(),
                        ModItems.STEEL_HOE_HEAD.get(),
                        ModItems.DIAMOND_HOE_HEAD.get()
                );

        this.tag(Tags.Items.TOOLS)
                .add(
                        ModItems.IRON_TONGS.get(),
                        ModItems.STEEL_TONGS.get(),
                        ModItems.SMITHING_HAMMER.get()
                );
        this.tag(ModTags.Items.HEATABLE_METALS)
                .add(
                        ModItems.STEEL_INGOT.get(),
                        Items.IRON_INGOT
                );
        this.tag(ModTags.Items.HEATED_METALS)
                .add(
                        ModItems.HEATED_STEEL_INGOT.get(),
                        ModItems.HEATED_IRON_INGOT.get()
                );
        this.tag(ModTags.Items.SMITHING_HAMMERS)
                .add(
                        ModItems.SMITHING_HAMMER.get()
                );
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel")))
                .add(ModItems.STEEL_INGOT.get());
    }
}


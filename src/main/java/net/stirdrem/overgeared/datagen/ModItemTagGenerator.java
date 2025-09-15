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
import net.stirdrem.overgeared.block.ModBlocks;
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
                        ModItems.STEEL_TONGS.get(),
                        ModItems.WOODEN_TONGS.get()
                );
        this.tag(Tags.Items.INGOTS)
                .add(ModItems.STEEL_INGOT.get()
                );
        this.tag(Tags.Items.NUGGETS)
                .add(ModItems.COPPER_NUGGET.get(),
                        ModItems.STEEL_NUGGET.get()
                );
        this.tag(ModTags.Items.TOOL_PARTS)
                .add(
                        // Stone
                        ModItems.STONE_SWORD_BLADE.get(),
                        ModItems.STONE_PICKAXE_HEAD.get(),
                        ModItems.STONE_AXE_HEAD.get(),
                        ModItems.STONE_SHOVEL_HEAD.get(),
                        ModItems.STONE_HOE_HEAD.get(),

                        // Copper
                        ModItems.COPPER_SWORD_BLADE.get(),
                        ModItems.COPPER_PICKAXE_HEAD.get(),
                        ModItems.COPPER_AXE_HEAD.get(),
                        ModItems.COPPER_HOE_HEAD.get(),
                        ModItems.COPPER_SHOVEL_HEAD.get(),

                        // Iron
                        ModItems.IRON_SWORD_BLADE.get(),
                        ModItems.IRON_PICKAXE_HEAD.get(),
                        ModItems.IRON_AXE_HEAD.get(),
                        ModItems.IRON_SHOVEL_HEAD.get(),
                        ModItems.IRON_HOE_HEAD.get(),

                        // Golden
                        ModItems.GOLDEN_SWORD_BLADE.get(),
                        ModItems.GOLDEN_PICKAXE_HEAD.get(),
                        ModItems.GOLDEN_AXE_HEAD.get(),
                        ModItems.GOLDEN_SHOVEL_HEAD.get(),
                        ModItems.GOLDEN_HOE_HEAD.get(),

                        // Steel
                        ModItems.STEEL_SWORD_BLADE.get(),
                        ModItems.STEEL_PICKAXE_HEAD.get(),
                        ModItems.STEEL_AXE_HEAD.get(),
                        ModItems.STEEL_SHOVEL_HEAD.get(),
                        ModItems.STEEL_HOE_HEAD.get(),

                        /*
                        // Diamond (commented out)
                        ModItems.DIAMOND_SWORD_BLADE.get(),
                        ModItems.DIAMOND_PICKAXE_HEAD.get(),
                        ModItems.DIAMOND_AXE_HEAD.get(),
                        ModItems.DIAMOND_SHOVEL_HEAD.get(),
                        ModItems.DIAMOND_HOE_HEAD.get()
                        */
                        ModItems.IRON_ARROW_HEAD.get(),
                        ModItems.STEEL_ARROW_HEAD.get(),
                        ModItems.DIAMOND_SHARD.get()
                );

        this.tag(ItemTags.TOOLS)
                .add(
                        ModItems.WOODEN_TONGS.get(),
                        ModItems.IRON_TONGS.get(),
                        ModItems.STEEL_TONGS.get(),
                        ModItems.SMITHING_HAMMER.get(),
                        ModItems.COPPER_SMITHING_HAMMER.get()
                );
        this.tag(ModTags.Items.HEATABLE_METALS)
                .add(
                        ModItems.STEEL_INGOT.get(),
                        Items.IRON_INGOT,
                        ModItems.CRUDE_STEEL.get(),
                        Items.COPPER_INGOT
                );
        this.tag(ModTags.Items.HEATED_METALS)
                .add(
                        ModItems.HEATED_STEEL_INGOT.get(),
                        ModItems.HEATED_IRON_INGOT.get(),
                        ModItems.HEATED_CRUDE_STEEL.get(),
                        ModItems.HEATED_COPPER_INGOT.get()
                );
        this.tag(ModTags.Items.SMITHING_HAMMERS)
                .add(
                        ModItems.SMITHING_HAMMER.get(),
                        ModItems.COPPER_SMITHING_HAMMER.get()
                );
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel")))
                .add(ModItems.STEEL_INGOT.get());
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "nuggets/steel")))
                .add(ModItems.STEEL_NUGGET.get());
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "nuggets/copper")))
                .add(ModItems.COPPER_NUGGET.get());
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "plates/copper")))
                .add(ModItems.COPPER_PLATE.get());
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "plates/iron")))
                .add(ModItems.IRON_PLATE.get());
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "plates/steel")))
                .add(ModItems.STEEL_PLATE.get());

        this.tag(Tags.Items.ARMORS_HELMETS)
                .add(ModItems.STEEL_HELMET.get())
                .add(ModItems.COPPER_HELMET.get())
        ;
        this.tag(Tags.Items.ARMORS_CHESTPLATES)
                .add(ModItems.STEEL_CHESTPLATE.get())
                .add(ModItems.COPPER_CHESTPLATE.get());
        this.tag(Tags.Items.ARMORS_LEGGINGS)
                .add(ModItems.STEEL_LEGGINGS.get())
                .add(ModItems.COPPER_LEGGINGS.get())
        ;
        this.tag(Tags.Items.ARMORS_BOOTS)
                .add(ModItems.STEEL_BOOTS.get())
                .add(ModItems.COPPER_BOOTS.get())
        ;

        this.tag(Tags.Items.TOOLS)
                .add(
                        ModItems.STEEL_AXE.get(),
                        ModItems.STEEL_PICKAXE.get(),
                        ModItems.STEEL_HOE.get(),
                        ModItems.STEEL_SHOVEL.get(),
                        ModItems.STEEL_SWORD.get(),
                        ModItems.COPPER_AXE.get(),
                        ModItems.COPPER_PICKAXE.get(),
                        ModItems.COPPER_HOE.get(),
                        ModItems.COPPER_SHOVEL.get(),
                        ModItems.COPPER_SWORD.get()
                );
        // Add to vanilla Minecraft tags
        this.tag(ItemTags.HOES)
                .add(ModItems.COPPER_HOE.get())
                .add(ModItems.STEEL_HOE.get());
        this.tag(ItemTags.AXES)
                .add(ModItems.COPPER_AXE.get())
                .add(ModItems.STEEL_AXE.get())
        ;
        this.tag(ItemTags.PICKAXES)
                .add(ModItems.COPPER_PICKAXE.get())
                .add(ModItems.STEEL_PICKAXE.get())
        ;
        this.tag(ItemTags.SHOVELS)
                .add(ModItems.COPPER_SHOVEL.get())
                .add(ModItems.STEEL_SHOVEL.get());
        this.tag(ItemTags.SWORDS)
                .add(ModItems.COPPER_SWORD.get())
                .add(ModItems.STEEL_SWORD.get());
        // Hoes
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "tools/hoes")))
                .add(ModItems.STEEL_HOE.get())
                .add(ModItems.COPPER_HOE.get());

        // Axes
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "tools/axes")))
                .add(ModItems.COPPER_AXE.get())
                .add(ModItems.STEEL_AXE.get())
        ;

        // Pickaxes
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "tools/pickaxes")))
                .add(ModItems.COPPER_PICKAXE.get())
                .add(ModItems.STEEL_PICKAXE.get())
        ;

        // Shovels
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "tools/shovels")))
                .add(ModItems.STEEL_SHOVEL.get())
                .add(ModItems.COPPER_SHOVEL.get())
        ;

        // Swords
        this.tag(ItemTags.create(ResourceLocation.tryBuild("forge", "tools/swords")))
                .add(ModItems.STEEL_SWORD.get())
                .add(ModItems.COPPER_SWORD.get())
        ;

        this.tag(ItemTags.TRIMMABLE_ARMOR)
                .add(
                        ModItems.STEEL_HELMET.get(),
                        ModItems.STEEL_CHESTPLATE.get(),
                        ModItems.STEEL_LEGGINGS.get(),
                        ModItems.STEEL_BOOTS.get(),
                        ModItems.COPPER_HELMET.get(),
                        ModItems.COPPER_CHESTPLATE.get(),
                        ModItems.COPPER_LEGGINGS.get(),
                        ModItems.COPPER_BOOTS.get()
                );
        this.tag(ItemTags.ARROWS)
                .add(
                        ModItems.LINGERING_ARROW.get(),
                        //ModItems.MODULAR_ARROW.get(),
                        ModItems.IRON_UPGRADE_ARROW.get(),
                        ModItems.STEEL_UPGRADE_ARROW.get(),
                        ModItems.DIAMOND_UPGRADE_ARROW.get()
                );
        this.tag(ModTags.Items.GRINDABLE)
                .add(
                        Items.DIAMOND
                );
        this.tag(ModTags.Items.GRINDED)
                .add(
                        ModItems.DIAMOND_SHARD.get()
                );
        this.tag(ModTags.Items.HOT_ITEMS)
                .add(
                        Items.LAVA_BUCKET
                );
    }
}


package net.stirdrem.overgeared.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
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
                        ModItems.STONE_HAMMER_HEAD.get(),
                        ModItems.STONE_SWORD_BLADE.get(),
                        ModItems.STONE_PICKAXE_HEAD.get(),
                        ModItems.STONE_AXE_HEAD.get(),
                        ModItems.STONE_SHOVEL_HEAD.get(),
                        ModItems.STONE_HOE_HEAD.get(),

                        // Copper
                        ModItems.COPPER_HAMMER_HEAD.get(),
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
                        ModItems.STEEL_HAMMER_HEAD.get(),
                        ModItems.STEEL_SWORD_BLADE.get(),
                        ModItems.STEEL_PICKAXE_HEAD.get(),
                        ModItems.STEEL_AXE_HEAD.get(),
                        ModItems.STEEL_SHOVEL_HEAD.get(),
                        ModItems.STEEL_HOE_HEAD.get(),

                        ModItems.IRON_ARROW_HEAD.get(),
                        ModItems.STEEL_ARROW_HEAD.get(),
                        ModItems.DIAMOND_SHARD.get()
                );

        this.tag(ModTags.Items.HEATED_METALS)
                .add(
                        ModItems.HEATED_STEEL_INGOT.get(),
                        ModItems.HEATED_IRON_INGOT.get(),
                        ModItems.HEATED_CRUDE_STEEL.get(),
                        ModItems.HEATED_COPPER_INGOT.get(),
                        ModItems.HEATED_SILVER_INGOT.get(),
                        ModItems.HEATED_NETHERITE_ALLOY.get()
                );
        this.tag(ModTags.Items.SMITHING_HAMMERS)
                .add(
                        ModItems.SMITHING_HAMMER.get(),
                        ModItems.COPPER_SMITHING_HAMMER.get()
                );
        this.tag(ItemTags.create(ResourceLocation.parse("c:ingots/steel")))
                .add(ModItems.STEEL_INGOT.get());
        this.tag(ItemTags.create(ResourceLocation.parse("c:nuggets/steel")))
                .add(ModItems.STEEL_NUGGET.get());
        this.tag(ItemTags.create(ResourceLocation.parse("c:nuggets/copper")))
                .add(ModItems.COPPER_NUGGET.get());
        this.tag(ItemTags.create(ResourceLocation.parse("c:plates/copper")))
                .add(ModItems.COPPER_PLATE.get());
        this.tag(ItemTags.create(ResourceLocation.parse("c:plates/iron")))
                .add(ModItems.IRON_PLATE.get());
        this.tag(ItemTags.create(ResourceLocation.parse("c:plates/steel")))
                .add(ModItems.STEEL_PLATE.get());

        this.tag(ItemTags.HEAD_ARMOR)
                .add(ModItems.STEEL_HELMET.get())
                .add(ModItems.COPPER_HELMET.get())
        ;
        this.tag(ItemTags.CHEST_ARMOR)
                .add(ModItems.STEEL_CHESTPLATE.get())
                .add(ModItems.COPPER_CHESTPLATE.get());
        this.tag(ItemTags.LEG_ARMOR)
                .add(ModItems.STEEL_LEGGINGS.get())
                .add(ModItems.COPPER_LEGGINGS.get())
        ;
        this.tag(ItemTags.FOOT_ARMOR)
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
                        ModItems.COPPER_SWORD.get(),
                        ModItems.WOODEN_TONGS.get(),
                        ModItems.IRON_TONGS.get(),
                        ModItems.STEEL_TONGS.get(),
                        ModItems.SMITHING_HAMMER.get(),
                        ModItems.COPPER_SMITHING_HAMMER.get()
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
        this.tag(ItemTags.create(ResourceLocation.parse("c:tools/hoes")))
                .add(ModItems.STEEL_HOE.get())
                .add(ModItems.COPPER_HOE.get());

        // Axes
        this.tag(ItemTags.create(ResourceLocation.parse("c:tools/axes")))
                .add(ModItems.COPPER_AXE.get())
                .add(ModItems.STEEL_AXE.get())
        ;

        // Pickaxes
        this.tag(ItemTags.create(ResourceLocation.parse("c:tools/pickaxes")))
                .add(ModItems.COPPER_PICKAXE.get())
                .add(ModItems.STEEL_PICKAXE.get())
        ;

        // Shovels
        this.tag(ItemTags.create(ResourceLocation.parse("c:tools/shovels")))
                .add(ModItems.STEEL_SHOVEL.get())
                .add(ModItems.COPPER_SHOVEL.get())
        ;

        // Swords
        this.tag(ItemTags.create(ResourceLocation.parse("c:tools/swords")))
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
                        ModItems.IRON_UPGRADE_ARROW.get(),
                        ModItems.STEEL_UPGRADE_ARROW.get(),
                        ModItems.DIAMOND_UPGRADE_ARROW.get()
                );
        this.tag(ModTags.Items.HOT_ITEMS)
                .add(
                        Items.LAVA_BUCKET
                );
        this.tag(ModTags.Items.TOOL_CAST)
                .add(
                        ModItems.CLAY_TOOL_CAST.get(),
                        ModItems.NETHER_TOOL_CAST.get()
                );
    }
}


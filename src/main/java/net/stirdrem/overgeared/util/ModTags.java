package net.stirdrem.overgeared.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.stirdrem.overgeared.OvergearedMod;

public class ModTags {
    public static class Blocks {
        /*public static final TagKey<Block> TONGS = tag("tongs");*/
        public static final TagKey<Block> NEEDS_STEEL_TOOL = tag("needs_steel_tool");
        public static final TagKey<Block> NEEDS_COPPER_TOOL = tag("needs_copper_tool");
        public static final TagKey<Block> SMITHING = tag("smithing");
        public static final TagKey<Block> SMITHING_ANVIL = tag("smithing_anvil");
        public static final TagKey<Block> ANVIL_BASES = tag("stone_anvil_bases");
        public static final TagKey<Block> GRINDSTONES = tag("grindstones");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.tryBuild(OvergearedMod.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> TONGS = tag("tongs");
        public static final TagKey<Item> TOOL_PARTS = tag("tool_parts");
        public static final TagKey<Item> HEATABLE_METALS = tag("heatable_metals");
        public static final TagKey<Item> HEATED_METALS = tag("heated_metals");
        public static final TagKey<Item> HOT_ITEMS = tag("hot_items");
        public static final TagKey<Item> SMITHING_HAMMERS = tag("smithing_hammers");
        public static final TagKey<Item> GRINDABLE = tag("grindable");
        public static final TagKey<Item> GRINDED = tag("grinded");


        private static TagKey<Item> tag(String name) {
            return ItemTags.create(ResourceLocation.tryBuild(OvergearedMod.MOD_ID, name));
        }


    }
}

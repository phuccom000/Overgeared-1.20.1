package net.stirdrem.overgearedmod.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.stirdrem.overgearedmod.OvergearedMod;

public class ModTags {
    public static class Blocks {
        /*public static final TagKey<Block> TONGS = tag("tongs");*/

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.tryBuild(OvergearedMod.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> TONGS = tag("tongs");
        public static final TagKey<Item> TOOL_PARTS = tag("tool_parts");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(ResourceLocation.tryBuild(OvergearedMod.MOD_ID, name));
        }

        
    }
}

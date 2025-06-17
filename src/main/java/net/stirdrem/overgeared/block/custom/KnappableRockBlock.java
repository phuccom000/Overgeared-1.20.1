package net.stirdrem.overgeared.block.custom;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class KnappableRockBlock extends Block {
    public KnappableRockBlock(Properties pProperties) {
        super(pProperties
                .instabreak() // Easily breakable
                .sound(SoundType.STONE));
    }
}

package net.stirdrem.overgeared.component.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.stirdrem.overgeared.ForgingQuality;

public class ForgingQualityComponents {
    // Codec for serialization/deserialization (NBT/data saving)
    public static final Codec<ForgingQuality> CODEC = Codec.STRING.xmap(
            ForgingQuality::fromString,
            ForgingQuality::getDisplayName
    );

    // StreamCodec for network synchronization
    public static final StreamCodec<ByteBuf, ForgingQuality> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(
            ForgingQuality::fromString,
            ForgingQuality::getDisplayName
    );
}
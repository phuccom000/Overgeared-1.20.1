package net.stirdrem.overgeared.compoment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ForgingQualityValue(String qualityDisplayName) {
    public static final Codec<ForgingQualityValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("forging_quality").forGetter(ForgingQualityValue::qualityDisplayName)
            ).apply(instance, ForgingQualityValue::new)
    );

    public static final StreamCodec<ByteBuf, ForgingQualityValue> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32767), ForgingQualityValue::qualityDisplayName,
            ForgingQualityValue::new
    );
}
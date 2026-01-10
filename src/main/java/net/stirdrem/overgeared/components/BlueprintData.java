package net.stirdrem.overgeared.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.stirdrem.overgeared.BlueprintQuality;

public record BlueprintData(
        String quality,
        String toolType,
        int uses,
        int usesToLevel,
        boolean required
) {
    private static final String DEFAULT_TOOL_TYPE = "sword";

    public static final Codec<BlueprintData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("quality", BlueprintQuality.POOR.getId()).forGetter(BlueprintData::quality),
                    Codec.STRING.optionalFieldOf("tool_type", DEFAULT_TOOL_TYPE).forGetter(BlueprintData::toolType),
                    Codec.INT.optionalFieldOf("uses", 0).forGetter(BlueprintData::uses),
                    Codec.INT.optionalFieldOf("uses_to_level", BlueprintQuality.POOR.getUse()).forGetter(BlueprintData::usesToLevel),
                    Codec.BOOL.optionalFieldOf("required", false).forGetter(BlueprintData::required)
            ).apply(instance, BlueprintData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BlueprintData::quality,
            ByteBufCodecs.STRING_UTF8, BlueprintData::toolType,
            ByteBufCodecs.VAR_INT, BlueprintData::uses,
            ByteBufCodecs.VAR_INT, BlueprintData::usesToLevel,
            ByteBufCodecs.BOOL, BlueprintData::required,
            BlueprintData::new
    );

    /**
     * Creates a default BlueprintData instance.
     */
    public static BlueprintData createDefault() {
        return new BlueprintData(
                BlueprintQuality.POOR.getId(),
                DEFAULT_TOOL_TYPE,
                0,
                BlueprintQuality.POOR.getUse(),
                false
        );
    }

    public BlueprintData withQuality(String quality) {
        return new BlueprintData(quality, this.toolType, this.uses, this.usesToLevel, this.required);
    }

    public BlueprintData withToolType(String toolType) {
        return new BlueprintData(this.quality, toolType, this.uses, this.usesToLevel, this.required);
    }

    public BlueprintData withUses(int uses) {
        return new BlueprintData(this.quality, this.toolType, uses, this.usesToLevel, this.required);
    }

    public BlueprintData withUsesToLevel(int usesToLevel) {
        return new BlueprintData(this.quality, this.toolType, this.uses, usesToLevel, this.required);
    }

    public BlueprintData withRequired(boolean required) {
        return new BlueprintData(this.quality, this.toolType, this.uses, this.usesToLevel, required);
    }

    public BlueprintQuality getQualityEnum() {
        return BlueprintQuality.fromString(quality);
    }
}

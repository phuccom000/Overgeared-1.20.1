package net.stirdrem.overgeared;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum ForgingQuality implements StringRepresentable {
    POOR("poor"),
    WELL("well"),
    EXPERT("expert"),
    PERFECT("perfect"),
    MASTER("master"),
    NONE("none");

    private final String displayName;

    // Codec for persistence (saves as string)
    public static final Codec<ForgingQuality> CODEC = Codec.STRING.xmap(
            ForgingQuality::fromString,
            ForgingQuality::getDisplayName
    );

    // StreamCodec for network synchronization
    public static final StreamCodec<ByteBuf, ForgingQuality> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(
            ForgingQuality::fromString,
            ForgingQuality::getDisplayName
    );

    ForgingQuality(String displayName) {
        this.displayName = displayName;
    }

    public static ForgingQuality fromString(String quality) {
        for (ForgingQuality q : values()) {
            if (q.displayName.equalsIgnoreCase(quality)) return q;
        }
        return POOR; // fallback
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSerializedName() {
        return displayName;
    }

    public ForgingQuality getLowerQuality() {
        // NONE should never downgrade to MASTER
        if (this == NONE) {
            return NONE;
        }

        ForgingQuality[] values = values();
        int index = this.ordinal();
        return index > 0 ? values[index - 1] : this; // POOR stays POOR
    }
}


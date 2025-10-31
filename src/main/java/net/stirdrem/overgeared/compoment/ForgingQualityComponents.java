package net.stirdrem.overgeared.compoment; // Adjust to your package

import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
 // Note: Use DataComponents for cleaner typing
import net.minecraft.core.registries.Registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;

// Single class handling the ForgingQuality data component
public class ForgingQualityComponents {

    // Deferred register for DataComponentTypes (using DataComponents wrapper for convenience)
    public static final DeferredRegister.DataComponents REGISTRAR = (DeferredRegister.DataComponents) DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, OvergearedMod.MOD_ID);

    // The value holder (record for the string display name)
    public record ForgingQualityValue(String qualityDisplayName) {
        // Codec for persistence (saving/loading)
        public static final Codec<ForgingQualityValue> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("forging_quality").forGetter(ForgingQualityValue::qualityDisplayName)
                ).apply(instance, ForgingQualityValue::new)
        );

        // StreamCodec for networking (multiplayer sync)
        public static final StreamCodec<ByteBuf, ForgingQualityValue> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.stringUtf8(256), ForgingQualityValue::qualityDisplayName,
                ForgingQualityValue::new
        );
    }

    // The registered DataComponentType (using DeferredHolder for type-safe access)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ForgingQualityValue>> FORGING_QUALITY =
            REGISTRAR.register("forging_quality", () ->
                    DataComponentType.<ForgingQualityValue>builder()
                            .persistent(ForgingQualityValue.CODEC)
                            .networkSynchronized(ForgingQualityValue.STREAM_CODEC)
                            .build()
            );

    // Static helper to apply the component (for convenience)
    public static void applyPoorQuality(net.minecraft.world.item.ItemStack stack) {
        stack.set(FORGING_QUALITY.get(), new ForgingQualityValue(ForgingQuality.POOR.getDisplayName()));
    }

    // Static helper to retrieve the value (returns null if absent)
    public static ForgingQualityValue getQuality(net.minecraft.world.item.ItemStack stack) {
        return stack.get(FORGING_QUALITY.get());
    }
}


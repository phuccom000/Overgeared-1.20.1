package net.stirdrem.overgeared.components;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.function.Supplier;

public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, OvergearedMod.MOD_ID);

    public static final Supplier<DataComponentType<Boolean>> HEATED_COMPONENT = COMPONENTS.register(
            "heated",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static final Supplier<DataComponentType<Boolean>> LINGERING_STATUS =
            COMPONENTS.register("lingering_status", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static final Supplier<DataComponentType<CastData>> CAST_DATA =
            COMPONENTS.register("cast_data", () -> DataComponentType.<CastData>builder()
                    .persistent(CastData.CODEC)
                    .networkSynchronized(CastData.STREAM_CODEC)
                    .build());

    public static final Supplier<DataComponentType<BlueprintData>> BLUEPRINT_DATA =
            COMPONENTS.register("blueprint_data", () -> DataComponentType.<BlueprintData>builder()
                    .persistent(BlueprintData.CODEC)
                    .networkSynchronized(BlueprintData.STREAM_CODEC)
                    .build());

    // Forging quality stored on crafted items (sword, armor, etc.)
    public static final Supplier<DataComponentType<String>> FORGING_QUALITY =
            COMPONENTS.register("forging_quality", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    // Creator name for items
    public static final Supplier<DataComponentType<String>> CREATOR =
            COMPONENTS.register("creator", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    // Whether item needs polishing
    public static final Supplier<DataComponentType<Boolean>> POLISHED =
            COMPONENTS.register("polished", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}

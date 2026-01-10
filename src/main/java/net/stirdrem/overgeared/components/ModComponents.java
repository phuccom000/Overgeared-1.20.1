package net.stirdrem.overgeared.components;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.stirdrem.overgeared.ForgingQuality;
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

    // Forging quality stored on crafted items (sword, armor, etc.) - stored as enum
    public static final Supplier<DataComponentType<ForgingQuality>> FORGING_QUALITY =
            COMPONENTS.register("forging_quality", () -> DataComponentType.<ForgingQuality>builder()
                    .persistent(ForgingQuality.CODEC)
                    .networkSynchronized(ForgingQuality.STREAM_CODEC)
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

    // Track how many times a potion has been used for arrow tipping
    public static final Supplier<DataComponentType<Integer>> TIPPED_USES =
            COMPONENTS.register("tipped_uses", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    // Marks an item as a failed crafting result (used for JEI display)
    public static final Supplier<DataComponentType<Boolean>> FAILED_RESULT =
            COMPONENTS.register("failed_result", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    // Track when an item was heated (game tick when heated)
    public static final Supplier<DataComponentType<Long>> HEATED_TIME =
            COMPONENTS.register("heated_time", () -> DataComponentType.<Long>builder()
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG)
                    .build());

    // Track how many times an item has been ground (reduces max durability)
    public static final Supplier<DataComponentType<Integer>> REDUCED_GRIND_COUNT =
            COMPONENTS.register("reduced_grind_count", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}

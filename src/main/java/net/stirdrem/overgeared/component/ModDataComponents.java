package net.stirdrem.overgeared.component;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.component.data.BlueprintComponents;
import net.stirdrem.overgeared.component.data.ForgingQualityComponents;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents REGISTER =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, OvergearedMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlueprintComponents>> BLUEPRINT_DATA =
            REGISTER.register("blueprint_data",
                    () -> DataComponentType.<BlueprintComponents>builder().persistent(BlueprintComponents.CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ForgingQuality>> FORGING_QUALITY =
            REGISTER.registerComponentType(
                    "forging_quality",
                    builder -> builder
                            .persistent(ForgingQualityComponents.CODEC)
                            .networkSynchronized(ForgingQualityComponents.STREAM_CODEC)
            );
    // Register to mod bus in your mod constructor
    // COMPONENT_TYPES.register(modBus);
}
package net.stirdrem.overgeared.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, OvergearedMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<LingeringArrowEntity>> LINGERING_ARROW =
            ENTITY_TYPES.register("lingering_arrow", () ->
                    EntityType.Builder.<LingeringArrowEntity>of(LingeringArrowEntity::new, MobCategory.MISC)
                            .setShouldReceiveVelocityUpdates(true)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("lingering_arrow")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<UpgradeArrowEntity>> UPGRADE_ARROW =
            ENTITY_TYPES.register("upgrade_arrow", () ->
                    EntityType.Builder.<UpgradeArrowEntity>of(UpgradeArrowEntity::new, MobCategory.MISC)
                            .setShouldReceiveVelocityUpdates(true)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("upgrade_arrow")
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

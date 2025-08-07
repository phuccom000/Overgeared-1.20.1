package net.stirdrem.overgeared.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.entity.custom.LingeringArrowEntity;

import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, OvergearedMod.MOD_ID);

    public static final RegistryObject<EntityType<LingeringArrowEntity>> LINGERING_ARROW =
            ENTITY_TYPES.register("lingering_arrow", () ->
                    EntityType.Builder.<LingeringArrowEntity>of(LingeringArrowEntity::new, MobCategory.MISC)
                            .setShouldReceiveVelocityUpdates(true)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("lingering_arrow")
            );

  /*  public static final RegistryObject<EntityType<ModularArrowEntity>> MODULAR_ARROW =
            ENTITY_TYPES.register("modular_arrow", () ->
                    EntityType.Builder.<ModularArrowEntity>of(ModularArrowEntity::new, MobCategory.MISC)
                            .setShouldReceiveVelocityUpdates(true)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("modular_arrow")
            );*/

    public static final RegistryObject<EntityType<UpgradeArrowEntity>> UPGRADE_ARROW =
            ENTITY_TYPES.register("upgrade_arrow", () ->
                    EntityType.Builder.<UpgradeArrowEntity>of(UpgradeArrowEntity::new, MobCategory.MISC)
                            .setShouldReceiveVelocityUpdates(true)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("upgrade_arrow")
            );

    /*public static final RegistryObject<EntityType<Arrow>> ARROW =
            ENTITY_TYPES.register("arrow", () ->
                    EntityType.Builder.<Arrow>of(Arrow::new, MobCategory.MISC)
                            .setShouldReceiveVelocityUpdates(true)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("arrow")
            );*/

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

package net.stirdrem.overgeared.event;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.OvergearedMod;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, OvergearedMod.MOD_ID);

    /*public static final RegistryObject<Attribute> ATTACK_REACH =
            ATTRIBUTES.register("attack_reach",
                    () -> new RangedAttribute("attribute." + OvergearedMod.MOD_ID + ".attack_reach",
                            5.0D, 0.0D, 1024.0D)
                            .setSyncable(true));*/

    public static void register(IEventBus bus) {
        ATTRIBUTES.register(bus);
    }
}




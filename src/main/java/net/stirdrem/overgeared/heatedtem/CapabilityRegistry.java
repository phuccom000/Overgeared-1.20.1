package net.stirdrem.overgeared.heatedtem;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilityRegistry {
    public static Capability<HeatedItemCapability> HEATED_ITEM;

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(HeatedItemCapability.class);
    }
}

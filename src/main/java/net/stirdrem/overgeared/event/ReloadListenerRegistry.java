package net.stirdrem.overgeared.event;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgeared.datapack.*;
import net.stirdrem.overgeared.item.ToolTypeRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReloadListenerRegistry {

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent event) {
        event.addListener(new BlueprintTooltypesReloadListener());
        event.addListener(new GrindingBlacklistReloadListener());
        event.addListener(new DurabilityBlacklistReloadListener());
        event.addListener(new CastingToolTypesReloadListener());
        event.addListener(new MaterialSettingsReloadListener());
    }
}

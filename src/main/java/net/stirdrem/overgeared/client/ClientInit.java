package net.stirdrem.overgeared.client;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.stirdrem.overgeared.OvergearedMod;

public class ClientInit {

    public static void init() {
        ModList.get().getModContainerById(OvergearedMod.MOD_ID).orElseThrow()
                .registerExtensionPoint(
                        IConfigScreenFactory.class,
                        (container, parent) -> new OvergearedConfigScreen(parent));
    }
}

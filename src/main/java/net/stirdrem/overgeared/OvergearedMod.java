package net.stirdrem.overgeared;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.stirdrem.overgeared.items.ModItems;
import org.slf4j.Logger;

@Mod(OvergearedMod.MOD_ID)
public final class OvergearedMod {
    public static final String MOD_ID = "overgeared";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OvergearedMod(IEventBus modEventBus) {
        ModItems.register(modEventBus);
    }
}

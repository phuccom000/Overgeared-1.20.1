package net.stirdrem.overgeared;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ModRecipes;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.screen.ModMenuTypes;
import org.slf4j.Logger;

@Mod(OvergearedMod.MOD_ID)
public final class OvergearedMod {
    public static final String MOD_ID = "overgeared";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OvergearedMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModRecipes.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, ServerConfig.SERVER_CONFIG);
        //ModLoadingContext.get().registerExtensionPoint(ModConfig.Type.CLIENT, ClientConfig.CLIENT_CONFIG);
    }
}

package net.stirdrem.overgeared.platform;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.stirdrem.overgeared.platform.services.PlatformHelper;

/**
 * The platform service implementation for Forge.
 */
public class ForgePlatformHelper implements PlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

}
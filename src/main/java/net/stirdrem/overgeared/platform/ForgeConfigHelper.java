package net.stirdrem.overgeared.platform;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.platform.services.ConfigHelper;

import java.nio.file.Path;

/**
 * The config service implementation for Forge.
 */
public class ForgeConfigHelper implements ConfigHelper {

    private static final ForgeConfigSpec SERVER_CONFIG;

    private static final ForgeConfigSpec.IntValue BREAK_TEMPERATURE;
    private static final ForgeConfigSpec.IntValue DURABILITY;
    private static final ForgeConfigSpec.BooleanValue FISH_OBTAINING_ENABLED;
    private static final ForgeConfigSpec.BooleanValue MILKING_ENABLED;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Balance Options");

        BREAK_TEMPERATURE = builder
                .comment(BREAK_TEMPERATURE_DESCRIPTION)
                .defineInRange("crackingTemperature", BREAK_TEMPERATURE_DEFAULT, BREAK_TEMPERATURE_MIN, BREAK_TEMPERATURE_MAX);

        DURABILITY = builder
                .comment(DURABILITY_DESCRIPTION)
                .defineInRange("durability", DURABILITY_DEFAULT, DURABILITY_MIN, DURABILITY_MAX);

        FISH_OBTAINING_ENABLED = builder
                .comment(FISH_OBTAINING_ENABLED_DESCRIPTION)
                .define("fishObtainingEnabled", FISH_OBTAINING_ENABLED_DEFAULT);

        MILKING_ENABLED = builder
                .comment(MILKING_ENABLED_DESCRIPTION)
                .define("milkingEnabled", MILKING_ENABLED_DEFAULT);

        builder.pop();

        SERVER_CONFIG = builder.build();
    }

    @Override
    public void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
        Path path = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve(OvergearedMod.MOD_ID + "-server.toml");
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        SERVER_CONFIG.setConfig(configData);
    }

    @Override
    public int getBreakTemperature() {
        try {
            return BREAK_TEMPERATURE.get();
        } catch (IllegalStateException ex) {
            return BREAK_TEMPERATURE_DEFAULT;
        }
    }

    @Override
    public int getBurningTemperature() {
        return 0;
    }

    @Override
    public int getDurability() {
        try {
            return DURABILITY.get();
        } catch (IllegalStateException ex) {
            return DURABILITY_DEFAULT;
        }
    }

    @Override
    public boolean isFishObtainingEnabled() {
        try {
            return FISH_OBTAINING_ENABLED.get();
        } catch (IllegalStateException ex) {
            return FISH_OBTAINING_ENABLED_DEFAULT;
        }
    }

    @Override
    public int getFreezingTemperature() {
        return 0;
    }

    @Override
    public boolean isMilkingEnabled() {
        try {
            return MILKING_ENABLED.get();
        } catch (IllegalStateException ex) {
            return MILKING_ENABLED_DEFAULT;
        }
    }

}

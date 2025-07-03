package net.stirdrem.overgeared.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public class ClientConfig {

    public static final ForgeConfigSpec CLIENT_CONFIG;


    public static final ForgeConfigSpec.IntValue MINIGAME_OVERLAY_HEIGHT;
    public static final ForgeConfigSpec.BooleanValue PLAYER_HUD_TOGGLE;


    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("Minigame Config");

        MINIGAME_OVERLAY_HEIGHT = builder
                .comment("Vertical position of the minigame overlay")
                .defineInRange("overlayHeight", 55, -10000, 10000);
        PLAYER_HUD_TOGGLE = builder
                .comment("If player's vanilla HUD is visible during minigame. May not work with modded HUD.")
                .define("HUDVisible", true);

        builder.pop();


        CLIENT_CONFIG = builder.build();
    }

    public static final void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        spec.setConfig(configData);
    }

}

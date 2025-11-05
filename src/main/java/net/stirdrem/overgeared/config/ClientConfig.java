package net.stirdrem.overgeared.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec CLIENT_CONFIG;

    public static final ModConfigSpec.IntValue MINIGAME_OVERLAY_HEIGHT;
    public static final ModConfigSpec.BooleanValue PLAYER_HUD_TOGGLE;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
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
}
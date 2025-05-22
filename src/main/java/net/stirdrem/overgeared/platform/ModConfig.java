package net.stirdrem.overgeared.platform;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Define a BooleanValue
    public static final ForgeConfigSpec.BooleanValue ENABLE_MILKING;

    static {
        BUILDER.push("Bucket Settings");

        // Assign the BooleanValue (default: true)
        ENABLE_MILKING = BUILDER
                .comment("Whether milking with buckets is enabled")
                .define("enableMilking", true); // Creates a BooleanValue

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
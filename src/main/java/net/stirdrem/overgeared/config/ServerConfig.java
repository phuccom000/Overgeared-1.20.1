package net.stirdrem.overgeared.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public class ServerConfig {

    public static final int DEFAULT_WOODEN_BUCKET_BREAK_TEMPERATURE = 1000;

    public static ForgeConfigSpec SERVER_CONFIG;

    public static final ForgeConfigSpec.IntValue WOODEN_BUCKET_BREAK_TEMPERATURE;
    public static final ForgeConfigSpec.BooleanValue MILKING_ENABLED;
    public static final ForgeConfigSpec.BooleanValue FISH_OBTAINING_ENABLED;
    public static final ForgeConfigSpec.IntValue WOODEN_BUCKET_DURABILITY;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Balance Options");

        WOODEN_BUCKET_BREAK_TEMPERATURE = builder
                .comment("Minimum temperature of fluid at which the Ceramic Bucket breaks when emptied. (-1 means that bucket never breaks caused by high fluid temperature)")
                .defineInRange("ceramicBucketBreakTemperature", DEFAULT_WOODEN_BUCKET_BREAK_TEMPERATURE, 0, 10000);

        MILKING_ENABLED = builder
                .comment("Whether or not milking entities with a Ceramic Bucket should be enabled.")
                .define("milkingEnabled", true);

        FISH_OBTAINING_ENABLED = builder
                .comment("Whether or not obtaining fish with a Ceramic Bucket should be enabled.")
                .define("fishObtainingEnabled", true);

        WOODEN_BUCKET_DURABILITY = builder
                .comment("Defines the maximum durability of a Ceramic Bucket. (0 deactivates the durability)")
                .defineInRange("durability", 0, 0, 10000);

        builder.pop();

        SERVER_CONFIG = builder.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        spec.setConfig(configData);
    }

}

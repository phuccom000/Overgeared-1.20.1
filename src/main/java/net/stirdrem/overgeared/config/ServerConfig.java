package net.stirdrem.overgeared.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import io.netty.util.Attribute;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;

public class ServerConfig {

    public static final int DEFAULT_WOODEN_BUCKET_BREAK_TEMPERATURE = 1000;
    public static final ForgeConfigSpec.IntValue MAX_ANVIL_DISTANCE;

    public static final ForgeConfigSpec SERVER_CONFIG;

    // Weapon bonuses
    public static final ForgeConfigSpec.DoubleValue PERFECT_WEAPON_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue EXPERT_WEAPON_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue WELL_WEAPON_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue POOR_WEAPON_DAMAGE;

    public static final ForgeConfigSpec.DoubleValue PERFECT_WEAPON_SPEED;
    public static final ForgeConfigSpec.DoubleValue EXPERT_WEAPON_SPEED;
    public static final ForgeConfigSpec.DoubleValue WELL_WEAPON_SPEED;
    public static final ForgeConfigSpec.DoubleValue POOR_WEAPON_SPEED;

    // Armor bonuses
    public static final ForgeConfigSpec.DoubleValue PERFECT_ARMOR_BONUS;
    public static final ForgeConfigSpec.DoubleValue EXPERT_ARMOR_BONUS;
    public static final ForgeConfigSpec.DoubleValue WELL_ARMOR_BONUS;
    public static final ForgeConfigSpec.DoubleValue POOR_ARMOR_BONUS;

    public static final ForgeConfigSpec.DoubleValue DEFAULT_ARROW_SPEED;
    public static final ForgeConfigSpec.DoubleValue DEFAULT_ARROW_SPEED_INCREASE;
    public static final ForgeConfigSpec.DoubleValue MAX_SPEED;
    public static final ForgeConfigSpec.IntValue ZONE_STARTING_SIZE;
    public static final ForgeConfigSpec.IntValue MIN_PERFECT_ZONE;
    public static final ForgeConfigSpec.DoubleValue ZONE_SHRINK_FACTOR;
    public static final ForgeConfigSpec.IntValue MINIGAME_TIMEOUT_TICKS;
    public static final ForgeConfigSpec.DoubleValue BASE_DURABILITY_MULTIPLIER;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Balance Options");

        BASE_DURABILITY_MULTIPLIER = builder
                .comment("Defines the base durability multiplier of all items that has durability.")
                .defineInRange("durability", 1f, 0, 10000);

        builder.pop();

        builder.push("Weapon Damage Bonuses");

        PERFECT_WEAPON_DAMAGE = builder
                .comment("Damage bonus for perfect quality weapons")
                .defineInRange("perfectWeaponDamage", 2.0, -10.0, 10.0);

        EXPERT_WEAPON_DAMAGE = builder
                .comment("Damage bonus for expert quality weapons")
                .defineInRange("expertWeaponDamage", 1.5, -10.0, 10.0);

        WELL_WEAPON_DAMAGE = builder
                .comment("Damage bonus for well-made quality weapons")
                .defineInRange("wellWeaponDamage", 0.0, -10.0, 10.0);

        POOR_WEAPON_DAMAGE = builder
                .comment("Damage penalty for poor quality weapons")
                .defineInRange("poorWeaponDamage", -1.0, -10.0, 10.0);
        builder.pop();

        builder.push("Weapon Speed Bonuses");

        PERFECT_WEAPON_SPEED = builder
                .comment("Attack speed bonus for perfect quality weapons")
                .defineInRange("perfectWeaponSpeed", 0.5, -2.0, 2.0);

        EXPERT_WEAPON_SPEED = builder
                .comment("Attack speed bonus for expert quality weapons")
                .defineInRange("expertWeaponSpeed", 0.25, -2.0, 2.0);

        WELL_WEAPON_SPEED = builder
                .comment("Attack speed bonus for well-made quality weapons")
                .defineInRange("wellWeaponSpeed", 0.0, -2.0, 2.0);

        POOR_WEAPON_SPEED = builder
                .comment("Attack speed penalty for poor quality weapons")
                .defineInRange("poorWeaponSpeed", -0.5, -2.0, 2.0);

        builder.pop();

        builder.push("Armor Bonuses");

        PERFECT_ARMOR_BONUS = builder
                .comment("Armor bonus for perfect quality armor")
                .defineInRange("perfectArmorBonus", 1.5, -5.0, 5.0);

        EXPERT_ARMOR_BONUS = builder
                .comment("Armor bonus for expert quality armor")
                .defineInRange("expertArmorBonus", 1.0, -5.0, 5.0);

        WELL_ARMOR_BONUS = builder
                .comment("Armor bonus for well-made quality armor")
                .defineInRange("wellArmorBonus", 0.0, -5.0, 5.0);

        POOR_ARMOR_BONUS = builder
                .comment("Armor penalty for poor quality armor")
                .defineInRange("poorArmorBonus", -1.0, -5.0, 5.0);

        builder.pop();

        builder.push("Minigame Config");

        DEFAULT_ARROW_SPEED = builder
                .comment("Default arrow speed for the forging minigame")
                .defineInRange("arrowSpeed", 2.0, -5.0, 5.0);

        DEFAULT_ARROW_SPEED_INCREASE = builder
                .comment("Default arrow speed increase for the forging minigame")
                .defineInRange("arrowSpeedIncrease", 0.8, -5.0, 5.0);

        MAX_SPEED = builder
                .comment("Maximum arrow speed for the forging minigame")
                .defineInRange("maxArrowSpeed", 5, -10.0, 10.0);

        ZONE_STARTING_SIZE = builder
                .comment("Zone starting size for the forging minigame,  in percentage")
                .defineInRange("zoneStartingSize", 20, 0, 100);

        ZONE_SHRINK_FACTOR = builder
                .comment("Zone shrink factor for the forging minigame, default value here means that it shrinks to 70% of its original size")
                .defineInRange("zoneShrinkFactor", 0.7, 0, 1);

        MAX_ANVIL_DISTANCE = builder
                .comment("Maximum distance you can go from your Smithing Anvil before minigame reset")
                .defineInRange("maxAnvilDistance", 100, 0, 1000);

        MIN_PERFECT_ZONE = builder
                .comment("Smallest perfect zone it can become")
                .defineInRange("minPerfectZone", 3, 0, 100);

        MINIGAME_TIMEOUT_TICKS = builder
                .comment("Minigame resets after a certain amount of ticks")
                .defineInRange("minigameTimeout", 6000, 0, 36000);

        builder.pop();

        SERVER_CONFIG = builder.build();
    }

    public static final void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        spec.setConfig(configData);
    }

}

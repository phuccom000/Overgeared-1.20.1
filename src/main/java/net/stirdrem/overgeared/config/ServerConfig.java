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
    public static final ForgeConfigSpec.DoubleValue MASTER_WEAPON_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue PERFECT_WEAPON_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue EXPERT_WEAPON_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue WELL_WEAPON_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue POOR_WEAPON_DAMAGE;

    public static final ForgeConfigSpec.DoubleValue MASTER_WEAPON_SPEED;
    public static final ForgeConfigSpec.DoubleValue PERFECT_WEAPON_SPEED;
    public static final ForgeConfigSpec.DoubleValue EXPERT_WEAPON_SPEED;
    public static final ForgeConfigSpec.DoubleValue WELL_WEAPON_SPEED;
    public static final ForgeConfigSpec.DoubleValue POOR_WEAPON_SPEED;

    // Armor bonuses
    public static final ForgeConfigSpec.DoubleValue MASTER_ARMOR_BONUS;
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
    public static final ForgeConfigSpec.DoubleValue ROCK_DROPPING_CHANCE;
    public static final ForgeConfigSpec.DoubleValue FLINT_BREAKING_CHANCE;
    public static final ForgeConfigSpec.DoubleValue DURABILITY_REDUCE_PER_GRIND;
    public static final ForgeConfigSpec.DoubleValue DAMAGE_RESTORE_PER_GRIND;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MINIGAME;
    public static final ForgeConfigSpec.DoubleValue MASTER_QUALITY_CHANCE;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("Minigame Config");

        DEFAULT_ARROW_SPEED = builder
                .comment("Default arrow speed for the forging minigame")
                .defineInRange("arrowSpeed", 2.0, -5.0, 5.0);

        ENABLE_MINIGAME = builder
                .comment("Toggle for the forging minigame. " +
                        "You cannot craft with unpolished tool heads and the quality modifier is disabled if set to false.")
                .define("enableMinigame", true);

        DEFAULT_ARROW_SPEED_INCREASE = builder
                .comment("Default arrow speed increase for the forging minigame")
                .defineInRange("arrowSpeedIncrease", 0.8, -5.0, 5.0);

        MASTER_QUALITY_CHANCE = builder
                .comment("How likely it is for the player to get Masterfully Forged. Set to 0 to disable it.")
                .defineInRange("masterQualityChance", 0.05, 0, 1);

        MAX_SPEED = builder
                .comment("Maximum arrow speed for the forging minigame")
                .defineInRange("maxArrowSpeed", 5, -10.0, 10.0);

        ZONE_STARTING_SIZE = builder
                .comment("Zone starting size for the forging minigame,  in chance")
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
        builder.push("Balance Options");

        BASE_DURABILITY_MULTIPLIER = builder
                .comment("Defines the base durability multiplier of all items that has durability.")
                .defineInRange("durability", 1f, 0, 10000);

        builder.pop();

        builder.push("Weapon Damage Bonuses");

        MASTER_WEAPON_DAMAGE = builder
                .comment("Damage bonus for master quality weapons")
                .defineInRange("masterWeaponDamage", 3.0, -10.0, 10.0);

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

        MASTER_WEAPON_SPEED = builder
                .comment("Attack speed bonus for master quality weapons")
                .defineInRange("masterWeaponSpeed", 1, -2.0, 2.0);

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

        MASTER_ARMOR_BONUS = builder
                .comment("Armor bonus for master quality armor")
                .defineInRange("masterArmorBonus", 2, -5.0, 5.0);

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

        builder.push("Knapping Config");

        ROCK_DROPPING_CHANCE = builder
                .comment("How likely it is for rock to drop from flint carving stone")
                .defineInRange("rockDroppingChance", 0.1, 0, 1);
        FLINT_BREAKING_CHANCE = builder
                .comment("How likely it is for flint to break when carving stone")
                .defineInRange("flintBreakingChance", 0.1, 0, 1);

        builder.pop();

        builder.push("Grinding Config");

        DURABILITY_REDUCE_PER_GRIND = builder
                .comment("How much the item durability reduce per grindstone use")
                .defineInRange("durabilityReduce", 0.05, 0, 1);
        DAMAGE_RESTORE_PER_GRIND = builder
                .comment("How much the item's durability restore per grindstone use. " +
                        "Should be larger than durability reduce per grind to avoid durability going to the negative")
                .defineInRange("damageRestore", 0.1, 0, 1);
        builder.pop();


        SERVER_CONFIG = builder.build();
    }

    public static final void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        spec.setConfig(configData);
    }

}

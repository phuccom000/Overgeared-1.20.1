package net.stirdrem.overgeared.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ServerConfig {

    public static final int DEFAULT_WOODEN_BUCKET_BREAK_TEMPERATURE = 1000;
    public static final ForgeConfigSpec SERVER_CONFIG;

    // --- Core Anvil Configs ---
    public static final ForgeConfigSpec.IntValue MAX_ANVIL_DISTANCE;
    public static final ForgeConfigSpec.IntValue STONE_ANVIL_MAX_USES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_STONE_TO_ANVIL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ANVIL_TO_SMITHING;

    // --- Heated Items ---
    public static final ForgeConfigSpec.IntValue HEATED_ITEM_COOLDOWN_TICKS;

    // --- Arrow Settings ---
    public static final ForgeConfigSpec.BooleanValue UPGRADE_ARROW_POTION_TOGGLE;
    public static final ForgeConfigSpec.DoubleValue DEFAULT_ARROW_SPEED;
    public static final ForgeConfigSpec.DoubleValue DEFAULT_ARROW_SPEED_INCREASE;
    public static final ForgeConfigSpec.DoubleValue MAX_ARROW_SPEED;
    public static final ForgeConfigSpec.IntValue MAX_POTION_TIPPING_USE;

    // --- Minigame Settings ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_MINIGAME;
    public static final ForgeConfigSpec.DoubleValue MASTER_QUALITY_CHANCE;
    public static final ForgeConfigSpec.DoubleValue MASTER_FROM_INGREDIENT_CHANCE;
    public static final ForgeConfigSpec.IntValue MINIGAME_TIMEOUT_TICKS;
    public static final ForgeConfigSpec.IntValue ZONE_STARTING_SIZE;
    public static final ForgeConfigSpec.IntValue MIN_PERFECT_ZONE;
    public static final ForgeConfigSpec.DoubleValue ZONE_SHRINK_FACTOR;
    public static final ForgeConfigSpec.DoubleValue PERFECT_QUALITY_SCORE;
    public static final ForgeConfigSpec.DoubleValue EXPERT_QUALITY_SCORE;
    public static final ForgeConfigSpec.DoubleValue WELL_QUALITY_SCORE;

    // --- Durability & Grinding ---
    public static final ForgeConfigSpec.DoubleValue BASE_DURABILITY_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BASE_DURABILITY_BLACKLIST;
    public static final ForgeConfigSpec.BooleanValue GRINDING_RESTORE_DURABILITY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> GRINDING_BLACKLIST;
    public static final ForgeConfigSpec.DoubleValue DURABILITY_REDUCE_PER_GRIND;
    public static final ForgeConfigSpec.DoubleValue DAMAGE_RESTORE_PER_GRIND;

    // --- Quality & Failure Chances ---
    public static final ForgeConfigSpec.DoubleValue FAIL_ON_WELL_QUALITY_CHANCE;
    public static final ForgeConfigSpec.DoubleValue FAIL_ON_EXPERT_QUALITY_CHANCE;

    // --- Tool/Blueprint Settings ---
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> AVAILABLE_TOOL_TYPES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_TOOL_TYPES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HIDDEN_TOOL_TYPES;
    public static final ForgeConfigSpec.IntValue MASTER_MAX_USE;
    public static final ForgeConfigSpec.IntValue PERFECT_MAX_USE;
    public static final ForgeConfigSpec.IntValue EXPERT_MAX_USE;
    public static final ForgeConfigSpec.IntValue WELL_MAX_USE;
    public static final ForgeConfigSpec.IntValue POOR_MAX_USE;

    // --- Weapon Bonuses ---
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

    // --- Armor Bonuses ---
    public static final ForgeConfigSpec.DoubleValue MASTER_ARMOR_BONUS;
    public static final ForgeConfigSpec.DoubleValue PERFECT_ARMOR_BONUS;
    public static final ForgeConfigSpec.DoubleValue EXPERT_ARMOR_BONUS;
    public static final ForgeConfigSpec.DoubleValue WELL_ARMOR_BONUS;
    public static final ForgeConfigSpec.DoubleValue POOR_ARMOR_BONUS;

    // --- Durability Bonuses ---
    public static final ForgeConfigSpec.DoubleValue MASTER_DURABILITY_BONUS;
    public static final ForgeConfigSpec.DoubleValue PERFECT_DURABILITY_BONUS;
    public static final ForgeConfigSpec.DoubleValue EXPERT_DURABILITY_BONUS;
    public static final ForgeConfigSpec.DoubleValue WELL_DURABILITY_BONUS;
    public static final ForgeConfigSpec.DoubleValue POOR_DURABILITY_BONUS;

    // --- Knapping Settings ---
    public static final ForgeConfigSpec.BooleanValue GET_ROCK_USING_FLINT;
    public static final ForgeConfigSpec.DoubleValue ROCK_DROPPING_CHANCE;
    public static final ForgeConfigSpec.DoubleValue FLINT_BREAKING_CHANCE;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // --- Anvil Conversion ---
        builder.push("Anvil Conversion");
        ENABLE_STONE_TO_ANVIL = builder.comment("Allow shift-right-clicking stone to convert into Stone Smithing Anvil").define("enableStoneToAnvil", true);
        ENABLE_ANVIL_TO_SMITHING = builder.comment("Allow shift-right-clicking vanilla anvil to convert into Smithing Anvil").define("enableAnvilToSmithing", true);
        builder.pop();

        builder.push("Stone Smithing Anvil");
        STONE_ANVIL_MAX_USES = builder.comment("Number of uses before the Stone Smithing Anvil breaks. Set to 0 to disable.").defineInRange("max_uses", 64, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Heated Items");
        HEATED_ITEM_COOLDOWN_TICKS = builder.comment("How many ticks before a heated item cools off in inventory (default: 1200 = 60s)").defineInRange("heatedItemCooldownTicks", 1200, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Arrow Settings");
        UPGRADE_ARROW_POTION_TOGGLE = builder.comment("Toggle for the ability to tip iron, steel, diamond arrows.").define("enableUpgradeArrowTipping", true);
        DEFAULT_ARROW_SPEED = builder.comment("Default arrow speed for the forging minigame").defineInRange("arrowSpeed", 2.0, -5.0, 5.0);
        DEFAULT_ARROW_SPEED_INCREASE = builder.comment("Default arrow speed increase for the forging minigame").defineInRange("arrowSpeedIncrease", 0.75, -5.0, 5.0);
        MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for the forging minigame").defineInRange("maxArrowSpeed", 5, 0, 10.0);
        MAX_POTION_TIPPING_USE = builder.comment("How many arrows can a bottle of potion tip before it's depleted").defineInRange("maxPotionTipping", 8, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Minigame Settings");
        ENABLE_MINIGAME = builder.comment("Toggle for the forging minigame").define("enableMinigame", true);
        MASTER_QUALITY_CHANCE = builder.comment("How likely it is for the player to get Masterfully Forged. Set to 0 to disable it.").defineInRange("masterQualityChance", 0.05, 0, 1);
        MASTER_FROM_INGREDIENT_CHANCE = builder.comment("Chance that using a Master-quality ingredient upgrades a Perfect hit to Master").defineInRange("masterFromIngredientChance", 0.5, 0.0, 1.0);
        MAX_ANVIL_DISTANCE = builder.comment("Maximum distance you can go from your Smithing Anvil before minigame reset").defineInRange("maxAnvilDistance", 100, 0, 1000);
        MINIGAME_TIMEOUT_TICKS = builder.comment("Minigame resets after a certain amount of seconds").defineInRange("minigameTimeout", 6000, 0, 36000);
        ZONE_STARTING_SIZE = builder.comment("Zone starting size for the forging minigame, in chance").defineInRange("zoneStartingSize", 20, 0, 100);
        ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for the forging minigame").defineInRange("zoneShrinkFactor", 0.7, 0, 1);
        MIN_PERFECT_ZONE = builder.comment("Smallest perfect zone it can become").defineInRange("minPerfectZone", 10, 0, 100);
        PERFECT_QUALITY_SCORE = builder.comment("Lowest score required to get perfect quality").defineInRange("perfectQualityScore", 0.9, 0, 1.0);
        EXPERT_QUALITY_SCORE = builder.comment("Lowest score required to get expert quality").defineInRange("expertQualityScore", 0.6, 0, 1.0);
        WELL_QUALITY_SCORE = builder.comment("Lowest score required to get well quality").defineInRange("wellQualityScore", 0.3, 0, 1.0);
        builder.pop();

        builder.push("Durability & Grinding");
        BASE_DURABILITY_MULTIPLIER = builder.comment("Defines the base durability multiplier of all items that has durability.").defineInRange("durability", 1f, 0, 10000);
        BASE_DURABILITY_BLACKLIST = builder.comment("Items that will NOT receive base durability multiplier").defineListAllowEmpty("base_durability_blacklist", List.of("minecraft:flint_and_steel"), o -> o instanceof String);
        GRINDING_RESTORE_DURABILITY = builder.comment("Can the grindstone be used for restoring durability or not").define("grindingToggle", true);
        GRINDING_BLACKLIST = builder.comment("Items that cannot be repaired or affected by grinding").defineList("grindingBlacklist", List.of("minecraft:elytra"), obj -> obj instanceof String);
        DURABILITY_REDUCE_PER_GRIND = builder.comment("How much the item durability reduce per grindstone use").defineInRange("durabilityReduce", 0.05, 0, 1);
        DAMAGE_RESTORE_PER_GRIND = builder.comment("How much the item's durability restore per grindstone use").defineInRange("damageRestore", 0.1, 0, 1);
        builder.pop();

        builder.push("Quality Failure Chances");
        FAIL_ON_WELL_QUALITY_CHANCE = builder.comment("Chance that forging with WELL quality fails").defineInRange("failOnWellQualityChance", 0.1, 0.0, 1.0);
        FAIL_ON_EXPERT_QUALITY_CHANCE = builder.comment("Chance that forging with EXPERT quality fails").defineInRange("failOnExpertQualityChance", 0.05, 0.0, 1.0);
        builder.pop();

        builder.push("Blueprint & Tool Types");
        AVAILABLE_TOOL_TYPES = builder.comment("List of available tool types for blueprints").defineList("availableToolTypes", Arrays.asList("SWORD", "AXE", "PICKAXE", "SHOVEL", "HOE", "HAMMER"), entry -> entry instanceof String);
        CUSTOM_TOOL_TYPES = builder.comment("Add custom tool types as key-value pairs").defineList("customToolTypes", Arrays.asList(), entry -> entry instanceof String);
        HIDDEN_TOOL_TYPES = builder.comment("Add hidden custom tool types as key-value pairs").defineList("hiddenToolTypes", Arrays.asList(), entry -> entry instanceof String);
        MASTER_MAX_USE = builder.comment("Uses required to reach the next quality after Master").defineInRange("masterMaxUse", 0, 0, Integer.MAX_VALUE);
        PERFECT_MAX_USE = builder.comment("Uses required to reach the next quality after Perfect").defineInRange("perfectMaxUse", 50, 0, 1000);
        EXPERT_MAX_USE = builder.comment("Uses required to reach the next quality after Expert").defineInRange("expertMaxUse", 20, 0, 1000);
        WELL_MAX_USE = builder.comment("Uses required to reach the next quality after Well").defineInRange("wellMaxUse", 10, 0, 1000);
        POOR_MAX_USE = builder.comment("Uses required to reach the next quality after Poor").defineInRange("poorMaxUse", 5, 0, 1000);
        builder.pop();

        builder.push("Weapon Bonuses");
        MASTER_WEAPON_DAMAGE = builder.defineInRange("masterWeaponDamage", 3.0, -10.0, 10.0);
        PERFECT_WEAPON_DAMAGE = builder.defineInRange("perfectWeaponDamage", 2.0, -10.0, 10.0);
        EXPERT_WEAPON_DAMAGE = builder.defineInRange("expertWeaponDamage", 1.5, -10.0, 10.0);
        WELL_WEAPON_DAMAGE = builder.defineInRange("wellWeaponDamage", 0.0, -10.0, 10.0);
        POOR_WEAPON_DAMAGE = builder.defineInRange("poorWeaponDamage", -1.0, -10.0, 10.0);
        MASTER_WEAPON_SPEED = builder.defineInRange("masterWeaponSpeed", 1, -2.0, 2.0);
        PERFECT_WEAPON_SPEED = builder.defineInRange("perfectWeaponSpeed", 0.5, -2.0, 2.0);
        EXPERT_WEAPON_SPEED = builder.defineInRange("expertWeaponSpeed", 0.25, -2.0, 2.0);
        WELL_WEAPON_SPEED = builder.defineInRange("wellWeaponSpeed", 0.0, -2.0, 2.0);
        POOR_WEAPON_SPEED = builder.defineInRange("poorWeaponSpeed", -0.5, -2.0, 2.0);
        builder.pop();

        builder.push("Armor Bonuses");
        MASTER_ARMOR_BONUS = builder.defineInRange("masterArmorBonus", 2, -5.0, 5.0);
        PERFECT_ARMOR_BONUS = builder.defineInRange("perfectArmorBonus", 1.5, -5.0, 5.0);
        EXPERT_ARMOR_BONUS = builder.defineInRange("expertArmorBonus", 1.0, -5.0, 5.0);
        WELL_ARMOR_BONUS = builder.defineInRange("wellArmorBonus", 0.0, -5.0, 5.0);
        POOR_ARMOR_BONUS = builder.defineInRange("poorArmorBonus", -1.0, -5.0, 5.0);
        builder.pop();

        builder.push("Durability Bonuses");
        MASTER_DURABILITY_BONUS = builder.defineInRange("masterDurabilityBonus", 1.6, -5.0, 5.0);
        PERFECT_DURABILITY_BONUS = builder.defineInRange("perfectDurabilityBonus", 1.5, -5.0, 5.0);
        EXPERT_DURABILITY_BONUS = builder.defineInRange("expertDurabilityBonus", 1.3, -5.0, 5.0);
        WELL_DURABILITY_BONUS = builder.defineInRange("wellDurabilityBonus", 1, -5.0, 5.0);
        POOR_DURABILITY_BONUS = builder.defineInRange("poorDurabilityBonus", 0.7, 0, 5.0);
        builder.pop();

        builder.push("Knapping Settings");
        GET_ROCK_USING_FLINT = builder.comment("Toggle for obtaining rock using flint").define("useFlintGetRock", true);
        ROCK_DROPPING_CHANCE = builder.defineInRange("rockDroppingChance", 0.1, 0, 1);
        FLINT_BREAKING_CHANCE = builder.defineInRange("flintBreakingChance", 0.1, 0, 1);
        builder.pop();

        SERVER_CONFIG = builder.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).preserveInsertionOrder().build();
        configData.load();
        spec.setConfig(configData);
    }
}

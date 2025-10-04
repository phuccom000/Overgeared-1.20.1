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
    public static final ForgeConfigSpec.BooleanValue ENABLE_MOD_TOOLTIPS;


    // --- Core Anvil Configs ---
    public static final ForgeConfigSpec.IntValue MAX_ANVIL_DISTANCE;
    public static final ForgeConfigSpec.IntValue STONE_ANVIL_MAX_USES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_STONE_TO_ANVIL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ANVIL_TO_SMITHING;

    // --- Heated Items ---
    public static final ForgeConfigSpec.IntValue HEATED_ITEM_COOLDOWN_TICKS;

    // --- Arrow Settings ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_DRAGON_BREATH_RECIPE;
    public static final ForgeConfigSpec.BooleanValue UPGRADE_ARROW_POTION_TOGGLE;
    public static final ForgeConfigSpec.IntValue MAX_POTION_TIPPING_USE;

    // --- Minigame Settings ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_MINIGAME;
    public static final ForgeConfigSpec.DoubleValue MASTER_QUALITY_CHANCE;
    public static final ForgeConfigSpec.DoubleValue MASTER_FROM_INGREDIENT_CHANCE;


    public static final ForgeConfigSpec.DoubleValue PERFECT_QUALITY_SCORE;
    public static final ForgeConfigSpec.DoubleValue EXPERT_QUALITY_SCORE;
    public static final ForgeConfigSpec.DoubleValue WELL_QUALITY_SCORE;


    public static final ForgeConfigSpec.IntValue POOR_ZONE_STARTING_SIZE;
    public static final ForgeConfigSpec.IntValue POOR_MIN_PERFECT_ZONE;
    public static final ForgeConfigSpec.DoubleValue POOR_ZONE_SHRINK_FACTOR;
    public static final ForgeConfigSpec.DoubleValue POOR_ARROW_SPEED;
    public static final ForgeConfigSpec.DoubleValue POOR_ARROW_SPEED_INCREASE;
    public static final ForgeConfigSpec.DoubleValue POOR_MAX_ARROW_SPEED;

    public static ForgeConfigSpec.IntValue WELL_ZONE_STARTING_SIZE;
    public static ForgeConfigSpec.DoubleValue WELL_ZONE_SHRINK_FACTOR;
    public static ForgeConfigSpec.IntValue WELL_MIN_PERFECT_ZONE;
    public static ForgeConfigSpec.DoubleValue WELL_ARROW_SPEED;
    public static ForgeConfigSpec.DoubleValue WELL_ARROW_SPEED_INCREASE;
    public static ForgeConfigSpec.DoubleValue WELL_MAX_ARROW_SPEED;

    public static ForgeConfigSpec.IntValue EXPERT_ZONE_STARTING_SIZE;
    public static ForgeConfigSpec.DoubleValue EXPERT_ZONE_SHRINK_FACTOR;
    public static ForgeConfigSpec.IntValue EXPERT_MIN_PERFECT_ZONE;
    public static ForgeConfigSpec.DoubleValue EXPERT_ARROW_SPEED;
    public static ForgeConfigSpec.DoubleValue EXPERT_ARROW_SPEED_INCREASE;
    public static ForgeConfigSpec.DoubleValue EXPERT_MAX_ARROW_SPEED;

    public static ForgeConfigSpec.IntValue PERFECT_ZONE_STARTING_SIZE;
    public static ForgeConfigSpec.DoubleValue PERFECT_ZONE_SHRINK_FACTOR;
    public static ForgeConfigSpec.IntValue PERFECT_MIN_PERFECT_ZONE;
    public static ForgeConfigSpec.DoubleValue PERFECT_ARROW_SPEED;
    public static ForgeConfigSpec.DoubleValue PERFECT_ARROW_SPEED_INCREASE;
    public static ForgeConfigSpec.DoubleValue PERFECT_MAX_ARROW_SPEED;

    public static ForgeConfigSpec.IntValue MASTER_ZONE_STARTING_SIZE;
    public static ForgeConfigSpec.DoubleValue MASTER_ZONE_SHRINK_FACTOR;
    public static ForgeConfigSpec.IntValue MASTER_MIN_PERFECT_ZONE;
    public static ForgeConfigSpec.DoubleValue MASTER_ARROW_SPEED;
    public static ForgeConfigSpec.DoubleValue MASTER_ARROW_SPEED_INCREASE;
    public static ForgeConfigSpec.DoubleValue MASTER_MAX_ARROW_SPEED;


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

    public static final ForgeConfigSpec.BooleanValue ENABLE_FLETCHING_RECIPES;

    public static final ForgeConfigSpec.BooleanValue ENABLE_LOOT_QUALITY;

    public static final ForgeConfigSpec.IntValue QUALITY_WEIGHT_POOR;
    public static final ForgeConfigSpec.IntValue QUALITY_WEIGHT_WELL;
    public static final ForgeConfigSpec.IntValue QUALITY_WEIGHT_EXPERT;
    public static final ForgeConfigSpec.IntValue QUALITY_WEIGHT_PERFECT;
    public static final ForgeConfigSpec.IntValue QUALITY_WEIGHT_MASTER;


    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("General Configs");
        ENABLE_MOD_TOOLTIPS = builder.comment("Toggle for mod's custom tooltips").define("enableModTooltips", true);
        builder.pop();
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

        builder.push("Arrow Fletching Settings");
        ENABLE_FLETCHING_RECIPES = builder
                .comment("Enable or disable all Fletching recipes and related tab")
                .define("enableFletchingRecipes", true);
        ENABLE_DRAGON_BREATH_RECIPE = builder
                .comment("Enable or disable Dragon Breath's brewing recipe")
                .define("enableDragonBreathRecipe", true);
        UPGRADE_ARROW_POTION_TOGGLE = builder.comment("Toggle for the ability to tip iron, steel, diamond arrows.").define("enableUpgradeArrowTipping", true);
        MAX_POTION_TIPPING_USE = builder.comment("How many arrows can a bottle of potion tip before it's depleted").defineInRange("maxPotionTipping", 8, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Minigame Common Settings");
        ENABLE_MINIGAME = builder.comment("Toggle for the forging minigame").define("enableMinigame", true);
        MASTER_QUALITY_CHANCE = builder.comment("How likely it is for the player to get Masterwork when getting Perfectly Forged. Set to 0 to disable it.").defineInRange("masterQualityChance", 0.05, 0, 1);
        MASTER_FROM_INGREDIENT_CHANCE = builder.comment("Chance that using a Master-quality ingredient results in Master-quality result").defineInRange("masterFromIngredientChance", 0.5, 0.0, 1.0);
        MAX_ANVIL_DISTANCE = builder.comment("Maximum distance you can go from your Smithing Anvil before minigame reset").defineInRange("maxAnvilDistance", 100, 0, 1000);

        PERFECT_QUALITY_SCORE = builder.comment("Lowest score required to get perfect quality").defineInRange("perfectQualityScore", 0.9, 0, 1.0);
        EXPERT_QUALITY_SCORE = builder.comment("Lowest score required to get expert quality").defineInRange("expertQualityScore", 0.6, 0, 1.0);
        WELL_QUALITY_SCORE = builder.comment("Lowest score required to get well quality").defineInRange("wellQualityScore", 0.3, 0, 1.0);

        builder.pop();

        builder.push("Poorly Forged");
        POOR_ZONE_STARTING_SIZE = builder.comment("Zone starting size for POOR forging (in % chance)")
                .defineInRange("zoneStartingSize", 30, 0, 100);
        POOR_ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for POOR forging")
                .defineInRange("zoneShrinkFactor", 0.9, 0.0, 1.0);
        POOR_MIN_PERFECT_ZONE = builder.comment("Minimum perfect zone size for POOR forging")
                .defineInRange("minPerfectZone", 15, 0, 100);
        POOR_ARROW_SPEED = builder.comment("Arrow speed for POOR forging")
                .defineInRange("arrowSpeed", 1.5, -5.0, 5.0);
        POOR_ARROW_SPEED_INCREASE = builder.comment("Arrow speed increase per hit for POOR forging")
                .defineInRange("arrowSpeedIncrease", 0.5, -5.0, 5.0);
        POOR_MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for POOR forging")
                .defineInRange("maxArrowSpeed", 3.0, 0.0, 10.0);
        builder.pop();

        builder.push("Well Forged");
        WELL_ZONE_STARTING_SIZE = builder.comment("Zone starting size for WELL forging").defineInRange("zoneStartingSize", 20, 0, 100);
        WELL_ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for WELL forging").defineInRange("zoneShrinkFactor", 0.7, 0, 1);
        WELL_MIN_PERFECT_ZONE = builder.comment("Minimum perfect zone size for WELL forging").defineInRange("minPerfectZone", 10, 0, 100);
        WELL_ARROW_SPEED = builder.comment("Arrow speed for WELL forging").defineInRange("arrowSpeed", 2.0, -5.0, 5.0);
        WELL_ARROW_SPEED_INCREASE = builder.comment("Arrow speed increase per hit for WELL forging").defineInRange("arrowSpeedIncrease", 0.75, -5.0, 5.0);
        WELL_MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for WELL forging").defineInRange("maxArrowSpeed", 5.0, 0.0, 10.0);
        builder.pop();

        // EXPERT
        builder.push("Expertly Forged");
        EXPERT_ZONE_STARTING_SIZE = builder.comment("Zone starting size for EXPERT forging").defineInRange("zoneStartingSize", 18, 0, 100);
        EXPERT_ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for EXPERT forging").defineInRange("zoneShrinkFactor", 0.65, 0, 1);
        EXPERT_MIN_PERFECT_ZONE = builder.comment("Minimum perfect zone size for EXPERT forging").defineInRange("minPerfectZone", 8, 0, 100);
        EXPERT_ARROW_SPEED = builder.comment("Arrow speed for EXPERT forging").defineInRange("arrowSpeed", 2.5, -5.0, 5.0);
        EXPERT_ARROW_SPEED_INCREASE = builder.comment("Arrow speed increase per hit for EXPERT forging").defineInRange("arrowSpeedIncrease", 0.85, -5.0, 5.0);
        EXPERT_MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for EXPERT forging").defineInRange("maxArrowSpeed", 6.0, 0.0, 10.0);
        builder.pop();

        // PERFECT
        builder.push("Perfectly Forged");
        PERFECT_ZONE_STARTING_SIZE = builder.comment("Zone starting size for PERFECT forging").defineInRange("zoneStartingSize", 15, 0, 100);
        PERFECT_ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for PERFECT forging").defineInRange("zoneShrinkFactor", 0.6, 0, 1);
        PERFECT_MIN_PERFECT_ZONE = builder.comment("Minimum perfect zone size for PERFECT forging").defineInRange("minPerfectZone", 6, 0, 100);
        PERFECT_ARROW_SPEED = builder.comment("Arrow speed for PERFECT forging").defineInRange("arrowSpeed", 3.0, -5.0, 5.0);
        PERFECT_ARROW_SPEED_INCREASE = builder.comment("Arrow speed increase per hit for PERFECT forging").defineInRange("arrowSpeedIncrease", 1.0, -5.0, 5.0);
        PERFECT_MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for PERFECT forging").defineInRange("maxArrowSpeed", 7.0, 0.0, 10.0);
        builder.pop();

        // MASTER
        builder.push("Masterwork");
        MASTER_ZONE_STARTING_SIZE = builder.comment("Zone starting size for MASTER forging").defineInRange("zoneStartingSize", 12, 0, 100);
        MASTER_ZONE_SHRINK_FACTOR = builder.comment("Zone shrink factor for MASTER forging").defineInRange("zoneShrinkFactor", 0.55, 0, 1);
        MASTER_MIN_PERFECT_ZONE = builder.comment("Minimum perfect zone size for MASTER forging").defineInRange("minPerfectZone", 4, 0, 100);
        MASTER_ARROW_SPEED = builder.comment("Arrow speed for MASTER forging").defineInRange("arrowSpeed", 3.5, -5.0, 5.0);
        MASTER_ARROW_SPEED_INCREASE = builder.comment("Arrow speed increase per hit for MASTER forging").defineInRange("arrowSpeedIncrease", 1.2, -5.0, 5.0);
        MASTER_MAX_ARROW_SPEED = builder.comment("Maximum arrow speed for MASTER forging").defineInRange("maxArrowSpeed", 8.0, 0.0, 10.0);
        builder.pop();

        builder.push("Durability & Grinding");
        BASE_DURABILITY_MULTIPLIER = builder.comment("Defines the base durability multiplier of all items that has durability.").defineInRange("durability", 1f, 0, 10000);
        BASE_DURABILITY_BLACKLIST = builder.comment("Items that will NOT receive base durability multiplier").defineListAllowEmpty("base_durability_blacklist", List.of("minecraft:flint_and_steel"), o -> o instanceof String);
        GRINDING_RESTORE_DURABILITY = builder.comment("Can the grindstone be used for restoring durability or not").define("grindingToggle", true);
        GRINDING_BLACKLIST = builder.comment("Items that cannot be repaired or affected by grinding").defineList("grindingBlacklist", List.of("minecraft:elytra", "overgeared:wooden_tongs"), obj -> obj instanceof String);
        DURABILITY_REDUCE_PER_GRIND = builder.comment("How much the item durability reduce per grindstone use").defineInRange("durabilityReduce", 0.05, 0, 1);
        DAMAGE_RESTORE_PER_GRIND = builder.comment("How much the item's durability restore per grindstone use").defineInRange("damageRestore", 0.1, 0, 1);
        builder.pop();

        builder.push("Quality Failure Chances");
        FAIL_ON_WELL_QUALITY_CHANCE = builder.comment("Chance that forging with WELL quality fails").defineInRange("failOnWellQualityChance", 0.1, 0.0, 1.0);
        FAIL_ON_EXPERT_QUALITY_CHANCE = builder.comment("Chance that forging with EXPERT quality fails").defineInRange("failOnExpertQualityChance", 0.05, 0.0, 1.0);
        builder.pop();

        builder.push("Blueprint & Tool Types");
        AVAILABLE_TOOL_TYPES = builder.comment(
                        "List of available tool types for blueprints",
                        "Default options: SWORD, AXE, PICKAXE, SHOVEL, HOE",
                        "Remove any types you don't want to be available",
                        "Add custom types below in customToolTypes")
                .defineList("availableToolTypes",
                        Arrays.asList("SWORD", "AXE", "PICKAXE", "SHOVEL", "HOE", "HAMMER"),
                        entry -> entry instanceof String);

        CUSTOM_TOOL_TYPES = builder.comment("Add custom tool types as key-value pairs",
                        "Format: [\"TYPE_ID\",\"Display Name\", \"TYPE_ID2\",\"Display Name 2\"]",
                        "Example: [\"SPEAR\",\"Spear\", \"BROADSWORD\",\"Broadsword\"]")
                .defineList("customToolTypes", Arrays.asList(), entry -> {
                    if (!(entry instanceof String)) return false;
                    // Validation will happen in ToolTypeRegistry
                    return true;
                });

        HIDDEN_TOOL_TYPES = builder.comment("Add hidden custom tool types as key-value pairs. Does not appear in the Drafting Table",
                        "Format: [\"TYPE_ID\",\"Display Name\", \"TYPE_ID2\",\"Display Name 2\"]",
                        "Example: [\"SPEAR\",\"Spear\", \"BROADSWORD\",\"Broadsword\"]")
                .defineList("hiddenToolTypes", Arrays.asList(), entry -> {
                    if (!(entry instanceof String)) return false;
                    // Validation will happen in ToolTypeRegistry
                    return true;
                });
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
        builder.push("Loot Quality");
        ENABLE_LOOT_QUALITY = builder.comment("Toggle for loot quality").define("enableLootQuality", true);

        QUALITY_WEIGHT_POOR = builder
                .comment("Weight for Poor quality for loot-tools/weapons. Set to 0 to disable.")
                .defineInRange("weightPoorQuality", 50, 0, Integer.MAX_VALUE);
        QUALITY_WEIGHT_WELL = builder
                .comment("Weight for Well quality for loot-tools/weapons. Set to 0 to disable.")
                .defineInRange("weightWellQuality", 30, 0, Integer.MAX_VALUE);
        QUALITY_WEIGHT_EXPERT = builder
                .comment("Weight for Expert quality for loot-tools/weapons. Set to 0 to disable.")
                .defineInRange("weightExpertQuality", 10, 0, Integer.MAX_VALUE);
        QUALITY_WEIGHT_PERFECT = builder
                .comment("Weight for Perfect quality for loot-tools/weapons. Set to 0 to disable.")
                .defineInRange("weightPerfectQuality", 5, 0, Integer.MAX_VALUE);
        QUALITY_WEIGHT_MASTER = builder
                .comment("Weight for Master quality for loot-tools/weapons. Set to 0 to disable.")
                .defineInRange("weightMasterQuality", 1, 0, Integer.MAX_VALUE);
        builder.pop();
        SERVER_CONFIG = builder.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).preserveInsertionOrder().build();
        configData.load();
        spec.setConfig(configData);
    }
}

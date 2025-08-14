package net.stirdrem.overgeared.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

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

    public static final ForgeConfigSpec.DoubleValue MASTER_DURABILITY_BONUS;
    public static final ForgeConfigSpec.DoubleValue PERFECT_DURABILITY_BONUS;
    public static final ForgeConfigSpec.DoubleValue EXPERT_DURABILITY_BONUS;
    public static final ForgeConfigSpec.DoubleValue WELL_DURABILITY_BONUS;
    public static final ForgeConfigSpec.DoubleValue POOR_DURABILITY_BONUS;

    public static final ForgeConfigSpec.DoubleValue PERFECT_QUALITY_SCORE;
    public static final ForgeConfigSpec.DoubleValue EXPERT_QUALITY_SCORE;
    public static final ForgeConfigSpec.DoubleValue WELL_QUALITY_SCORE;

    public static final ForgeConfigSpec.DoubleValue DEFAULT_ARROW_SPEED;
    public static final ForgeConfigSpec.DoubleValue DEFAULT_ARROW_SPEED_INCREASE;
    public static final ForgeConfigSpec.DoubleValue MAX_ARROW_SPEED;
    public static final ForgeConfigSpec.IntValue ZONE_STARTING_SIZE;
    public static final ForgeConfigSpec.IntValue MIN_PERFECT_ZONE;
    public static final ForgeConfigSpec.DoubleValue ZONE_SHRINK_FACTOR;
    public static final ForgeConfigSpec.IntValue MINIGAME_TIMEOUT_TICKS;
    public static final ForgeConfigSpec.DoubleValue BASE_DURABILITY_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BASE_DURABILITY_BLACKLIST;
    public static final ForgeConfigSpec.DoubleValue ROCK_DROPPING_CHANCE;
    public static final ForgeConfigSpec.DoubleValue FLINT_BREAKING_CHANCE;
    public static final ForgeConfigSpec.DoubleValue DURABILITY_REDUCE_PER_GRIND;
    public static final ForgeConfigSpec.DoubleValue DAMAGE_RESTORE_PER_GRIND;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MINIGAME;
    public static final ForgeConfigSpec.DoubleValue MASTER_QUALITY_CHANCE;
    public static final ForgeConfigSpec.IntValue STONE_ANVIL_MAX_USES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> AVAILABLE_TOOL_TYPES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_TOOL_TYPES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HIDDEN_TOOL_TYPES;
    public static final ForgeConfigSpec.IntValue HEATED_ITEM_COOLDOWN_TICKS;

    public static final ForgeConfigSpec.IntValue MASTER_MAX_USE;
    public static final ForgeConfigSpec.IntValue PERFECT_MAX_USE;
    public static final ForgeConfigSpec.IntValue EXPERT_MAX_USE;
    public static final ForgeConfigSpec.IntValue WELL_MAX_USE;
    public static final ForgeConfigSpec.IntValue POOR_MAX_USE;

    public static final ForgeConfigSpec.DoubleValue FAIL_ON_WELL_QUALITY_CHANCE;
    public static final ForgeConfigSpec.DoubleValue FAIL_ON_EXPERT_QUALITY_CHANCE;

    public static final ForgeConfigSpec.IntValue MAX_POTION_TIPPING_USE;
    //public static final ForgeConfigSpec.IntValue SLIME_ARROW_BOUNCE;
    //public static final ForgeConfigSpec.DoubleValue SLIME_ARROW_BOUNCE_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue UPGRADE_ARROW_POTION_TOGGLE;


    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("Arrow Settings");
        //SLIME_ARROW_BOUNCE = builder.comment("Defines how many times slime arrows bounce before coming to a stop.").worldRestart().defineInRange("slimeArrowsBounceAmount", 3, 0, 1000);
        //SLIME_ARROW_BOUNCE_MULTIPLIER = builder.comment("Defines the bounce multiplier of slime arrows.").worldRestart().defineInRange("slimeArrowsBounceMultiplier", (double) 5.0F, (double) 0.0F, (double) 1000.0F);
        UPGRADE_ARROW_POTION_TOGGLE = builder
                .comment("Toggle for the ability to tip iron, steel, diamond arrows.")
                .define("enableUpgradeArrowTipping", true);

        builder.pop();

        builder.push("Heated Items Settings");

        HEATED_ITEM_COOLDOWN_TICKS = builder.comment("How many ticks before a heated item cools off in inventory (default: 1200 = 60s)")
                .defineInRange("heatedItemCooldownTicks", 1200, 1, Integer.MAX_VALUE);

        builder.pop();

        builder.push("Blueprint Settings");

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

        builder.pop();

        builder.push("Stone Smithing Anvil");
        STONE_ANVIL_MAX_USES = builder
                .comment("Number of uses before the Stone Smithing Anvil breaks. Set to 0 to disable.")
                .defineInRange("max_uses", 64, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Minigame Config");
        ENABLE_MINIGAME = builder
                .comment("Toggle for the forging minigame. " +
                        "You cannot craft with unpolished tool heads and the quality modifier is disabled if set to false.")
                .define("enableMinigame", true);

        MASTER_QUALITY_CHANCE = builder
                .comment("How likely it is for the player to get Masterfully Forged. Set to 0 to disable it.")
                .defineInRange("masterQualityChance", 0.05, 0, 1);

        MAX_ANVIL_DISTANCE = builder
                .comment("Maximum distance you can go from your Smithing Anvil before minigame reset")
                .defineInRange("maxAnvilDistance", 100, 0, 1000);
        
        DEFAULT_ARROW_SPEED = builder
                .comment("Default arrow speed for the forging minigame")
                .defineInRange("arrowSpeed", 2.0, -5.0, 5.0);


        DEFAULT_ARROW_SPEED_INCREASE = builder
                .comment("Default arrow speed increase for the forging minigame")
                .defineInRange("arrowSpeedIncrease", 0.75, -5.0, 5.0);

        MAX_ARROW_SPEED = builder
                .comment("Maximum arrow speed for the forging minigame")
                .defineInRange("maxArrowSpeed", 5, 0, 10.0);

        ZONE_STARTING_SIZE = builder
                .comment("Zone starting size for the forging minigame,  in chance")
                .defineInRange("zoneStartingSize", 20, 0, 100);

        ZONE_SHRINK_FACTOR = builder
                .comment("Zone shrink factor for the forging minigame, default value here means that it shrinks to 70% of its original size")
                .defineInRange("zoneShrinkFactor", 0.7, 0, 1);

        MIN_PERFECT_ZONE = builder
                .comment("Smallest perfect zone it can become")
                .defineInRange("minPerfectZone", 10, 0, 100);

        MINIGAME_TIMEOUT_TICKS = builder
                .comment("Minigame resets after a certain amount of seconds")
                .defineInRange("minigameTimeout", 6000, 0, 36000);

        PERFECT_QUALITY_SCORE = builder
                .comment(
                        "Quality score is based on your forging performance.",
                        "Perfect hits = 1.0 points, Good hits = 0.6 points, Missed hits = 0.",
                        "Formula: (perfectHits * 1.0 + goodHits * 0.6) / totalHits",
                        "Lowest score required to get perfect quality"
                )
                .defineInRange("perfectQualityScore", 0.9, 0, 1.0);

        EXPERT_QUALITY_SCORE = builder
                .comment("Lowest score required to get expert quality")
                .defineInRange("expertQualityScore", 0.6, 0, 1.0);

        WELL_QUALITY_SCORE = builder
                .comment("Lowest score required to get well quality")
                .defineInRange("wellQualityScore", 0.3, 0, 1.0);

        builder.pop();
        builder.push("Balance Options");

        BASE_DURABILITY_MULTIPLIER = builder
                .comment("Defines the base durability multiplier of all items that has durability.")
                .defineInRange("durability", 1f, 0, 10000);

        BASE_DURABILITY_BLACKLIST = builder.comment("Items that will NOT receive base durability multiplier. Use item IDs like 'minecraft:flint_and_steel'")
                .defineListAllowEmpty("base_durability_blacklist",
                        List.of("minecraft:flint_and_steel"),
                        o -> o instanceof String);

        FAIL_ON_WELL_QUALITY_CHANCE = builder.comment("Chance that forging with WELL quality fails and returns the failed result (0.0 - 1.0)")
                .defineInRange("failOnWellQualityChance", 0.1, 0.0, 1.0);

        FAIL_ON_EXPERT_QUALITY_CHANCE = builder.comment("Chance that forging with EXPERT quality fails and returns the failed result (0.0 - 1.0)")
                .defineInRange("failOnExpertQualityChance", 0.05, 0.0, 1.0);

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
        builder.push("Durability Bonuses");

        MASTER_DURABILITY_BONUS = builder
                .comment("Durability bonus for master quality durability")
                .defineInRange("masterDurabilityBonus", 1.6, -5.0, 5.0);

        PERFECT_DURABILITY_BONUS = builder
                .comment("Durability bonus for perfect quality durability")
                .defineInRange("perfectDurabilityBonus", 1.5, -5.0, 5.0);

        EXPERT_DURABILITY_BONUS = builder
                .comment("Durability bonus for expert quality durability")
                .defineInRange("expertDurabilityBonus", 1.3, -5.0, 5.0);

        WELL_DURABILITY_BONUS = builder
                .comment("Durability bonus for well-made quality durability")
                .defineInRange("wellDurabilityBonus", 1, -5.0, 5.0);

        POOR_DURABILITY_BONUS = builder
                .comment("Durability penalty for poor quality durability")
                .defineInRange("poorDurabilityBonus", 0.7, 0, 5.0);

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

        builder.push("Blueprint Config");
        MASTER_MAX_USE = builder
                .comment("Uses required to reach the next quality after Master")
                .defineInRange("masterMaxUse", 0, 0, Integer.MAX_VALUE);

        PERFECT_MAX_USE = builder
                .comment("Uses required to reach the next quality after Perfect")
                .defineInRange("perfectMaxUse", 50, 0, 1000);

        EXPERT_MAX_USE = builder
                .comment("Uses required to reach the next quality after Expert")
                .defineInRange("expertMaxUse", 20, 0, 1000);

        WELL_MAX_USE = builder
                .comment("Uses required to reach the next quality after Well")
                .defineInRange("wellMaxUse", 10, 0, 1000);

        POOR_MAX_USE = builder
                .comment("Uses required to reach the next quality after Poor")
                .defineInRange("poorMaxUse", 5, 0, 1000);
        builder.pop();

        builder.push("Potion Config");
        MAX_POTION_TIPPING_USE
                = builder
                .comment("How many arrows can a bottle of potion tips before it's depleted")
                .defineInRange("maxPotionTipping", 8, 0, Integer.MAX_VALUE);

        builder.pop();

        SERVER_CONFIG = builder.build();
    }

    public static final void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).preserveInsertionOrder().build();
        configData.load();
        spec.setConfig(configData);
    }

}

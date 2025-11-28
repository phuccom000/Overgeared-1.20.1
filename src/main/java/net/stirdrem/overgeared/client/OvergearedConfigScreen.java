package net.stirdrem.overgeared.client;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.stirdrem.overgeared.config.ServerConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class OvergearedConfigScreen extends Screen {
    private final Screen parent;
    private CommentedFileConfig configFile;
    private ForgeConfigSpec configSpec;
    private double scrollPosition; // Add this field to store scroll position

    private ConfigList configList;

    public OvergearedConfigScreen(Screen parent) {
        super(Component.literal("Overgeared Configuration"));
        this.parent = parent;
        loadConfig();
    }

    private void loadConfig() {
        Path configPath = Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config")
                .resolve("overgeared-common.toml");

        this.configFile = CommentedFileConfig.builder(configPath)
                .sync()
                .writingMode(WritingMode.REPLACE)
                .preserveInsertionOrder()
                .build();
        this.configFile.load();

        this.configSpec = ServerConfig.SERVER_CONFIG;
    }


    @Override
    protected void init() {
        super.init();

        // Calculate list dimensions
        int listWidth = this.width; // Subtract padding from both sides
        int listHeight = this.height - 80;
        int listTop = 40;
        int listLeft = 0; // Start at padding position instead of center

        this.configList = new ConfigList(
                Minecraft.getInstance(),
                listWidth, listHeight,
                listTop, listTop + listHeight, 24
        );
        this.configList.setLeftPos(listLeft); // Set the centered position
        this.addWidget(configList);

        buildEntries();

        // Save / Done button - keep your existing button code
        int btnW = 200;
        int btnH = 20;
       /* this.addRenderableWidget(
                Button.builder(Component.literal("Reset Configs"), b -> {
                            resetAllToDefaults();
                        }).bounds(this.width / 2 - btnW / 2, this.height - 30, btnW, btnH)
                        .build()
        );*/
        this.addRenderableWidget(
                Button.builder(Component.literal("Done"), b -> {
                            save();
                            this.scrollPosition = configList.getScrollAmount();
                            this.minecraft.setScreen(parent);
                        }).bounds(this.width / 2 - btnW / 2, this.height - 30, btnW, btnH)
                        .build()
        );
    }

    @Override
    public void onClose() {
        super.onClose();
        if (configFile != null) {
            try {
                configFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void restoreScrollPosition() {
        if (this.configList != null && this.scrollPosition > 0) {
            this.configList.setScrollAmount(this.scrollPosition);
            this.scrollPosition = configList.getScrollAmount();
        }
    }

    private void buildEntries() {
        //this.scrollPosition = configList.getScrollAmount();
        this.configList.setScrollAmount(this.scrollPosition);
        buildGeneralConfigs();
        buildLootQuality();
        buildAnvilForging();
        buildQualitySettings();
        buildForgingZones();
        buildWeaponBonuses();
        buildArmorBonuses();
        buildDurabilityBonuses();
        buildHeatedItems();
        buildGrindingDurability();
        buildQualityFailure();
        buildBlueprintSettings();
        buildKnapping();
        buildCasting();
    }

    private void buildGeneralConfigs() {
        configList.addConfigEntry(new HeaderEntry("General Configs"));
        configList.addConfigEntry(new BooleanEntry(
                "Enable the mod's custom tooltips",
                () -> getBoolean("General Configs.enableModTooltips"),
                v -> setBoolean("General Configs.enableModTooltips", v)
        ));
    }

    private void buildLootQuality() {
        configList.addConfigEntry(new HeaderEntry("Loot Quality"));
        configList.addConfigEntry(new BooleanEntry(
                "Enable Loot to have quality",
                () -> getBoolean("Loot Quality.enableLootQuality"),
                v -> setBoolean("Loot Quality.enableLootQuality", v)
        ));
        configList.addConfigEntry(new IntEntry(
                "Poor Quality Weight",
                () -> getInt("Loot Quality.weightPoorQuality"),
                v -> setInt("Loot Quality.weightPoorQuality", v),
                0, 100
        ));
        configList.addConfigEntry(new IntEntry(
                "Well Quality Weight",
                () -> getInt("Loot Quality.weightWellQuality"),
                v -> setInt("Loot Quality.weightWellQuality", v),
                0, 100
        ));
        configList.addConfigEntry(new IntEntry(
                "Expert Quality Weight",
                () -> getInt("Loot Quality.weightExpertQuality"),
                v -> setInt("Loot Quality.weightExpertQuality", v),
                0, 100
        ));
        configList.addConfigEntry(new IntEntry(
                "Perfect Quality Weight",
                () -> getInt("Loot Quality.weightPerfectQuality"),
                v -> setInt("Loot Quality.weightPerfectQuality", v),
                0, 100
        ));
        configList.addConfigEntry(new IntEntry(
                "Master Quality Weight",
                () -> getInt("Loot Quality.weightMasterQuality"),
                v -> setInt("Loot Quality.weightMasterQuality", v),
                0, 100
        ));
    }

    private void buildAnvilForging() {
        configList.addConfigEntry(new HeaderEntry("Anvil & Forging"));
        configList.addConfigEntry(new BooleanEntry(
                "Enable Forging Minigame",
                () -> getBoolean("Minigame Common Settings.enableMinigame"),
                v -> setBoolean("Minigame Common Settings.enableMinigame", v)
        ));
        configList.addConfigEntry(new BooleanEntry(
                "Convert Stone to Stone Smithing Anvil",
                () -> getBoolean("Anvil Conversion.enableStoneToAnvil"),
                v -> setBoolean("Anvil Conversion.enableStoneToAnvil", v)
        ));
        configList.addConfigEntry(new BooleanEntry(
                "Convert Vanilla Anvil to Smithing Anvil",
                () -> getBoolean("Anvil Conversion.enableAnvilToSmithing"),
                v -> setBoolean("Anvil Conversion.enableAnvilToSmithing", v)
        ));
        configList.addConfigEntry(new BooleanEntry(
                "Enable Fletching",
                () -> getBoolean("Arrow Fletching Settings.enableFletchingRecipes"),
                v -> setBoolean("Arrow Fletching Settings.enableFletchingRecipes", v)
        ));
        configList.addConfigEntry(new BooleanEntry(
                "Enable Arrow Tipping",
                () -> getBoolean("Arrow Fletching Settings.enableUpgradeArrowTipping"),
                v -> setBoolean("Arrow Fletching Settings.enableUpgradeArrowTipping", v)
        ));
        configList.addConfigEntry(new BooleanEntry(
                "Enable Dragon Breath Recipe",
                () -> getBoolean("Arrow Fletching Settings.enableDragonBreathRecipe"),
                v -> setBoolean("Arrow Fletching Settings.enableDragonBreathRecipe", v)
        ));
        configList.addConfigEntry(new ManualInputEntry(
                "How many time a potion bottle can be used for tipping",
                () -> (double) getInt("Arrow Fletching Settings.maxPotionTipping"),
                v -> setInt("Arrow Fletching Settings.maxPotionTipping", v.intValue()), true
        ));
        configList.addConfigEntry(new ManualInputEntry(
                "How many uses the Stone Anvil has. Set 0 to disable",
                () -> (double) getInt("Stone Smithing Anvil.max_uses"),
                v -> setInt("Stone Smithing Anvil.max_uses", v.intValue()),
                true));
        configList.addConfigEntry(new BooleanEntry(
                "Enable the Stone Anvil to turn into Cobblestone after falling",
                () -> getBoolean("Stone Smithing Anvil.enableAnvilToStone"),
                v -> setBoolean("Stone Smithing Anvil.enableAnvilToStone", v)
        ));
        configList.addConfigEntry(new IntEntry(
                "Max distance from the anvil until minigame resets",
                () -> getInt("Minigame Common Settings.maxAnvilDistance"),
                v -> setInt("Minigame Common Settings.maxAnvilDistance", v),
                0, 1000
        ));
        configList.addConfigEntry(new BooleanEntry(
                "Enable adding the maker's name to result",
                () -> getBoolean("Minigame Common Settings.enableAuthorTooltips"),
                v -> setBoolean("Minigame Common Settings.enableAuthorTooltips", v)
        ));
    }

    private void buildQualitySettings() {
        configList.addConfigEntry(new HeaderEntry("Quality Settings"));
        configList.addConfigEntry(new BooleanEntry(
                "Ingredients' quality cap result's quality",
                () -> getBoolean("Minigame Common Settings.ingredientsDefineQuality"),
                v -> setBoolean("Minigame Common Settings.ingredientsDefineQuality", v)
        ));
        configList.addConfigEntry(new DoubleEntry(
                "Chance for Master ingredient results in Master result",
                () -> getDouble("Minigame Common Settings.masterFromIngredientChance"),
                v -> setDouble("Minigame Common Settings.masterFromIngredientChance", v),
                0.0, 1.0
        ));
        configList.addConfigEntry(new DoubleEntry(
                "Well Quality Score",
                () -> getDouble("Minigame Common Settings.wellQualityScore"),
                v -> setDouble("Minigame Common Settings.wellQualityScore", v),
                0.0, 1.0
        ));
        configList.addConfigEntry(new DoubleEntry(
                "Expert Quality Score",
                () -> getDouble("Minigame Common Settings.expertQualityScore"),
                v -> setDouble("Minigame Common Settings.expertQualityScore", v),
                0.0, 1.0
        ));
        configList.addConfigEntry(new DoubleEntry(
                "Perfect Quality Score",
                () -> getDouble("Minigame Common Settings.perfectQualityScore"),
                v -> setDouble("Minigame Common Settings.perfectQualityScore", v),
                0.0, 1.0
        ));
        configList.addConfigEntry(new DoubleEntry(
                "How likely it is to get Masterwork",
                () -> getDouble("Minigame Common Settings.masterQualityChance"),
                v -> setDouble("Minigame Common Settings.masterQualityChance", v),
                0.0, 1.0
        ));

    }

    private void buildForgingZones() {
        buildForgingZone("Default", "Default (No Blueprint)");
        buildForgingZone("Poor", "Poorly Forged");
        buildForgingZone("Well", "Well Forged");
        buildForgingZone("Expert", "Expertly Forged");
        buildForgingZone("Perfect", "Perfectly Forged");
        buildForgingZone("Master", "Masterwork");
    }

    private void buildForgingZone(String quality, String configPath) {
        configList.addConfigEntry(new HeaderEntry("Minigame Config - " + quality));
        configList.addConfigEntry(new IntEntry(
                quality + " Zone Starting Size",
                () -> getInt(configPath + ".zoneStartingSize"),
                v -> setInt(configPath + ".zoneStartingSize", v),
                0, 100
        ));
        configList.addConfigEntry(new DoubleEntry(
                quality + " Zone Shrink Factor",
                () -> getDouble(configPath + ".zoneShrinkFactor"),
                v -> setDouble(configPath + ".zoneShrinkFactor", v),
                0.0, 1.0
        ));
        configList.addConfigEntry(new IntEntry(
                quality + " Min Perfect Zone",
                () -> getInt(configPath + ".minPerfectZone"),
                v -> setInt(configPath + ".minPerfectZone", v),
                0, 100
        ));
        configList.addConfigEntry(new DoubleEntry(
                quality + " Arrow Speed",
                () -> getDouble(configPath + ".arrowSpeed"),
                v -> setDouble(configPath + ".arrowSpeed", v),
                -5.0, 5.0
        ));
        configList.addConfigEntry(new DoubleEntry(
                quality + " Arrow Speed Increase",
                () -> getDouble(configPath + ".arrowSpeedIncrease"),
                v -> setDouble(configPath + ".arrowSpeedIncrease", v),
                -5.0, 5.0
        ));
        configList.addConfigEntry(new DoubleEntry(
                quality + " Max Arrow Speed",
                () -> getDouble(configPath + ".maxArrowSpeed"),
                v -> setDouble(configPath + ".maxArrowSpeed", v),
                0.0, 10.0
        ));
    }

    private void buildWeaponBonuses() {
        configList.addConfigEntry(new HeaderEntry("Weapon Bonuses"));

        // Damage Bonuses - sorted from Poor to Master
        buildWeaponBonus("Poor", "Weapon Bonuses.poorWeaponDamage", -10.0, 10.0);
        buildWeaponBonus("Well", "Weapon Bonuses.wellWeaponDamage", -10.0, 10.0);
        buildWeaponBonus("Expert", "Weapon Bonuses.expertWeaponDamage", -10.0, 10.0);
        buildWeaponBonus("Perfect", "Weapon Bonuses.perfectWeaponDamage", -10.0, 10.0);
        buildWeaponBonus("Master", "Weapon Bonuses.masterWeaponDamage", -10.0, 10.0);
        configList.addConfigEntry(new HeaderEntry(""));
        // Speed Bonuses - sorted from Poor to Master
        buildWeaponSpeedBonus("Poor", "Weapon Bonuses.poorWeaponSpeed", -2.0, 2.0);
        buildWeaponSpeedBonus("Well", "Weapon Bonuses.wellWeaponSpeed", -2.0, 2.0);
        buildWeaponSpeedBonus("Expert", "Weapon Bonuses.expertWeaponSpeed", -2.0, 2.0);
        buildWeaponSpeedBonus("Perfect", "Weapon Bonuses.perfectWeaponSpeed", -2.0, 2.0);
        buildWeaponSpeedBonus("Master", "Weapon Bonuses.masterWeaponSpeed", -2.0, 2.0);
    }

    private void buildWeaponBonus(String quality, String path, double min, double max) {
        configList.addConfigEntry(new DoubleEntry(
                quality + " Weapon Damage Addition",
                () -> getDouble(path),
                v -> setDouble(path, v),
                min, max
        ));
    }

    private void buildWeaponSpeedBonus(String quality, String path, double min, double max) {
        configList.addConfigEntry(new DoubleEntry(
                quality + " Weapon Speed Addition",
                () -> getDouble(path),
                v -> setDouble(path, v),
                min, max
        ));
    }

    private void buildArmorBonuses() {
        configList.addConfigEntry(new HeaderEntry("Armor Bonuses"));
        buildArmorBonus("Poor", "Armor Bonuses.poorArmorBonus", -5.0, 5.0);
        buildArmorBonus("Well", "Armor Bonuses.wellArmorBonus", -5.0, 5.0);
        buildArmorBonus("Expert", "Armor Bonuses.expertArmorBonus", -5.0, 5.0);
        buildArmorBonus("Perfect", "Armor Bonuses.perfectArmorBonus", -5.0, 5.0);
        buildArmorBonus("Master", "Armor Bonuses.masterArmorBonus", -5.0, 5.0);
    }

    private void buildArmorBonus(String quality, String path, double min, double max) {
        configList.addConfigEntry(new DoubleEntry(
                quality + " Armor Bonus Addition",
                () -> getDouble(path),
                v -> setDouble(path, v),
                min, max
        ));
    }

    private void buildDurabilityBonuses() {
        configList.addConfigEntry(new HeaderEntry("Durability Bonuses"));
        buildDurabilityBonus("Poor", "Durability Bonuses.poorDurabilityBonus", -5.0, 5.0);
        buildDurabilityBonus("Well", "Durability Bonuses.wellDurabilityBonus", -5.0, 5.0);
        buildDurabilityBonus("Expert", "Durability Bonuses.expertDurabilityBonus", -5.0, 5.0);
        buildDurabilityBonus("Perfect", "Durability Bonuses.perfectDurabilityBonus", -5.0, 5.0);
        buildDurabilityBonus("Master", "Durability Bonuses.masterDurabilityBonus", -5.0, 5.0);
    }

    private void buildDurabilityBonus(String quality, String path, double min, double max) {
        configList.addConfigEntry(new DoubleEntry(
                quality + " Durability Bonus Multiplier",
                () -> getDouble(path),
                v -> setDouble(path, v),
                min, max
        ));
    }

    private void buildHeatedItems() {
        configList.addConfigEntry(new HeaderEntry("Heated Items"));
        configList.addConfigEntry(new IntEntry(
                "How many ticks until heated items cool down",
                () -> getInt("Heated Items.heatedItemCooldownTicks"),
                v -> setInt("Heated Items.heatedItemCooldownTicks", v),
                1, Integer.MAX_VALUE
        ));
    }

    private void buildGrindingDurability() {
        configList.addConfigEntry(new HeaderEntry("Grinding & Durability"));
        configList.addConfigEntry(new ManualInputEntry(
                "Base Durability Multiplier",
                () -> getDouble("Durability & Grinding.durability"),
                v -> setDouble("Durability & Grinding.durability", v)
        ));
        configList.addConfigEntry(new BooleanEntry(
                "Enable Grinding",
                () -> getBoolean("Durability & Grinding.grindingToggle"),
                v -> setBoolean("Durability & Grinding.grindingToggle", v)
        ));
        configList.addConfigEntry(new ManualInputEntry(
                "Tool's Durability Reduce per Grind",
                () -> getDouble("Durability & Grinding.durabilityReduce"),
                v -> setDouble("Durability & Grinding.durabilityReduce", v)
        ));
        configList.addConfigEntry(new ManualInputEntry(
                "Tool's Damage Restore per Grind",
                () -> getDouble("Durability & Grinding.damageRestore"),
                v -> setDouble("Durability & Grinding.damageRestore", v)
        ));
        configList.addConfigEntry(new BlacklistEntry(
                "Durability Blacklist",
                () -> getStringList("Durability & Grinding.base_durability_blacklist"),
                v -> setStringList("Durability & Grinding.base_durability_blacklist", v)
        ));
        configList.addConfigEntry(new BlacklistEntry(
                "Grinding Blacklist",
                () -> getStringList("Durability & Grinding.grindingBlacklist"),
                v -> setStringList("Durability & Grinding.grindingBlacklist", v)
        ));
    }

    private void buildQualityFailure() {
        configList.addConfigEntry(new HeaderEntry("Failure chance for recipes that use the failure mechanic"));
        configList.addConfigEntry(new DoubleEntry(
                "Fail on Well Chance",
                () -> getDouble("Quality Failure Chances.failOnWellQualityChance"),
                v -> setDouble("Quality Failure Chances.failOnWellQualityChance", v),
                0.0, 1.0
        ));
        configList.addConfigEntry(new DoubleEntry(
                "Fail on Expert Chance",
                () -> getDouble("Quality Failure Chances.failOnExpertQualityChance"),
                v -> setDouble("Quality Failure Chances.failOnExpertQualityChance", v),
                0.0, 1.0
        ));
    }

    private void buildBlueprintSettings() {
        configList.addConfigEntry(new HeaderEntry("Blueprint Settings"));
        configList.addConfigEntry(new BooleanEntry(
                "Only Expert and above increase Blueprint's uses",
                () -> getBoolean("Blueprint & Tool Types.expertAboveIncreaseBlueprintToggle"),
                v -> setBoolean("Blueprint & Tool Types.expertAboveIncreaseBlueprintToggle", v)
        ));
        configList.addConfigEntry(new ManualInputEntry(
                "Poor Max Uses",
                () -> (double) getInt("Blueprint & Tool Types.poorMaxUse"),
                v -> setInt("Blueprint & Tool Types.poorMaxUse", v.intValue()), true
        ));
        configList.addConfigEntry(new ManualInputEntry(
                "Well Max Uses",
                () -> (double) getInt("Blueprint & Tool Types.wellMaxUse"),
                v -> setInt("Blueprint & Tool Types.wellMaxUse", v.intValue()), true
        ));
        configList.addConfigEntry(new ManualInputEntry(
                "Expert Max Uses",
                () -> (double) getInt("Blueprint & Tool Types.expertMaxUse"),
                v -> setInt("Blueprint & Tool Types.expertMaxUse", v.intValue()), true
        ));
        configList.addConfigEntry(new ManualInputEntry(
                "Perfect Max Uses",
                () -> (double) getInt("Blueprint & Tool Types.perfectMaxUse"),
                v -> setInt("Blueprint & Tool Types.perfectMaxUse", v.intValue()), true
        ));
        configList.addConfigEntry(new ManualInputEntry(
                "Master Max Uses",
                () -> (double) getInt("Blueprint & Tool Types.masterMaxUse"),
                v -> setInt("Blueprint & Tool Types.masterMaxUse", v.intValue()), true
        ));
    }

    private void buildKnapping() {
        configList.addConfigEntry(new HeaderEntry("Knapping"));
        configList.addConfigEntry(new BooleanEntry(
                "Enable using Flint to get Rock",
                () -> getBoolean("Knapping Settings.useFlintGetRock"),
                v -> setBoolean("Knapping Settings.useFlintGetRock", v)
        ));
        configList.addConfigEntry(new DoubleEntry(
                "Rock Dropping Chance",
                () -> getDouble("Knapping Settings.rockDroppingChance"),
                v -> setDouble("Knapping Settings.rockDroppingChance", v),
                0.0, 1.0
        ));
        configList.addConfigEntry(new DoubleEntry(
                "Flint Breaking Chance",
                () -> getDouble("Knapping Settings.flintBreakingChance"),
                v -> setDouble("Knapping Settings.flintBreakingChance", v),
                0.0, 1.0
        ));
    }

    private void buildCasting() {
        configList.addConfigEntry(new HeaderEntry("Casting"));
        configList.addConfigEntry(new BooleanEntry(
                "Enable Casting",
                () -> getBoolean("Casting.castingToggle"),
                v -> setBoolean("Casting.castingToggle", v)
        ));
        configList.addConfigEntry(new IntEntry(
                "Clay Tool Cast's Durability",
                () -> getInt("Casting.firedCastDurability"),
                v -> setInt("Casting.firedCastDurability", v),
                0, Integer.MAX_VALUE
        ));
    }

    private void save() {
        try {
            configFile.save();
            configSpec.correct(configFile);
        } catch (Exception e) {
            e.printStackTrace();
            // Try to handle the error gracefully
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui);
        if (scrollPosition > 0) {
            configList.setScrollAmount(scrollPosition);
            scrollPosition = 0; // Reset immediately after applying
        }

        this.configList.render(gui, mouseX, mouseY, partialTicks);
        super.

                render(gui, mouseX, mouseY, partialTicks);
        gui.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

// --- Config Get/Set Helpers ---

    private boolean getBoolean(String path) {
        Boolean v = configFile.get(path);
        return v != null && v;
    }

    private void setBoolean(String path, boolean val) {
        configFile.set(path, val);
    }

    private int getInt(String path) {
        Integer v = configFile.get(path);
        return v == null ? 0 : v;
    }

    private void setInt(String path, int val) {
        configFile.set(path, val);
    }

    private double getDouble(String path) {
        Double v = configFile.get(path);
        return v == null ? 0.0 : v;
    }

    private void setDouble(String path, double val) {
        configFile.set(path, val);
    }

    private List<String> getStringList(String path) {
        List<? extends String> list = configFile.get(path);
        if (list == null) return new ArrayList<>();
        return new ArrayList<>(list);
    }

    private void setStringList(String path, List<? extends String> values) {
        configFile.set(path, values);
    }

    public void setScrollPosition(double scrollPosition) {
        this.scrollPosition = scrollPosition;
        if (this.configList != null) {
            this.configList.setScrollAmount(scrollPosition);
        }
    }

    private static class ConfigList extends ObjectSelectionList<ConfigList.ConfigEntry> {
        private final int entryPadding = 30; // Padding on left and right of each entry
        private final int scrollbarPadding = 25; // Additional padding for scrollbar

        public ConfigList(Minecraft mc, int width, int height, int top, int bottom, int itemHeight) {
            super(mc, width, height, top, bottom, itemHeight);
        }

        // Add this method to set the horizontal position (like KeyBindsList)
        public void setLeftPos(int left) {
            this.x0 = left;
            this.x1 = left + this.width;
        }

        @Override
        public int getRowWidth() {
            return this.width - (entryPadding * 2); // Reduce available width by padding
        }

        @Override
        public int getRowLeft() {
            return this.x0 + entryPadding; // Start drawing content after padding
        }

        public void addConfigEntry(ConfigEntry entry) {
            super.addEntry(entry);
        }

        @Override
        public int getScrollbarPosition() {
            // Move scrollbar further right by adding padding
            return this.x1 - scrollbarPadding;
        }


        // Add this method to disable the selection background
        @Override
        protected void renderSelection(GuiGraphics gui, int top, int width, int height, int outlineColor, int fillColor) {
            // Empty implementation to remove the selection background
        }

        @Override
        public void setRenderSelection(boolean pRenderSelection) {
            super.setRenderSelection(false);
        }

        public abstract static class ConfigEntry extends ObjectSelectionList.Entry<ConfigEntry> {
        }
    }

    private class HeaderEntry extends ConfigList.ConfigEntry {
        private final String label;

        public HeaderEntry(String label) {
            this.label = label;
        }

        @Override
        public void render(GuiGraphics gui, int idx, int y, int x, int listWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float partialTicks) {
            int textWidth = font.width(label);
            int centeredX = x + (listWidth - textWidth) / 2;
            gui.drawString(font, label, centeredX, y + (entryHeight / 2) - 4, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(label);
        }
    }

    private class BooleanEntry extends ConfigList.ConfigEntry {
        private final Supplier<Boolean> getter;
        private final Consumer<Boolean> setter;
        private final Boolean defaultValue;
        private final Button toggle;
        private final String label;

        public BooleanEntry(String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
            this.label = label;
            this.getter = getter;
            this.setter = setter;
            this.defaultValue = getter.get(); // Store default value

            this.toggle = Button.builder(Component.literal(getter.get().toString()), btn -> {
                boolean newVal = !getter.get();
                setter.accept(newVal);
                btn.setMessage(Component.literal(Boolean.toString(newVal)));
            }).bounds(0, 0, 80, 20).build();

        }


        @Override
        public void render(GuiGraphics gui, int idx, int y, int x, int listWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float pt) {
            gui.drawString(font, label, x + 2, y + 5, 0xFFFFFF);

            int controlWidth = 80; // toggle width + reset button width + spacing
            toggle.setX(x + listWidth - controlWidth);
            toggle.setY(y);
            toggle.render(gui, mouseX, mouseY, pt);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return toggle.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public Component getNarration() {
            return Component.literal(label);
        }
    }

    private class IntEntry extends ConfigList.ConfigEntry {
        private final Supplier<Integer> getter;
        private final Consumer<Integer> setter;
        private final Integer defaultValue;
        private final AbstractSliderButton slider;
        private final String label;
        private final int min, max;

        public IntEntry(String label, Supplier<Integer> getter, Consumer<Integer> setter, int min, int max) {
            this.label = label;
            this.getter = getter;
            this.setter = setter;
            this.defaultValue = getter.get();
            this.min = min;
            this.max = max;

            double normalized = (getter.get() - min) / (double) (max - min);
            this.slider = new AbstractSliderButton(0, 0, 80, 20,
                    Component.literal(getter.get().toString()), normalized) {
                @Override
                protected void updateMessage() {
                    int value = min + (int) (this.value * (max - min));
                    setter.accept(value);
                    setMessage(Component.literal(Integer.toString(value)));
                }

                @Override
                protected void applyValue() {
                }
            };
        }


        @Override
        public void render(GuiGraphics gui, int idx, int y, int x, int listWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float pt) {
            gui.drawString(font, label, x + 2, y + 5, 0xFFFFFF);

            int controlWidth = 80; // slider width + reset button width + spacing
            slider.setX(x + listWidth - controlWidth);
            slider.setY(y);
            slider.render(gui, mouseX, mouseY, pt);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return slider.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
            return slider.mouseDragged(mouseX, mouseY, button, dx, dy);
        }

        @Override
        public Component getNarration() {
            return Component.literal(label);
        }
    }

    private class DoubleEntry extends ConfigList.ConfigEntry {
        private final Supplier<Double> getter;
        private final Consumer<Double> setter;
        private final Double defaultValue;
        private final AbstractSliderButton slider;
        private final String label;
        private final double min, max;

        public DoubleEntry(String label, Supplier<Double> getter, Consumer<Double> setter, double min, double max) {
            this.label = label;
            this.getter = getter;
            this.setter = setter;
            this.defaultValue = getter.get();
            this.min = min;
            this.max = max;

            double cur = getter.get();
            double normalized = (cur - min) / (max - min);
            this.slider = new AbstractSliderButton(0, 0, 80, 20,
                    Component.literal(String.format("%.2f", cur)), normalized) {
                @Override
                protected void updateMessage() {
                    double value = min + this.value * (max - min);
                    setter.accept(value);
                    setMessage(Component.literal(String.format("%.2f", value)));
                }

                @Override
                protected void applyValue() {
                }
            };


        }


        @Override
        public void render(GuiGraphics gui, int idx, int y, int x, int listWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float pt) {
            gui.drawString(font, label, x + 2, y + 5, 0xFFFFFF);

            int controlWidth = 80; // slider width + reset button width + spacing
            slider.setX(x + listWidth - controlWidth);
            slider.setY(y);
            slider.render(gui, mouseX, mouseY, pt);

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return slider.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
            return slider.mouseDragged(mouseX, mouseY, button, dx, dy);
        }

        @Override
        public Component getNarration() {
            return Component.literal(label);
        }
    }

    private class ManualInputEntry extends ConfigList.ConfigEntry {
        private final String label;
        private final Supplier<Double> getter;
        private final Consumer<Double> setter;
        private final EditBox editBox;
        private final boolean acceptIntegers;

        public ManualInputEntry(String label, Supplier<Double> getter, Consumer<Double> setter) {
            this(label, getter, setter, false);
        }

        public ManualInputEntry(String label, Supplier<Double> getter, Consumer<Double> setter, boolean acceptIntegers) {
            this.label = label;
            this.getter = getter;
            this.setter = setter;
            this.acceptIntegers = acceptIntegers;

            // width 80 should be enough, adjust if needed
            this.editBox = new EditBox(font, 0, 0, 80, 20, Component.literal(""));
            double cur = getter.get();
            if (acceptIntegers) {
                this.editBox.setValue(Integer.toString((int) cur));
            } else {
                this.editBox.setValue(Double.toString(cur));
            }

            // Set up the edit box properties
            this.editBox.setBordered(true);
            this.editBox.setVisible(true);
            this.editBox.setCanLoseFocus(true);

            // Add a responder to handle live updates
            this.editBox.setResponder(value -> {
                try {
                    double newValue;
                    if (acceptIntegers) {
                        newValue = Integer.parseInt(value);
                    } else {
                        newValue = Double.parseDouble(value);
                    }
                    setter.accept(newValue);
                } catch (NumberFormatException ignored) {
                }
            });
        }

        @Override
        public void render(GuiGraphics gui, int idx, int y, int x, int listWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float pt) {
            gui.drawString(font, label, x + 2, y + 5, 0xFFFFFF);
            editBox.setX(x + listWidth - 80);
            editBox.setY(y);
            editBox.render(gui, mouseX, mouseY, pt);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Check if click is within the edit box area
            if (editBox.isMouseOver(mouseX, mouseY)) {
                editBox.setFocused(true);
                return editBox.mouseClicked(mouseX, mouseY, button);
            } else {
                // Clicked outside - remove focus
                editBox.setFocused(false);
                return false;
            }
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return editBox.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (editBox.isFocused()) {
                if (keyCode == 256) { // ESC key
                    // Cancel and revert to original value
                    editBox.setValue(String.valueOf(getter.get()));
                    editBox.setFocused(false);
                    return true;
                } else if (keyCode == 257) { // Enter key
                    // Confirm and lose focus
                    editBox.setFocused(false);
                    return true;
                }
                return editBox.keyPressed(keyCode, scanCode, modifiers);
            }
            return false;
        }

        @Override
        public boolean charTyped(char codePoint, int modifiers) {
            return editBox.charTyped(codePoint, modifiers);
        }

        @Override
        public void setFocused(boolean focused) {
            if (!focused && editBox.isFocused()) {
                editBox.setFocused(false);
            }
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return editBox.isMouseOver(mouseX, mouseY);
        }

        @Override
        public Component getNarration() {
            return Component.literal(label);
        }
    }


    private class BlacklistEntry extends ConfigList.ConfigEntry {
        private final String label;
        private final Supplier<List<? extends String>> getter;
        private final Consumer<List<? extends String>> setter;
        private final Button editButton;

        public BlacklistEntry(String label, Supplier<List<? extends String>> getter, Consumer<List<? extends String>> setter) {
            this.label = label;
            this.getter = getter;
            this.setter = setter;

            this.editButton = Button.builder(Component.literal("Edit Items"), btn -> {
                openBlacklistEditor(label, getter.get());
            }).bounds(0, 0, 80, 20).build();
        }

        private void openBlacklistEditor(String blacklistName, List<? extends String> currentItems) {
            final double savedScrollPosition = configList.getScrollAmount();
            Minecraft mc = OvergearedConfigScreen.this.minecraft;

            Screen editorScreen = new BlacklistEditorScreen(
                    blacklistName,
                    currentItems,
                    setter,
                    savedScrollPosition,
                    OvergearedConfigScreen.this
            );

            mc.setScreen(editorScreen);
        }

        @Override
        public void render(GuiGraphics gui, int idx, int y, int x, int listWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float pt) {
            gui.drawString(font, label, x + 2, y + 5, 0xFFFFFF);

            List<? extends String> items = getter.get();
            String itemCountText = " (" + items.size() + " items)";
            gui.drawString(font, itemCountText, x + font.width(label) + 5, y + 5, 0xAAAAAA);

            editButton.setX(x + listWidth - 80);
            editButton.setY(y);
            editButton.render(gui, mouseX, mouseY, pt);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return editButton.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public Component getNarration() {
            return Component.literal(label);
        }
    }

    private void resetAllToDefaults() {
        // Reset all boolean values
        setBoolean("General Configs.enableModTooltips", true);
        setBoolean("Loot Quality.enableLootQuality", true);
        setBoolean("Minigame Common Settings.enableMinigame", true);
        setBoolean("Anvil Conversion.enableStoneToAnvil", true);
        setBoolean("Anvil Conversion.enableAnvilToSmithing", true);
        setBoolean("Arrow Fletching Settings.enableFletchingRecipes", true);
        setBoolean("Arrow Fletching Settings.enableUpgradeArrowTipping", true);
        setBoolean("Arrow Fletching Settings.enableDragonBreathRecipe", true);
        setBoolean("Minigame Common Settings.ingredientsDefineQuality", true);
        setBoolean("Durability & Grinding.grindingToggle", true);
        setBoolean("Blueprint & Tool Types.expertAboveIncreaseBlueprintToggle", true);
        setBoolean("Knapping Settings.useFlintGetRock", true);
        setBoolean("Casting.castingToggle", true);

        // Reset all integer values
        setInt("Loot Quality.weightPoorQuality", 50);
        setInt("Loot Quality.weightWellQuality", 30);
        setInt("Loot Quality.weightExpertQuality", 10);
        setInt("Loot Quality.weightPerfectQuality", 5);
        setInt("Loot Quality.weightMasterQuality", 1);
        setInt("Arrow Fletching Settings.maxPotionTipping", 8);
        setInt("Minigame Common Settings.maxAnvilDistance", 100);
        setInt("Stone Smithing Anvil.max_uses", 64);
        setInt("Heated Items.heatedItemCooldownTicks", 1200);
        setInt("Blueprint & Tool Types.poorMaxUse", 5);
        setInt("Blueprint & Tool Types.wellMaxUse", 10);
        setInt("Blueprint & Tool Types.expertMaxUse", 20);
        setInt("Blueprint & Tool Types.perfectMaxUse", 50);
        setInt("Blueprint & Tool Types.masterMaxUse", 0);
        setInt("Casting.firedCastDurability", 5);

        // Reset forging zone defaults
        resetForgingZoneDefaults();

        // Reset double values
        setDouble("Minigame Common Settings.masterQualityChance", 0.05);
        setDouble("Minigame Common Settings.masterFromIngredientChance", 0.5);
        setDouble("Minigame Common Settings.perfectQualityScore", 0.9);
        setDouble("Minigame Common Settings.expertQualityScore", 0.6);
        setDouble("Minigame Common Settings.wellQualityScore", 0.3);
        setDouble("Durability & Grinding.durability", 1.0);
        setDouble("Durability & Grinding.durabilityReduce", 0.05);
        setDouble("Durability & Grinding.damageRestore", 0.1);
        setDouble("Quality Failure Chances.failOnWellQualityChance", 0.1);
        setDouble("Quality Failure Chances.failOnExpertQualityChance", 0.05);
        setDouble("Knapping Settings.rockDroppingChance", 0.1);
        setDouble("Knapping Settings.flintBreakingChance", 0.1);

        // Reset weapon bonuses
        resetWeaponBonuses();
        resetArmorBonuses();
        resetDurabilityBonuses();

        // Refresh the config list to show updated values
        this.configList.children().clear();
        buildEntries();
    }

    private void resetForgingZoneDefaults() {
        // Default
        setInt("Default (No Blueprint).zoneStartingSize", 20);
        setDouble("Default (No Blueprint).zoneShrinkFactor", 0.9);
        setInt("Default (No Blueprint).minPerfectZone", 8);
        setDouble("Default (No Blueprint).arrowSpeed", 2.0);
        setDouble("Default (No Blueprint).arrowSpeedIncrease", 0.6);
        setDouble("Default (No Blueprint).maxArrowSpeed", 8.0);

        // Poor
        setInt("Poorly Forged.zoneStartingSize", 30);
        setDouble("Poorly Forged.zoneShrinkFactor", 0.9);
        setInt("Poorly Forged.minPerfectZone", 15);
        setDouble("Poorly Forged.arrowSpeed", 1.5);
        setDouble("Poorly Forged.arrowSpeedIncrease", 0.5);
        setDouble("Poorly Forged.maxArrowSpeed", 4.0);

        // Well
        setInt("Well Forged.zoneStartingSize", 20);
        setDouble("Well Forged.zoneShrinkFactor", 0.8);
        setInt("Well Forged.minPerfectZone", 12);
        setDouble("Well Forged.arrowSpeed", 2.0);
        setDouble("Well Forged.arrowSpeedIncrease", 0.7);
        setDouble("Well Forged.maxArrowSpeed", 5.0);

        // Expert
        setInt("Expertly Forged.zoneStartingSize", 18);
        setDouble("Expertly Forged.zoneShrinkFactor", 0.8);
        setInt("Expertly Forged.minPerfectZone", 10);
        setDouble("Expertly Forged.arrowSpeed", 2.5);
        setDouble("Expertly Forged.arrowSpeedIncrease", 0.85);
        setDouble("Expertly Forged.maxArrowSpeed", 6.0);

        // Perfect
        setInt("Perfectly Forged.zoneStartingSize", 15);
        setDouble("Perfectly Forged.zoneShrinkFactor", 0.8);
        setInt("Perfectly Forged.minPerfectZone", 10);
        setDouble("Perfectly Forged.arrowSpeed", 3.0);
        setDouble("Perfectly Forged.arrowSpeedIncrease", 1.0);
        setDouble("Perfectly Forged.maxArrowSpeed", 7.0);

        // Master
        setInt("Masterwork.zoneStartingSize", 12);
        setDouble("Masterwork.zoneShrinkFactor", 0.7);
        setInt("Masterwork.minPerfectZone", 8);
        setDouble("Masterwork.arrowSpeed", 3.5);
        setDouble("Masterwork.arrowSpeedIncrease", 1.2);
        setDouble("Masterwork.maxArrowSpeed", 8.0);
    }

    private void resetWeaponBonuses() {
        // Damage bonuses
        setDouble("Weapon Bonuses.masterWeaponDamage", 3.0);
        setDouble("Weapon Bonuses.perfectWeaponDamage", 2.0);
        setDouble("Weapon Bonuses.expertWeaponDamage", 1.5);
        setDouble("Weapon Bonuses.wellWeaponDamage", 0.0);
        setDouble("Weapon Bonuses.poorWeaponDamage", -1.0);

        // Speed bonuses
        setDouble("Weapon Bonuses.masterWeaponSpeed", 1.0);
        setDouble("Weapon Bonuses.perfectWeaponSpeed", 0.5);
        setDouble("Weapon Bonuses.expertWeaponSpeed", 0.25);
        setDouble("Weapon Bonuses.wellWeaponSpeed", 0.0);
        setDouble("Weapon Bonuses.poorWeaponSpeed", -0.5);
    }

    private void resetArmorBonuses() {
        setDouble("Armor Bonuses.masterArmorBonus", 2.0);
        setDouble("Armor Bonuses.perfectArmorBonus", 1.5);
        setDouble("Armor Bonuses.expertArmorBonus", 1.0);
        setDouble("Armor Bonuses.wellArmorBonus", 0.0);
        setDouble("Armor Bonuses.poorArmorBonus", -1.0);
    }

    private void resetDurabilityBonuses() {
        setDouble("Durability Bonuses.masterDurabilityBonus", 1.6);
        setDouble("Durability Bonuses.perfectDurabilityBonus", 1.5);
        setDouble("Durability Bonuses.expertDurabilityBonus", 1.3);
        setDouble("Durability Bonuses.wellDurabilityBonus", 1.0);
        setDouble("Durability Bonuses.poorDurabilityBonus", 0.7);
    }
}
package net.stirdrem.overgeared.platform.services;

/**
 * Common configuration helper service interface.
 */
public interface ConfigHelper {

    /**
     * Default value of break temperature
     */
    int BREAK_TEMPERATURE_DEFAULT = 1000;
    /**
     * Config description of the break temperature
     */
    String BREAK_TEMPERATURE_DESCRIPTION = "Minimum temperature of fluid at which the Wooden Bucket breaks when emptied. (default: " + BREAK_TEMPERATURE_DEFAULT + ") (Deactivation by setting a number larger than the hottest fluid)";
    /**
     * Minimal value of the break temperature
     */
    int BREAK_TEMPERATURE_MIN = -10000;
    /**
     * Maximal value of the break temperature
     */
    int BREAK_TEMPERATURE_MAX = 10000;
    /**
     * Default value of burning temperature
     */
    int BURNING_TEMPERATURE_DEFAULT = 1000;
    /**
     * Config description of the burning temperature
     */
    String BURNING_TEMPERATURE_DESCRIPTION = "Fluid temperature from which you get a burning effect if a Wooden Bucket contains such a hot fluid. (default: " + BURNING_TEMPERATURE_DEFAULT + ") (Deactivation by setting a number larger than the hottest fluid)";
    /**
     * Minimal value of the burning temperature
     */
    int BURNING_TEMPERATURE_MIN = -10000;
    /**
     * Maximal value of the burning temperature
     */
    int BURNING_TEMPERATURE_MAX = 10000;
    /**
     * Default value of durability
     */
    int DURABILITY_DEFAULT = 25;
    /**
     * Config description of the durability
     */
    String DURABILITY_DESCRIPTION = "Defines the maximum durability of Wooden Bucket. (default: " + DURABILITY_DEFAULT + ", 0: deactivates the durability)";
    /**
     * Minimal value of the durability
     */
    int DURABILITY_MIN = 0;
    /**
     * Maximal value of the durability
     */
    int DURABILITY_MAX = 10000;
    /**
     * Default value of fish obtaining option
     */
    boolean FISH_OBTAINING_ENABLED_DEFAULT = true;
    /**
     * Config description of the durability
     */
    String FISH_OBTAINING_ENABLED_DESCRIPTION = "Whether or not obtaining fish with a Wooden Bucket should be enabled. (default: " + FISH_OBTAINING_ENABLED_DEFAULT + ")";
    /**
     * Default value of freezing temperature
     */
    int FREEZING_TEMPERATURE_DEFAULT = -1;
    /**
     * Config description of the freezing temperature
     */
    String FREEZING_TEMPERATURE_DESCRIPTION = "Fluid temperature from which you get a freezing effect if a Wooden Bucket contains this fluid. (default: " + FREEZING_TEMPERATURE_DEFAULT + ") (Deactivation by setting a number lower than the coldest fluid)";
    /**
     * Minimal value of the freezing temperature
     */
    int FREEZING_TEMPERATURE_MIN = -10000;
    /**
     * Maximal value of the freezing temperature
     */
    int FREEZING_TEMPERATURE_MAX = 10000;
    /**
     * Default value of milking option
     */
    boolean MILKING_ENABLED_DEFAULT = true;
    /**
     * Config description of the milking option
     */
    String MILKING_ENABLED_DESCRIPTION = "Whether or not milking entities with a Wooden Bucket should be enabled. (default: " + MILKING_ENABLED_DEFAULT + ")";


    /**
     * Initialization method for the Service implementations.
     */
    void init();

    /**
     * Gets the configured break temperature value.
     *
     * @return configured break temperature value
     */
    int getBreakTemperature();

    /**
     * Gets the configured burning temperature value.
     *
     * @return configured burning temperature value
     */
    int getBurningTemperature();

    /**
     * Gets the configured durability value.
     *
     * @return configured durability value
     */
    int getDurability();

    /**
     * Gets the fish obtaining enabled value.
     *
     * @return configured fish obtaining enabled value
     */
    boolean isFishObtainingEnabled();

    /**
     * Gets the configured freezing temperature value.
     *
     * @return configured freezing temperature value
     */
    int getFreezingTemperature();

    /**
     * Gets the configured milking enabled value.
     *
     * @return configured milking enabled value
     */
    boolean isMilkingEnabled();

}

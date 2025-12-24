package net.stirdrem.overgeared.advancement;

import net.minecraft.advancements.CriteriaTriggers;

public class ModAdvancementTriggers {

    public static final MakeSmithingAnvilTrigger MAKE_SMITHING_ANVIL =
            new MakeSmithingAnvilTrigger();
    public static final KnappingAdvancementTrigger KNAPPING =
            new KnappingAdvancementTrigger();
    public static final ForgingQualityTrigger FORGING_QUALITY =
            new ForgingQualityTrigger();
    public static final BlueprintQualityTrigger BLUEPRINT_QUALITY =
            new BlueprintQualityTrigger();
    public static final MaxLevelBlueprintAdvancementTrigger MAX_LEVEL_BLUEPRINT =
            new MaxLevelBlueprintAdvancementTrigger();

    public static void register() {
        CriteriaTriggers.register(MAKE_SMITHING_ANVIL);
        CriteriaTriggers.register(KNAPPING);
        CriteriaTriggers.register(FORGING_QUALITY);
        CriteriaTriggers.register(BLUEPRINT_QUALITY);
        CriteriaTriggers.register(MAX_LEVEL_BLUEPRINT);
    }
}

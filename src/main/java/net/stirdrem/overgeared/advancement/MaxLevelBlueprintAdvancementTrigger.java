package net.stirdrem.overgeared.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.OvergearedMod;

public class MaxLevelBlueprintAdvancementTrigger extends SimpleCriterionTrigger<MaxLevelBlueprintAdvancementTrigger.TriggerInstance> {

    public static final ResourceLocation ID =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "max_level_blueprint");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json,
                                             ContextAwarePredicate player,
                                             DeserializationContext context) {
        return new TriggerInstance(player);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    // ---------------- Trigger Instance ----------------

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        public TriggerInstance(ContextAwarePredicate player) {
            super(ID, player);
        }

        public static TriggerInstance instance() {
            return new TriggerInstance(ContextAwarePredicate.ANY);
        }
    }
}

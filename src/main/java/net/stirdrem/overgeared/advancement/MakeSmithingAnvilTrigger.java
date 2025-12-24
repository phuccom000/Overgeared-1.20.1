package net.stirdrem.overgeared.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.OvergearedMod;

import javax.annotation.Nullable;

public class MakeSmithingAnvilTrigger
        extends SimpleCriterionTrigger<MakeSmithingAnvilTrigger.TriggerInstance> {

    public static final ResourceLocation ID =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "make_smithing_anvil");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(
            JsonObject json,
            ContextAwarePredicate player,
            DeserializationContext context
    ) {
        String tier = null;

        if (json.has("tier")) {
            tier = json.get("tier").getAsString();
        }

        return new TriggerInstance(player, tier);
    }

    public void trigger(ServerPlayer player, String tierUsed) {
        this.trigger(player, instance -> instance.matches(tierUsed));
    }

    // ---------------- Trigger Instance ----------------

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        @Nullable
        private final String tier;

        public TriggerInstance(ContextAwarePredicate player,
                               @Nullable String tier) {
            super(ID, player);
            this.tier = tier;
        }

        public boolean matches(String tierUsed) {
            // No condition = always match
            if (this.tier == null) {
                return true;
            }
            return this.tier.equals(tierUsed);
        }
    }
}


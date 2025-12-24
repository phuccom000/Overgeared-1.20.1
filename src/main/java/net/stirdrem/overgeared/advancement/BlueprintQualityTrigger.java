package net.stirdrem.overgeared.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.stirdrem.overgeared.OvergearedMod;

import javax.annotation.Nullable;

public class BlueprintQualityTrigger
        extends SimpleCriterionTrigger<BlueprintQualityTrigger.TriggerInstance> {

    public static final ResourceLocation ID =
            new ResourceLocation(OvergearedMod.MOD_ID, "blueprint_quality");

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
        @Nullable String quality = null;

        if (json.has("quality")) {
            quality = GsonHelper.getAsString(json, "quality");
        }

        return new TriggerInstance(player, quality);
    }

    /**
     * Call when forging completes
     */
    public void trigger(ServerPlayer player, String forgedQuality) {
        this.trigger(player, inst -> inst.matches(forgedQuality));
    }

    // ─────────────────────────────────────────────────────────────

    public static class TriggerInstance
            extends AbstractCriterionTriggerInstance {

        @Nullable
        private final String requiredQuality;

        public TriggerInstance(ContextAwarePredicate player,
                               @Nullable String requiredQuality) {
            super(ID, player);
            this.requiredQuality = requiredQuality;
        }

        public boolean matches(String forgedQuality) {
            // No condition → always match
            if (this.requiredQuality == null) {
                return true;
            }
            return this.requiredQuality.equals(forgedQuality);
        }
    }
}

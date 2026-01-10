package net.stirdrem.overgeared.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class BlueprintQualityTrigger extends SimpleCriterionTrigger<BlueprintQualityTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    /**
     * Call when blueprint quality is set
     */
    public void trigger(ServerPlayer player, String blueprintQuality) {
        this.trigger(player, inst -> inst.matches(blueprintQuality));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> requiredQuality)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.optionalFieldOf("quality").forGetter(TriggerInstance::requiredQuality))
                .apply(instance, TriggerInstance::new));

        public boolean matches(String blueprintQuality) {
            // No condition → always match
            if (requiredQuality.isEmpty()) return true;
            return requiredQuality.get().equals(blueprintQuality);
        }
    }
}

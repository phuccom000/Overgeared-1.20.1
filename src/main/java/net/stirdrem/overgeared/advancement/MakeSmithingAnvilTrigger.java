package net.stirdrem.overgeared.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.Optional;

public class MakeSmithingAnvilTrigger extends SimpleCriterionTrigger<MakeSmithingAnvilTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, String tierUsed) {
        this.trigger(player, instance -> instance.matches(tierUsed));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> tier)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.optionalFieldOf("tier").forGetter(TriggerInstance::tier))
                .apply(instance, TriggerInstance::new));

        public boolean matches(String tierUsed) {
            // No condition = always match
            if (tier.isEmpty()) return true;
            return tier.get().equals(tierUsed);
        }
    }
}

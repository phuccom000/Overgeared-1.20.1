package net.stirdrem.overgeared.sound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

//import net.minecraftforge.common.util.ForgeSoundType;
//import net.minecraftforge.eventbus.api.IEventBus;
//import net.minecraftforge.registries.DeferredRegister;
//import net.minecraftforge.registries.ForgeRegistries;
//import net.minecraftforge.registries.RegistryObject;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, OvergearedMod.MOD_ID);

    /*public static final Supplier<SoundEvent> METAL_DETECTOR_FOUND_ORE = registerSoundEvent("metal_detector_found_ore");

    public static final Supplier<SoundEvent> SOUND_BLOCK_BREAK = registerSoundEvent("sound_block_break");
    public static final Supplier<SoundEvent> SOUND_BLOCK_STEP = registerSoundEvent("sound_block_step");
    public static final Supplier<SoundEvent> SOUND_BLOCK_FALL = registerSoundEvent("sound_block_fall");
    public static final Supplier<SoundEvent> SOUND_BLOCK_PLACE = registerSoundEvent("sound_block_place");
    public static final Supplier<SoundEvent> SOUND_BLOCK_HIT = registerSoundEvent("sound_block_hit");*/

    public static final Supplier<SoundEvent> ANVIL_HIT = registerSoundEvent("anvil_hit");
    public static final Supplier<SoundEvent> FORGING_COMPLETE = registerSoundEvent("forging_complete");
    public static final Supplier<SoundEvent> FORGING_FAILED = registerSoundEvent("forging_failed");


    /*public static final ForgeSoundType SOUND_BLOCK_SOUNDS = new ForgeSoundType(1f, 1f,
            ModSounds.SOUND_BLOCK_BREAK, ModSounds.SOUND_BLOCK_STEP, ModSounds.SOUND_BLOCK_PLACE,
            ModSounds.SOUND_BLOCK_HIT, ModSounds.SOUND_BLOCK_FALL);*/


    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(OvergearedMod.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
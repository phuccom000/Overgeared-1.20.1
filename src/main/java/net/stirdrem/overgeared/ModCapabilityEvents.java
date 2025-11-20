package net.stirdrem.overgeared;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;


@EventBusSubscriber(modid = OvergearedMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilityEvents {

    @net.neoforged.bus.api.SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register for each concrete anvil BlockEntityType (since Abstract can't be registered directly)
        // Example for one tier; repeat for others (e.g., ModBlockEntities.BASIC_ANVIL_BE.get())
       /* event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,  // NeoForge's built-in item handler capability
                ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get(),  // Your DeferredHolder<BlockEntityType<ConcreteAnvilBE>>
                (blockEntity, side) -> {  // Provider: Returns IItemHandler; use side for sided logic if needed
                    AbstractSmithingAnvilBlockEntity anvil = blockEntity;
                    // Optional: Add side checks, e.g., if (side == Direction.DOWN) return null; // No input from bottom
                    return anvil.getItemHandler();  // Returns your itemHandler directly (no LazyOptional)
                }
        );*/
        // Add more registrations for other anvil types here
    }
}

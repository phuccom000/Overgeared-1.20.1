package net.stirdrem.overgeared;

import net.minecraft.core.Direction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;

public class ModCapabilities {

  public static void register(IEventBus modBus) {
    modBus.addListener(ModCapabilities::registerCapabilities);
  }

  private static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.ALLOY_FURNACE_BE.get(),
            (blockEntity, side) -> {
              if (side == null) {
                return blockEntity.getItemHandler();
              }
              if (side == Direction.UP) {
                return new SidedInvWrapper(blockEntity, Direction.UP);
              } else if (side == Direction.DOWN) {
                return new SidedInvWrapper(blockEntity, Direction.DOWN);
              } else {
                return new SidedInvWrapper(blockEntity, Direction.NORTH);
              }
            }
    );
  }
}
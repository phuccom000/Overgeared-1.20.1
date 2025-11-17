package net.stirdrem.overgeared.heatedtem;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HeatedItemProvider implements ICapabilityProvider {
    private final LazyOptional<HeatedItemCapability> inst = LazyOptional.of(HeatedItemCapability::new);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == CapabilityRegistry.HEATED_ITEM ? inst.cast() : LazyOptional.empty();
    }
}

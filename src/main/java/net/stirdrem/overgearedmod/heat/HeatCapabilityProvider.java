package net.stirdrem.overgearedmod.heat;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeatCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<HeatCapability> ITEM_HEAT = CapabilityManager.get(new CapabilityToken<HeatCapability>() {
    });

    private HeatCapability heat = null;
    private LazyOptional<HeatCapability> optional = LazyOptional.of(this::createHeatCapability);

    private HeatCapability createHeatCapability() {
        if (this.heat == null) {
            this.heat = new HeatCapability();
        }
        return this.heat;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ITEM_HEAT) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createHeatCapability().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createHeatCapability().loadNBTData(nbt);
    }
}

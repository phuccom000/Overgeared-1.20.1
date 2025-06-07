package net.stirdrem.overgeared.minigame;

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

public class AnvilMinigameProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<AnvilMinigame> ANVIL_MINIGAME = CapabilityManager.get(new CapabilityToken<AnvilMinigame>() {
    });

    private AnvilMinigame game = null;
    private final LazyOptional<AnvilMinigame> optional = LazyOptional.of(this::createAnvilMinigame);

    private AnvilMinigame createAnvilMinigame() {
        if (this.game == null) {
            this.game = new AnvilMinigame();
        }

        return this.game;
    }

    @Override

    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ANVIL_MINIGAME) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createAnvilMinigame().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createAnvilMinigame().loadNBTData(nbt);
    }
}

package net.stirdrem.overgeared.heat;


import net.minecraft.nbt.CompoundTag;

public class HeatCapability {
    private int heat;
    private final int MIN_HEAT = 0;
    private final int MAX_HEAT = 10;

    public int getHeat() {
        return heat;
    }

    public int getMaxHeat() {
        return MAX_HEAT;
    }

    public void addHeat(int add) {
        this.heat = Math.min(heat + add, MAX_HEAT);
    }

    public void subHeat(int sub) {
        this.heat = Math.max(heat - sub, MIN_HEAT);
    }

    public void setHeat(int value) {
        heat = value < 0 ? 0 : Math.min(value, MAX_HEAT);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("heat", heat);
    }

    public void loadNBTData(CompoundTag nbt) {
        heat = nbt.getInt("heat");
    }
}
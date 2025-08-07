package net.stirdrem.overgeared.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class FletchingRecipeMaker {
    private List<ItemStack> getTippedArrowPotions() {
        List<ItemStack> potions = new ArrayList<>();
        // Example: Add all potion types (you can customize this)
        for (Potion potion : ForgeRegistries.POTIONS) {
            if (potion != Potions.EMPTY) {
                potions.add(PotionUtils.setPotion(new ItemStack(Items.POTION), potion));
            }
        }
        return potions;
    }

    // Helper method to get lingering potions (for lingering arrows)
    private List<ItemStack> getLingeringPotions() {
        List<ItemStack> potions = new ArrayList<>();
        // Example: Add all lingering potion types
        for (Potion potion : ForgeRegistries.POTIONS) {
            if (potion != Potions.EMPTY) {
                potions.add(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion));
            }
        }
        return potions;
    }
}

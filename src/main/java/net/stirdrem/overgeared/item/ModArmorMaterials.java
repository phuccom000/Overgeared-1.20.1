package net.stirdrem.overgeared.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.List;
import java.util.Map;

import static net.minecraft.world.item.Items.COPPER_INGOT;

public final class ModArmorMaterials {

  public static final Holder<ArmorMaterial> STEEL = Holder.direct(new ArmorMaterial(
          Map.of(
                  ArmorItem.Type.HELMET, 3,
                  ArmorItem.Type.CHESTPLATE, 7,
                  ArmorItem.Type.LEGGINGS, 5,
                  ArmorItem.Type.BOOTS, 2
          ),
          12,
          SoundEvents.ARMOR_EQUIP_IRON,
          () -> Ingredient.of(ModItems.STEEL_INGOT.get()),
          List.of(
                  new ArmorMaterial.Layer(OvergearedMod.loc("steel"))
          ),
          1f,
          0f
  ));

  public static final Holder<ArmorMaterial> COPPER = Holder.direct(new ArmorMaterial(
          Map.of(
                  ArmorItem.Type.HELMET, 1,
                  ArmorItem.Type.CHESTPLATE, 4,
                  ArmorItem.Type.LEGGINGS, 3,
                  ArmorItem.Type.BOOTS, 1
          ),
          15,
          SoundEvents.ARMOR_EQUIP_IRON,
          () -> Ingredient.of(COPPER_INGOT),
          List.of(
                  new ArmorMaterial.Layer(OvergearedMod.loc("copper"))
          ),
          0f,
          0f
  ));

  private static final int[] BASE_DURABILITY = {11, 16, 15, 13};

  public static int getDurabilityForType(Holder<ArmorMaterial> material, ArmorItem.Type pType) {
    int durabilityMultiplier = 0;
    if (material.equals(STEEL)) {
        durabilityMultiplier = 26;
    } else if (material.equals(COPPER)) {
        durabilityMultiplier = 10;
    }
    return BASE_DURABILITY[pType.ordinal()] * durabilityMultiplier;
  }
}
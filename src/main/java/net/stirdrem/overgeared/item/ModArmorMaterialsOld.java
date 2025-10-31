package net.stirdrem.overgeared.item;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
@Deprecated
public enum ModArmorMaterialsOld implements Holder<ArmorMaterial> {



    STEEL("steel", 26, new int[]{3, 7, 5, 2}, 12,
            (SoundEvent) SoundEvents.ARMOR_EQUIP_IRON, 1f, 0f, () -> Ingredient.of(ModItems.STEEL_INGOT.get())),

    COPPER("copper", 10, new int[]{1, 4, 3, 1}, 15,
            (SoundEvent) SoundEvents.ARMOR_EQUIP_IRON, 0f, 0f, () -> Ingredient.of(Items.COPPER_INGOT));

    private final String name;
    private final int durabilityMultiplier;
    private final int[] protectionAmounts;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    private static final int[] BASE_DURABILITY = {11, 16, 15, 13};

    ModArmorMaterialsOld(String name, int durabilityMultiplier, int[] protectionAmounts, int enchantmentValue, SoundEvent equipSound,
                         float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int DurabilityForType(ArmorItem.Type pType) {
        return BASE_DURABILITY[pType.ordinal()] * this.durabilityMultiplier;
    }

    @Override
    public int getDefense(ArmorItem.Type pType) {
        return this.protectionAmounts[pType.ordinal()];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        return OvergearedMod.MOD_ID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    @Override
    public ArmorMaterial value() {
        return null;
    }

    @Override
    public boolean isBound() {
        return false;
    }

    @Override
    public boolean is(ResourceLocation resourceLocation) {
        return false;
    }

    @Override
    public boolean is(ResourceKey<ArmorMaterial> resourceKey) {
        return false;
    }

    @Override
    public boolean is(Predicate<ResourceKey<ArmorMaterial>> predicate) {
        return false;
    }

    @Override
    public boolean is(TagKey<ArmorMaterial> tagKey) {
        return false;
    }

    @Override
    public boolean is(Holder<ArmorMaterial> holder) {
        return false;
    }

    @Override
    public Stream<TagKey<ArmorMaterial>> tags() {
        return Stream.empty();
    }

    @Override
    public Either<ResourceKey<ArmorMaterial>, ArmorMaterial> unwrap() {
        return null;
    }

    @Override
    public Optional<ResourceKey<ArmorMaterial>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public Kind kind() {
        return null;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<ArmorMaterial> holderOwner) {
        return false;
    }
}
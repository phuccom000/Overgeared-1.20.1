package net.stirdrem.overgeared.item.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.stirdrem.overgeared.item.armor.custom.ArmorModelHelper;
import net.stirdrem.overgeared.item.armor.model.CustomCopperHelmet;
import net.stirdrem.overgeared.item.armor.model.CustomCopperLeggings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CopperLeggings extends ArmorItem {
    public CopperLeggings(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                Map<String, ModelPart> parts = new HashMap<>();
                parts.put("head", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("body", new CustomCopperLeggings<>(Minecraft.getInstance().getEntityModels().bakeLayer(CustomCopperLeggings.LAYER_LOCATION)).Body);
                parts.put("right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("right_leg", new CustomCopperLeggings<>(Minecraft.getInstance().getEntityModels().bakeLayer(CustomCopperLeggings.LAYER_LOCATION)).RightLeg);
                parts.put("left_leg", new CustomCopperLeggings<>(Minecraft.getInstance().getEntityModels().bakeLayer(CustomCopperLeggings.LAYER_LOCATION)).LeftLeg);

                return new HumanoidModel<>(new ModelPart(Collections.emptyList(), parts));
            }
        });

    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "overgeared:textures/models/armor/copper_layer_2.png";
    }
}

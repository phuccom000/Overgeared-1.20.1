package net.stirdrem.overgeared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.armor.custom.ArmorModelHelper;
import net.stirdrem.overgeared.item.armor.model.CopperHelmet;
import net.stirdrem.overgeared.item.armor.model.CopperLeggings;
import net.stirdrem.overgeared.screen.ModMenuTypes;
import net.stirdrem.overgeared.screen.RockKnappingMenu;
import net.stirdrem.overgeared.screen.RockKnappingScreen;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientInit.init();
    }
    
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        // event.register(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU.get(), SteelSmithingAnvilScreen::new);
        // event.register(ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU.get(), TierASmithingAnvilScreen::new);
        // event.register(ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU.get(), TierBSmithingAnvilScreen::new);
        // event.register(ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(), StoneSmithingAnvilScreen::new);
        event.register((MenuType<RockKnappingMenu>)ModMenuTypes.ROCK_KNAPPING_MENU.get(), RockKnappingScreen::new);
        // event.register(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU.get(), BlueprintWorkbenchScreen::new);
        // event.register(ModMenuTypes.FLETCHING_STATION_MENU.get(), FletchingStationScreen::new);
        // event.register(ModMenuTypes.ALLOY_SMELTER_MENU.get(), AlloySmelterScreen::new);
        // event.register(ModMenuTypes.NETHER_ALLOY_SMELTER_MENU.get(), NetherAlloySmelterScreen::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CopperHelmet.LAYER_LOCATION, CopperHelmet::createBodyLayer);
        event.registerLayerDefinition(CopperLeggings.LAYER_LOCATION, CopperLeggings::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return ArmorModelHelper.withPart("head", new CopperHelmet<>(Minecraft.getInstance().getEntityModels().bakeLayer(CopperHelmet.LAYER_LOCATION)).Head);
            }
        }, ModItems.COPPER_HELMET);

        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                Map<String, ModelPart> parts = new HashMap<>();
                parts.put("head", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("body", new CopperLeggings<>(Minecraft.getInstance().getEntityModels().bakeLayer(CopperLeggings.LAYER_LOCATION)).Body);
                parts.put("right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
                parts.put("right_leg", new CopperLeggings<>(Minecraft.getInstance().getEntityModels().bakeLayer(CopperLeggings.LAYER_LOCATION)).RightLeg);
                parts.put("left_leg", new CopperLeggings<>(Minecraft.getInstance().getEntityModels().bakeLayer(CopperLeggings.LAYER_LOCATION)).LeftLeg);

                return new HumanoidModel<>(new ModelPart(Collections.emptyList(), parts));
            }
        }, ModItems.COPPER_LEGGINGS);
    }
}


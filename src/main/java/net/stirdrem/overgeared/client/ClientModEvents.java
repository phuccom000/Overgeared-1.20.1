package net.stirdrem.overgeared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.renderer.SmithingAnvilBlockEntityRenderer;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.entity.renderer.LingeringArrowEntityRenderer;
import net.stirdrem.overgeared.entity.renderer.UpgradeArrowEntityRenderer;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.armor.custom.ArmorModelHelper;
import net.stirdrem.overgeared.item.armor.model.CopperHelmet;
import net.stirdrem.overgeared.item.armor.model.CopperLeggings;
import net.stirdrem.overgeared.item.custom.LingeringArrowItem;
import net.stirdrem.overgeared.item.custom.UpgradeArrowItem;
import net.stirdrem.overgeared.screen.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModList.get().getModContainerById(OvergearedMod.MOD_ID).orElseThrow()
                .registerExtensionPoint(
                        IConfigScreenFactory.class,
                        (container, parent) -> new OvergearedConfigScreen(parent));
        
        event.enqueueWork(() -> {
            // Register item properties for arrow potion type variants
            // For LINGERING_ARROW, always show lingering variant when potion is present
            ItemProperties.register(ModItems.LINGERING_ARROW.get(), OvergearedMod.loc("potion_type"),
                    (stack, level, entity, seed) -> {
                        PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                        if (potionContents.potion().isPresent() || potionContents.hasEffects()) {
                            return 2.0f; // Lingering arrows always use lingering model
                        }
                        return 0.0f; // Base model
                    });
            
            // For upgrade arrows, check LINGERING_STATUS to differentiate tipped vs lingering
            registerArrowProperties(ModItems.IRON_UPGRADE_ARROW.get());
            registerArrowProperties(ModItems.STEEL_UPGRADE_ARROW.get());
            registerArrowProperties(ModItems.DIAMOND_UPGRADE_ARROW.get());
            
            // Register arrow color handlers
            ItemColors itemColors = Minecraft.getInstance().getItemColors();
            itemColors.register(LingeringArrowItem::getColor, ModItems.LINGERING_ARROW.get());
            itemColors.register(UpgradeArrowItem::getColor, 
                ModItems.IRON_UPGRADE_ARROW.get(),
                ModItems.STEEL_UPGRADE_ARROW.get(),
                ModItems.DIAMOND_UPGRADE_ARROW.get());
        });
    }
    
    private static void registerArrowProperties(Item item) {
        ItemProperties.register(item, OvergearedMod.loc("potion_type"),
                (stack, level, entity, seed) -> {
                    PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                    boolean isLingering = stack.getOrDefault(ModComponents.LINGERING_STATUS, false);
                    
                    if (isLingering && (potionContents.potion().isPresent() || potionContents.hasEffects())) {
                        return 2.0f; // Lingering potion model
                    } else if (potionContents.potion().isPresent() || potionContents.hasEffects()) {
                        return 1.0f; // Tipped potion model
                    }
                    return 0.0f; // Base model
                });
    }
    
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU.get(), SteelSmithingAnvilScreen::new);
        event.register(ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU.get(), TierASmithingAnvilScreen::new);
        event.register(ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU.get(), TierBSmithingAnvilScreen::new);
        event.register(ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(), StoneSmithingAnvilScreen::new);
        event.register(ModMenuTypes.ROCK_KNAPPING_MENU.get(), RockKnappingScreen::new);
        event.register(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU.get(), BlueprintWorkbenchScreen::new);
        event.register(ModMenuTypes.FLETCHING_STATION_MENU.get(), FletchingStationScreen::new);
        event.register(ModMenuTypes.ALLOY_SMELTER_MENU.get(), AlloySmelterScreen::new);
        event.register(ModMenuTypes.NETHER_ALLOY_SMELTER_MENU.get(), NetherAlloySmelterScreen::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CopperHelmet.LAYER_LOCATION, CopperHelmet::createBodyLayer);
        event.registerLayerDefinition(CopperLeggings.LAYER_LOCATION, CopperLeggings::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LINGERING_ARROW.get(), LingeringArrowEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.UPGRADE_ARROW.get(), UpgradeArrowEntityRenderer::new);
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

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TIER_A_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TIER_B_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
    }

    /**
     * MOD bus events for client-side registration
     */
    @EventBusSubscriber(modid = OvergearedMod.MOD_ID, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerGuiLayers(RegisterGuiLayersEvent event) {
            // Register anvil minigame overlay below the hotbar
            event.registerBelow(VanillaGuiLayers.HOTBAR, AnvilMinigameOverlay.ID, AnvilMinigameOverlay.INSTANCE);
            // Register popup overlay above the minigame overlay
            event.registerAbove(AnvilMinigameOverlay.ID, PopupOverlay.ID, PopupOverlay.INSTANCE);
        }
    }
}


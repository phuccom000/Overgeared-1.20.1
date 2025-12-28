package net.stirdrem.overgeared.client;

import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.screen.ModMenuTypes;
import net.stirdrem.overgeared.screen.RockKnappingMenu;
import net.stirdrem.overgeared.screen.RockKnappingScreen;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

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
}


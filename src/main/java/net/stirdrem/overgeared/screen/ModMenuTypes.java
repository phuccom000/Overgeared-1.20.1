package net.stirdrem.overgeared.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, OvergearedMod.MOD_ID);


//    public static final RegistryObject<MenuType<SteelSmithingAnvilMenu>> STEEL_SMITHING_ANVIL_MENU =
//            registerMenuType("smithing_anvil_menu", SteelSmithingAnvilMenu::new);
//
//
//    public static final RegistryObject<MenuType<TierASmithingAnvilMenu>> TIER_A_SMITHING_ANVIL_MENU =
//            registerMenuType("tier_a_smithing_anvil_menu", TierASmithingAnvilMenu::new);
//
//
//    public static final RegistryObject<MenuType<TierBSmithingAnvilMenu>> TIER_B_SMITHING_ANVIL_MENU =
//            registerMenuType("tier_b_smithing_anvil_menu", TierBSmithingAnvilMenu::new);
//
//    public static final RegistryObject<MenuType<StoneSmithingAnvilMenu>> STONE_SMITHING_ANVIL_MENU =
//            registerMenuType("stone_smithing_anvil_menu", StoneSmithingAnvilMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<?>> ROCK_KNAPPING_MENU =
            registerMenuType("rock_knapping_menu", RockKnappingMenu::new);

//    public static final RegistryObject<MenuType<BlueprintWorkbenchMenu>> BLUEPRINT_WORKBENCH_MENU =
//            MENUS.register("blueprint_workbench",
//                    () -> new MenuType<>(BlueprintWorkbenchMenu::new, FeatureFlagSet.of()));
//
//    public static final RegistryObject<MenuType<FletchingStationMenu>> FLETCHING_STATION_MENU =
//            MENUS.register("fletching_station",
//                    () -> new MenuType<>(FletchingStationMenu::new, FeatureFlagSet.of()));
//
    public static final DeferredHolder<MenuType<?>, MenuType<?>> ALLOY_SMELTER_MENU =
            registerMenuType("alloy_smelter_menu", AlloySmelterMenu::new);
//
//    public static final RegistryObject<MenuType<NetherAlloySmelterMenu>> NETHER_ALLOY_SMELTER_MENU =
//            registerMenuType("nether_alloy_smelter_menu", NetherAlloySmelterMenu::new);
    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<?>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
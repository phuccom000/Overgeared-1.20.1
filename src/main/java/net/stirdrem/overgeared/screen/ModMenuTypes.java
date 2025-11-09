package net.stirdrem.overgeared.screen;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.OvergearedMod;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, OvergearedMod.MOD_ID);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    public static final RegistryObject<MenuType<SteelSmithingAnvilMenu>> STEEL_SMITHING_ANVIL_MENU =
            registerMenuType("smithing_anvil_menu", SteelSmithingAnvilMenu::new);


    public static final RegistryObject<MenuType<TierASmithingAnvilMenu>> TIER_A_SMITHING_ANVIL_MENU =
            registerMenuType("tier_a_smithing_anvil_menu", TierASmithingAnvilMenu::new);


    public static final RegistryObject<MenuType<TierBSmithingAnvilMenu>> TIER_B_SMITHING_ANVIL_MENU =
            registerMenuType("tier_b_smithing_anvil_menu", TierBSmithingAnvilMenu::new);

    public static final RegistryObject<MenuType<StoneSmithingAnvilMenu>> STONE_SMITHING_ANVIL_MENU =
            registerMenuType("stone_smithing_anvil_menu", StoneSmithingAnvilMenu::new);

    public static final RegistryObject<MenuType<RockKnappingMenu>> ROCK_KNAPPING_MENU =
            registerMenuType("rock_knapping_menu", RockKnappingMenu::new);

    public static final RegistryObject<MenuType<BlueprintWorkbenchMenu>> BLUEPRINT_WORKBENCH_MENU =
            MENUS.register("blueprint_workbench",
                    () -> new MenuType<>(BlueprintWorkbenchMenu::new, FeatureFlagSet.of()));

    public static final RegistryObject<MenuType<FletchingStationMenu>> FLETCHING_STATION_MENU =
            MENUS.register("fletching_station",
                    () -> new MenuType<>(FletchingStationMenu::new, FeatureFlagSet.of()));

    public static final RegistryObject<MenuType<AlloySmelterMenu>> ALLOY_SMELTER_MENU =
            registerMenuType("alloy_smelter_menu", AlloySmelterMenu::new);

    public static final RegistryObject<MenuType<NetherAlloySmelterMenu>> NETHER_ALLOY_SMELTER_MENU =
            registerMenuType("nether_alloy_smelter_menu", NetherAlloySmelterMenu::new);

}
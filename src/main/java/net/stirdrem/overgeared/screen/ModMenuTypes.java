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

    public static final RegistryObject<MenuType<SteelSmithingAnvilMenu>> STEEL_SMITHING_ANVIL_MENU =
            registerMenuType("smithing_anvil_menu", SteelSmithingAnvilMenu::new);

    public static final RegistryObject<MenuType<StoneSmithingAnvilMenu>> STONE_SMITHING_ANVIL_MENU =
            registerMenuType("stone_smithing_anvil_menu", StoneSmithingAnvilMenu::new);

    public static final RegistryObject<MenuType<RockKnappingMenu>> ROCK_KNAPPING_MENU =
            registerMenuType("rock_knapping_menu", RockKnappingMenu::new);

    public static final RegistryObject<MenuType<BlueprintWorkbenchMenu>> BLUEPRINT_WORKBENCH_MENU =
            MENUS.register("blueprint_workbench",
                    () -> new MenuType<>(BlueprintWorkbenchMenu::new, FeatureFlagSet.of()));

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
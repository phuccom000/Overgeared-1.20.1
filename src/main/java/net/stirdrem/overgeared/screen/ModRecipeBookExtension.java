/*
package net.stirdrem.overgeared.screen;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.recipe.ForgingRecipe;

@Mod(OvergearedMod.MOD_ID)
public class ModRecipeBookExtension {
    public static final boolean ENABLED = false;

    public static final String MOD_ID = OvergearedMod.MOD_ID;
    public static final RecipeBookType FORGING = RecipeBookType.create("FORGING");

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
    public static final RegistryObject<RecipeSerializer<ForgingRecipe>> FORGING_SERIALIZER =
            RECIPE_SERIALIZER.register("forging", () -> ForgingRecipe.Serializer.INSTANCE);

    public static final DeferredRegister<MenuType<?>> MENU_TYPE = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);
    public static final RegistryObject<MenuType<SmithingAnvilMenu>> RECIPE_BOOK_TEST_MENU_TYPE =
            MENU_TYPE.register("test_recipe_menu", () -> IForgeMenuType.create(SmithingAnvilMenu::new));

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPE = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MOD_ID);
    public static final RegistryObject<RecipeType<ForgingRecipe>> RECIPE_BOOK_TEST_RECIPE_TYPE = RECIPE_TYPE.register("test_recipe", () -> RecipeType.simple(getId("test_recipe")));

    public ModRecipeBookExtension() {
        if (!ENABLED)
            return;

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        RECIPE_SERIALIZER.register(modBus);
        MENU_TYPE.register(modBus);
        RECIPE_TYPE.register(modBus);

        MinecraftForge.EVENT_BUS.addListener(this::onRightClick);
    }

    private void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide)
            return;
        if (event.getLevel().getBlockState(event.getPos()).getBlock() == Blocks.GRASS_BLOCK)
            NetworkHooks.openScreen((ServerPlayer) event.getEntity(), new SimpleMenuProvider((id, inv, p) -> new SmithingAnvilMenu(id, inv, ContainerLevelAccess.create(event.getLevel(), event.getPos())), Component.literal("Test")));
    }

    public static ResourceLocation getId(String name) {
        return ResourceLocation.tryBuild(MOD_ID, name);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientHandler {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            if (!ENABLED)
                return;
            event.enqueueWork(() ->
            {
                MenuScreens.register(RECIPE_BOOK_TEST_MENU_TYPE.get(), SmithingAnvilScreen::new);
            });
        }

        @SubscribeEvent
        public static void onRegisterRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
            if (!ENABLED)
                return;
            RecipeBookExtensionClientHelper.init(event);
        }
    }

    public static class RecipeBookTestContainer extends SimpleContainer {
        public RecipeBookTestContainer() {
            super(8);
        }
    }
}

*/

package net.stirdrem.overgeared;

//import cech12.bucketlib.api.BucketLibApi;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.renderer.SmithingAnvilBlockEntityRenderer;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.client.RecipeBookExtensionClientHelper;
import net.stirdrem.overgeared.config.ServerConfig;
//import net.stirdrem.overgeared.core.waterbarrel.BarrelInteraction;
import net.stirdrem.overgeared.event.ModAttributes;
import net.stirdrem.overgeared.item.ModCreativeModeTabs;
import net.stirdrem.overgeared.item.ModItems;

import net.stirdrem.overgeared.loot.ModLootModifiers;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.ModRecipeSerializers;
import net.stirdrem.overgeared.screen.ModMenuTypes;
import net.stirdrem.overgeared.screen.SmithingAnvilScreen;
import net.stirdrem.overgeared.sound.ModSounds;
import net.stirdrem.overgeared.util.TickScheduler;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(OvergearedMod.MOD_ID)
public class OvergearedMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "overgeared";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    //public static final AnvilMinigameHandler SERVER_HANDLER = new AnvilMinigameHandler();
    public static final RecipeBookType RECIPE_TYPE_FORGING = RecipeBookType.create("FORGING");
    public static boolean polymorph;

    public OvergearedMod(FMLJavaModLoadingContext context) {
        //ServerConfig.registerConfig();

        IEventBus modEventBus = context.getModEventBus();

        ServerConfig.loadConfig(ServerConfig.SERVER_CONFIG, FMLPaths.GAMEDIR.get().resolve(FMLPaths.CONFIGDIR.get()).resolve(MOD_ID + "-common.toml"));

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);

        ModBlocks.register(modEventBus);

        ModBlockEntities.register(modEventBus);

        ModMenuTypes.register(modEventBus);

        ModRecipeSerializers.register(modEventBus);

        ModRecipeTypes.register(modEventBus);

        ModLootModifiers.register(modEventBus);

        ModSounds.register(modEventBus);

        ModAttributes.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(TickScheduler.class);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::sendImc);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);

        polymorph = ModList.get().isLoaded("polymorph");

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        //context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModMessages.register();
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.IRON_TONGS);
            event.accept(ModItems.STEEL_TONGS);
            event.accept(ModItems.SMITHING_HAMMER);
            event.accept(ModItems.STEEL_PICKAXE.get());
            event.accept(ModItems.STEEL_AXE.get());
            event.accept(ModItems.STEEL_SHOVEL.get());
            event.accept(ModItems.STEEL_HOE.get());
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.STEEL_SWORD.get());
        }

    }

    private void sendImc(InterModEnqueueEvent evt) {
        //register your bucket at the BucketLib mod to activate all features for your bucket
        //BucketLibApi.registerBucket(ModItems.WOODEN_BUCKET.getId());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ModMenuTypes.SMITHING_ANVIL_MENU.get(), SmithingAnvilScreen::new);
            //MenuScreens.register(ModMenuTypes.SMITHING_ANVIL_MG_MENU.get(), SmithingAnvilMinigameScreen::new);
            //BarrelInteraction.bootStrap();
        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerBelowAll("anvil_mg", AnvilMinigameOverlay.ANVIL_MG);
        }

        @SubscribeEvent
        public static void onRegisterRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
            //ModRecipeBookTypes.registerRecipeBookCategories(event);
            RecipeBookExtensionClientHelper.init(event);
        }
    }
}

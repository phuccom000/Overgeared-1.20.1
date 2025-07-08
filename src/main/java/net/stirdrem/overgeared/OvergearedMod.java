package net.stirdrem.overgeared;

//import cech12.bucketlib.api.BucketLibApi;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.renderer.SmithingAnvilBlockEntityRenderer;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.config.ServerConfig;
//import net.stirdrem.overgeared.core.waterbarrel.BarrelInteraction;
import net.stirdrem.overgeared.event.ModAttributes;
import net.stirdrem.overgeared.event.ModItemInteractEvents;
import net.stirdrem.overgeared.item.ModCreativeModeTabs;
import net.stirdrem.overgeared.item.ModItems;

import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.loot.ModLootModifiers;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.ModRecipes;
import net.stirdrem.overgeared.screen.*;
import net.stirdrem.overgeared.sound.ModSounds;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.TickScheduler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Unique;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(OvergearedMod.MOD_ID)
public class OvergearedMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "overgeared";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    //public static final AnvilMinigameHandler SERVER_HANDLER = new AnvilMinigameHandler();

    public static boolean polymorph;

    public OvergearedMod() {
        //ServerConfig.registerConfig();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //IEventBus modEventBus = context.getModEventBus();

        ServerConfig.loadConfig(ServerConfig.SERVER_CONFIG, FMLPaths.GAMEDIR.get().resolve(FMLPaths.CONFIGDIR.get()).resolve(MOD_ID + "-common.toml"));
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_CONFIG);

        modEventBus.addListener(this::onConfigLoaded);

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);

        ModBlocks.register(modEventBus);

        ModBlockEntities.register(modEventBus);

        ModMenuTypes.register(modEventBus);

        ModRecipes.register(modEventBus);

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

    private void onConfigLoaded(ModConfigEvent.Loading event) {
        //if (event.getConfig().getSpec() == ServerConfig.SERVER_CONFIG) {
        ToolTypeRegistry.init();
        LOGGER.info("Tool types initialized: {}",
                ToolTypeRegistry.getRegisteredTypes().size());
        //}
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.IRON_TONGS);
            event.accept(ModItems.STEEL_TONGS);
            event.accept(ModItems.COPPER_SMITHING_HAMMER);
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

    @Unique
    public static Item getCooledIngot(Item heatedItem) {
        var heatedTag = ForgeRegistries.ITEMS.tags().getTag(ModTags.Items.HEATED_METALS);
        var cooledTag = ForgeRegistries.ITEMS.tags().getTag(ModTags.Items.HEATABLE_METALS);

        int index = 0;
        for (Item item : heatedTag) {
            if (item == heatedItem) {
                int i = 0;
                for (Item cooledItem : cooledTag) {
                    if (i == index) {
                        return cooledItem;
                    }
                    i++;
                }
            }
            index++;
        }
        return null;
    }
    
    @Unique
    public static boolean isDurabilityMultiplierBlacklisted(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ServerConfig.BASE_DURABILITY_BLACKLIST.get().contains(id.toString());
    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU.get(), SteelSmithingAnvilScreen::new);
            MenuScreens.register(ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(), StoneSmithingAnvilScreen::new);
            MenuScreens.register(ModMenuTypes.ROCK_KNAPPING_MENU.get(), RockKnappingScreen::new);
            MenuScreens.register(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU.get(), BlueprintWorkbenchScreen::new);
            //BarrelInteraction.bootStrap();
        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerBelowAll("anvil_mg", AnvilMinigameOverlay.ANVIL_MG);
        }
    }
}

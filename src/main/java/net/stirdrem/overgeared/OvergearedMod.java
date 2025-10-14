package net.stirdrem.overgeared;

//import cech12.bucketlib.api.BucketLibApi;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.block.UpgradeArrowDispenseBehavior;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.entity.ModEntities;

import net.stirdrem.overgeared.entity.renderer.LingeringArrowEntityRenderer;
import net.stirdrem.overgeared.entity.renderer.UpgradeArrowEntityRenderer;
import net.stirdrem.overgeared.item.armor.model.CustomCopperHelmet;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.renderer.SmithingAnvilBlockEntityRenderer;
import net.stirdrem.overgeared.config.ClientConfig;
import net.stirdrem.overgeared.config.ServerConfig;
//import net.stirdrem.overgeared.core.waterbarrel.BarrelInteraction;
import net.stirdrem.overgeared.event.ModAttributes;
import net.stirdrem.overgeared.item.ModCreativeModeTabs;
import net.stirdrem.overgeared.item.ModItems;

import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.item.armor.model.CustomCopperLeggings;
import net.stirdrem.overgeared.loot.ModLootModifiers;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.recipe.BetterBrewingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.ModRecipes;
import net.stirdrem.overgeared.screen.*;
import net.stirdrem.overgeared.sound.ModSounds;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.util.TickScheduler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

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

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);

        ModBlocks.register(modEventBus);

        ModBlockEntities.register(modEventBus);

        ModMenuTypes.register(modEventBus);

        ModRecipes.register(modEventBus);

        ModRecipeTypes.register(modEventBus);

        ModLootModifiers.register(modEventBus);

        ModSounds.register(modEventBus);

        ModEntities.register(modEventBus);

        ModAttributes.register(modEventBus);


        MinecraftForge.EVENT_BUS.register(TickScheduler.class);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::sendImc);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);

        polymorph = ModList.get().isLoaded("polymorph");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ServerConfig.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_CONFIG);
        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        //context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModMessages.register();
        ToolTypeRegistry.init();
        LOGGER.info("Tool types initialized: {}",
                ToolTypeRegistry.getRegisteredTypes().size());
        //}
        if (ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get())
            BrewingRecipeRegistry.addRecipe(
                    new BetterBrewingRecipe(
                            Potions.THICK,
                            Items.CHORUS_FRUIT,
                            new ItemStack(Items.DRAGON_BREATH)
                    )
            );
    }

    // Add the example block item to the building blocks tab
    private void addCreative(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            var entries = event.getEntries();
            entries.putAfter(
                    new ItemStack(Blocks.CRAFTING_TABLE),
                    new ItemStack(ModBlocks.DRAFTING_TABLE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            var entries = event.getEntries();
            entries.putAfter(
                    new ItemStack(Blocks.GOLD_BLOCK),
                    new ItemStack(ModBlocks.STEEL_BLOCK.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            var entries = event.getEntries();
            entries.putAfter(
                    new ItemStack(Items.GOLD_INGOT),
                    new ItemStack(ModItems.STEEL_INGOT.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putAfter(
                    new ItemStack(Items.RAW_GOLD),
                    new ItemStack(ModItems.CRUDE_STEEL.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            var entries = event.getEntries();

            entries.putAfter(
                    new ItemStack(Items.NETHERITE_HOE),
                    new ItemStack(ModItems.IRON_TONGS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );

            entries.putAfter(
                    new ItemStack(ModItems.IRON_TONGS.get()),
                    new ItemStack(ModItems.STEEL_TONGS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putAfter(
                    new ItemStack(ModItems.STEEL_TONGS.get()),
                    new ItemStack(ModItems.COPPER_SMITHING_HAMMER.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putAfter(
                    new ItemStack(ModItems.COPPER_SMITHING_HAMMER.get()),
                    new ItemStack(ModItems.SMITHING_HAMMER.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );

            // Steel tools after Iron tools

            entries.putAfter(
                    new ItemStack(Items.GOLDEN_HOE),
                    new ItemStack(ModItems.COPPER_SHOVEL.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putAfter(
                    new ItemStack(ModItems.COPPER_SHOVEL.get()),
                    new ItemStack(ModItems.COPPER_PICKAXE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putAfter(
                    new ItemStack(ModItems.COPPER_PICKAXE.get()),
                    new ItemStack(ModItems.COPPER_AXE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putAfter(
                    new ItemStack(ModItems.COPPER_AXE.get()),
                    new ItemStack(ModItems.COPPER_HOE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putBefore(
                    new ItemStack(Items.IRON_SHOVEL),
                    new ItemStack(ModItems.COPPER_HOE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putBefore(
                    new ItemStack(ModItems.COPPER_HOE.get()),
                    new ItemStack(ModItems.COPPER_AXE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putBefore(
                    new ItemStack(ModItems.COPPER_AXE.get()),
                    new ItemStack(ModItems.COPPER_PICKAXE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putBefore(
                    new ItemStack(ModItems.COPPER_PICKAXE.get()),
                    new ItemStack(ModItems.COPPER_SHOVEL.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );

            entries.putBefore(
                    new ItemStack(Items.DIAMOND_SHOVEL),
                    new ItemStack(ModItems.STEEL_HOE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putBefore(
                    new ItemStack(ModItems.STEEL_HOE.get()),
                    new ItemStack(ModItems.STEEL_AXE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putBefore(
                    new ItemStack(ModItems.STEEL_AXE.get()),
                    new ItemStack(ModItems.STEEL_PICKAXE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            entries.putBefore(
                    new ItemStack(ModItems.STEEL_PICKAXE.get()),
                    new ItemStack(ModItems.STEEL_SHOVEL.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );


        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            if (ServerConfig.ENABLE_FLETCHING_RECIPES.get()) {
                for (Potion potion : ForgeRegistries.POTIONS) {
                    if (potion == Potions.EMPTY) continue;

                    ItemStack arrow = new ItemStack(ModItems.LINGERING_ARROW.get());
                    arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                    event.accept(arrow);
                }
                for (Potion potion : ForgeRegistries.POTIONS) {
                    if (potion == Potions.EMPTY) continue;

                    ItemStack arrow = new ItemStack(ModItems.IRON_UPGRADE_ARROW.get());
                    arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                    event.accept(arrow);
                }
                for (Potion potion : ForgeRegistries.POTIONS) {
                    if (potion == Potions.EMPTY) continue;

                    ItemStack arrow = new ItemStack(ModItems.IRON_UPGRADE_ARROW.get());
                    arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                    arrow.getOrCreateTag().putBoolean("LingeringPotion", true);
                    event.accept(arrow);
                }
                for (Potion potion : ForgeRegistries.POTIONS) {
                    if (potion == Potions.EMPTY) continue;

                    ItemStack arrow = new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get());
                    arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                    event.accept(arrow);
                }
                for (Potion potion : ForgeRegistries.POTIONS) {
                    if (potion == Potions.EMPTY) continue;

                    ItemStack arrow = new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get());
                    arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                    arrow.getOrCreateTag().putBoolean("LingeringPotion", true);
                    event.accept(arrow);
                }
                for (Potion potion : ForgeRegistries.POTIONS) {
                    if (potion == Potions.EMPTY) continue;

                    ItemStack arrow = new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get());
                    arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                    event.accept(arrow);
                }
                for (Potion potion : ForgeRegistries.POTIONS) {
                    if (potion == Potions.EMPTY) continue;

                    ItemStack arrow = new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get());
                    arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                    arrow.getOrCreateTag().putBoolean("LingeringPotion", true);
                    event.accept(arrow);
                }
            }
            event.getEntries().putBefore(
                    new ItemStack(Items.IRON_SWORD),           // anchor: Stone Sword
                    new ItemStack(ModItems.COPPER_SWORD.get()), // item to insert
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(Items.GOLDEN_SWORD),
                    new ItemStack(ModItems.STEEL_SWORD.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putBefore(
                    new ItemStack(Items.IRON_AXE),
                    new ItemStack(ModItems.COPPER_AXE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(Items.GOLDEN_AXE),
                    new ItemStack(ModItems.STEEL_AXE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );

            event.getEntries().putAfter(
                    new ItemStack(Items.GOLDEN_BOOTS),
                    new ItemStack(ModItems.STEEL_HELMET.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(ModItems.STEEL_HELMET.get()),
                    new ItemStack(ModItems.STEEL_CHESTPLATE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(ModItems.STEEL_CHESTPLATE.get()),
                    new ItemStack(ModItems.STEEL_LEGGINGS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(ModItems.STEEL_LEGGINGS.get()),
                    new ItemStack(ModItems.STEEL_BOOTS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(Items.LEATHER_BOOTS),
                    new ItemStack(ModItems.COPPER_HELMET.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(ModItems.COPPER_HELMET.get()),
                    new ItemStack(ModItems.COPPER_CHESTPLATE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(ModItems.COPPER_CHESTPLATE.get()),
                    new ItemStack(ModItems.COPPER_LEGGINGS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(ModItems.COPPER_LEGGINGS.get()),
                    new ItemStack(ModItems.COPPER_BOOTS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );

            event.getEntries().putAfter(
                    new ItemStack(Items.ARROW),
                    new ItemStack(ModItems.IRON_UPGRADE_ARROW.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(ModItems.IRON_UPGRADE_ARROW.get()),
                    new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get()),
                    new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            /*event.getEntries().putAfter(
                    new ItemStack(Items.IRON_BOOTS),
                    new ItemStack(ModItems.STEEL_HELMET.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(ModItems.STEEL_HELMET.get()),
                    new ItemStack(ModItems.STEEL_CHESTPLATE.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(ModItems.STEEL_CHESTPLATE.get()),
                    new ItemStack(ModItems.STEEL_LEGGINGS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.getEntries().putAfter(
                    new ItemStack(ModItems.STEEL_LEGGINGS.get()),
                    new ItemStack(ModItems.STEEL_BOOTS.get()),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );*/

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
        return heatedItem;
    }

    @Unique
    public static boolean isDurabilityMultiplierBlacklisted(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return ServerConfig.BASE_DURABILITY_BLACKLIST.get().contains(id.toString());
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonModEvents {
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                // Register dispenser behaviors on COMMON side
                DispenserBlock.registerBehavior(ModItems.LINGERING_ARROW.get(), new UpgradeArrowDispenseBehavior());
                DispenserBlock.registerBehavior(ModItems.IRON_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
                DispenserBlock.registerBehavior(ModItems.STEEL_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
                DispenserBlock.registerBehavior(ModItems.DIAMOND_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
            });
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ModMenuTypes.STEEL_SMITHING_ANVIL_MENU.get(), SteelSmithingAnvilScreen::new);
            MenuScreens.register(ModMenuTypes.TIER_A_SMITHING_ANVIL_MENU.get(), TierASmithingAnvilScreen::new);
            MenuScreens.register(ModMenuTypes.TIER_B_SMITHING_ANVIL_MENU.get(), TierBSmithingAnvilScreen::new);
            MenuScreens.register(ModMenuTypes.STONE_SMITHING_ANVIL_MENU.get(), StoneSmithingAnvilScreen::new);
            MenuScreens.register(ModMenuTypes.ROCK_KNAPPING_MENU.get(), RockKnappingScreen::new);
            MenuScreens.register(ModMenuTypes.BLUEPRINT_WORKBENCH_MENU.get(), BlueprintWorkbenchScreen::new);
            MenuScreens.register(ModMenuTypes.FLETCHING_STATION_MENU.get(), FletchingStationScreen::new);

            registerArrowProperties(ModItems.IRON_UPGRADE_ARROW.get());
            registerArrowProperties(ModItems.STEEL_UPGRADE_ARROW.get());
            registerArrowProperties(ModItems.DIAMOND_UPGRADE_ARROW.get());
        }

        private static void registerArrowProperties(Item item) {
            ItemProperties.register(item,
                    new ResourceLocation(OvergearedMod.MOD_ID, "potion_type"),
                    (stack, level, entity, seed) -> {
                        if (!stack.hasTag()) return 0.0F;
                        var tag = stack.getTag();
                        if (tag.contains("LingeringPotion")) return 2.0F;
                        if (tag.contains("Potion") || tag.contains("CustomPotionEffects")) return 1.0F;
                        return 0.0F;
                    });
        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.STONE_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.TIER_A_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.TIER_B_SMITHING_ANVIL_BE.get(), SmithingAnvilBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerBelowAll("anvil_mg", AnvilMinigameOverlay.ANVIL_MG);
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(CustomCopperHelmet.LAYER_LOCATION, CustomCopperHelmet::createBodyLayer);
            event.registerLayerDefinition(CustomCopperLeggings.LAYER_LOCATION, CustomCopperLeggings::createBodyLayer);
        }

        @SubscribeEvent
        public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
            // Helper method for color registration
            registerArrowColor(event, ModItems.IRON_UPGRADE_ARROW.get());
            registerArrowColor(event, ModItems.STEEL_UPGRADE_ARROW.get());
            registerArrowColor(event, ModItems.DIAMOND_UPGRADE_ARROW.get());
            registerArrowColor(event, ModItems.LINGERING_ARROW.get());
        }

        private static void registerArrowColor(RegisterColorHandlersEvent.Item event, Item item) {
            event.register((stack, tintIndex) ->
                            tintIndex == 0 && stack.hasTag() ? getColor(stack) : 0xFFFFFFFF,
                    item);
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            //event.registerEntityRenderer(ModEntities.MODULAR_ARROW.get(), ModularArrowEntityRenderer::new);
            event.registerEntityRenderer(ModEntities.LINGERING_ARROW.get(), LingeringArrowEntityRenderer::new);
            event.registerEntityRenderer(ModEntities.UPGRADE_ARROW.get(), UpgradeArrowEntityRenderer::new);
        }

        public static int getColor(ItemStack pStack) {
            CompoundTag compoundtag = pStack.getTag();
            if (compoundtag != null && compoundtag.contains("CustomPotionColor", 99)) {
                return compoundtag.getInt("CustomPotionColor");
            } else {
                return getPotion(pStack) == Potions.EMPTY ? 16253176 : getColor(getMobEffects(pStack));
            }
        }

        public static int getColor(Collection<MobEffectInstance> pEffects) {
            int i = 3694022;
            if (pEffects.isEmpty()) {
                return 3694022;
            } else {
                float f = 0.0F;
                float f1 = 0.0F;
                float f2 = 0.0F;
                int j = 0;

                for (MobEffectInstance mobeffectinstance : pEffects) {
                    if (mobeffectinstance.isVisible()) {
                        int k = mobeffectinstance.getEffect().getColor();
                        int l = mobeffectinstance.getAmplifier() + 1;
                        f += (float) (l * (k >> 16 & 255)) / 255.0F;
                        f1 += (float) (l * (k >> 8 & 255)) / 255.0F;
                        f2 += (float) (l * (k >> 0 & 255)) / 255.0F;
                        j += l;
                    }
                }

                if (j == 0) {
                    return 0;
                } else {
                    f = f / (float) j * 255.0F;
                    f1 = f1 / (float) j * 255.0F;
                    f2 = f2 / (float) j * 255.0F;
                    return (int) f << 16 | (int) f1 << 8 | (int) f2;
                }
            }
        }

        public static Potion getPotion(ItemStack pStack) {
            return getPotion(pStack.getTag());
        }

        public static Potion getPotion(@Nullable CompoundTag tag) {
            if (tag == null) return Potions.EMPTY;

            // Prioritize "LingeringPotion" if present
            if (tag.contains("LingeringPotion", 8)) { // 8 = string type
                return Potion.byName(tag.getString("LingeringPotion"));
            }
            if (tag.contains("LingeringPotion") && tag.getBoolean("LingeringPotion")) { // 8 = string type
                return Potion.byName(tag.getString("Potion"));
            }
            if (tag.contains("Potion", 8)) {
                return Potion.byName(tag.getString("Potion"));
            }

            return Potions.EMPTY;
        }


        public static List<MobEffectInstance> getMobEffects(ItemStack pStack) {
            return getAllEffects(pStack.getTag());
        }

        public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag pCompoundTag) {
            List<MobEffectInstance> list = Lists.newArrayList();
            list.addAll(getPotion(pCompoundTag).getEffects());
            getCustomEffects(pCompoundTag, list);
            return list;
        }

        public static void getCustomEffects(@Nullable CompoundTag pCompoundTag, List<MobEffectInstance> pEffectList) {
            if (pCompoundTag != null && pCompoundTag.contains("CustomPotionEffects", 9)) {
                ListTag listtag = pCompoundTag.getList("CustomPotionEffects", 10);

                for (int i = 0; i < listtag.size(); ++i) {
                    CompoundTag compoundtag = listtag.getCompound(i);
                    MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag);
                    if (mobeffectinstance != null) {
                        pEffectList.add(mobeffectinstance);
                    }
                }
            }

        }
    }
}

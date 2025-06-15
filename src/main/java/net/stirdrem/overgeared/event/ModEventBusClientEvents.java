package net.stirdrem.overgeared.event;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.block.entity.renderer.SmithingAnvilBlockEntityRenderer;
import net.stirdrem.overgeared.client.RecipeBookExtensionClientHelper;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.recipe.ModRecipeBookTypes;
import net.stirdrem.overgeared.screen.ModMenuTypes;
import net.stirdrem.overgeared.screen.SmithingAnvilScreen;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {


    // In your client setup (e.g., ModClientEvents.java)
    /*@SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                new WaterBarrelBlockColor(),
                ModBlocks.WATER_BARREL_FULL.get()  // Your block here
        );
    }*/


}

package net.stirdrem.overgeared.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.UpgradeArrowDispenseBehavior;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.recipe.BetterBrewingRecipe;

@EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class CommonModEvents {
  @SubscribeEvent
  public static void commonSetup(final FMLCommonSetupEvent event) {
    ToolTypeRegistry.init();

    DispenserBlock.registerBehavior(ModItems.LINGERING_ARROW.get(), new UpgradeArrowDispenseBehavior());
    DispenserBlock.registerBehavior(ModItems.IRON_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
    DispenserBlock.registerBehavior(ModItems.STEEL_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
    DispenserBlock.registerBehavior(ModItems.DIAMOND_UPGRADE_ARROW.get(), new UpgradeArrowDispenseBehavior());
  }

  @SubscribeEvent
  public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
    PotionBrewing.Builder builder = event.getBuilder();

    if (ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get()) {
      builder.addRecipe(new BetterBrewingRecipe(
              Potions.THICK.value(),
              Items.CHORUS_FRUIT,
              new ItemStack(Items.DRAGON_BREATH)
      ));
    }
  }
}

package net.stirdrem.overgeared.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OvergearedMod.MOD_ID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<CreativeModeTab, CreativeModeTab> OVERGEARED_TAB =
            CREATIVE_MODE_TABS.register("overgeared_tab",
                    () -> CreativeModeTab.builder()
                            .icon(() -> new ItemStack(ModItems.IRON_TONGS.get()))
                            .title(Component.translatable("creativetab.overgeared_tab"))
                            .displayItems((parameters, output) -> {
                                // General materials/tools
                                output.accept(ModItems.CRUDE_STEEL.get());
                                output.accept(ModItems.HEATED_CRUDE_STEEL.get());
                                output.accept(ModItems.ROCK.get());
                                output.accept(ModItems.COPPER_NUGGET.get());
                                output.accept(ModItems.STEEL_INGOT.get());
                                output.accept(ModItems.STEEL_NUGGET.get());
                                output.accept(ModItems.IRON_ARROW_HEAD.get());
                                output.accept(ModItems.STEEL_ARROW_HEAD.get());
                                output.accept(ModItems.DIAMOND_SHARD.get());
                                output.accept(ModItems.IRON_UPGRADE_ARROW.get());
                                output.accept(ModItems.STEEL_UPGRADE_ARROW.get());
                                output.accept(ModItems.DIAMOND_UPGRADE_ARROW.get());
                                output.accept(ModItems.HEATED_COPPER_INGOT.get());
                                output.accept(ModItems.HEATED_IRON_INGOT.get());
                                output.accept(ModItems.HEATED_SILVER_INGOT.get());
                                output.accept(ModItems.HEATED_STEEL_INGOT.get());
                                output.accept(ModItems.NETHERITE_ALLOY.get());
                                output.accept(ModItems.HEATED_NETHERITE_ALLOY.get());
                                output.accept(ModItems.COPPER_PLATE.get());
                                output.accept(ModItems.IRON_PLATE.get());
                                output.accept(ModItems.STEEL_PLATE.get());
                                output.accept(ModItems.IRON_TONG.get());
                                output.accept(ModItems.STEEL_TONG.get());
                                output.accept(ModItems.WOODEN_TONGS.get());
                                output.accept(ModItems.IRON_TONGS.get());
                                output.accept(ModItems.STEEL_TONGS.get());
                                output.accept(ModItems.STONE_HAMMER_HEAD.get());
                                output.accept(ModItems.COPPER_HAMMER_HEAD.get());
                                output.accept(ModItems.STEEL_HAMMER_HEAD.get());
                                output.accept(ModItems.SMITHING_HAMMER.get());
                                output.accept(ModItems.EMPTY_BLUEPRINT.get());
                                output.accept(ModItems.BLUEPRINT.get());
                                output.accept(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get());
                                output.accept(ModItems.UNFIRED_TOOL_CAST.get());
                                output.accept(ModItems.CLAY_TOOL_CAST.get());
                                output.accept(ModItems.NETHER_TOOL_CAST.get());
                                output.accept(ModItems.COPPER_HELMET.get());
                                output.accept(ModItems.COPPER_CHESTPLATE.get());
                                output.accept(ModItems.COPPER_LEGGINGS.get());
                                output.accept(ModItems.COPPER_BOOTS.get());

                                output.accept(ModItems.STEEL_HELMET.get());
                                output.accept(ModItems.STEEL_CHESTPLATE.get());
                                output.accept(ModItems.STEEL_LEGGINGS.get());
                                output.accept(ModItems.STEEL_BOOTS.get());

                                output.accept(ModItems.COPPER_SWORD.get());
                                output.accept(ModItems.COPPER_PICKAXE.get());
                                output.accept(ModItems.COPPER_AXE.get());
                                output.accept(ModItems.COPPER_SHOVEL.get());
                                output.accept(ModItems.COPPER_HOE.get());

                                output.accept(ModItems.STEEL_SWORD.get());
                                output.accept(ModItems.STEEL_PICKAXE.get());
                                output.accept(ModItems.STEEL_AXE.get());
                                output.accept(ModItems.STEEL_SHOVEL.get());
                                output.accept(ModItems.STEEL_HOE.get());

                                // === STONE ===
                                output.accept(ModItems.STONE_SWORD_BLADE.get());
                                output.accept(ModItems.STONE_PICKAXE_HEAD.get());
                                output.accept(ModItems.STONE_AXE_HEAD.get());
                                output.accept(ModItems.STONE_SHOVEL_HEAD.get());
                                output.accept(ModItems.STONE_HOE_HEAD.get());

                                // === COPPER ===
                                output.accept(ModItems.COPPER_SWORD_BLADE.get());
                                output.accept(ModItems.COPPER_PICKAXE_HEAD.get());
                                output.accept(ModItems.COPPER_AXE_HEAD.get());
                                output.accept(ModItems.COPPER_SHOVEL_HEAD.get());
                                output.accept(ModItems.COPPER_HOE_HEAD.get());

                                // === IRON ===
                                output.accept(ModItems.IRON_SWORD_BLADE.get());
                                output.accept(ModItems.IRON_PICKAXE_HEAD.get());
                                output.accept(ModItems.IRON_AXE_HEAD.get());
                                output.accept(ModItems.IRON_SHOVEL_HEAD.get());
                                output.accept(ModItems.IRON_HOE_HEAD.get());

                                // === GOLD ===
                                output.accept(ModItems.GOLDEN_SWORD_BLADE.get());
                                output.accept(ModItems.GOLDEN_PICKAXE_HEAD.get());
                                output.accept(ModItems.GOLDEN_AXE_HEAD.get());
                                output.accept(ModItems.GOLDEN_SHOVEL_HEAD.get());
                                output.accept(ModItems.GOLDEN_HOE_HEAD.get());

                                // === STEEL ===
                                output.accept(ModItems.STEEL_SWORD_BLADE.get());
                                output.accept(ModItems.STEEL_PICKAXE_HEAD.get());
                                output.accept(ModItems.STEEL_AXE_HEAD.get());
                                output.accept(ModItems.STEEL_SHOVEL_HEAD.get());
                                output.accept(ModItems.STEEL_HOE_HEAD.get());

//                                output.accept(ModBlocks.STONE_SMITHING_ANVIL.get());
//                                output.accept(ModBlocks.SMITHING_ANVIL.get());
//                                output.accept(ModBlocks.TIER_A_SMITHING_ANVIL.get());
//                                output.accept(ModBlocks.TIER_B_SMITHING_ANVIL.get());
                                output.accept(ModBlocks.STEEL_BLOCK.get());
//                                output.accept(ModBlocks.DRAFTING_TABLE.get());
                                output.accept(ModBlocks.ALLOY_FURNACE.get());
//                                output.accept(ModBlocks.NETHER_ALLOY_FURNACE.get());
                            })
                            .build());

    public static final net.neoforged.neoforge.registries.DeferredHolder<CreativeModeTab, CreativeModeTab> LINGERING_ARROWS_TAB =
            CREATIVE_MODE_TABS.register("lingering_arrows_tab",
                    () -> CreativeModeTab.builder()
                            .icon(() -> new ItemStack(Blocks.FLETCHING_TABLE))
                            .title(Component.translatable("creativetab.overgeared.lingering_arrows_tab"))
                            .displayItems((parameters, output) -> {
                                // Only add items if the config allows it
                                if (!ServerConfig.ENABLE_FLETCHING_RECIPES.get()) return;

                                output.accept(Items.ARROW);
                                output.accept(ModItems.IRON_UPGRADE_ARROW.get());
                                output.accept(ModItems.STEEL_UPGRADE_ARROW.get());
                                output.accept(ModItems.DIAMOND_UPGRADE_ARROW.get());

                                // === LINGERING ARROWS (with potions) ===
                                for (Potion potion : BuiltInRegistries.POTION) {
                                    ItemStack arrow = new ItemStack(ModItems.LINGERING_ARROW.get());
                                    arrow.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
                                    output.accept(arrow);
                                }

                                // === IRON UPGRADE ARROWS (tipped + lingering) ===
                                for (Potion potion : BuiltInRegistries.POTION) {
                                    // Tipped version
                                    ItemStack iron = new ItemStack(ModItems.IRON_UPGRADE_ARROW.get());
                                    iron.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
                                    output.accept(iron);

                                    // Lingering version
                                    ItemStack ironLingering = iron.copy();
                                    ironLingering.set(ModComponents.LINGERING_STATUS, true);
                                    output.accept(ironLingering);
                                }

                                // === STEEL UPGRADE ARROWS (tipped + lingering) ===
                                for (Potion potion : BuiltInRegistries.POTION) {
                                    // Tipped version
                                    ItemStack steel = new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get());
                                    steel.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
                                    output.accept(steel);

                                    // Lingering version
                                    ItemStack steelLingering = steel.copy();
                                    steelLingering.set(ModComponents.LINGERING_STATUS, true);
                                    output.accept(steelLingering);
                                }

                                // === DIAMOND UPGRADE ARROWS (tipped + lingering) ===
                                for (Potion potion : BuiltInRegistries.POTION) {
                                    // Tipped version
                                    ItemStack diamond = new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get());
                                    diamond.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
                                    output.accept(diamond);

                                    // Lingering version
                                    ItemStack diamondLingering = diamond.copy();
                                    diamondLingering.set(ModComponents.LINGERING_STATUS, true);
                                    output.accept(diamondLingering);
                                }
                            })
                            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

package net.stirdrem.overgeared.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.config.ServerConfig;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OvergearedMod.MOD_ID);

    public static final Supplier<CreativeModeTab> OVERGEARED_TAB = CREATIVE_MODE_TABS.register("overgeared_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.IRON_TONGS.get()))
                    .title(Component.translatable("creativetab.overgeared_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        // General materials/tools
                        pOutput.accept(ModItems.CRUDE_STEEL.get());
                        pOutput.accept(ModItems.HEATED_CRUDE_STEEL.get());
                        pOutput.accept(ModItems.ROCK.get());
                        pOutput.accept(ModItems.COPPER_NUGGET.get());
                        pOutput.accept(ModItems.STEEL_INGOT.get());
                        pOutput.accept(ModItems.STEEL_NUGGET.get());
                        pOutput.accept(ModItems.IRON_ARROW_HEAD.get());
                        pOutput.accept(ModItems.STEEL_ARROW_HEAD.get());
                        pOutput.accept(ModItems.DIAMOND_SHARD.get());
                        pOutput.accept(ModItems.IRON_UPGRADE_ARROW.get());
                        pOutput.accept(ModItems.STEEL_UPGRADE_ARROW.get());
                        pOutput.accept(ModItems.DIAMOND_UPGRADE_ARROW.get());
                        pOutput.accept(ModItems.HEATED_COPPER_INGOT.get());
                        pOutput.accept(ModItems.HEATED_IRON_INGOT.get());
                        pOutput.accept(ModItems.HEATED_STEEL_INGOT.get());
                        pOutput.accept(ModItems.COPPER_PLATE.get());
                        pOutput.accept(ModItems.IRON_PLATE.get());
                        pOutput.accept(ModItems.STEEL_PLATE.get());
                        pOutput.accept(ModItems.IRON_TONG.get());
                        pOutput.accept(ModItems.STEEL_TONG.get());
                        pOutput.accept(ModItems.WOODEN_TONGS.get());
                        pOutput.accept(ModItems.IRON_TONGS.get());
                        pOutput.accept(ModItems.STEEL_TONGS.get());
                        pOutput.accept(ModItems.COPPER_SMITHING_HAMMER.get());
                        pOutput.accept(ModItems.SMITHING_HAMMER.get());
                        pOutput.accept(ModItems.EMPTY_BLUEPRINT.get());
                        pOutput.accept(ModItems.BLUEPRINT.get());
                        pOutput.accept(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get());
                        //pOutput.accept(ModItems.WOODEN_BUCKET.get());
                        pOutput.accept(ModItems.COPPER_HELMET.get());
                        pOutput.accept(ModItems.COPPER_CHESTPLATE.get());
                        pOutput.accept(ModItems.COPPER_LEGGINGS.get());
                        pOutput.accept(ModItems.COPPER_BOOTS.get());

                        pOutput.accept(ModItems.STEEL_HELMET.get());
                        pOutput.accept(ModItems.STEEL_CHESTPLATE.get());
                        pOutput.accept(ModItems.STEEL_LEGGINGS.get());
                        pOutput.accept(ModItems.STEEL_BOOTS.get());

                        pOutput.accept(ModItems.COPPER_SWORD.get());
                        pOutput.accept(ModItems.COPPER_PICKAXE.get());
                        pOutput.accept(ModItems.COPPER_AXE.get());
                        pOutput.accept(ModItems.COPPER_SHOVEL.get());
                        pOutput.accept(ModItems.COPPER_HOE.get());

                        pOutput.accept(ModItems.STEEL_SWORD.get());
                        pOutput.accept(ModItems.STEEL_PICKAXE.get());
                        pOutput.accept(ModItems.STEEL_AXE.get());
                        pOutput.accept(ModItems.STEEL_SHOVEL.get());
                        pOutput.accept(ModItems.STEEL_HOE.get());

                        // === STONE ===
                        pOutput.accept(ModItems.STONE_SWORD_BLADE.get());
                        pOutput.accept(ModItems.STONE_PICKAXE_HEAD.get());
                        pOutput.accept(ModItems.STONE_AXE_HEAD.get());
                        pOutput.accept(ModItems.STONE_SHOVEL_HEAD.get());
                        pOutput.accept(ModItems.STONE_HOE_HEAD.get());

                        // === COPPER ===
                        pOutput.accept(ModItems.COPPER_SWORD_BLADE.get());
                        pOutput.accept(ModItems.COPPER_PICKAXE_HEAD.get());
                        pOutput.accept(ModItems.COPPER_AXE_HEAD.get());
                        pOutput.accept(ModItems.COPPER_SHOVEL_HEAD.get());
                        pOutput.accept(ModItems.COPPER_HOE_HEAD.get());

                        // === IRON ===
                        pOutput.accept(ModItems.IRON_SWORD_BLADE.get());
                        pOutput.accept(ModItems.IRON_PICKAXE_HEAD.get());
                        pOutput.accept(ModItems.IRON_AXE_HEAD.get());
                        pOutput.accept(ModItems.IRON_SHOVEL_HEAD.get());
                        pOutput.accept(ModItems.IRON_HOE_HEAD.get());

                        // === GOLD ===
                        pOutput.accept(ModItems.GOLDEN_SWORD_BLADE.get());
                        pOutput.accept(ModItems.GOLDEN_PICKAXE_HEAD.get());
                        pOutput.accept(ModItems.GOLDEN_AXE_HEAD.get());
                        pOutput.accept(ModItems.GOLDEN_SHOVEL_HEAD.get());
                        pOutput.accept(ModItems.GOLDEN_HOE_HEAD.get());

                        // === STEEL ===
                        pOutput.accept(ModItems.STEEL_SWORD_BLADE.get());
                        pOutput.accept(ModItems.STEEL_PICKAXE_HEAD.get());
                        pOutput.accept(ModItems.STEEL_AXE_HEAD.get());
                        pOutput.accept(ModItems.STEEL_SHOVEL_HEAD.get());
                        pOutput.accept(ModItems.STEEL_HOE_HEAD.get());

                        // === DIAMOND ===
                       /* pOutput.accept(ModItems.DIAMOND_SWORD_BLADE.get());
                        pOutput.accept(ModItems.DIAMOND_PICKAXE_HEAD.get());
                        pOutput.accept(ModItems.DIAMOND_AXE_HEAD.get());
                        pOutput.accept(ModItems.DIAMOND_SHOVEL_HEAD.get());
                        pOutput.accept(ModItems.DIAMOND_HOE_HEAD.get());
*/
                        pOutput.accept(ModBlocks.STONE_SMITHING_ANVIL.get());
                        pOutput.accept(ModBlocks.SMITHING_ANVIL.get());
                        pOutput.accept(ModBlocks.TIER_A_SMITHING_ANVIL.get());
                        pOutput.accept(ModBlocks.TIER_B_SMITHING_ANVIL.get());
                        pOutput.accept(ModBlocks.STEEL_BLOCK.get());
                        pOutput.accept(ModBlocks.DRAFTING_TABLE.get());
                        //pOutput.accept(ModBlocks.SMITHING_ANVIL_TEST.get());
                        //pOutput.accept(ModBlocks.WATER_BARREL.get());
                        //pOutput.accept(ModBlocks.WATER_BARREL_FULL.get());


                        //pOutput.accept(Items.DIAMOND); //.get() only for custom items

                    })
                    .build());

    public static final Supplier<CreativeModeTab> LINGERING_ARROWS_TAB =
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

//                                for (Potion potion : BuiltInRegistries.POTION) {
//                                    if (potion.getEffects().isEmpty()) continue;  // Skips all no-effect potions (universal check)
//
//                                    ItemStack arrow = new ItemStack(Items.TIPPED_ARROW);
//                                    arrow.set(DataComponents.POTION_CONTENTS, new PotionContents((Holder<Potion>)  potion)); /*what is this ???*/
//
//                                    output.accept(arrow);
//                                }

                                for (Potion potion : BuiltInRegistries.POTION) {
                                    if (potion.getEffects().isEmpty()) continue;
    // ~ need fix change getOrCreateTag to new method (idk)
                                    ItemStack arrow = new ItemStack(Items.TIPPED_ARROW);
                                    arrow.getOrCreateTag().putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
                                    output.accept(arrow);
                                }

                                for (Potion potion : BuiltInRegistries.POTION) {
                                    if (potion == Potions.EMPTY) continue;

                                    ItemStack arrow = new ItemStack(ModItems.LINGERING_ARROW.get());
                                    arrow.getOrCreateTag().putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
                                    output.accept(arrow);
                                }

                                for (Potion potion : BuiltInRegistries.POTION) {
                                    if (potion == Potions.EMPTY) continue;

                                    ItemStack iron = new ItemStack(ModItems.IRON_UPGRADE_ARROW.get());
                                    iron.getOrCreateTag().putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
                                    output.accept(iron);

                                    ItemStack ironLingering = iron.copy();
                                    ironLingering.getOrCreateTag().putBoolean("LingeringPotion", true);
                                    output.accept(ironLingering);
                                }

                                for (Potion potion : BuiltInRegistries.POTION) {
                                    if (potion == Potions.EMPTY) continue;

                                    ItemStack steel = new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get());
                                    steel.getOrCreateTag().putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
                                    output.accept(steel);

                                    ItemStack steelLingering = steel.copy();
                                    steelLingering.getOrCreateTag().putBoolean("LingeringPotion", true);
                                    output.accept(steelLingering);
                                }

                                for (Potion potion : BuiltInRegistries.POTION) {
                                    if (potion == Potions.EMPTY) continue;

                                    ItemStack diamond = new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get());
                                    diamond.getOrCreateTag().putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
                                    output.accept(diamond);

                                    ItemStack diamondLingering = diamond.copy();
                                    diamondLingering.getOrCreateTag().putBoolean("LingeringPotion", true);
                                    output.accept(diamondLingering);
                                }
                            })
                            .build());

    public static final Supplier<CreativeModeTab> BLUEPRINT_TAB = CREATIVE_MODE_TABS.register("blueprint_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.BLUEPRINT.get()))
                    .title(Component.translatable("creativetab.overgeared.blueprint_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.EMPTY_BLUEPRINT.get());
                        // === Blueprint Variants ===
                        for (ToolType toolType : ToolTypeRegistry.getRegisteredTypesAll()) {
                            for (BlueprintQuality quality : BlueprintQuality.values()) {
                                ItemStack blueprint = new ItemStack(ModItems.BLUEPRINT.get());
                                CompoundTag tag = blueprint.getOrCreateTag();

                                tag.putString("ToolType", toolType.getId());
                                tag.putString("Quality", quality.name());
                                tag.putInt("Uses", 0);
                                tag.putInt("UsesToLevel", quality.getUse());
// ~ setTag(tag); cant  resolve need change
                                blueprint.setTag(tag);
                                output.accept(blueprint);
                            }
                        }
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

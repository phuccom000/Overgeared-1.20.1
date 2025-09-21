package net.stirdrem.overgeared.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OvergearedMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> OVERGEARED_TAB = CREATIVE_MODE_TABS.register("overgeared_tab",
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
    public static final RegistryObject<CreativeModeTab> LINGERING_ARROWS_TAB = CREATIVE_MODE_TABS.register("lingering_arrows_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Blocks.FLETCHING_TABLE))
                    .title(Component.translatable("creativetab.overgeared.lingering_arrows_tab"))
                    .displayItems((parameters, output) -> {
                        // First add all vanilla potion variants
                        output.accept(Items.ARROW);
                        output.accept(ModItems.IRON_UPGRADE_ARROW.get());
                        output.accept(ModItems.STEEL_UPGRADE_ARROW.get());
                        output.accept(ModItems.DIAMOND_UPGRADE_ARROW.get());
                        for (Potion potion : ForgeRegistries.POTIONS) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(Items.TIPPED_ARROW);
                            arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                            output.accept(arrow);
                        }
                        for (Potion potion : ForgeRegistries.POTIONS) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(ModItems.LINGERING_ARROW.get());
                            arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                            output.accept(arrow);
                        }

                        for (Potion potion : ForgeRegistries.POTIONS) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(ModItems.IRON_UPGRADE_ARROW.get());
                            arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                            output.accept(arrow);
                        }
                        for (Potion potion : ForgeRegistries.POTIONS) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(ModItems.IRON_UPGRADE_ARROW.get());
                            arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                            arrow.getOrCreateTag().putBoolean("LingeringPotion", true);
                            output.accept(arrow);
                        }
                        for (Potion potion : ForgeRegistries.POTIONS) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get());
                            arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                            output.accept(arrow);
                        }
                        for (Potion potion : ForgeRegistries.POTIONS) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get());
                            arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                            arrow.getOrCreateTag().putBoolean("LingeringPotion", true);
                            output.accept(arrow);
                        }
                        for (Potion potion : ForgeRegistries.POTIONS) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get());
                            arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                            output.accept(arrow);
                        }
                        for (Potion potion : ForgeRegistries.POTIONS) {
                            if (potion == Potions.EMPTY) continue;

                            ItemStack arrow = new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get());
                            arrow.getOrCreateTag().putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());
                            arrow.getOrCreateTag().putBoolean("LingeringPotion", true);
                            output.accept(arrow);
                        }

                        /*// Now add modular arrows with all combinations
                        String[] tips = {"ender_pearl", "chorus_fruit", "amethyst_shard", "fire_charge", "glow_ink", "iron_nugget", "steel_nugget", "diamond_shard"};
                        String[] shafts = {"blaze_rod", "breeze_rod", "end_rod", "bamboo"};
                        String[] feathers = {"prismarine", "slime_ball"};

                        // Create all possible combinations
                        for (String tip : tips) {
                            for (String shaft : shafts) {
                                for (String feather : feathers) {
                                    ItemStack modularArrow = new ItemStack(ModItems.MODULAR_ARROW.get());
                                    CompoundTag tag = modularArrow.getOrCreateTag();
                                    tag.putString("Tip", tip);
                                    tag.putString("Shaft", shaft);
                                    tag.putString("Feather", feather);

                                    output.accept(modularArrow);
                                }
                            }
                        }*/

                        /*// Also add potion variants of modular arrows
                        for (Potion potion : ForgeRegistries.POTIONS) {
                            if (potion == Potions.EMPTY) continue;

                            for (String tip : tips) {
                                for (String shaft : shafts) {
                                    for (String feather : feathers) {
                                        ItemStack potionModularArrow = new ItemStack(ModItems.MODULAR_ARROW.get());
                                        CompoundTag tag = potionModularArrow.getOrCreateTag();
                                        tag.putString("Tip", tip);
                                        tag.putString("Shaft", shaft);
                                        tag.putString("Feather", feather);
                                        tag.putString("Potion", ForgeRegistries.POTIONS.getKey(potion).toString());

                                        String arrowName = String.format("%s %s %s %s Arrow",
                                                potion.getName("item.minecraft.lingering_potion.effect."),
                                                tip.replace("_", " "),
                                                shaft.replace("_", " "),
                                                feather.replace("_", " "));
                                        potionModularArrow.setHoverName(Component.literal(arrowName));

                                        output.accept(potionModularArrow);
                                    }
                                }
                            }
                        }*/
                    })
                    .build());
    public static final RegistryObject<CreativeModeTab> BLUEPRINT_TAB = CREATIVE_MODE_TABS.register("blueprint_tab",
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

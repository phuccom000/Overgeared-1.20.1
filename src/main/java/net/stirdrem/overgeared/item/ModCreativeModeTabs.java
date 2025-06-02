package net.stirdrem.overgeared.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
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
                        pOutput.accept(ModItems.STEEL_INGOT.get());
                        pOutput.accept(ModItems.HEATED_IRON_INGOT.get());
                        pOutput.accept(ModItems.HEATED_STEEL_INGOT.get());
                        pOutput.accept(ModItems.STEEL_TONG.get());
                        pOutput.accept(ModItems.IRON_TONGS.get());
                        pOutput.accept(ModItems.STEEL_TONGS.get());
                        pOutput.accept(ModItems.SMITHING_HAMMER.get());
                        pOutput.accept(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get());
                        pOutput.accept(ModItems.WOODEN_BUCKET.get());

                        pOutput.accept(ModItems.STEEL_HELMET.get());
                        pOutput.accept(ModItems.STEEL_CHESTPLATE.get());
                        pOutput.accept(ModItems.STEEL_LEGGINGS.get());
                        pOutput.accept(ModItems.STEEL_BOOTS.get());

                        pOutput.accept(ModItems.STEEL_SWORD.get());
                        pOutput.accept(ModItems.STEEL_PICKAXE.get());
                        pOutput.accept(ModItems.STEEL_AXE.get());
                        pOutput.accept(ModItems.STEEL_SHOVEL.get());
                        pOutput.accept(ModItems.STEEL_HOE.get());

                        // === STONE ===
                        /*pOutput.accept(ModItems.STONE_SWORD_BLADE.get());
                        pOutput.accept(ModItems.STONE_PICKAXE_HEAD.get());
                        pOutput.accept(ModItems.STONE_AXE_HEAD.get());
                        pOutput.accept(ModItems.STONE_SHOVEL_HEAD.get());
                        pOutput.accept(ModItems.STONE_HOE_HEAD.get());*/

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
                        pOutput.accept(ModBlocks.SMITHING_ANVIL.get());
                        pOutput.accept(ModBlocks.STEEL_BLOCK.get());
                        pOutput.accept(ModBlocks.WATER_BARREL.get());
                        //pOutput.accept(ModBlocks.WATER_BARREL_FULL.get());

                        //pOutput.accept(Items.DIAMOND); //.get() only for custom items

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

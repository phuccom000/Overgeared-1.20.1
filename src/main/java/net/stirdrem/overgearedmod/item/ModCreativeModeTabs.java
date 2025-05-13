package net.stirdrem.overgearedmod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgearedmod.OvergearedMod;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OvergearedMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> OVERGEARED_TAB = CREATIVE_MODE_TABS.register("overgeared_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.IRON_TONGS.get()))
                    .title(Component.translatable("creativetab.overgeared_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.STEEL_COMPOUND.get());
                        pOutput.accept(ModItems.STEEL_INGOT.get());
                        pOutput.accept(ModItems.IRON_TONGS.get());
                        pOutput.accept(ModItems.STEEL_TONGS.get());
                        pOutput.accept(ModItems.HEATED_IRON_INGOT.get());
                        pOutput.accept(ModItems.HEATED_STEEL_INGOT.get());

                        // Sword blades
                        pOutput.accept(ModItems.STONE_SWORD_BLADE.get());
                        pOutput.accept(ModItems.IRON_SWORD_BLADE.get());
                        pOutput.accept(ModItems.GOLD_SWORD_BLADE.get());
                        pOutput.accept(ModItems.STEEL_SWORD_BLADE.get());
                        pOutput.accept(ModItems.DIAMOND_SWORD_BLADE.get());

                        // Pickaxe heads
                        pOutput.accept(ModItems.STONE_PICKAXE_HEAD.get());
                        pOutput.accept(ModItems.IRON_PICKAXE_HEAD.get());
                        pOutput.accept(ModItems.GOLD_PICKAXE_HEAD.get());
                        pOutput.accept(ModItems.STEEL_PICKAXE_HEAD.get());
                        pOutput.accept(ModItems.DIAMOND_PICKAXE_HEAD.get());

                        // Axe heads
                        pOutput.accept(ModItems.STONE_AXE_HEAD.get());
                        pOutput.accept(ModItems.IRON_AXE_HEAD.get());
                        pOutput.accept(ModItems.GOLD_AXE_HEAD.get());
                        pOutput.accept(ModItems.STEEL_AXE_HEAD.get());
                        pOutput.accept(ModItems.DIAMOND_AXE_HEAD.get());

                        // Shovel heads
                        pOutput.accept(ModItems.STONE_SHOVEL_HEAD.get());
                        pOutput.accept(ModItems.IRON_SHOVEL_HEAD.get());
                        pOutput.accept(ModItems.GOLD_SHOVEL_HEAD.get());
                        pOutput.accept(ModItems.STEEL_SHOVEL_HEAD.get());
                        pOutput.accept(ModItems.DIAMOND_SHOVEL_HEAD.get());

                        // Hoe heads
                        pOutput.accept(ModItems.STONE_HOE_HEAD.get());
                        pOutput.accept(ModItems.IRON_HOE_HEAD.get());
                        pOutput.accept(ModItems.GOLD_HOE_HEAD.get());
                        pOutput.accept(ModItems.STEEL_HOE_HEAD.get());
                        pOutput.accept(ModItems.DIAMOND_HOE_HEAD.get());

                        //pOutput.accept(Items.DIAMOND); //.get() only for custom items

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

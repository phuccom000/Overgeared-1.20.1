package net.stirdrem.overgearedmod.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgearedmod.OvergearedMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OvergearedMod.MOD_ID);

    public static final RegistryObject<Item> STEEL_COMPOUND = ITEMS.register("steel_compound",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_INGOT = ITEMS.register("steel_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEATED_IRON_INGOT = ITEMS.register("heated_iron_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEATED_STEEL_INGOT = ITEMS.register("heated_steel_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_TONGS = ITEMS.register("iron_tongs",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_TONGS = ITEMS.register("steel_tongs",
            () -> new Item(new Item.Properties()));

    // Add these inside your ModItems class
    public static final RegistryObject<Item> STONE_SWORD_BLADE = ITEMS.register("stone_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_SWORD_BLADE = ITEMS.register("iron_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLD_SWORD_BLADE = ITEMS.register("gold_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_SWORD_BLADE = ITEMS.register("steel_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_SWORD_BLADE = ITEMS.register("diamond_sword_blade",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STONE_PICKAXE_HEAD = ITEMS.register("stone_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_PICKAXE_HEAD = ITEMS.register("iron_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLD_PICKAXE_HEAD = ITEMS.register("gold_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_PICKAXE_HEAD = ITEMS.register("steel_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_PICKAXE_HEAD = ITEMS.register("diamond_pickaxe_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STONE_AXE_HEAD = ITEMS.register("stone_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_AXE_HEAD = ITEMS.register("iron_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLD_AXE_HEAD = ITEMS.register("gold_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_AXE_HEAD = ITEMS.register("steel_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_AXE_HEAD = ITEMS.register("diamond_axe_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STONE_SHOVEL_HEAD = ITEMS.register("stone_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_SHOVEL_HEAD = ITEMS.register("iron_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLD_SHOVEL_HEAD = ITEMS.register("gold_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_SHOVEL_HEAD = ITEMS.register("steel_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_SHOVEL_HEAD = ITEMS.register("diamond_shovel_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STONE_HOE_HEAD = ITEMS.register("stone_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_HOE_HEAD = ITEMS.register("iron_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLD_HOE_HEAD = ITEMS.register("gold_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_HOE_HEAD = ITEMS.register("steel_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_HOE_HEAD = ITEMS.register("diamond_hoe_head",
            () -> new Item(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

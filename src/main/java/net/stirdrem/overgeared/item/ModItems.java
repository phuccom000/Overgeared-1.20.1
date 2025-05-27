package net.stirdrem.overgeared.item;

import cech12.bucketlib.api.item.UniversalBucketItem;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.custom.SmithingHammer;
import net.stirdrem.overgeared.item.custom.Tongs;
import net.stirdrem.overgeared.item.custom.ToolParts;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OvergearedMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS_MINECRAFT =
            DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");

    /*public static final RegistryObject<Item> IRON_INGOT = ITEMS_MINECRAFT.register("iron_ingot",
            () -> new Item(new Item.Properties()));*/

    public static final RegistryObject<Item> STEEL_ALLOY = ITEMS.register("steel_alloy",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_INGOT = ITEMS.register("steel_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEATED_IRON_INGOT = ITEMS.register("heated_iron_ingot",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> HEATED_STEEL_INGOT = ITEMS.register("heated_steel_ingot",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> STEEL_TONG = ITEMS.register("steel_tong",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_TONGS = ITEMS.register("iron_tongs",
            () -> new Tongs(new Item.Properties().durability(512)));

    public static final RegistryObject<Item> STEEL_TONGS = ITEMS.register("steel_tongs",
            () -> new Tongs(new Item.Properties().durability(1024)));

    public static final RegistryObject<Item> SMITHING_HAMMER = ITEMS.register("smithing_hammer",
            () -> new SmithingHammer(new Item.Properties().durability(512)));
    public static final RegistryObject<Item> WOODEN_BUCKET = ITEMS.register("wooden_bucket",
            () -> new UniversalBucketItem(new UniversalBucketItem.Properties().durability(100)
                    .upperCrackingTemperature(ServerConfig.WOODEN_BUCKET_BREAK_TEMPERATURE)
                    //.crackingFluids(ModTags.Fluids.WOODEN_CRACKING)
                    .milking(ServerConfig.MILKING_ENABLED)
                    .entityObtaining(ServerConfig.FISH_OBTAINING_ENABLED)
                    //.dyeable(14975336)
                    .durability(ServerConfig.WOODEN_BUCKET_DURABILITY)
            ));

    // Add these inside your ModItems class
    public static final RegistryObject<Item> STONE_SWORD_BLADE = ITEMS.register("stone_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_SWORD_BLADE = ITEMS.register("iron_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_SWORD_BLADE = ITEMS.register("golden_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_SWORD_BLADE = ITEMS.register("steel_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_SWORD_BLADE = ITEMS.register("diamond_sword_blade",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STONE_PICKAXE_HEAD = ITEMS.register("stone_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_PICKAXE_HEAD = ITEMS.register("iron_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_PICKAXE_HEAD = ITEMS.register("golden_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_PICKAXE_HEAD = ITEMS.register("steel_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_PICKAXE_HEAD = ITEMS.register("diamond_pickaxe_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STONE_AXE_HEAD = ITEMS.register("stone_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_AXE_HEAD = ITEMS.register("iron_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_AXE_HEAD = ITEMS.register("golden_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_AXE_HEAD = ITEMS.register("steel_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_AXE_HEAD = ITEMS.register("diamond_axe_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STONE_SHOVEL_HEAD = ITEMS.register("stone_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_SHOVEL_HEAD = ITEMS.register("iron_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_SHOVEL_HEAD = ITEMS.register("golden_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_SHOVEL_HEAD = ITEMS.register("steel_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_SHOVEL_HEAD = ITEMS.register("diamond_shovel_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STONE_HOE_HEAD = ITEMS.register("stone_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_HOE_HEAD = ITEMS.register("iron_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_HOE_HEAD = ITEMS.register("golden_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_HOE_HEAD = ITEMS.register("steel_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_HOE_HEAD = ITEMS.register("diamond_hoe_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_SWORD = ITEMS.register("steel_sword",
            () -> new SwordItem(ModToolTiers.STEEL, 3, -2.4f, new Item.Properties()));
    public static final RegistryObject<Item> STEEL_PICKAXE = ITEMS.register("steel_pickaxe",
            () -> new PickaxeItem(ModToolTiers.STEEL, 1, -2.8f, new Item.Properties()));
    public static final RegistryObject<Item> STEEL_AXE = ITEMS.register("steel_axe",
            () -> new AxeItem(ModToolTiers.STEEL, 5, -3f, new Item.Properties()));
    public static final RegistryObject<Item> STEEL_HOE = ITEMS.register("steel_hoe",
            () -> new HoeItem(ModToolTiers.STEEL, -3, -0.5f, new Item.Properties()));
    public static final RegistryObject<Item> STEEL_SHOVEL = ITEMS.register("steel_shovel",
            () -> new ShovelItem(ModToolTiers.STEEL, 1, -3, new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }


}

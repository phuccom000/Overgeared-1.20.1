package net.stirdrem.overgeared.item;

//import cech12.bucketlib.api.item.UniversalBucketItem;

import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.item.armor.CopperHelmet;
import net.stirdrem.overgeared.item.armor.CopperLeggings;
import net.stirdrem.overgeared.item.custom.*;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OvergearedMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS_MINECRAFT =
            DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");

    /*public static final RegistryObject<Item> IRON_INGOT = ITEMS_MINECRAFT.register("iron_ingot",
            () -> new Item(new Item.Properties()));*/

    public static final RegistryObject<Item> CRUDE_STEEL = ITEMS.register("crude_steel",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEATED_CRUDE_STEEL = ITEMS.register("heated_crude_steel",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ROCK = ITEMS.register("knappable_rock",
            () -> new KnappableRockItem(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_INGOT = ITEMS.register("steel_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> NETHERITE_ALLOY = ITEMS.register("netherite_alloy",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> UNFIRED_TOOL_CAST = ITEMS.register("unfired_tool_cast",
            () -> new ToolCastItem(false, new Item.Properties()));

    public static final RegistryObject<Item> CLAY_TOOL_CAST = ITEMS.register("clay_tool_cast",
            () -> new ToolCastItem(() -> ServerConfig.FIRED_CAST_DURABILITY.get(), true,
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> NETHER_TOOL_CAST = ITEMS.register("nether_tool_cast",
            () -> new ToolCastItem(true, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> STEEL_NUGGET = ITEMS.register("steel_nugget",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_NUGGET = ITEMS.register("copper_nugget",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_ARROW_HEAD = ITEMS.register("iron_arrow_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_ARROW_HEAD = ITEMS.register("steel_arrow_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> DIAMOND_SHARD = ITEMS.register("diamond_shard",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEATED_IRON_INGOT = ITEMS.register("heated_iron_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEATED_COPPER_INGOT = ITEMS.register("heated_copper_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEATED_STEEL_INGOT = ITEMS.register("heated_steel_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEATED_SILVER_INGOT = ITEMS.register("heated_silver_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> HEATED_NETHERITE_ALLOY = ITEMS.register("heated_netherite_alloy",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_PLATE = ITEMS.register("copper_plate",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_PLATE = ITEMS.register("iron_plate",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_PLATE = ITEMS.register("steel_plate",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_TONG = ITEMS.register("steel_tong",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_TONG = ITEMS.register("iron_tong",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WOODEN_TONGS = ITEMS.register("wooden_tongs",
            () -> new Tongs(Tiers.WOOD, -1, -2f, new Item.Properties().durability(120)));
    public static final RegistryObject<Item> IRON_TONGS = ITEMS.register("iron_tongs",
            () -> new Tongs(Tiers.IRON, -1, -2f, new Item.Properties().durability(512)));

    public static final RegistryObject<Item> STEEL_TONGS = ITEMS.register("steel_tongs",
            () -> new Tongs(ModToolTiers.STEEL, -1, -2f, new Item.Properties().durability(1024)));

    public static final RegistryObject<Item> STONE_HAMMER_HEAD = ITEMS.register("stone_hammer_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_HAMMER_HEAD = ITEMS.register("copper_hammer_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STEEL_HAMMER_HEAD = ITEMS.register("steel_hammer_head",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SMITHING_HAMMER = ITEMS.register("smithing_hammer",
            () -> new SmithingHammer(ModToolTiers.STEEL, -1, -2.8f, new Item.Properties().durability(512)));

    public static final RegistryObject<Item> COPPER_SMITHING_HAMMER = ITEMS.register("copper_smithing_hammer",
            () -> new SmithingHammer(ModToolTiers.COPPER, -1, -2.8f, new Item.Properties().durability(120)));

    /*public static final RegistryObject<Item> WOODEN_BUCKET = ITEMS.register("wooden_bucket",
            () -> new UniversalBucketItem(new UniversalBucketItem.Properties().durability(100)
                    .upperCrackingTemperature(ServerConfig.WOODEN_BUCKET_BREAK_TEMPERATURE)
                    //.crackingFluids(ModTags.Fluids.WOODEN_CRACKING)
                    .milking(ServerConfig.MILKING_ENABLED)
                    .entityObtaining(ServerConfig.FISH_OBTAINING_ENABLED)
                    //.dyeable(14975336)
                    .durability(ServerConfig.WOODEN_BUCKET_DURABILITY)
            ));*/

    public static final RegistryObject<Item> DIAMOND_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("diamond_upgrade_smithing_template",
            DiamondUpgradeTemplateItem::createDiamondUpgradeTemplate);

    public static final RegistryObject<Item> EMPTY_BLUEPRINT = ITEMS.register("empty_blueprint",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BLUEPRINT = ITEMS.register("blueprint",
            () -> new BlueprintItem(new Item.Properties()));

    // Add these inside your ModItems class
    public static final RegistryObject<Item> STONE_SWORD_BLADE = ITEMS.register("stone_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_SWORD_BLADE = ITEMS.register("iron_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_SWORD_BLADE = ITEMS.register("golden_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_SWORD_BLADE = ITEMS.register("steel_sword_blade",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_SWORD_BLADE = ITEMS.register("copper_sword_blade",
            () -> new Item(new Item.Properties()));
/*
    public static final RegistryObject<Item> DIAMOND_SWORD_BLADE = ITEMS.register("diamond_sword_blade",
            () -> new Item(new Item.Properties()));
*/

    public static final RegistryObject<Item> STONE_PICKAXE_HEAD = ITEMS.register("stone_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_PICKAXE_HEAD = ITEMS.register("iron_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_PICKAXE_HEAD = ITEMS.register("golden_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_PICKAXE_HEAD = ITEMS.register("steel_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_PICKAXE_HEAD = ITEMS.register("copper_pickaxe_head",
            () -> new Item(new Item.Properties()));
/*
    public static final RegistryObject<Item> DIAMOND_PICKAXE_HEAD = ITEMS.register("diamond_pickaxe_head",
            () -> new Item(new Item.Properties()));
*/


    public static final RegistryObject<Item> STONE_AXE_HEAD = ITEMS.register("stone_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_AXE_HEAD = ITEMS.register("iron_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_AXE_HEAD = ITEMS.register("golden_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_AXE_HEAD = ITEMS.register("steel_axe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_AXE_HEAD = ITEMS.register("copper_axe_head",
            () -> new Item(new Item.Properties()));
/*    public static final RegistryObject<Item> DIAMOND_AXE_HEAD = ITEMS.register("diamond_axe_head",
            () -> new Item(new Item.Properties()));*/


    public static final RegistryObject<Item> STONE_SHOVEL_HEAD = ITEMS.register("stone_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_SHOVEL_HEAD = ITEMS.register("iron_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_SHOVEL_HEAD = ITEMS.register("golden_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_SHOVEL_HEAD = ITEMS.register("steel_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_SHOVEL_HEAD = ITEMS.register("copper_shovel_head",
            () -> new Item(new Item.Properties()));
/*    public static final RegistryObject<Item> DIAMOND_SHOVEL_HEAD = ITEMS.register("diamond_shovel_head",
            () -> new Item(new Item.Properties()));*/

    public static final RegistryObject<Item> STONE_HOE_HEAD = ITEMS.register("stone_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_HOE_HEAD = ITEMS.register("iron_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GOLDEN_HOE_HEAD = ITEMS.register("golden_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEEL_HOE_HEAD = ITEMS.register("steel_hoe_head",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_HOE_HEAD = ITEMS.register("copper_hoe_head",
            () -> new Item(new Item.Properties()));
/*
    public static final RegistryObject<Item> DIAMOND_HOE_HEAD = ITEMS.register("diamond_hoe_head",
            () -> new Item(new Item.Properties()));
*/

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

    public static final RegistryObject<Item> STEEL_HELMET = ITEMS.register("steel_helmet",
            () -> new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> STEEL_CHESTPLATE = ITEMS.register("steel_chestplate",
            () -> new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> STEEL_LEGGINGS = ITEMS.register("steel_leggings",
            () -> new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> STEEL_BOOTS = ITEMS.register("steel_boots",
            () -> new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> COPPER_HELMET = ITEMS.register("copper_helmet",
            () -> new CopperHelmet(ModArmorMaterials.COPPER, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> COPPER_CHESTPLATE = ITEMS.register("copper_chestplate",
            () -> new ArmorItem(ModArmorMaterials.COPPER, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> COPPER_LEGGINGS = ITEMS.register("copper_leggings",
            () -> new CopperLeggings(ModArmorMaterials.COPPER, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> COPPER_BOOTS = ITEMS.register("copper_boots",
            () -> new ArmorItem(ModArmorMaterials.COPPER, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> COPPER_SWORD = ITEMS.register("copper_sword",
            () -> new SwordItem(ModToolTiers.COPPER, 3, -2.4f, new Item.Properties()));
    public static final RegistryObject<Item> COPPER_PICKAXE = ITEMS.register("copper_pickaxe",
            () -> new PickaxeItem(ModToolTiers.COPPER, 1, -2.8f, new Item.Properties()));
    public static final RegistryObject<Item> COPPER_AXE = ITEMS.register("copper_axe",
            () -> new AxeItem(ModToolTiers.COPPER, 5, -3f, new Item.Properties()));
    public static final RegistryObject<Item> COPPER_HOE = ITEMS.register("copper_hoe",
            () -> new HoeItem(ModToolTiers.COPPER, -1, -1.5f, new Item.Properties()));
    public static final RegistryObject<Item> COPPER_SHOVEL = ITEMS.register("copper_shovel",
            () -> new ShovelItem(ModToolTiers.COPPER, 1.5f, -3, new Item.Properties()));

    public static final RegistryObject<Item> LINGERING_ARROW = ITEMS.register("lingering_arrow",
            () -> new LingeringArrowItem(new Item.Properties(), ArrowTier.FLINT));

    /*public static final RegistryObject<Item> MODULAR_ARROW = ITEMS.register("modular_arrow",
            () -> new ModularArrowItem(new Item.Properties()));*/


    public static final RegistryObject<Item> IRON_UPGRADE_ARROW = ITEMS.register("iron_arrow",
            () -> new UpgradeArrowItem(new Item.Properties(), ArrowTier.IRON));
    public static final RegistryObject<Item> STEEL_UPGRADE_ARROW = ITEMS.register("steel_arrow",
            () -> new UpgradeArrowItem(new Item.Properties(), ArrowTier.STEEL));
    public static final RegistryObject<Item> DIAMOND_UPGRADE_ARROW = ITEMS.register("diamond_arrow",
            () -> new UpgradeArrowItem(new Item.Properties(), ArrowTier.DIAMOND));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }


}

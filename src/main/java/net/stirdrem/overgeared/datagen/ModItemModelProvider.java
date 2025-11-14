package net.stirdrem.overgeared.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;

import java.util.LinkedHashMap;
import java.util.Locale;

public class ModItemModelProvider extends ItemModelProvider {
    private static LinkedHashMap<ResourceKey<TrimMaterial>, Float> trimMaterials = new LinkedHashMap<>();

    static {
        trimMaterials.put(TrimMaterials.QUARTZ, 0.1F);
        trimMaterials.put(TrimMaterials.IRON, 0.2F);
        trimMaterials.put(TrimMaterials.NETHERITE, 0.3F);
        trimMaterials.put(TrimMaterials.REDSTONE, 0.4F);
        trimMaterials.put(TrimMaterials.COPPER, 0.5F);
        trimMaterials.put(TrimMaterials.GOLD, 0.6F);
        trimMaterials.put(TrimMaterials.EMERALD, 0.7F);
        trimMaterials.put(TrimMaterials.DIAMOND, 0.8F);
        trimMaterials.put(TrimMaterials.LAPIS, 0.9F);
        trimMaterials.put(TrimMaterials.AMETHYST, 1.0F);
    }

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OvergearedMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.CRUDE_STEEL);
        simpleItem(ModItems.HEATED_CRUDE_STEEL);
        simpleItem(ModItems.ROCK);
        simpleItem(ModItems.STEEL_INGOT);
        simpleItem(ModItems.STEEL_NUGGET);
        simpleItem(ModItems.NETHERITE_ALLOY);
        simpleItem(ModItems.COPPER_NUGGET);
        simpleItem(ModItems.DIAMOND_SHARD);
        simpleItem(ModItems.IRON_ARROW_HEAD);
        simpleItem(ModItems.STEEL_ARROW_HEAD);
        simpleItem(ModItems.UNFIRED_TOOL_CAST);
        simpleItem(ModItems.CLAY_TOOL_CAST);
        simpleItem(ModItems.NETHER_TOOL_CAST);
        upgradeArrowModel(ModItems.IRON_UPGRADE_ARROW);
        upgradeArrowModel(ModItems.STEEL_UPGRADE_ARROW);
        upgradeArrowModel(ModItems.DIAMOND_UPGRADE_ARROW);
        simpleItem(ModItems.HEATED_COPPER_INGOT);
        simpleItem(ModItems.HEATED_IRON_INGOT);
        simpleItem(ModItems.HEATED_STEEL_INGOT);
        simpleItem(ModItems.HEATED_SILVER_INGOT);
        simpleItem(ModItems.HEATED_NETHERITE_ALLOY);
        simpleItem(ModItems.COPPER_PLATE);
        simpleItem(ModItems.IRON_PLATE);
        simpleItem(ModItems.STEEL_PLATE);
        simpleItem(ModItems.STEEL_TONG);
        simpleItem(ModItems.IRON_TONG);
        simpleItem(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE);
        simpleItem(ModItems.EMPTY_BLUEPRINT);
        simpleItem(ModItems.BLUEPRINT);
        trimmedArmorItem(ModItems.STEEL_HELMET);
        trimmedArmorItem(ModItems.STEEL_BOOTS);
        trimmedArmorItem(ModItems.STEEL_CHESTPLATE);
        trimmedArmorItem(ModItems.STEEL_LEGGINGS);
        trimmedArmorItem(ModItems.COPPER_HELMET);
        trimmedArmorItem(ModItems.COPPER_CHESTPLATE);
        trimmedArmorItemWithOverlay(ModItems.COPPER_LEGGINGS);
        trimmedArmorItem(ModItems.COPPER_BOOTS);

        simpleHandheld(ModItems.IRON_TONGS);
        simpleHandheld(ModItems.STEEL_TONGS);
        simpleHandheld(ModItems.WOODEN_TONGS);
        simpleHandheld(ModItems.STONE_HAMMER_HEAD);
        simpleHandheld(ModItems.COPPER_HAMMER_HEAD);
        simpleHandheld(ModItems.STEEL_HAMMER_HEAD);
        simpleHandheld(ModItems.SMITHING_HAMMER);
        simpleHandheld(ModItems.COPPER_SMITHING_HAMMER);
        simpleHandheld(ModItems.STEEL_SWORD);
        simpleHandheld(ModItems.STEEL_PICKAXE);
        simpleHandheld(ModItems.STEEL_AXE);
        simpleHandheld(ModItems.STEEL_SHOVEL);
        simpleHandheld(ModItems.STEEL_HOE);
        simpleHandheld(ModItems.COPPER_SWORD);
        simpleHandheld(ModItems.COPPER_PICKAXE);
        simpleHandheld(ModItems.COPPER_AXE);
        simpleHandheld(ModItems.COPPER_SHOVEL);
        simpleHandheld(ModItems.COPPER_HOE);

        // Sword Blades
        simpleItem(ModItems.STONE_SWORD_BLADE);
        simpleItem(ModItems.IRON_SWORD_BLADE);
        simpleItem(ModItems.GOLDEN_SWORD_BLADE);
        simpleItem(ModItems.STEEL_SWORD_BLADE);
        simpleItem(ModItems.COPPER_SWORD_BLADE);
        //simpleItem(ModItems.DIAMOND_SWORD_BLADE);

        // Pickaxe Heads
        simpleItem(ModItems.STONE_PICKAXE_HEAD);
        simpleItem(ModItems.IRON_PICKAXE_HEAD);
        simpleItem(ModItems.GOLDEN_PICKAXE_HEAD);
        simpleItem(ModItems.STEEL_PICKAXE_HEAD);
        simpleItem(ModItems.COPPER_PICKAXE_HEAD);
        //simpleItem(ModItems.DIAMOND_PICKAXE_HEAD);

        // Axe Heads
        simpleItem(ModItems.STONE_AXE_HEAD);
        simpleItem(ModItems.IRON_AXE_HEAD);
        simpleItem(ModItems.GOLDEN_AXE_HEAD);
        simpleItem(ModItems.STEEL_AXE_HEAD);
        simpleItem(ModItems.COPPER_AXE_HEAD);
        //simpleItem(ModItems.DIAMOND_AXE_HEAD);

        // Shovel Heads
        simpleItem(ModItems.STONE_SHOVEL_HEAD);
        simpleItem(ModItems.IRON_SHOVEL_HEAD);
        simpleItem(ModItems.GOLDEN_SHOVEL_HEAD);
        simpleItem(ModItems.STEEL_SHOVEL_HEAD);
        simpleItem(ModItems.COPPER_SHOVEL_HEAD);
        //simpleItem(ModItems.DIAMOND_SHOVEL_HEAD);

        // Hoe Heads
        simpleItem(ModItems.STONE_HOE_HEAD);
        simpleItem(ModItems.IRON_HOE_HEAD);
        simpleItem(ModItems.GOLDEN_HOE_HEAD);
        simpleItem(ModItems.STEEL_HOE_HEAD);
        simpleItem(ModItems.COPPER_HOE_HEAD);
        //simpleItem(ModItems.DIAMOND_HOE_HEAD);

    }


    // Shoutout to El_Redstoniano for making this
    private void trimmedArmorItem(RegistryObject<Item> itemRegistryObject) {
        final String MOD_ID = OvergearedMod.MOD_ID; // Change this to your mod id

        if (itemRegistryObject.get() instanceof ArmorItem armorItem) {
            trimMaterials.entrySet().forEach(entry -> {

                ResourceKey<TrimMaterial> trimMaterial = entry.getKey();
                float trimValue = entry.getValue();

                String armorType = switch (armorItem.getEquipmentSlot()) {
                    case HEAD -> "helmet";
                    case CHEST -> "chestplate";
                    case LEGS -> "leggings";
                    case FEET -> "boots";
                    default -> "";
                };

                String armorItemPath = "item/" + armorItem;
                String trimPath = "trims/items/" + armorType + "_trim_" + trimMaterial.location().getPath();
                String currentTrimName = armorItemPath + "_" + trimMaterial.location().getPath() + "_trim";
                ResourceLocation armorItemResLoc = ResourceLocation.tryBuild(MOD_ID, armorItemPath);
                ResourceLocation trimResLoc = ResourceLocation.tryParse(trimPath); // minecraft namespace
                ResourceLocation trimNameResLoc = ResourceLocation.tryBuild(MOD_ID, currentTrimName);

                // This is used for making the ExistingFileHelper acknowledge that this texture exist, so this will
                // avoid an IllegalArgumentException
                existingFileHelper.trackGenerated(trimResLoc, PackType.CLIENT_RESOURCES, ".png", "textures");

                // Trimmed armorItem files
                getBuilder(currentTrimName)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", armorItemResLoc)
                        .texture("layer1", trimResLoc);

                // Non-trimmed armorItem file (normal variant)
                this.withExistingParent(itemRegistryObject.getId().getPath(),
                                mcLoc("item/generated"))
                        .override()
                        .model(new ModelFile.UncheckedModelFile(trimNameResLoc))
                        .predicate(mcLoc("trim_type"), trimValue).end()
                        .texture("layer0",
                                ResourceLocation.tryBuild(MOD_ID,
                                        "item/" + itemRegistryObject.getId().getPath()));
            });
        }
    }

    private void trimmedArmorItemWithOverlay(RegistryObject<Item> itemRegistryObject) {
        final String MOD_ID = OvergearedMod.MOD_ID;

        if (itemRegistryObject.get() instanceof ArmorItem armorItem) {
            trimMaterials.entrySet().forEach(entry -> {
                ResourceKey<TrimMaterial> trimMaterial = entry.getKey();
                float trimValue = entry.getValue();

                String armorType = switch (armorItem.getEquipmentSlot()) {
                    case HEAD -> "helmet";
                    case CHEST -> "chestplate";
                    case LEGS -> "leggings";
                    case FEET -> "boots";
                    default -> "";
                };

                String armorItemPath = "item/" + itemRegistryObject.getId().getPath();
                String trimPath = "trims/items/" + armorType + "_trim_" + trimMaterial.location().getPath();
                String currentTrimName = armorItemPath + "_" + trimMaterial.location().getPath() + "_trim";

                ResourceLocation armorItemResLoc = ResourceLocation.tryBuild(MOD_ID, armorItemPath);
                ResourceLocation overlayResLoc = ResourceLocation.tryBuild(MOD_ID, armorItemPath + "_overlay");
                ResourceLocation trimResLoc = ResourceLocation.tryParse(trimPath); // "minecraft" namespace
                ResourceLocation trimNameResLoc = ResourceLocation.tryBuild(MOD_ID, currentTrimName);

                existingFileHelper.trackGenerated(trimResLoc, PackType.CLIENT_RESOURCES, ".png", "textures");

                // 🔷 Trimmed variant: layer0 = base, layer1 = overlay, layer2 = trim
                getBuilder(currentTrimName)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", armorItemResLoc)
                        .texture("layer1", trimResLoc)
                        .texture("layer2", overlayResLoc);

                // 🔷 Base item model (untrimmed)
                this.withExistingParent(itemRegistryObject.getId().getPath(),
                                mcLoc("item/generated"))
                        .texture("layer0", armorItemResLoc)
                        .texture("layer1", overlayResLoc) // overlay always included
                        .override()
                        .model(new ModelFile.UncheckedModelFile(trimNameResLoc))
                        .predicate(mcLoc("trim_type"), trimValue)
                        .end();
            });
        }
    }

    private String getToolTypeFromName(String itemName) {
        itemName = itemName.toLowerCase(Locale.ROOT);
        if (itemName.contains("sword")) return "sword";
        if (itemName.contains("axe")) return "axe";
        if (itemName.contains("pickaxe")) return "pickaxe";
        if (itemName.contains("shovel")) return "shovel";
        if (itemName.contains("hoe")) return "hoe";
        return "generic";
    }

    private void polishItem(RegistryObject<Item> itemRegistryObject) {
        final String MOD_ID = OvergearedMod.MOD_ID;
        Item item = itemRegistryObject.get();

        String itemPath = itemRegistryObject.getId().getPath(); // e.g. "steel_pickaxe_head"
        String toolType = getToolTypeFromName(itemPath);        // → "pickaxe"

        ResourceLocation baseTexture = ResourceLocation.tryBuild(MOD_ID, "item/" + itemPath);
        ResourceLocation overlayTexture = ResourceLocation.tryBuild(MOD_ID, "item/unpolished_overlay/" + toolType);
        ResourceLocation unpolishedModelLoc = ResourceLocation.tryBuild(MOD_ID, "item/" + itemPath + "_unpolished");

        // Ensure texture is tracked to avoid missing resource errors during datagen
        existingFileHelper.trackGenerated(overlayTexture, PackType.CLIENT_RESOURCES, ".png", "textures");

        // Model for unpolished version (layered with overlay)
        getBuilder(unpolishedModelLoc.getPath())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", baseTexture)
                .texture("layer1", overlayTexture);

        // Base model: polished item with override for unpolished (predicate polished=0)
        getBuilder(itemPath)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", baseTexture)
                .override()
                .model(new ModelFile.UncheckedModelFile(unpolishedModelLoc))
                .predicate(ResourceLocation.tryBuild(MOD_ID, "polished"), 0.0f)
                .end();
    }


    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.tryParse("item/generated")).texture("layer0",
                ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleHandheld(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.tryParse("item/handheld")).texture("layer0",
                ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "item/" + item.getId().getPath()));
    }

    public void evenSimplerBlockItem(RegistryObject<Block> block) {
        this.withExistingParent(OvergearedMod.MOD_ID + ":" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath(),
                modLoc("block/" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath()));
    }

    public void trapdoorItem(RegistryObject<Block> block) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(),
                modLoc("block/" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath() + "_bottom"));
    }

    public void fenceItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(), mcLoc("block/fence_inventory"))
                .texture("texture", ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(baseBlock.get()).getPath()));
    }

    public void buttonItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(), mcLoc("block/button_inventory"))
                .texture("texture", ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(baseBlock.get()).getPath()));
    }

    public void wallItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(), mcLoc("block/wall_inventory"))
                .texture("wall", ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(baseBlock.get()).getPath()));
    }

    private ItemModelBuilder handheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.tryParse("item/handheld")).texture("layer0",
                ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItem(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.tryParse("item/generated")).texture("layer0",
                ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItemBlockTexture(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.tryParse("item/generated")).texture("layer0",
                ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "block/" + item.getId().getPath()));
    }

    private void upgradeArrowModel(RegistryObject<Item> item) {
        String baseName = item.getId().getPath();

        String head = "item/" + baseName;

        String tippedHead = "item/tipped_" + baseName + "_head";
        String tippedBase = "item/tipped_" + baseName + "_base";

        String lingeringHead = "item/lingering_" + baseName + "_head";
        String lingeringBase = "item/lingering_" + baseName + "_base";

        // Base arrow (no potion) — only layer0
        getBuilder(baseName)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc(head))
                .override()
                .predicate(new ResourceLocation("overgeared", "potion_type"), 1f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/" + baseName + "_tipped")))
                .end()
                .override()
                .predicate(new ResourceLocation("overgeared", "potion_type"), 2f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/" + baseName + "_lingering")))
                .end();

        // Tipped arrow — 2 layers
        getBuilder(baseName + "_tipped")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc(tippedHead))
                .texture("layer1", modLoc(tippedBase));

        // Lingering arrow — 2 layers
        getBuilder(baseName + "_lingering")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc(lingeringHead))
                .texture("layer1", modLoc(lingeringBase));
    }

}
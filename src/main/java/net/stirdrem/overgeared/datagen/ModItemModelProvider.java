package net.stirdrem.overgeared.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;

import java.util.LinkedHashMap;
import java.util.Locale;

public class ModItemModelProvider extends ItemModelProvider {
    private static final LinkedHashMap<ResourceKey<TrimMaterial>, Float> trimMaterials = new LinkedHashMap<>();

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
//        simpleItem(ModItems.UNFIRED_TOOL_CAST);
//        simpleItem(ModItems.CLAY_TOOL_CAST);
//        simpleItem(ModItems.NETHER_TOOL_CAST);
        upgradeArrowModel(ModItems.LINGERING_ARROW);
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
//        simpleItem(ModItems.BLUEPRINT);
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
    private void trimmedArmorItem(DeferredHolder<Item, Item> itemRegistryObject) {
        if (itemRegistryObject.get() instanceof ArmorItem armorItem) {
            trimMaterials.forEach((trimMaterial, value) -> {

              float trimValue = value;

              String armorType = getEquipmentTypeFromSlot(armorItem.getEquipmentSlot());

              String armorItemPath = "item/" + ResourceLocation.parse(armorItem.toString()).getPath();
              String trimPath = "trims/items/" + armorType + "_trim_" + trimMaterial.location().getPath();
              String currentTrimName = armorItemPath + "_" + trimMaterial.location().getPath() + "_trim";
              ResourceLocation armorItemResLoc = OvergearedMod.loc(armorItemPath);
              ResourceLocation trimResLoc = ResourceLocation.tryParse(trimPath); // minecraft namespace
              ResourceLocation trimNameResLoc = OvergearedMod.loc(currentTrimName);

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
                      .texture("layer0", OvergearedMod.loc("item/" + itemRegistryObject.getId().getPath()));
            });
        }
    }

    private String getEquipmentTypeFromSlot(EquipmentSlot slot) {
      return switch (slot) {
        case HEAD -> "helmet";
        case CHEST -> "chestplate";
        case LEGS -> "leggings";
        case FEET -> "boots";
        default -> "";
      };
    }

    private void trimmedArmorItemWithOverlay(DeferredHolder<Item, Item> itemRegistryObject) {
        final String MOD_ID = OvergearedMod.MOD_ID;

        if (itemRegistryObject.get() instanceof ArmorItem armorItem) {
            trimMaterials.forEach((trimMaterial, value) -> {
              float trimValue = value;

              String armorType = getEquipmentTypeFromSlot(armorItem.getEquipmentSlot());

              String armorItemPath = "item/" + itemRegistryObject.getId().getPath();
              String trimPath = "trims/items/" + armorType + "_trim_" + trimMaterial.location().getPath();
              String currentTrimName = armorItemPath + "_" + trimMaterial.location().getPath() + "_trim";

              ResourceLocation armorItemResLoc = OvergearedMod.loc(armorItemPath);
              ResourceLocation overlayResLoc = OvergearedMod.loc(armorItemPath + "_overlay");
              ResourceLocation trimResLoc = mcLoc(trimPath);
              ResourceLocation trimNameResLoc = OvergearedMod.loc(currentTrimName);

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

    private void polishItem(DeferredHolder<Item, Item> itemRegistryObject) {
        Item item = itemRegistryObject.get();

        String itemPath = itemRegistryObject.getId().getPath(); // e.g. "steel_pickaxe_head"
        String toolType = getToolTypeFromName(itemPath);        // → "pickaxe"

        ResourceLocation baseTexture = OvergearedMod.loc("item/" + itemPath);
        ResourceLocation overlayTexture = OvergearedMod.loc("item/unpolished_overlay/" + toolType);
        ResourceLocation unpolishedModelLoc = OvergearedMod.loc("item/" + itemPath + "_unpolished");

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
                .predicate(OvergearedMod.loc("polished"), 0.0f)
                .end();
    }


    private void simpleItem(DeferredHolder<Item, Item> item) {
        withExistingParent(item.getId().getPath(),
                mcLoc("item/generated")).texture("layer0",
                OvergearedMod.loc("item/" + item.getId().getPath()));
    }

    private void simpleHandheld(DeferredHolder<Item, Item> item) {
        withExistingParent(item.getId().getPath(),
                mcLoc("item/handheld")).texture("layer0",
                OvergearedMod.loc("item/" + item.getId().getPath()));
    }

    public void evenSimplerBlockItem(DeferredHolder<Block, Block> block) {
        this.withExistingParent(OvergearedMod.MOD_ID + ":" + block.getRegisteredName(),
                modLoc("block/" + block.getRegisteredName()));
    }

    public void trapdoorItem(DeferredHolder<Block, Block> block) {
        this.withExistingParent(block.getRegisteredName(),
                modLoc("block/" + block.getRegisteredName() + "_bottom"));
    }

    public void fenceItem(DeferredHolder<Block, Block> block, DeferredHolder<Block, Block> baseBlock) {
        this.withExistingParent(block.getRegisteredName(), mcLoc("block/fence_inventory"))
                .texture("texture", OvergearedMod.loc("block/" + baseBlock.getRegisteredName()));
    }

    public void buttonItem(DeferredHolder<Block, Block> block, DeferredHolder<Block, Block> baseBlock) {
        this.withExistingParent(block.getRegisteredName(), mcLoc("block/button_inventory"))
                .texture("texture", OvergearedMod.loc("block/" + baseBlock.getRegisteredName()));
    }

    public void wallItem(DeferredHolder<Block, Block> block, DeferredHolder<Block, Block> baseBlock) {
        this.withExistingParent(block.getRegisteredName(), mcLoc("block/wall_inventory"))
                .texture("wall", OvergearedMod.loc("block/" + baseBlock.getRegisteredName()));
    }

    private ItemModelBuilder handheldItem(DeferredHolder<Item, Item> item) {
        return withExistingParent(item.getId().getPath(),
                mcLoc("item/handheld")).texture("layer0",
                OvergearedMod.loc("item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItem(DeferredHolder<Block, Block> item) {
        return withExistingParent(item.getId().getPath(),
                mcLoc("item/generated")).texture("layer0",
                OvergearedMod.loc("item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItemBlockTexture(DeferredHolder<Block, Block> item) {
        return withExistingParent(item.getId().getPath(),
                mcLoc("item/generated")).texture("layer0",
                OvergearedMod.loc("block/" + item.getId().getPath()));
    }

    private void upgradeArrowModel(DeferredHolder<Item, Item> item) {
        String baseName = item.getId().getPath();
        boolean isLingeringArrow = baseName.startsWith("lingering_");

        // For lingering arrows, get the base arrow name (without "lingering_" prefix)
        String baseArrowName = isLingeringArrow ? baseName.substring(10) : baseName;
        
        // For lingering arrows, use the base arrow texture
        ResourceLocation headLoc;
        if (isLingeringArrow) {
            if (baseArrowName.equals("arrow")) {
                // Use vanilla arrow texture
                headLoc = ResourceLocation.withDefaultNamespace("item/arrow");
            } else {
                // Use mod's arrow texture (e.g., iron_arrow, steel_arrow)
                headLoc = modLoc("item/" + baseArrowName);
            }
        } else {
            headLoc = modLoc("item/" + baseName);
        }

        String tippedHead = "item/tipped_" + baseArrowName + "_head";
        String tippedBase = "item/tipped_" + baseArrowName + "_base";

        String lingeringHead = "item/lingering_" + baseArrowName + "_head";
        String lingeringBase = "item/lingering_" + baseArrowName + "_base";

        // Base arrow (no potion) — only layer0
        ItemModelBuilder baseBuilder = getBuilder(baseName)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", headLoc);

        // Only add tipped override for non-lingering arrows
        if (!isLingeringArrow) {
            baseBuilder
                    .override()
                    .predicate(OvergearedMod.loc("potion_type"), 1f)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + baseName + "_tipped")))
                    .end();
        }

        // Add lingering override for all arrows
        baseBuilder
                .override()
                .predicate(OvergearedMod.loc("potion_type"), 2f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/" + baseName + "_lingering")))
                .end();

        // Tipped arrow — 2 layers (only for non-lingering arrows)
        if (!isLingeringArrow) {
            getBuilder(baseName + "_tipped")
                    .parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer0", modLoc(tippedHead))
                    .texture("layer1", modLoc(tippedBase));
        }

        // Lingering arrow — 2 layers
        getBuilder(baseName + "_lingering")
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc(lingeringHead))
                .texture("layer1", modLoc(lingeringBase));
    }
}
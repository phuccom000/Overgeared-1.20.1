package net.stirdrem.overgearedmod.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.stirdrem.overgearedmod.OvergearedMod;
import net.stirdrem.overgearedmod.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OvergearedMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.STEEL_ALLOY);
        simpleItem(ModItems.STEEL_INGOT);
        simpleItem(ModItems.HEATED_IRON_INGOT);
        simpleItem(ModItems.HEATED_STEEL_INGOT);
        simpleHandheld(ModItems.IRON_TONGS);
        simpleHandheld(ModItems.STEEL_TONGS);
        simpleHandheld(ModItems.SMITHING_HAMMER);
        simpleHandheld(ModItems.STEEL_SWORD);
        simpleHandheld(ModItems.STEEL_PICKAXE);
        simpleHandheld(ModItems.STEEL_AXE);
        simpleHandheld(ModItems.STEEL_SHOVEL);
        simpleHandheld(ModItems.STEEL_HOE);

        // Sword Blades
        simpleItem(ModItems.STONE_SWORD_BLADE);
        simpleItem(ModItems.IRON_SWORD_BLADE);
        simpleItem(ModItems.GOLDEN_SWORD_BLADE);
        simpleItem(ModItems.STEEL_SWORD_BLADE);
        simpleItem(ModItems.DIAMOND_SWORD_BLADE);

        // Pickaxe Heads
        simpleItem(ModItems.STONE_PICKAXE_HEAD);
        simpleItem(ModItems.IRON_PICKAXE_HEAD);
        simpleItem(ModItems.GOLDEN_PICKAXE_HEAD);
        simpleItem(ModItems.STEEL_PICKAXE_HEAD);
        simpleItem(ModItems.DIAMOND_PICKAXE_HEAD);

        // Axe Heads
        simpleItem(ModItems.STONE_AXE_HEAD);
        simpleItem(ModItems.IRON_AXE_HEAD);
        simpleItem(ModItems.GOLDEN_AXE_HEAD);
        simpleItem(ModItems.STEEL_AXE_HEAD);
        simpleItem(ModItems.DIAMOND_AXE_HEAD);

        // Shovel Heads
        simpleItem(ModItems.STONE_SHOVEL_HEAD);
        simpleItem(ModItems.IRON_SHOVEL_HEAD);
        simpleItem(ModItems.GOLDEN_SHOVEL_HEAD);
        simpleItem(ModItems.STEEL_SHOVEL_HEAD);
        simpleItem(ModItems.DIAMOND_SHOVEL_HEAD);

        // Hoe Heads
        simpleItem(ModItems.STONE_HOE_HEAD);
        simpleItem(ModItems.IRON_HOE_HEAD);
        simpleItem(ModItems.GOLDEN_HOE_HEAD);
        simpleItem(ModItems.STEEL_HOE_HEAD);
        simpleItem(ModItems.DIAMOND_HOE_HEAD);

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
}
package net.stirdrem.overgeared.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.client.ForgingBookRecipeBookTab;
import net.stirdrem.overgeared.item.ModItems;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    private static final List<ItemLike> STEEL_SMELTABLES = List.of(
            ModItems.CRUDE_STEEL.get()
    );

    private static final List<ItemLike> IRON_SOURCE = List.of(
            Items.IRON_INGOT,
            Items.RAW_IRON,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.IRON_ORE
    );

    private static final List<ItemLike> STEEL_INGOT = List.of(
            ModItems.STEEL_INGOT.get()
    );


    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        CompoundTag nbtTag = new CompoundTag();
        nbtTag.putString("heat",
                "200");

        //oreCampfire(pWriter, STEEL_SMELTABLES, RecipeCategory.MISC, ModItems.STEEL_INGOT.get(), 0.7f, 140, "");
        //oreCampfire(pWriter, IRON_INGOT, RecipeCategory.MISC, ModItems.HEATED_IRON_INGOT.get(), 0.5f, 140, "iron_ingot");
        //oreCampfire(pWriter, STEEL_INGOT, RecipeCategory.MISC, ModItems.HEATED_STEEL_INGOT.get(), 0.7f, 140, "steel_ingot");
        oreBlasting(pWriter, STEEL_SMELTABLES, RecipeCategory.MISC, ModItems.STEEL_INGOT.get(), 0.7f, 100, "steel_ingot");
        oreBlasting(pWriter, IRON_SOURCE, RecipeCategory.MISC, ModItems.HEATED_IRON_INGOT.get(), 0.7f, 100, "iron_ingot");
        //oreBlasting(pWriter, STEEL_INGOT, RecipeCategory.MISC, ModItems.HEATED_STEEL_INGOT.get(), 0.7f, 100, "steel_ingot");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_BLOCK.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy("has_steel_ingot", has(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel"))))
                .save(pWriter);

/*        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.IRON_AXE)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.IRON_AXE_HEAD.get()), has(ModItems.IRON_AXE_HEAD.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.IRON_PICKAXE)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.IRON_PICKAXE_HEAD.get()), has(ModItems.IRON_PICKAXE_HEAD.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.IRON_SHOVEL)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.IRON_SHOVEL_HEAD.get()), has(ModItems.IRON_SHOVEL_HEAD.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.IRON_HOE)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.IRON_HOE_HEAD.get()), has(ModItems.IRON_HOE_HEAD.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.IRON_SWORD)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.IRON_SWORD_BLADE.get()), has(ModItems.IRON_SWORD_BLADE.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.GOLDEN_AXE)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.GOLDEN_AXE_HEAD.get()), has(ModItems.GOLDEN_AXE_HEAD.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.GOLDEN_PICKAXE)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.GOLDEN_PICKAXE_HEAD.get()), has(ModItems.GOLDEN_PICKAXE_HEAD.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.GOLDEN_SHOVEL)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.GOLDEN_SHOVEL_HEAD.get()), has(ModItems.GOLDEN_SHOVEL_HEAD.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.GOLDEN_HOE)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.GOLDEN_HOE_HEAD.get()), has(ModItems.GOLDEN_HOE_HEAD.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.GOLDEN_SWORD)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.GOLDEN_SWORD_BLADE.get()), has(ModItems.GOLDEN_SWORD_BLADE.get()))
                .save(pWriter);*/

        /// //////////////////////
        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.IRON_AXE)
                .requires(ModItems.IRON_AXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_AXE_HEAD.get()), has(ModItems.IRON_AXE_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.IRON_PICKAXE)
                .requires(ModItems.IRON_PICKAXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_PICKAXE_HEAD.get()), has(ModItems.IRON_PICKAXE_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.IRON_SHOVEL)
                .requires(ModItems.IRON_SHOVEL_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_SHOVEL_HEAD.get()), has(ModItems.IRON_SHOVEL_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.IRON_HOE)
                .requires(ModItems.IRON_HOE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_HOE_HEAD.get()), has(ModItems.IRON_HOE_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.IRON_SWORD)
                .requires(ModItems.IRON_SWORD_BLADE.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_SWORD_BLADE.get()), has(ModItems.IRON_SWORD_BLADE.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_AXE.get())
                .requires(ModItems.STEEL_AXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_AXE_HEAD.get()), has(ModItems.STEEL_AXE_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_PICKAXE.get())
                .requires(ModItems.STEEL_PICKAXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_PICKAXE_HEAD.get()), has(ModItems.STEEL_PICKAXE_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_SHOVEL.get())
                .requires(ModItems.STEEL_SHOVEL_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_SHOVEL_HEAD.get()), has(ModItems.STEEL_SHOVEL_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_HOE.get())
                .requires(ModItems.STEEL_HOE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_HOE_HEAD.get()), has(ModItems.STEEL_HOE_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_SWORD.get())
                .requires(ModItems.STEEL_SWORD_BLADE.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_SWORD_BLADE.get()), has(ModItems.STEEL_SWORD_BLADE.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.GOLDEN_AXE)
                .requires(ModItems.GOLDEN_AXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_AXE_HEAD.get()), has(ModItems.GOLDEN_AXE_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.GOLDEN_PICKAXE)
                .requires(ModItems.GOLDEN_PICKAXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_PICKAXE_HEAD.get()), has(ModItems.GOLDEN_PICKAXE_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.GOLDEN_SHOVEL)
                .requires(ModItems.GOLDEN_SHOVEL_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_SHOVEL_HEAD.get()), has(ModItems.GOLDEN_SHOVEL_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.GOLDEN_HOE)
                .requires(ModItems.GOLDEN_HOE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_HOE_HEAD.get()), has(ModItems.GOLDEN_HOE_HEAD.get()))
                .save(pWriter);

        OvergearedShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.GOLDEN_SWORD)
                .requires(ModItems.GOLDEN_SWORD_BLADE.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_SWORD_BLADE.get()), has(ModItems.GOLDEN_SWORD_BLADE.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.STEEL_INGOT.get(), 9)
                .requires(ModBlocks.STEEL_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.STEEL_BLOCK.get()), has(ModBlocks.STEEL_BLOCK.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.SMITHING_ANVIL.get())
                .requires(ModItems.SMITHING_HAMMER.get())
                .requires(Blocks.ANVIL)
                .unlockedBy(getHasName(ModItems.SMITHING_HAMMER.get()), has(ModItems.SMITHING_HAMMER.get()))
                .save(pWriter);

        /*ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get(), 2)
                .pattern("axa")
                .pattern("aba")
                .pattern("aaa")
                .define('a', ModItems.STEEL_INGOT.get())
                .define('b', Items.DIAMOND)
                .define('x', ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get())
                .unlockedBy(getHasName(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()), has(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()))
                .save(pWriter);*/
        /*ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.STEEL_BLOCK.get(), 5)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy("has_steel_ingot", has(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel"))))
                .save(pWriter, OvergearedMod.MOD_ID + ":" + getItemName(ModBlocks.STEEL_BLOCK.get()) + "_from_forging_" + getItemName(ModItems.STEEL_INGOT.get()));*/

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_PLATE.get(), 3)
                .setQuality(false)
                .pattern("#")
                .define('#', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_PLATE.get(), 4)
                .setQuality(false)
                .pattern("#")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.STEEL_INGOT.get()), has(ModItems.STEEL_INGOT.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_PICKAXE_HEAD.get(), 3)
                .pattern("###")
                .define('#', ModItems.HEATED_IRON_INGOT.get())
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_SWORD_BLADE.get(), 3)
                .pattern("#")
                .pattern("#")
                .define('#', ModItems.HEATED_IRON_INGOT.get())
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_SHOVEL_HEAD.get(), 3)
                .pattern("#")
                .define('#', ModItems.HEATED_IRON_INGOT.get())
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_HOE_HEAD.get(), 3)
                .pattern("##")
                .define('#', ModItems.HEATED_IRON_INGOT.get())
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_AXE_HEAD.get(), 3)
                .pattern("##")
                .pattern("# ")
                .define('#', ModItems.HEATED_IRON_INGOT.get())
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);
// Steel Pickaxe Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_PICKAXE_HEAD.get(), 3)
                .pattern("###")
                .define('#', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy("has_steel_ingot", has(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel"))))
                .save(pWriter);

// Steel Sword Blade
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_SWORD_BLADE.get(), 3)
                .pattern("#")
                .pattern("#")
                .define('#', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy("has_steel_ingot", has(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel"))))
                .save(pWriter);

// Steel Shovel Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_SHOVEL_HEAD.get(), 3)
                .pattern("#")
                .define('#', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy("has_steel_ingot", has(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel"))))
                .save(pWriter);

// Steel Hoe Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_HOE_HEAD.get(), 3)
                .pattern("##")
                .define('#', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy("has_steel_ingot", has(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel"))))
                .save(pWriter);

// Steel Axe Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_AXE_HEAD.get(), 3)
                .pattern("##")
                .pattern("# ")
                .define('#', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy("has_steel_ingot", has(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel"))))
                .save(pWriter);
// Gold Pickaxe Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLDEN_PICKAXE_HEAD.get(), 3)
                .pattern("###")
                .define('#', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .save(pWriter);

// Gold Sword Blade
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLDEN_SWORD_BLADE.get(), 3)
                .pattern("#")
                .pattern("#")
                .define('#', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .save(pWriter);

// Gold Shovel Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLDEN_SHOVEL_HEAD.get(), 3)
                .pattern("#")
                .define('#', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .save(pWriter);

// Gold Hoe Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLDEN_HOE_HEAD.get(), 3)
                .pattern("##")
                .define('#', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .save(pWriter);

// Gold Axe Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLDEN_AXE_HEAD.get(), 3)
                .pattern("##")
                .pattern("# ")
                .define('#', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .save(pWriter);

        /*ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.WATER_BARREL.get())
                .pattern("# #")
                .pattern("#x#")
                .define('#', ItemTags.PLANKS)
                .define('x', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(pWriter);*/

       /* ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WOODEN_BUCKET.get())
                .pattern("# #")
                .pattern(" # ")
                .define('#', ItemTags.LOGS)
                .unlockedBy("has_logs", has(ItemTags.LOGS))
                .save(pWriter);*/


       /* ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SMITHING_HAMMER.get())
                .pattern(" x ")
                .pattern(" #x")
                .pattern("#  ")
                .define('#', Items.STICK)
                .define('x', ModItems.STEEL_INGOT.get())
                .unlockedBy("has_steel_ingot", has(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel"))))
                .save(pWriter);*/

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.IRON_TONGS.get())
                .pattern(" x ")
                .pattern("xxx")
                .pattern(" x ")
                .define('x', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.STEEL_TONG.get(), 2)
                .setQuality(false)
                .pattern("  x")
                .pattern(" xx")
                .pattern("x  ")
                .define('x', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy("has_steel_ingot", has(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel"))))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_TONGS.get())
                .requires(ModItems.STEEL_TONG.get())
                .requires(ModItems.STEEL_TONG.get())
                .unlockedBy("has_steel_ingot", has(ItemTags.create(ResourceLocation.tryBuild("forge", "ingots/steel"))))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CRUDE_STEEL.get(), 3)
                .requires(Items.COAL)
                .requires(Items.IRON_INGOT)
                .requires(Items.IRON_INGOT)
                .requires(Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .unlockedBy(getHasName(Items.COAL), has(Items.COAL))
                .save(pWriter);
        ;

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, Items.BUCKET, 3)
                .setQuality(false)
                .pattern("# #")
                .pattern(" # ")
                .define('#', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, Items.SHEARS, 3)
                .setQuality(false)
                .pattern(" #")
                .pattern("# ")
                .define('#', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.CAULDRON, 5)
                .setQuality(false)
                .pattern("# #")
                .pattern("# #")
                .pattern("###")
                .define('#', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.HEATED_STEEL_INGOT.get()), has(ModItems.HEATED_STEEL_INGOT.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.IRON_HELMET, 3)
                .pattern("###")
                .pattern("# #")
                .define('#', ModItems.IRON_PLATE.get())
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .unlockedBy(getHasName(ModItems.IRON_PLATE.get()), has(ModItems.IRON_PLATE.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.IRON_CHESTPLATE, 5)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .define('#', ModItems.IRON_PLATE.get())
                .unlockedBy(getHasName(ModItems.IRON_PLATE.get()), has(ModItems.IRON_PLATE.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.IRON_LEGGINGS, 4)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .define('#', ModItems.IRON_PLATE.get())
                .unlockedBy(getHasName(ModItems.IRON_PLATE.get()), has(ModItems.IRON_PLATE.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.IRON_BOOTS, 3)
                .pattern("# #")
                .pattern("# #")
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .define('#', ModItems.IRON_PLATE.get())
                .unlockedBy(getHasName(ModItems.IRON_PLATE.get()), has(ModItems.IRON_PLATE.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEEL_HELMET.get(), 3)
                .pattern("###")
                .pattern("# #")
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .define('#', ModItems.STEEL_PLATE.get())
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEEL_CHESTPLATE.get(), 5)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .define('#', ModItems.STEEL_PLATE.get())
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEEL_LEGGINGS.get(), 4)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .define('#', ModItems.STEEL_PLATE.get())
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.STEEL_BOOTS.get(), 3)
                .pattern("# #")
                .pattern("# #")
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .define('#', ModItems.STEEL_PLATE.get())
                .unlockedBy("has_steel_plate", has(ModItems.STEEL_PLATE.get()))
                .save(pWriter);


        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.GOLDEN_HELMET, 3)
                .pattern("###")
                .pattern("# #")
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .define('#', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.GOLDEN_CHESTPLATE, 5)
                .pattern("# #")
                .pattern("###")
                .pattern("###")
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .define('#', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.GOLDEN_LEGGINGS, 4)
                .pattern("###")
                .pattern("# #")
                .pattern("# #")
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .define('#', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.GOLDEN_BOOTS, 3)
                .pattern("# #")
                .pattern("# #")
                .setRecipeBookTab(ForgingBookRecipeBookTab.ARMORS)
                .define('#', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .save(pWriter);

        // Steel Axe to Diamond Axe
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_AXE.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_AXE
                ).unlocks("has_diamond", has(Items.DIAMOND))
                .save(pWriter, ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_axe_to_diamond_axe"));

        // Steel Pickaxe to Diamond Pickaxe
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_PICKAXE.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.TOOLS,
                        Items.DIAMOND_PICKAXE
                ).unlocks("has_diamond", has(Items.DIAMOND))
                .save(pWriter, ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_pickaxe_to_diamond_pickaxe"));

        // Steel Shovel to Diamond Shovel
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_SHOVEL.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.TOOLS,
                        Items.DIAMOND_SHOVEL
                ).unlocks("has_diamond", has(Items.DIAMOND))
                .save(pWriter, ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_shovel_to_diamond_shovel"));

        // Steel Hoe to Diamond Hoe
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_HOE.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.TOOLS,
                        Items.DIAMOND_HOE
                ).unlocks("has_diamond", has(Items.DIAMOND))
                .save(pWriter, ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_hoe_to_diamond_hoe"));

        // Steel Sword to Diamond Sword
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_SWORD.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_SWORD
                ).unlocks("has_diamond", has(Items.DIAMOND))
                .save(pWriter, ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_sword_to_diamond_sword"));

        // Steel Helmet to Diamond Helmet
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_HELMET.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_HELMET
                ).unlocks("has_diamond", has(Items.DIAMOND))
                .save(pWriter, ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_helmet_to_diamond_helmet"));

// Steel Chestplate to Diamond Chestplate
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_CHESTPLATE.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_CHESTPLATE
                ).unlocks("has_diamond", has(Items.DIAMOND))
                .save(pWriter, ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_chestplate_to_diamond_chestplate"));

// Steel Leggings to Diamond Leggings
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_LEGGINGS.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_LEGGINGS
                ).unlocks("has_diamond", has(Items.DIAMOND))
                .save(pWriter, ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_leggings_to_diamond_leggings"));

// Steel Boots to Diamond Boots
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(ModItems.STEEL_BOOTS.get()),
                        Ingredient.of(Items.DIAMOND),
                        RecipeCategory.COMBAT,
                        Items.DIAMOND_BOOTS
                ).unlocks("has_diamond", has(Items.DIAMOND))
                .save(pWriter, ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "steel_boots_to_diamond_boots"));
    }

    /*protected static void oreSmelting(Consumer<FinishedRecipe> consumer, List<ItemLike> ingredients, @Nullable CompoundTag nbt,
                                      RecipeCategory category, ItemLike result, float experience,
                                      int cookingTime, String group) {
        oreCooking(consumer, RecipeSerializer.SMELTING_RECIPE, ingredients, category, result, nbt, experience, cookingTime, group, "_from_smelting");

    }

    protected static void oreBlasting(Consumer<FinishedRecipe> consumer, List<ItemLike> ingredients, @Nullable CompoundTag nbt,
                                      RecipeCategory category, ItemLike result, float experience,
                                      int cookingTime, String group) {
        oreCooking(consumer, RecipeSerializer.BLASTING_RECIPE, ingredients, category, result, nbt, experience, cookingTime, group, "_from_blasting");
    }


    protected static void oreCooking(Consumer<FinishedRecipe> consumer, RecipeSerializer<? extends AbstractCookingRecipe> serializer,
                                     List<ItemLike> ingredients, RecipeCategory category, ItemLike result,
                                     @Nullable CompoundTag resultNbt, float experience, int cookingTime,
                                     String group, String recipeName) {
        for (ItemLike itemlike : ingredients) {
            CustomCookingRecipeBuilder.generic(Ingredient.of(itemlike), category, resultNbt, result,
                            experience, cookingTime, serializer)
                    .group(group).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(consumer, OvergearedMod.MOD_ID + ":" + getItemName(result) + recipeName + "_" + getItemName(itemlike));
        }
    }*/

    protected static void oreSmelting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreCampfire(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.CAMPFIRE_COOKING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_campfire");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for (ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult,
                            pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pFinishedRecipeConsumer, OvergearedMod.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }


}

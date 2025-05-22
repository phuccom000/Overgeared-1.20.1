package net.stirdrem.overgeared.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    private static final List<ItemLike> STEEL_SMELTABLES = List.of(
            ModItems.STEEL_ALLOY.get()
    );

    private static final List<ItemLike> IRON_INGOT = List.of(
            Items.IRON_INGOT
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
        nbtTag.putString("heat", "200");

        //oreSmoking(pWriter, STEEL_SMELTABLES, RecipeCategory.MISC, ModItems.STEEL_INGOT.get(), 0.7f, 140, "");
        //oreSmoking(pWriter, IRON_INGOT, RecipeCategory.MISC, ModItems.HEATED_IRON_INGOT.get(), 0.5f, 140, "iron_ingot");
        //oreSmoking(pWriter, STEEL_INGOT, RecipeCategory.MISC, ModItems.HEATED_STEEL_INGOT.get(), 0.7f, 140, "steel_ingot");
        oreBlasting(pWriter, STEEL_SMELTABLES, RecipeCategory.MISC, ModItems.STEEL_INGOT.get(), 0.7f, 70, "steel_ingot");
        oreBlasting(pWriter, IRON_INGOT, RecipeCategory.MISC, ModItems.HEATED_IRON_INGOT.get(), 0.5f, 70, "iron_ingot");
        oreBlasting(pWriter, STEEL_INGOT, RecipeCategory.MISC, ModItems.HEATED_STEEL_INGOT.get(), 0.7f, 70, "steel_ingot");

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_BLOCK.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.STEEL_INGOT.get()), has(ModItems.STEEL_INGOT.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.IRON_AXE)
                .requires(ModItems.IRON_AXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_AXE_HEAD.get()), has(ModItems.IRON_AXE_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.IRON_PICKAXE)
                .requires(ModItems.IRON_PICKAXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_PICKAXE_HEAD.get()), has(ModItems.IRON_PICKAXE_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.IRON_SHOVEL)
                .requires(ModItems.IRON_SHOVEL_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_SHOVEL_HEAD.get()), has(ModItems.IRON_SHOVEL_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.IRON_HOE)
                .requires(ModItems.IRON_HOE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_HOE_HEAD.get()), has(ModItems.IRON_HOE_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.IRON_SWORD)
                .requires(ModItems.IRON_SWORD_BLADE.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.IRON_SWORD_BLADE.get()), has(ModItems.IRON_SWORD_BLADE.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_AXE.get())
                .requires(ModItems.STEEL_AXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_AXE_HEAD.get()), has(ModItems.STEEL_AXE_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_PICKAXE.get())
                .requires(ModItems.STEEL_PICKAXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_PICKAXE_HEAD.get()), has(ModItems.STEEL_PICKAXE_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_SHOVEL.get())
                .requires(ModItems.STEEL_SHOVEL_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_SHOVEL_HEAD.get()), has(ModItems.STEEL_SHOVEL_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_HOE.get())
                .requires(ModItems.STEEL_HOE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_HOE_HEAD.get()), has(ModItems.STEEL_HOE_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.STEEL_SWORD.get())
                .requires(ModItems.STEEL_SWORD_BLADE.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.STEEL_SWORD_BLADE.get()), has(ModItems.STEEL_SWORD_BLADE.get()))
                .save(pWriter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.GOLDEN_AXE)
                .requires(ModItems.GOLDEN_AXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_AXE_HEAD.get()), has(ModItems.GOLDEN_AXE_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.GOLDEN_PICKAXE)
                .requires(ModItems.GOLDEN_PICKAXE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_PICKAXE_HEAD.get()), has(ModItems.GOLDEN_PICKAXE_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.GOLDEN_SHOVEL)
                .requires(ModItems.GOLDEN_SHOVEL_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_SHOVEL_HEAD.get()), has(ModItems.GOLDEN_SHOVEL_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.GOLDEN_HOE)
                .requires(ModItems.GOLDEN_HOE_HEAD.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_HOE_HEAD.get()), has(ModItems.GOLDEN_HOE_HEAD.get()))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, Items.GOLDEN_SWORD)
                .requires(ModItems.GOLDEN_SWORD_BLADE.get())
                .requires(Items.STICK)
                .unlockedBy(getHasName(ModItems.GOLDEN_SWORD_BLADE.get()), has(ModItems.GOLDEN_SWORD_BLADE.get()))
                .save(pWriter);


        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.STEEL_INGOT.get(), 9)
                .requires(ModBlocks.STEEL_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.STEEL_BLOCK.get()), has(ModBlocks.STEEL_BLOCK.get()))
                .save(pWriter);

        /*ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.STEEL_BLOCK.get(), 5)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.STEEL_INGOT.get()), has(ModItems.STEEL_INGOT.get()))
                .save(pWriter, OvergearedMod.MOD_ID + ":" + getItemName(ModBlocks.STEEL_BLOCK.get()) + "_from_forging_" + getItemName(ModItems.STEEL_INGOT.get()));*/

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_PICKAXE_HEAD.get(), 3)
                .pattern("###")
                .define('#', ModItems.HEATED_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.HEATED_IRON_INGOT.get()), has(ModItems.HEATED_IRON_INGOT.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_SWORD_BLADE.get(), 3)
                .pattern("#")
                .pattern("#")
                .define('#', ModItems.HEATED_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.HEATED_IRON_INGOT.get()), has(ModItems.HEATED_IRON_INGOT.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_SHOVEL_HEAD.get(), 3)
                .pattern("#")
                .define('#', ModItems.HEATED_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.HEATED_IRON_INGOT.get()), has(ModItems.HEATED_IRON_INGOT.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_HOE_HEAD.get(), 3)
                .pattern("##")
                .define('#', ModItems.HEATED_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.HEATED_IRON_INGOT.get()), has(ModItems.HEATED_IRON_INGOT.get()))
                .save(pWriter);

        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_AXE_HEAD.get(), 3)
                .pattern("##")
                .pattern("# ")
                .define('#', ModItems.HEATED_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.HEATED_IRON_INGOT.get()), has(ModItems.HEATED_IRON_INGOT.get()))
                .save(pWriter);
// Steel Pickaxe Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_PICKAXE_HEAD.get(), 3)
                .pattern("###")
                .define('#', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.HEATED_STEEL_INGOT.get()), has(ModItems.HEATED_STEEL_INGOT.get()))
                .save(pWriter);

// Steel Sword Blade
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_SWORD_BLADE.get(), 3)
                .pattern("#")
                .pattern("#")
                .define('#', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.HEATED_STEEL_INGOT.get()), has(ModItems.HEATED_STEEL_INGOT.get()))
                .save(pWriter);

// Steel Shovel Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_SHOVEL_HEAD.get(), 3)
                .pattern("#")
                .define('#', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.HEATED_STEEL_INGOT.get()), has(ModItems.HEATED_STEEL_INGOT.get()))
                .save(pWriter);

// Steel Hoe Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_HOE_HEAD.get(), 3)
                .pattern("##")
                .define('#', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.HEATED_STEEL_INGOT.get()), has(ModItems.HEATED_STEEL_INGOT.get()))
                .save(pWriter);

// Steel Axe Head
        ShapedForgingRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_AXE_HEAD.get(), 3)
                .pattern("##")
                .pattern("# ")
                .define('#', ModItems.HEATED_STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.HEATED_STEEL_INGOT.get()), has(ModItems.HEATED_STEEL_INGOT.get()))
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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.WATER_BARREL.get())
                .pattern("# #")
                .pattern("#x#")
                .define('#', ItemTags.PLANKS)
                .define('x', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(pWriter);


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

    protected static void oreSmoking(Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMOKING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_smoking");
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

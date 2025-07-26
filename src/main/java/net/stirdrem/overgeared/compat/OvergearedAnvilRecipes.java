package net.stirdrem.overgeared.compat;

import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;

import java.util.List;
import java.util.stream.Stream;

public class OvergearedAnvilRecipes {

    public static Stream<IJeiAnvilRecipe> getOvergearedAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        // Later you can concat enchantment recipes here
        return Stream.concat(
                getCustomRepairRecipes(vanillaRecipeFactory, ingredientManager),
                Stream.empty() // placeholder for potential enchantment recipes
        );
    }

    private static Stream<IJeiAnvilRecipe> getCustomRepairRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
        return getCustomRepairData()
                .flatMap(repairData -> createRepairRecipes(repairData, vanillaRecipeFactory, ingredientManager));
    }

    private static Stream<RepairData> getCustomRepairData() {
        return Stream.of(
                // Copper tools repair
                new RepairData(
                        Ingredient.of(Items.IRON_INGOT),
                        new ItemStack(ModItems.IRON_TONGS.get())
                ),
                new RepairData(
                        Ingredient.of(Items.COPPER_INGOT),
                        new ItemStack(ModItems.COPPER_SWORD.get()),
                        new ItemStack(ModItems.COPPER_PICKAXE.get()),
                        new ItemStack(ModItems.COPPER_AXE.get()),
                        new ItemStack(ModItems.COPPER_SHOVEL.get()),
                        new ItemStack(ModItems.COPPER_HOE.get())
                ),
                // Steel tools repair
                new RepairData(
                        Ingredient.of(ModItems.STEEL_INGOT.get()),
                        new ItemStack(ModItems.STEEL_TONGS.get()),
                        new ItemStack(ModItems.STEEL_SWORD.get()),
                        new ItemStack(ModItems.STEEL_PICKAXE.get()),
                        new ItemStack(ModItems.STEEL_AXE.get()),
                        new ItemStack(ModItems.STEEL_SHOVEL.get()),
                        new ItemStack(ModItems.STEEL_HOE.get()),
                        new ItemStack(ModItems.STEEL_HELMET.get()),
                        new ItemStack(ModItems.STEEL_CHESTPLATE.get()),
                        new ItemStack(ModItems.STEEL_LEGGINGS.get()),
                        new ItemStack(ModItems.STEEL_BOOTS.get())
                )
        );
    }

    private static Stream<IJeiAnvilRecipe> createRepairRecipes(
            RepairData repairData,
            IVanillaRecipeFactory vanillaRecipeFactory,
            IIngredientManager ingredientManager
    ) {
        Ingredient repairIngredient = repairData.getRepairIngredient();
        List<ItemStack> repairables = repairData.getRepairables();
        List<ItemStack> repairMaterials = List.of(repairIngredient.getItems());

        return repairables.stream()
                .mapMulti((itemStack, consumer) -> {
                    // Get the item's registry name properly
                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
                    String ingredientId = itemId.getPath();

                    // Self-repair recipe
                    ItemStack damagedThreeQuarters = itemStack.copy();
                    damagedThreeQuarters.setDamageValue(damagedThreeQuarters.getMaxDamage() * 3 / 4);
                    ItemStack damagedHalf = itemStack.copy();
                    damagedHalf.setDamageValue(damagedHalf.getMaxDamage() / 2);

                    IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(
                            List.of(damagedThreeQuarters),
                            List.of(damagedThreeQuarters),
                            List.of(damagedHalf),
                            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "self_repair." + ingredientId)
                    );
                    consumer.accept(repairWithSame);

                    // Material repair recipe
                    if (!repairMaterials.isEmpty()) {
                        ItemStack damagedFully = itemStack.copy();
                        damagedFully.setDamageValue(damagedFully.getMaxDamage());
                        IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(
                                List.of(damagedFully),
                                repairMaterials,
                                List.of(damagedThreeQuarters),
                                ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "material_repair." + ingredientId)
                        );
                        consumer.accept(repairWithMaterial);
                    }
                });
    }


    private static class RepairData {
        private final Ingredient repairIngredient;
        private final List<ItemStack> repairables;

        public RepairData(Ingredient repairIngredient, ItemStack... repairables) {
            this.repairIngredient = repairIngredient;
            this.repairables = List.of(repairables);
        }

        public Ingredient getRepairIngredient() {
            return repairIngredient;
        }

        public List<ItemStack> getRepairables() {
            return repairables;
        }
    }
}
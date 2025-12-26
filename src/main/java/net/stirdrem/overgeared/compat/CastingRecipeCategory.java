package net.stirdrem.overgeared.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.CastingRecipe;
import net.stirdrem.overgeared.util.ConfigHelper;

import java.util.List;
import java.util.Map;


public class CastingRecipeCategory implements IRecipeCategory<CastingRecipe> {

    public static final ResourceLocation UID =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "casting");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/casting_furnace_jei.png");

    // JEI recipe type — placed under vanilla smelting tab
    public static final RecipeType<CastingRecipe> CASTING_TYPE =
            new RecipeType<>(UID, CastingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final int animationTime = 200; // full cycle in ticks
    private final IDrawableAnimated arrowAnimated;
    private final IDrawableStatic arrowStatic;
    private final IDrawableAnimated flameAnimated;
    private final IDrawableStatic flameStatic;

    public CastingRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 89, 43)
                .setTextureSize(112, 43)
                .build();

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.CAST_FURNACE.get()));

        arrowStatic = helper.drawableBuilder(TEXTURE, 89, 14, 22, 16).setTextureSize(112, 43).build();
        arrowAnimated = helper.createAnimatedDrawable(arrowStatic, animationTime, IDrawableAnimated.StartDirection.LEFT, false);

        flameStatic = helper.drawableBuilder(TEXTURE, 89, 0, 14, 13).setTextureSize(112, 43).build();
        flameAnimated = helper.createAnimatedDrawable(
                flameStatic,
                100, // burn time
                IDrawableAnimated.StartDirection.TOP,
                true
        );
    }

    @Override
    public RecipeType<CastingRecipe> getRecipeType() {
        return CASTING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.jei.category.casting");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }


    @Override
    public void draw(CastingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Float exp = recipe.getExperience();
        arrowAnimated.draw(guiGraphics, 29, 9);
        flameAnimated.draw(guiGraphics, 33, 29);

        // Draw experience with formatting to avoid trailing .0
        String expText;
        if (exp == exp.intValue()) {
            expText = exp.intValue() + " XP";
        } else {
            expText = String.format("%.1f XP", exp);
        }

        // Calculate X position to right-align the text
        int textWidth = Minecraft.getInstance().font.width(expText);
        int xPos = this.background.getWidth() - textWidth; // 5 pixels from right edge

        guiGraphics.drawString(Minecraft.getInstance().font, expText, xPos, 35, 0xFFFFFFFF, true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CastingRecipe recipe, IFocusGroup focuses) {

        // -------------------------
        // MATERIAL INPUT SLOT
        // -------------------------
        NonNullList<Ingredient> materialIngredients = NonNullList.create();

        Map<String, Double> requiredMaterials = recipe.getRequiredMaterials();

        for (var entry : requiredMaterials.entrySet()) {
            String materialId = entry.getKey();
            double requiredAmount = entry.getValue();

            List<ItemStack> stacksForThisMaterial = new java.util.ArrayList<>();

            // All valid items for this material
            for (Item item : ConfigHelper.getItemListForMaterial(materialId)) {
                int value = ConfigHelper.getMaterialValue(item);
                if (value <= 0) continue;

                int count = (int) Math.ceil(requiredAmount / value);
                stacksForThisMaterial.add(new ItemStack(item, count));
            }

            if (!stacksForThisMaterial.isEmpty()) {
                materialIngredients.add(
                        Ingredient.of(stacksForThisMaterial.toArray(ItemStack[]::new))
                );
            }
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addIngredients(Ingredient.merge(materialIngredients));

        // -------------------------
        // TOOL CAST SLOT
        // -------------------------
        CompoundTag tag = new CompoundTag();
        tag.putString("ToolType", recipe.getToolType());

        double total = requiredMaterials.values().stream().mapToDouble(Double::doubleValue).sum();
        tag.putDouble("Amount", total);
        tag.putDouble("MaxAmount", total);

        ItemStack firedCast = new ItemStack(ModItems.CLAY_TOOL_CAST.get());
        firedCast.setTag(tag.copy());

        ItemStack netherCast = new ItemStack(ModItems.NETHER_TOOL_CAST.get());
        netherCast.setTag(tag.copy());

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addIngredients(Ingredient.of(firedCast, netherCast));

        // -------------------------
        // OUTPUT
        // -------------------------
        builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 10)
                .addItemStack(recipe.getResultItem(null));
    }

}
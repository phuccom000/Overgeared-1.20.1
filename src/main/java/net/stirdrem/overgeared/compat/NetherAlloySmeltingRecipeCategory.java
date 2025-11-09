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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.recipe.NetherAlloySmeltingRecipe;

import java.util.List;


public class NetherAlloySmeltingRecipeCategory implements IRecipeCategory<NetherAlloySmeltingRecipe> {

    public static final ResourceLocation UID =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "nether_alloy_smelting");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/nether_furnace_jei.png");

    // JEI recipe type — placed under vanilla smelting tab
    public static final RecipeType<NetherAlloySmeltingRecipe> ALLOY_SMELTING_TYPE =
            new RecipeType<>(UID, NetherAlloySmeltingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final int animationTime = 100; // full cycle in ticks
    private final IDrawableAnimated arrowAnimated;
    private final IDrawableStatic arrowStatic;
    private final IDrawableAnimated flameAnimated;
    private final IDrawableStatic flameStatic;
    private final int textureWidth = 143;
    private final int textureHeight = 54;

    public NetherAlloySmeltingRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE, 0, 0, 120, textureHeight)
                .setTextureSize(textureWidth, textureHeight)
                .build();

        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.NETHER_ALLOY_FURNACE.get()));

        arrowStatic = helper.drawableBuilder(TEXTURE, 120, 14, 23, 16).setTextureSize(textureWidth, textureHeight).build();
        arrowAnimated = helper.createAnimatedDrawable(arrowStatic, animationTime, IDrawableAnimated.StartDirection.LEFT, false);

        flameStatic = helper.drawableBuilder(TEXTURE, 120, 0, 14, 13).setTextureSize(textureWidth, textureHeight).build();
        flameAnimated = helper.createAnimatedDrawable(
                flameStatic,
                50, // burn time
                IDrawableAnimated.StartDirection.TOP,
                true
        );
    }

    @Override
    public RecipeType<NetherAlloySmeltingRecipe> getRecipeType() {
        return ALLOY_SMELTING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.jei.category.nether_alloy_smelting");
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
    public void draw(NetherAlloySmeltingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Float exp = recipe.getExperience();
        arrowAnimated.draw(guiGraphics, 60, 19);
        flameAnimated.draw(guiGraphics, 64, 39);

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

        guiGraphics.drawString(Minecraft.getInstance().font, expText, xPos, textureHeight - 9, 0xFFFFFFFF, true);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, NetherAlloySmeltingRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.getIngredientsList();

        // Add multiple input slots based on the recipe ingredients
        // Position inputs in a grid pattern to accommodate up to 4 ingredients
        int inputCount = ingredients.size();

        // Add 9 input slots in a 3x3 grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = row * 3 + col;
                if (slotIndex < ingredients.size()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, 1 + col * 18, 1 + row * 18)
                            .addIngredients(ingredients.get(slotIndex));
                }
            }
        }

        // Output slot
        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 20)
                .addItemStack(recipe.getResultItem(null));
    }
}
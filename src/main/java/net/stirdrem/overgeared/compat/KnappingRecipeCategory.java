package net.stirdrem.overgeared.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.datapack.KnappingResourceReloadListener;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;

public class KnappingRecipeCategory implements IRecipeCategory<RockKnappingRecipe> {
    public static final ResourceLocation UID = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "rock_knapping");
    public static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(OvergearedMod.MOD_ID,
            "textures/gui/rock_knapping_jei.png");
    private static final ResourceLocation CHIPPED_TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/blank.png");

    public static final RecipeType<RockKnappingRecipe> KNAPPING_RECIPE_TYPE =
            new RecipeType<>(UID, RockKnappingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private static final int WIDTH = 116;
    private static final int HEIGHT = 54;
    private final IGuiHelper helper;

    public KnappingRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.background = helper.createDrawable(TEXTURE, 7, 16, 138, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModItems.ROCK.get())); // Or your rock item if preferred
    }

    @Override
    public RecipeType<RockKnappingRecipe> getRecipeType() {
        return KNAPPING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.rock_knapping");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RockKnappingRecipe recipe, IFocusGroup focuses) {
        // Add input rock slot
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addIngredients(recipe.getIngredient()); // Or your knappable rock item
        // Add output slot
        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 19)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(RockKnappingRecipe recipe,
                     IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {

        boolean[][] pattern = recipe.getPattern();

        int patternHeight = pattern.length;
        int patternWidth = patternHeight > 0 ? pattern[0].length : 0;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {

                int posX = 25 + x * 16;
                int posY = 3 + y * 16;

                boolean isUnchipped = false;

                // ✅ SAFE bounds check
                if (y < patternHeight && x < patternWidth) {
                    isUnchipped = pattern[y][x];
                }

                ResourceLocation texture = isUnchipped
                        ? resolveUnchippedTexture(recipe)
                        : CHIPPED_TEXTURE;

                guiGraphics.blit(texture, posX, posY, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    private ResourceLocation resolveUnchippedTexture(RockKnappingRecipe recipe) {
        ItemStack[] stacks = recipe.getIngredient().getItems();

        for (ItemStack stack : stacks) {
            ResourceLocation tex = KnappingResourceReloadListener.getTexture(stack);
            if (tex != null) {
                return tex;
            }
        }

        // Fallback if no datapack entry exists
        return ResourceLocation.tryParse("textures/block/stone.png");
    }


}
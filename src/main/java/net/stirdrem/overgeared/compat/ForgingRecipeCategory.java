package net.stirdrem.overgeared.compat;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.SlotItemHandler;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.recipe.ForgingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ForgingRecipeCategory implements IRecipeCategory<ForgingRecipe> {
    public static final ResourceLocation UID = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "forging");
    public static final ResourceLocation TEXTURE = ResourceLocation.tryBuild(OvergearedMod.MOD_ID,
            "textures/gui/smithing_anvil_jei.png");

    public static final ResourceLocation RESULT_BIG = ResourceLocation.tryBuild(OvergearedMod.MOD_ID,
            "textures/gui/result_big.png");

    public static final ResourceLocation RESULT_TWOSLOT = ResourceLocation.tryBuild(OvergearedMod.MOD_ID,
            "textures/gui/twoslot.png");

    public static final RecipeType<ForgingRecipe> FORGING_RECIPE_TYPE =
            new RecipeType<>(UID, ForgingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private static final int imageWidth = 138;
    private static final int imageHeight = 54;

    public ForgingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 7, 16, imageWidth, imageHeight);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SMITHING_ANVIL.get()));

    }

    @Override
    public RecipeType<ForgingRecipe> getRecipeType() {
        return FORGING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void draw(ForgingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw the background first
        //this.background.draw(guiGraphics);
        String hitsText = Component.translatable("tooltip.overgeared.recipe.hits", recipe.getRemainingHits()).getString();

        String tierRaw = recipe.getAnvilTier();
        AnvilTier tierName = AnvilTier.fromDisplayName(tierRaw);
        Component tierText = Component.translatable("tooltip.overgeared.recipe.tier")
                .append(Component.literal(" "))
                .append(Component.translatable(tierName.getLang()));
        if (recipe.hasQuality() || !recipe.needsMinigame()) {
            guiGraphics.blit(RESULT_BIG, 112, 14, 0, 0, 26, 26, 26, 26);
        } else guiGraphics.blit(RESULT_TWOSLOT, 116, 9, 0, 0, 18, 36, 18, 36);

        guiGraphics.drawString(Minecraft.getInstance().font, hitsText, 79, 1, 0xFF808080, false);
        guiGraphics.drawString(Minecraft.getInstance().font, tierText, 79, 47, 0xFF808080, false);
        //guiGraphics.drawString(Minecraft.getInstance().font, blueprintText, 57, 1, 0xFF808080, false);
        //guiGraphics.drawString(Minecraft.getInstance().font, requiresBlueprintText, 0, 57, 0xFF808080, false);
    }


    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ForgingRecipe recipe, IFocusGroup focuses) {
        int width = recipe.width;
        int height = recipe.height;

        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        Set<String> type = recipe.getBlueprintTypes();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                Ingredient ingredient = ingredients.get(index);
                builder.addSlot(RecipeIngredientRole.INPUT, 23 + x * 18, 1 + y * 18)
                        .addIngredients(ingredient);
            }
        }
        //BLUEPRINT SLOT
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addItemStacks(createBlueprintStacksForRecipe(recipe));

        if (recipe.hasQuality() || !recipe.needsMinigame()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 19)
                    .addItemStack(recipe.getResultItem(null));
        } else {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 10)
                    .addItemStack(recipe.getResultItem(null));
            ItemStack failedStack = recipe.getFailedResultItem(null).copy();
            CompoundTag failedTag = failedStack.getOrCreateTag();
            failedTag.putBoolean("failedResult", true);
            failedStack.setTag(failedTag);

            builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 28)
                    .addItemStack(failedStack);

        }


    }

    private List<ItemStack> createBlueprintStacksForRecipe(ForgingRecipe recipe) {
        Set<String> types = recipe.getBlueprintTypes();
        boolean required = recipe.requiresBlueprint();
        List<ItemStack> stacks = new ArrayList<>();

        for (String type : types) {
            ItemStack stack = new ItemStack(ModItems.BLUEPRINT.get());
            CompoundTag tag = new CompoundTag();

            // Set a single string value for ToolType
            tag.putString("ToolType", type);
            tag.putBoolean("Required", required);
            stack.setTag(tag);
            stacks.add(stack);
        }

        return stacks;
    }


}
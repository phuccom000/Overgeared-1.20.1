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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.CastBlastingRecipe;
import net.stirdrem.overgeared.recipe.CastSmeltingRecipe;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class CastBlastingRecipeCategory implements IRecipeCategory<CastBlastingRecipe> {

    public static final ResourceLocation UID =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "cast_blasting");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/gui/furnace_jei.png");


    // JEI recipe type — placed under vanilla blasting tab
    public static final RecipeType<CastBlastingRecipe> CAST_BLASTING_TYPE =
            new RecipeType<>(UID, CastBlastingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final int animationTime = 100; // full cycle in ticks
    private final IDrawableAnimated arrowAnimated;
    private final IDrawableStatic arrowStatic;
    private final IDrawableAnimated flameAnimated;
    private final IDrawableStatic flameStatic;

    public CastBlastingRecipeCategory(IGuiHelper helper) {
        this.background = helper.drawableBuilder(TEXTURE,
                        0, 38, 82, 38
                )
                .setTextureSize(106, 38)
                .build();


        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModItems.FIRED_TOOL_CAST.get()));

        arrowStatic = helper.drawableBuilder(TEXTURE, 83, 14, 22, 16).setTextureSize(106, 38).build();
        arrowAnimated = helper.createAnimatedDrawable(arrowStatic, animationTime, IDrawableAnimated.StartDirection.LEFT, false);
        // Flame icon from vanilla furnace texture area (x=176, y=14, w=14, h=14)
        flameStatic = helper.drawableBuilder(TEXTURE, 82, 0, 14, 13).setTextureSize(106, 38).build();
        flameAnimated = helper.createAnimatedDrawable(
                flameStatic,
                50, // burn time (match your blasting or a constant)
                IDrawableAnimated.StartDirection.TOP,
                true
        );

    }

    @Override
    public RecipeType<CastBlastingRecipe> getRecipeType() {
        return CAST_BLASTING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.overgeared.jei.category.cast_blasting");
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
    public void draw(CastBlastingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Float exp = recipe.getExperience();
        arrowAnimated.draw(guiGraphics, 25, 5);
        flameAnimated.draw(guiGraphics, 1, 24); // same spot JEI had it
        guiGraphics.drawString(Minecraft.getInstance().font, exp + " XP", 26, 28, 0xFFFFFFFF, true);

    }


    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CastBlastingRecipe recipe, IFocusGroup focuses) {
        // --- Prepare NBT for both cast types ---
        CompoundTag tag = new CompoundTag();
        tag.putString("ToolType", recipe.getToolType());

        CompoundTag mats = new CompoundTag();
        AtomicInteger total = new AtomicInteger();
        recipe.getMaterialInputs().forEach((material, amount) -> {
            mats.putInt(material, amount.intValue());
            total.addAndGet(amount.intValue());
        });

        tag.put("Materials", mats);
        tag.putInt("Amount", total.get());
        tag.putInt("MaxAmount", total.get());

        // Create both stacks (Fired + Nether)
        ItemStack firedCast = new ItemStack(ModItems.FIRED_TOOL_CAST.get());
        firedCast.setTag(tag.copy());

        ItemStack netherCast = new ItemStack(ModItems.NETHER_TOOL_CAST.get());
        netherCast.setTag(tag.copy());

        // JEI input accepts either cast
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 5)
                .addItemStacks(List.of(firedCast, netherCast));

        // JEI output result (the finished tool)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 5)
                .addItemStack(recipe.getResultItem(null));
    }

}

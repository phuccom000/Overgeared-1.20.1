package net.stirdrem.overgeared.compat;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.Tags;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.util.ModTags;
import net.stirdrem.overgeared.recipe.RockKnappingRecipe;

import java.util.*;

/**
 * EMI integration for displaying Forging recipes.
 * This class is only loaded when EMI is present (handled by @EmiEntrypoint).
 */
@EmiEntrypoint
public class OvergearedEmiPlugin implements EmiPlugin {
    
    private static final ResourceLocation TEXTURE = OvergearedMod.loc("textures/gui/smithing_anvil_jei.png");
    private static final ResourceLocation KNAPPING_TEXTURE = OvergearedMod.loc("textures/gui/rock_knapping_gui.png");
    
    public static final EmiStack WORKSTATION = EmiStack.of(ModBlocks.SMITHING_ANVIL.get());
    
    public static final EmiRecipeCategory FORGING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("forging"),
            WORKSTATION,
            new EmiTexture(TEXTURE, 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.smithing_anvil");
        }
    };
    
    public static final EmiStack KNAPPING_WORKSTATION = EmiStack.of(ModItems.ROCK.get());
    public static final EmiRecipeCategory KNAPPING_CATEGORY = new EmiRecipeCategory(
            OvergearedMod.loc("rock_knapping"),
            KNAPPING_WORKSTATION,
            new EmiTexture(KNAPPING_TEXTURE, 0, 0, 16, 16)
    ) {
        @Override
        public Component getName() {
            return Component.translatable("gui.overgeared.rock_knapping");
        }
    };
    
    // Priority for sorting recipes by category
    private static final Map<String, Integer> CATEGORY_PRIORITY = Map.of(
            "tool_head", 0,
            "tools", 1,
            "armor", 2,
            "plate", 3,
            "misc", 4
    );
    
    @Override
    public void register(EmiRegistry registry) {
        OvergearedMod.LOGGER.info("Registering EMI plugin for Overgeared recipes.");
        
        // Register the forging category
        registry.addCategory(FORGING_CATEGORY);
        
        // Register all smithing anvil blocks as workstations (ordered by tier: Stone -> Iron -> A -> B)
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.STONE_SMITHING_ANVIL.get()));
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.SMITHING_ANVIL.get()));
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.TIER_A_SMITHING_ANVIL.get()));
        registry.addWorkstation(FORGING_CATEGORY, EmiStack.of(ModBlocks.TIER_B_SMITHING_ANVIL.get()));
        
        // Register Knapping
        registry.addCategory(KNAPPING_CATEGORY);
        registry.addWorkstation(KNAPPING_CATEGORY, KNAPPING_WORKSTATION);
        
        for (RecipeHolder<RockKnappingRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.KNAPPING.get())) {
            registry.addRecipe(new KnappingEmiRecipe(holder));
        }
        
        // Collect and sort all forging recipes
        List<RecipeHolder<ForgingRecipe>> allRecipes = new ArrayList<>(
                registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.FORGING.get())
        );
        
        // Sort recipes by category priority, then alphabetically by output name
        allRecipes.sort((a, b) -> {
            String catA = categorizeRecipe(a.value());
            String catB = categorizeRecipe(b.value());
            
            int priorityA = CATEGORY_PRIORITY.getOrDefault(catA, 999);
            int priorityB = CATEGORY_PRIORITY.getOrDefault(catB, 999);
            
            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB);
            }
            
            // Fallback: alphabetical by display name
            return a.value().getResultItem(null).getDisplayName().getString()
                    .compareToIgnoreCase(b.value().getResultItem(null).getDisplayName().getString());
        });
        
        // Add sorted recipes
        for (RecipeHolder<ForgingRecipe> holder : allRecipes) {
            registry.addRecipe(new ForgingEmiRecipe(holder));
        }
        
        OvergearedMod.LOGGER.info("EMI plugin registered successfully.");
    }
    
    /**
     * Categorize a recipe for sorting purposes.
     */
    private static String categorizeRecipe(ForgingRecipe recipe) {
        ItemStack output = recipe.getResultItem(null);
        if (output.is(Tags.Items.ARMORS)) return "armor";
        if (output.is(ModTags.Items.TOOL_PARTS)) return "tool_head";
        if (output.is(Tags.Items.TOOLS)) return "tools";
        if (output.is(ModItems.IRON_PLATE.get()) || output.is(ModItems.STEEL_PLATE.get()) || output.is(ModItems.COPPER_PLATE.get())) {
            return "plate";
        }
        return "misc";
    }
    
    /**
     * EMI recipe wrapper for ForgingRecipe.
     */
    public static class ForgingEmiRecipe implements EmiRecipe {
        private static final int DISPLAY_WIDTH = 150;
        private static final int DISPLAY_HEIGHT = 66;
        private static final int X_OFFSET = 8; // Offset to center the recipe
        
        private final ResourceLocation id;
        private final ForgingRecipe recipe;
        private final List<EmiIngredient> inputs;
        private final List<EmiStack> outputs;
        private final List<EmiStack> blueprintStacks;
        
        public ForgingEmiRecipe(RecipeHolder<ForgingRecipe> holder) {
            this.id = holder.id();
            this.recipe = holder.value();
            
            // Convert ingredients to EMI format
            this.inputs = recipe.getIngredients().stream()
                    .map(EmiIngredient::of)
                    .toList();
            
            this.outputs = List.of(EmiStack.of(recipe.getResultItem(null)));
            
            // Create blueprint stacks for recipes that support blueprints
            this.blueprintStacks = createBlueprintStacks();
        }
        
        /**
         * Create blueprint ItemStacks for this recipe's valid blueprint types.
         */
        private List<EmiStack> createBlueprintStacks() {
            Set<String> types = recipe.getBlueprintTypes();
            if (types.isEmpty()) {
                return List.of();
            }
            
            List<EmiStack> stacks = new ArrayList<>();
            for (String type : types) {
                ItemStack stack = new ItemStack(ModItems.BLUEPRINT.get());
                // Use BlueprintData with builder pattern
                net.stirdrem.overgeared.components.BlueprintData data = 
                        net.stirdrem.overgeared.components.BlueprintData.createDefault()
                                .withToolType(type)
                                .withRequired(recipe.requiresBlueprint());
                stack.set(net.stirdrem.overgeared.components.ModComponents.BLUEPRINT_DATA, data);
                stacks.add(EmiStack.of(stack));
            }
            return stacks;
        }

        
        @Override
        public EmiRecipeCategory getCategory() {
            return FORGING_CATEGORY;
        }
        
        @Override
        public ResourceLocation getId() {
            return id;
        }
        
        @Override
        public List<EmiIngredient> getInputs() {
            return inputs;
        }
        
        @Override
        public List<EmiStack> getOutputs() {
            return outputs;
        }
        
        @Override
        public int getDisplayWidth() {
            return DISPLAY_WIDTH;
        }
        
        @Override
        public int getDisplayHeight() {
            return DISPLAY_HEIGHT;
        }
        
        @Override
        public void addWidgets(WidgetHolder widgets) {
            // Blueprint slot on the left
            int blueprintX = X_OFFSET;
            int blueprintY = 24;
            if (!blueprintStacks.isEmpty()) {
                // Show blueprint variants cycling through
                widgets.addSlot(EmiIngredient.of(blueprintStacks), blueprintX, blueprintY);
            } else {
                widgets.addSlot(EmiStack.EMPTY, blueprintX, blueprintY);
            }
            
            // 3x3 crafting grid
            int gridStartX = X_OFFSET + 24;
            int gridStartY = 6;
            int slotSize = 18;
            
            int recipeWidth = recipe.width;
            int recipeHeight = recipe.height;
            
            // Create all 9 slots of the 3x3 grid
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int x = gridStartX + col * slotSize;
                    int y = gridStartY + row * slotSize;
                    
                    int recipeIndex = row * recipeWidth + col;
                    
                    // Check if this grid position has an ingredient in the recipe
                    if (col < recipeWidth && row < recipeHeight && recipeIndex < inputs.size()) {
                        widgets.addSlot(inputs.get(recipeIndex), x, y);
                    } else {
                        widgets.addSlot(EmiStack.EMPTY, x, y);
                    }
                }
            }
            
            // Arrow texture
            widgets.addTexture(EmiTexture.EMPTY_ARROW, X_OFFSET + 82, 24);
            
            // Output slot
            int outputX = X_OFFSET + 110;
            int outputY = 24;
            widgets.addSlot(outputs.get(0), outputX, outputY).large(true).recipeContext(this);
            
            // Draw "Hits: X" text (top right, above arrow)
            String hitsText = Component.translatable("tooltip.overgeared.recipe.hits", recipe.getRemainingHits()).getString();
            widgets.addText(Component.literal(hitsText), X_OFFSET + 82, 6, 0xFF808080, false);
            
            // Draw "Tier: X" text (bottom, below grid)
            String tierRaw = recipe.getAnvilTier();
            AnvilTier tierEnum = AnvilTier.fromDisplayName(tierRaw);
            Component tierText = Component.translatable("tooltip.overgeared.recipe.tier")
                    .append(Component.literal(" "))
                    .append(Component.translatable(tierEnum.getLang()));
            widgets.addText(tierText, X_OFFSET + 82, 54, 0xFF808080, false);
        }
    }
    
    /**
     * EMI recipe wrapper for RockKnappingRecipe.
     */
    public static class KnappingEmiRecipe implements EmiRecipe {
        private final ResourceLocation id;
        private final RockKnappingRecipe recipe;
        private final List<EmiIngredient> inputs;
        private final List<EmiStack> outputs;

        public KnappingEmiRecipe(RecipeHolder<RockKnappingRecipe> holder) {
            this.id = holder.id();
            this.recipe = holder.value();
            this.outputs = List.of(EmiStack.of(recipe.output()));
            
            // Build inputs based on pattern (true = Rock, false = Empty)
            List<EmiIngredient> inputList = new ArrayList<>();
            boolean[][] pattern = recipe.pattern();
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (pattern[row][col]) {
                        inputList.add(EmiStack.of(ModItems.ROCK.get()));
                    } else {
                        inputList.add(EmiStack.EMPTY);
                    }
                }
            }
            this.inputs = inputList;
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return KNAPPING_CATEGORY;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return inputs;
        }

        @Override
        public List<EmiStack> getOutputs() {
            return outputs;
        }

        @Override
        public int getDisplayWidth() {
            return 130;
        }

        @Override
        public int getDisplayHeight() {
            return 60;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            // Draw grid of slots
            // Centered mostly
            int startX = 4;
            int startY = 4;
            int slotSize = 18; // Standard EMI slot size

            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int index = row * 3 + col;
                    EmiIngredient input = inputs.get(index);
                    
                    // We only draw the slot if it's part of the "Required Rock" (true in pattern)
                    // Or do we draw all slots to show empty space?
                    // Showing all slots makes the "Shape" clearer.
                    widgets.addSlot(input, startX + col * slotSize, startY + row * slotSize);
                }
            }

            // Arrow
            widgets.addTexture(EmiTexture.EMPTY_ARROW, startX + 3 * slotSize + 4, startY + slotSize);

            // Output
            widgets.addSlot(outputs.get(0), startX + 3 * slotSize + 32, startY + slotSize - 4).large(true).recipeContext(this);
        }
    }
}

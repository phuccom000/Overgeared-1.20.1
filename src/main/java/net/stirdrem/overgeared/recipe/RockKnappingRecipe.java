package net.stirdrem.overgeared.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * @param pattern  true means clicked, false means unclicked
 * @param mirrored whether the pattern should accept mirror versions
 */
public record RockKnappingRecipe(ItemStack output, boolean[][] pattern,
                                 boolean mirrored) implements Recipe<RecipeInput> {
    public RockKnappingRecipe(ItemStack output, boolean[][] pattern, boolean mirrored) {
        this.output = output;
        this.pattern = pattern;
        this.mirrored = mirrored;

        // Validate pattern is 3x3
        if (pattern.length != 3 || pattern[0].length != 3) {
            throw new IllegalArgumentException("Knapping pattern must be 3x3");
        }
    }

    @Override
    public boolean matches(RecipeInput input, Level world) {
        if (input.size() != 9) return false;

        // Convert recipe input to 3x3 grid (true = unchipped, false = chipped)
        boolean[][] inputGrid = new boolean[3][3];
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            inputGrid[row][col] = input.getItem(i).isEmpty(); // Inverted logic: empty slot = chipped
        }

        // Check pattern at all possible offsets
        for (int offsetY = -2; offsetY <= 2; offsetY++) {
            for (int offsetX = -2; offsetX <= 2; offsetX++) {
                if (matchesPattern(inputGrid, offsetX, offsetY, false) ||
                        (mirrored && matchesPattern(inputGrid, offsetX, offsetY, true))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesPattern(boolean[][] inputGrid, int offsetX, int offsetY, boolean mirror) {
        // Check each position in the pattern
        for (int py = 0; py < 3; py++) {
            for (int px = 0; px < 3; px++) {
                // Skip if this pattern position is outside our defined pattern
                if (py >= pattern.length || px >= pattern[py].length) continue;

                int patternX = mirror ? (pattern[py].length - 1 - px) : px;
                int inputX = px + offsetX;
                int inputY = py + offsetY;

                // If pattern position is outside input grid
                if (inputX < 0 || inputX >= 3 || inputY < 0 || inputY >= 3) {
                    // Pattern requires this to be unchipped (true) but it's out of bounds
                    if (pattern[py][patternX]) {
                        return false;
                    }
                    continue;
                }

                // Check if input matches pattern requirement
                // pattern[py][px] = true means must be unchipped
                // inputGrid[inputY][inputX] = true means is unchipped
                if (pattern[py][patternX] != inputGrid[inputY][inputX]) {
                    return false;
                }
            }
        }

        // Check that all input positions not covered by pattern are chipped (false)
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                // Calculate corresponding pattern position
                int patternX = x - offsetX;
                int patternY = y - offsetY;

                // Skip if mirrored - we already checked those positions
                if (mirror) {
                    patternX = pattern[y].length - 1 - patternX;
                }

                // Check if this input position is outside the pattern
                boolean inPattern = patternY >= 0 && patternY < pattern.length &&
                        patternX >= 0 && patternX < pattern[patternY].length;

                if (!inPattern) {
                    // Position not in pattern must be chipped (false)
                    if (inputGrid[y][x]) {
                        return false;
                    }
                }
            }
        }

        return true;
    }


    @Override
    public ItemStack assemble(RecipeInput pInput, HolderLookup.Provider pRegistries) {
        return output.copy();
    }


    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width == 3 && height == 3;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ROCK_KNAPPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.KNAPPING.get();
    }
}

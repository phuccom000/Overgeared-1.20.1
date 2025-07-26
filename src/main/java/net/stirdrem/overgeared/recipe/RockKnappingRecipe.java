package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.OvergearedMod;
import org.jetbrains.annotations.Nullable;

public class RockKnappingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final ItemStack output;
    private final boolean[][] pattern; // true means clicked, false means unclicked
    private final boolean mirrored; // whether the pattern should accept mirror versions

    public RockKnappingRecipe(ResourceLocation id, ItemStack output, boolean[][] pattern, boolean mirrored) {
        this.id = id;
        this.output = output;
        this.pattern = pattern;
        this.mirrored = mirrored;

        // Validate pattern is 3x3
        if (pattern.length != 3 || pattern[0].length != 3) {
            throw new IllegalArgumentException("Knapping pattern must be 3x3");
        }
    }

    @Override
    public boolean matches(Container inv, Level world) {
        if (inv.getContainerSize() != 9) return false;

        // Convert container to 3x3 grid (true = unchipped, false = chipped)
        boolean[][] inputGrid = new boolean[3][3];
        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            inputGrid[row][col] = inv.getItem(i).isEmpty(); // Inverted logic: empty slot = chipped
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
    public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
        return output.copy();
    }


    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width == 3 && height == 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ROCK_KNAPPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.KNAPPING.get();
    }

    public boolean[][] getPattern() {
        return pattern;
    }

    public static class Type implements RecipeType<RockKnappingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "rock_knapping";
    }

    public static class Serializer implements RecipeSerializer<RockKnappingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "rock_knapping");

        @Override
        public RockKnappingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ItemStack output = buffer.readItem(); // Must match writeItem
            boolean[][] pattern = new boolean[3][3];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    pattern[i][j] = buffer.readBoolean(); // 9 reads
                }
            }
            boolean mirrored = buffer.readBoolean(); // 1 read
            return new RockKnappingRecipe(id, output, pattern, mirrored);
        }


        @Override
        public void toNetwork(FriendlyByteBuf buffer, RockKnappingRecipe recipe) {
            buffer.writeItem(recipe.output); // This writes variable number of bytes
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    buffer.writeBoolean(recipe.pattern[i][j]); // 9 booleans = 9 bytes
                }
            }
            buffer.writeBoolean(recipe.mirrored); // 1 byte
        }

        @Override
        public RockKnappingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "result"));

            JsonArray patternArray = GsonHelper.getAsJsonArray(pSerializedRecipe, "pattern");
            boolean[][] pattern = new boolean[3][3];

            for (int i = 0; i < patternArray.size(); i++) {
                String row = GsonHelper.convertToString(patternArray.get(i), "pattern row");
                for (int j = 0; j < row.length(); j++) {
                    char c = row.charAt(j);
                    pattern[i][j] = (c == 'x' || c == 'X');
                }
            }

            boolean mirrored = GsonHelper.getAsBoolean(pSerializedRecipe, "mirrored", false);

            return new RockKnappingRecipe(pRecipeId, output, pattern, mirrored);
        }
    }
}

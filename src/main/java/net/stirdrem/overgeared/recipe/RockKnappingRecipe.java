package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.OvergearedMod;

public class RockKnappingRecipe implements Recipe<RecipeInput> {
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

    public static class Serializer implements RecipeSerializer<RockKnappingRecipe> {

        private static final Codec<boolean[][]> PATTERN_CODEC = Codec.STRING.listOf().xmap(
                list -> {
                    boolean[][] pattern = new boolean[3][3];
                    for (int i = 0; i < Math.min(3, list.size()); i++) {
                        String row = list.get(i);
                        for (int j = 0; j < Math.min(3, row.length()); j++) {
                            char c = row.charAt(j);
                            pattern[i][j] = (c == 'x' || c == 'X');
                        }
                    }
                    return pattern;
                },
                pattern -> {
                    java.util.List<String> list = new java.util.ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        StringBuilder row = new StringBuilder();
                        for (int j = 0; j < 3; j++) {
                            row.append(pattern[i][j] ? 'x' : ' ');
                        }
                        list.add(row.toString());
                    }
                    return list;
                }
        );

        private static final MapCodec<RockKnappingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
                PATTERN_CODEC.fieldOf("pattern").forGetter(recipe -> recipe.pattern),
                Codec.BOOL.optionalFieldOf("mirrored", false).forGetter(recipe -> recipe.mirrored)
        ).apply(instance, (output, pattern, mirrored) -> {
            // We'll need to handle the ID separately since it's not in the JSON
            return new RockKnappingRecipe(ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "rock_knapping"), output, pattern, mirrored);
        }));

        private static final StreamCodec<RegistryFriendlyByteBuf, boolean[][]> PATTERN_STREAM_CODEC = new StreamCodec<>() {
          @Override
          public boolean[][] decode(RegistryFriendlyByteBuf buffer) {
            boolean[][] pattern = new boolean[3][3];
            for (int i = 0; i < 3; i++) {
              for (int j = 0; j < 3; j++) {
                pattern[i][j] = buffer.readBoolean();
              }
            }
            return pattern;
          }

          @Override
          public void encode(RegistryFriendlyByteBuf buffer, boolean[][] pattern) {
            for (int i = 0; i < 3; i++) {
              for (int j = 0; j < 3; j++) {
                buffer.writeBoolean(pattern[i][j]);
              }
            }
          }
        };

        @Override
        public MapCodec<RockKnappingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RockKnappingRecipe> streamCodec() {
            return StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    recipe -> recipe.output,
                    PATTERN_STREAM_CODEC,
                    recipe -> recipe.pattern,
                    new StreamCodec<>() {
                      @Override
                      public Boolean decode(RegistryFriendlyByteBuf buffer) {
                        return buffer.readBoolean();
                      }

                      @Override
                      public void encode(RegistryFriendlyByteBuf buffer, Boolean value) {
                        buffer.writeBoolean(value);
                      }
                    },
                    recipe -> recipe.mirrored,
                    (output, pattern, mirrored) -> new RockKnappingRecipe(ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "rock_knapping"), output, pattern, mirrored)
            );
        }

        // Legacy JSON support - kept for backwards compatibility if needed
        public RockKnappingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            // Use codec to parse from JSON
            JsonObject resultObj = GsonHelper.getAsJsonObject(pSerializedRecipe, "result");
            // Parse ItemStack from JSON using the codec
            com.mojang.serialization.JsonOps jsonOps = com.mojang.serialization.JsonOps.INSTANCE;
            ItemStack output = ItemStack.CODEC.parse(jsonOps, resultObj)
                    .resultOrPartial(error -> {}).orElse(ItemStack.EMPTY);

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

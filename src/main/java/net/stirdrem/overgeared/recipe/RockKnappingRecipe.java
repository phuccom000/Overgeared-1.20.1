package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.OvergearedMod;

public class RockKnappingRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final ItemStack output;
    private final Ingredient ingredient;

    private final boolean[][] pattern;
    private final int width;
    private final int height;
    private final boolean mirrored;


    /* ---------------- CONSTRUCTOR ---------------- */

    public RockKnappingRecipe(
            ResourceLocation id,
            ItemStack output,
            Ingredient ingredient,
            boolean[][] pattern,
            int width,
            int height,
            boolean mirrored
    ) {
        this.id = id;
        this.output = output;
        this.ingredient = ingredient;
        this.pattern = pattern;
        this.width = width;
        this.height = height;
        this.mirrored = mirrored;
    }

    /* ---------------- MATCHING LOGIC ---------------- */

    @Override
    public boolean matches(Container inv, Level level) {
        if (inv.getContainerSize() != 9) return false;

        // Validate ingredient
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && !ingredient.test(stack)) {
                return false;
            }
        }

        boolean[][] input = new boolean[3][3];
        for (int i = 0; i < 9; i++) {
            input[i / 3][i % 3] = inv.getItem(i).isEmpty(); // true = chipped
        }

        for (int y = 0; y <= 3 - height; y++) {
            for (int x = 0; x <= 3 - width; x++) {
                if (matchesAt(input, x, y, false)) return true;
                if (mirrored && matchesAt(input, x, y, true)) return true;
            }
        }

        return false;
    }

    private boolean matchesAt(boolean[][] input, int ox, int oy, boolean mirror) {
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int sx = mirror ? width - 1 - px : px;
                if (pattern[py][sx] != input[oy + py][ox + px]) {
                    return false;
                }
            }
        }

        // Outside pattern must be chipped
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                boolean inside =
                        x >= ox && x < ox + width &&
                                y >= oy && y < oy + height;

                if (!inside && input[y][x]) {
                    return false;
                }
            }
        }

        return true;
    }

    /* ---------------- RECIPE OUTPUT ---------------- */

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        return output.copy();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w == 3 && h == 3;
    }

    /* ---------------- GETTERS ---------------- */

    public boolean[][] getPattern() {
        return pattern;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    /* ---------------- RECIPE META ---------------- */

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

    /* ---------------- TYPE ---------------- */

    public static class Type implements RecipeType<RockKnappingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "rock_knapping";
    }

    /* ---------------- SERIALIZER ---------------- */

    public static class Serializer implements RecipeSerializer<RockKnappingRecipe> {

        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(OvergearedMod.MOD_ID, "rock_knapping");

        @Override
        public RockKnappingRecipe fromJson(ResourceLocation id, JsonObject json) {
            ItemStack result =
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

            Ingredient ingredient =
                    Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));

            JsonArray patternArray = GsonHelper.getAsJsonArray(json, "pattern");
            int height = patternArray.size();
            int width = patternArray.get(0).getAsString().length();

            boolean[][] pattern = new boolean[height][width];

            for (int y = 0; y < height; y++) {
                String row = GsonHelper.convertToString(patternArray.get(y), "pattern row");
                if (row.length() != width) {
                    throw new IllegalArgumentException("Pattern rows must be same width");
                }
                for (int x = 0; x < width; x++) {
                    char c = row.charAt(x);
                    pattern[y][x] = (c == 'x' || c == 'X');
                }
            }

            boolean mirrored = GsonHelper.getAsBoolean(json, "mirrored", false);

            return new RockKnappingRecipe(
                    id, result, ingredient, pattern,
                    width, height, mirrored
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, RockKnappingRecipe r) {
            buf.writeItem(r.output);
            r.ingredient.toNetwork(buf);

            buf.writeVarInt(r.width);
            buf.writeVarInt(r.height);

            for (int y = 0; y < r.height; y++) {
                for (int x = 0; x < r.width; x++) {
                    buf.writeBoolean(r.pattern[y][x]);
                }
            }

            buf.writeBoolean(r.mirrored);
        }

        @Override
        public RockKnappingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ItemStack output = buf.readItem();
            Ingredient ingredient = Ingredient.fromNetwork(buf);

            int width = buf.readVarInt();
            int height = buf.readVarInt();

            boolean[][] pattern = new boolean[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pattern[y][x] = buf.readBoolean();
                }
            }

            boolean mirrored = buf.readBoolean();

            return new RockKnappingRecipe(
                    id, output, ingredient, pattern,
                    width, height, mirrored
            );
        }
    }
}

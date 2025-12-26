package net.stirdrem.overgeared.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.util.ConfigHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CastingRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final String group;
    private final CookingBookCategory category;

    private final ItemStack result;
    private final float experience;
    private final int cookingTime;

    private final Map<String, Double> requiredMaterials;
    private final String toolType;
    private final boolean needPolishing;

    public CastingRecipe(
            ResourceLocation id,
            String group,
            CookingBookCategory category,
            ItemStack result,
            float experience,
            int cookingTime,
            Map<String, Double> requiredMaterials,
            String toolType,
            boolean needPolishing
    ) {
        this.id = id;
        this.group = group;
        this.category = category;
        this.result = result;
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.requiredMaterials = requiredMaterials;
        this.toolType = toolType.toLowerCase();
        this.needPolishing = needPolishing;
    }

    /* ============================================================= */
    /* MATCH LOGIC — EXACTLY LIKE CastSmeltingRecipe                  */
    /* ============================================================= */

    @Override
    public boolean matches(Container inv, Level level) {
        if (level.isClientSide) return false;

        // Tool cast (slot 3)
        ItemStack cast = inv.getItem(1);
        if (!(cast.getItem() instanceof ToolCastItem)) return false;

        CompoundTag castTag = cast.getTag();
        if (castTag == null) return false;

        // Tool type check (FROM CAST)
        if (!castTag.contains("ToolType")) return false;
        if (!toolType.equals(castTag.getString("ToolType").toLowerCase())) return false;

        // Material input slot (slot 0)
        ItemStack materialStack = inv.getItem(0);
        if (materialStack.isEmpty()) return false;

        // Must be a valid material
        if (!ConfigHelper.isValidMaterial(materialStack)) {
            return false;
        }

        // availableMaterials is derived ONLY from input slot
        Map<String, Integer> availableMaterials =
                ConfigHelper.getMaterialValuesForItem(materialStack);
        int count = materialStack.getCount();
        // Required material validation
        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase();
            double needed = entry.getValue();

            double available = availableMaterials
                    .getOrDefault(material, 0) * count;

            if (available < needed) {
                return false;
            }
        }

        return true;
    }


    /* ============================================================= */
    /* ASSEMBLE — FILLS THE CAST (LIKE YOUR SMELTING)                */
    /* ============================================================= */

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        ItemStack cast = inv.getItem(3);
        if (cast.isEmpty()) return ItemStack.EMPTY;

        CompoundTag castTag = cast.getOrCreateTag();

        // Build result item
        ItemStack out = result.copy();
        CompoundTag outTag = out.getOrCreateTag();

        // Transfer forging quality from cast
        if (castTag.contains("Quality")) {
            String q = castTag.getString("Quality");
            if (!q.equals("none")) {
                outTag.putString("ForgingQuality", q);
            }
        }

        // Polishing flag
        if (needPolishing) {
            outTag.putBoolean("Polished", false);
        }

        // Heated flag (used by your pipeline)
        outTag.putBoolean("Heated", true);

        // Creator tooltip
        if (cast.hasCustomHoverName() && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            outTag.putString("Creator", cast.getHoverName().getString());
        }

        /* -------------------------------------------------- */
        /* DAMAGE CAST — CAST STAYS IN SLOT                   */
        /* -------------------------------------------------- */

        if (cast.isDamageableItem()) {
            int newDamage = cast.getDamageValue() + 1;

            if (newDamage >= cast.getMaxDamage()) {
                // Cast breaks
                cast.shrink(1);
            } else {
                cast.setDamageValue(newDamage);
            }
        }

        // IMPORTANT: return the RESULT item
        return out;
    }

    /* ============================================================= */
    /* INGREDIENTS (JEI SUPPORT)                                     */
    /* ============================================================= */

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();

        CompoundTag tag = new CompoundTag();
        tag.putString("ToolType", toolType);

        CompoundTag mats = new CompoundTag();
        double total = 0;
        for (var e : requiredMaterials.entrySet()) {
            mats.putDouble(e.getKey(), e.getValue());
            total += e.getValue();
        }

        tag.put("Materials", mats);
        tag.putDouble("Amount", total);
        tag.putDouble("MaxAmount", total);

        ItemStack dummyCast = new ItemStack(net.stirdrem.overgeared.item.ModItems.CLAY_TOOL_CAST.get());
        dummyCast.setTag(tag);

        list.add(Ingredient.of(dummyCast));
        return list;
    }

    /* ============================================================= */
    /* BASIC META                                                    */
    /* ============================================================= */

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public String getGroup() {
        return group;
    }


    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CASTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CASTING.get();
    }

    /* ============================================================= */
    /* HELPERS                                                       */
    /* ============================================================= */

    public static Map<String, Double> readMaterials(CompoundTag tag) {
        Map<String, Double> map = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            if (tag.contains(key, Tag.TAG_DOUBLE)) {
                map.put(key, tag.getDouble(key));
            } else if (tag.contains(key, Tag.TAG_INT)) {
                map.put(key, (double) tag.getInt(key));
            }
        }
        return map;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public float getExperience() {
        return experience;
    }

    public boolean requiresPolishing() {
        return needPolishing;
    }

    public Map<String, Double> getRequiredMaterials() {
        return requiredMaterials;
    }

    public String getToolType() {
        return toolType;
    }

    public static class Type implements RecipeType<CastingRecipe> {
        public static final CastingRecipe.Type INSTANCE = new CastingRecipe.Type();
        public static final String ID = "casting";
    }
    /* ============================================================= */
    /* SERIALIZER                                                    */
    /* ============================================================= */

    public static class Serializer implements RecipeSerializer<CastingRecipe> {
        public static final CastingRecipe.Serializer INSTANCE = new CastingRecipe.Serializer();

        @Override
        public CastingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CookingBookCategory category = CookingBookCategory.MISC;

            JsonObject input = GsonHelper.getAsJsonObject(json, "input")
                    .getAsJsonObject("material");

            Map<String, Double> mats = new HashMap<>();
            input.entrySet().forEach(e -> mats.put(e.getKey().toLowerCase(), e.getValue().getAsDouble()));

            ItemStack result = ShapedRecipe.itemStackFromJson(
                    GsonHelper.getAsJsonObject(json, "result")
            );

            float xp = GsonHelper.getAsFloat(json, "experience", 0f);
            int time = GsonHelper.getAsInt(json, "cookingtime", 200);
            String toolType = GsonHelper.getAsString(json, "tool_type").toLowerCase();
            boolean polish = GsonHelper.getAsBoolean(json, "need_polishing", false);

            return new CastingRecipe(
                    id, group, category,
                    result, xp, time,
                    mats, toolType, polish
            );
        }

        @Override
        public CastingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CookingBookCategory category = CookingBookCategory.MISC;

            int size = buf.readInt();
            Map<String, Double> mats = new HashMap<>();
            for (int i = 0; i < size; i++) {
                mats.put(buf.readUtf(), buf.readDouble());
            }

            ItemStack result = buf.readItem();
            float xp = buf.readFloat();
            int time = buf.readVarInt();
            String toolType = buf.readUtf();
            boolean polish = buf.readBoolean();

            return new CastingRecipe(
                    id, group, category,
                    result, xp, time,
                    mats, toolType, polish
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CastingRecipe recipe) {
            buf.writeUtf(recipe.group);

            buf.writeInt(recipe.requiredMaterials.size());
            recipe.requiredMaterials.forEach((k, v) -> {
                buf.writeUtf(k);
                buf.writeDouble(v);
            });

            buf.writeItem(recipe.result);
            buf.writeFloat(recipe.experience);
            buf.writeVarInt(recipe.cookingTime);
            buf.writeUtf(recipe.toolType);
            buf.writeBoolean(recipe.needPolishing);
        }
    }
}

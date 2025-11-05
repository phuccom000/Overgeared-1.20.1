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
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CastBlastingRecipe extends BlastingRecipe {

    private final Map<String, Double> requiredMaterials;
    private final String toolType;
    private final boolean needPolishing;

    public CastBlastingRecipe(ResourceLocation id, String group, CookingBookCategory category,
                              ItemStack result, float xp, int time,
                              Map<String, Double> reqMaterials, String toolType, boolean needPolishing) {
        super(id, group, category, Ingredient.EMPTY, result, xp, time);
        this.requiredMaterials = reqMaterials;
        this.toolType = toolType;
        this.needPolishing = needPolishing;
    }

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

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();

        // Build the same NBT tag JEI uses
        CompoundTag tag = new CompoundTag();
        tag.putString("ToolType", toolType);

        CompoundTag mats = new CompoundTag();
        double total = 0;
        for (var entry : requiredMaterials.entrySet()) {
            String mat = entry.getKey();
            double amt = entry.getValue();
            total += amt;
            mats.putDouble(mat, amt);
        }

        tag.put("Materials", mats);
        tag.putDouble("Amount", total);
        tag.putDouble("MaxAmount", total);

        // Create cast stacks with NBT
        ItemStack firedCast = new ItemStack(ModItems.CLAY_TOOL_CAST.get());
        firedCast.setTag(tag.copy());

        ItemStack netherCast = new ItemStack(ModItems.NETHER_TOOL_CAST.get());
        netherCast.setTag(tag.copy());

        // Report them as the recipe input
        list.add(Ingredient.of(firedCast, netherCast));

        return list;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        ItemStack input = inv.getItem(0);
        if (!(input.getItem() instanceof ToolCastItem)) return false;
        CompoundTag tag = input.getTag();
        if (tag == null || !tag.contains("Materials")) return false;
        if (!toolType.equals(tag.getString("ToolType").toLowerCase())) return false;

        Map<String, Double> materials = readMaterials(tag.getCompound("Materials"));

        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase();
            double needed = entry.getValue();
            double available = materials.getOrDefault(material, 0.0);
            if (available < needed) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        ItemStack input = inv.getItem(0);
        ItemStack output = super.assemble(inv, registryAccess);

        // Transfer quality only
        if (input.hasTag() && input.getTag().contains("Quality")) {
            output.getOrCreateTag().put("ForgingQuality", input.getTag().get("Quality"));
        }
        // Only mark unpolished if recipe says so
        if (needPolishing) {
            output.getOrCreateTag().putBoolean("Polished", false);
        }

        output.getOrCreateTag().putBoolean("Heated", true);
        return output;
    }


    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(Container inv) {
        NonNullList<ItemStack> remains = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        ItemStack input = inv.getItem(0);

        if (!(input.getItem() instanceof ToolCastItem) || !input.hasTag()) {
            return remains;
        }

        ItemStack castCopy = input.copy();
        CompoundTag tag = getCompoundTag(castCopy);

        // Optional: damage cast OR destroy on empty
        if (castCopy.isDamageableItem()) {
            castCopy.setDamageValue(castCopy.getDamageValue() + 1);
            if (castCopy.getDamageValue() >= castCopy.getMaxDamage()) {
                remains.set(0, ItemStack.EMPTY);
                return remains;
            }
        }

        remains.set(0, castCopy);

        return remains;
    }

    private @NotNull CompoundTag getCompoundTag(ItemStack castCopy) {
        CompoundTag tag = castCopy.getOrCreateTag();
        if (tag.contains("input")) {
            tag.remove("input");
        }
        if (tag.contains("Materials")) {
            CompoundTag mats = tag.getCompound("Materials");
            List<String> toRemove = new ArrayList<>();

            // Reduce materials based on recipe JSON
            for (var entry : requiredMaterials.entrySet()) {
                String mat = entry.getKey().toLowerCase();
                double cost = entry.getValue();

                if (mats.contains(mat)) {
                    double current = mats.getDouble(mat);
                    double newAmount = Math.max(0, current - cost);

                    if (newAmount <= 0) {
                        toRemove.add(mat);
                    } else {
                        mats.putDouble(mat, newAmount);
                    }
                }
            }

            // Remove empty materials
            for (String r : toRemove) {
                mats.remove(r);
            }

            // ✅ Recalculate total "Amount"
            double total = 0;
            for (String mat : mats.getAllKeys()) {
                total += mats.getDouble(mat);
            }
            tag.putDouble("Amount", total);

            tag.put("Materials", mats);
        }
        return tag;
    }

    public Map<String, Double> getMaterialInputs() {
        return requiredMaterials;
    }

    public Map<String, Double> getRequiredMaterials() {
        return requiredMaterials;
    }

    public String getToolType() {
        return toolType;
    }

    public boolean requiresPolishing() {
        return needPolishing;
    }

    /*@Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CAST_BLASTING.get();
    }
*/
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CAST_BLASTING.get();
    }

    public static class Type implements RecipeType<CastBlastingRecipe> {
        public static final CastBlastingRecipe.Type INSTANCE = new Type();
        public static final String ID = "cast_blasting";
    }

    public static class Serializer implements RecipeSerializer<CastBlastingRecipe> {
        public static final CastBlastingRecipe.Serializer INSTANCE = new CastBlastingRecipe.Serializer();

        @Override
        public CastBlastingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CookingBookCategory category = CookingBookCategory.MISC;

            JsonObject inputObj = GsonHelper.getAsJsonObject(json, "input");
            Map<String, Double> reqMaterials = new HashMap<>();
            inputObj.entrySet().forEach(e -> reqMaterials.put(e.getKey(), e.getValue().getAsDouble()));

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            float xp = GsonHelper.getAsFloat(json, "experience", 0f);
            int time = GsonHelper.getAsInt(json, "cookingtime", 200);

            String toolType = GsonHelper.getAsString(json, "tool_type").toLowerCase();

            // ✅ New polishing flag from JSON (default = false)
            boolean needPolishing = GsonHelper.getAsBoolean(json, "need_polishing", false);

            return new CastBlastingRecipe(id, group, category, result, xp, time, reqMaterials, toolType, needPolishing);
        }

        @Override
        public CastBlastingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            String group = buf.readUtf();
            CookingBookCategory category = CookingBookCategory.MISC;
            int size = buf.readInt();
            Map<String, Double> reqMaterials = new HashMap<>();
            for (int i = 0; i < size; i++) {
                reqMaterials.put(buf.readUtf(), buf.readDouble());
            }
            ItemStack result = buf.readItem();
            float xp = buf.readFloat();
            int time = buf.readVarInt();
            String toolType = buf.readUtf();
            boolean needPolish = buf.readBoolean();

            return new CastBlastingRecipe(id, group, category, result, xp, time, reqMaterials, toolType, needPolish);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CastBlastingRecipe recipe) {
            buf.writeUtf(recipe.getGroup());
            buf.writeInt(recipe.requiredMaterials.size());
            recipe.requiredMaterials.forEach((k, v) -> {
                buf.writeUtf(k);
                buf.writeDouble(v);
            });
            buf.writeItem(recipe.getResultItem(null));
            buf.writeFloat(recipe.getExperience());
            buf.writeVarInt(recipe.getCookingTime());
            buf.writeUtf(recipe.toolType);
            buf.writeBoolean(recipe.needPolishing);
        }
    }
}

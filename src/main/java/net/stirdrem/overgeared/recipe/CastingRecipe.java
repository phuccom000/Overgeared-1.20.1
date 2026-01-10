package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.custom.ToolCastItem;
import net.stirdrem.overgeared.util.ConfigHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CastingRecipe implements Recipe<RecipeInput> {

    private final ResourceLocation id;
    private final String group;
    private final CraftingBookCategory category;
    private final Map<String, Integer> requiredMaterials;
    private final ItemStack result;
    private final float experience;
    private final int cookingTime;
    private final String toolType;
    private final boolean needPolishing;

    // Constructor that matches Codec field order
    public CastingRecipe(
            String group,
            CraftingBookCategory category,
            Map<String, Integer> requiredMaterials,
            ItemStack result,
            float experience,
            int cookingTime,
            String toolType,
            boolean needPolishing
    ) {
        this.id = null; // Will be set by RecipeManager
        this.group = group;
        this.category = category;
        this.requiredMaterials = requiredMaterials;
        this.result = result;
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.toolType = toolType.toLowerCase();
        this.needPolishing = needPolishing;
    }

    // Factory method for creating with ID (used by serializer)
    private CastingRecipe(
            ResourceLocation id,
            String group,
            CraftingBookCategory category,
            Map<String, Integer> requiredMaterials,
            ItemStack result,
            float experience,
            int cookingTime,
            String toolType,
            boolean needPolishing
    ) {
        this.id = id;
        this.group = group;
        this.category = category;
        this.requiredMaterials = requiredMaterials;
        this.result = result;
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.toolType = toolType.toLowerCase();
        this.needPolishing = needPolishing;
    }

    // Static factory method for Codec
    public static CastingRecipe create(
            String group,
            CraftingBookCategory category,
            Map<String, Integer> requiredMaterials,
            ItemStack result,
            float experience,
            int cookingTime,
            String toolType,
            boolean needPolishing
    ) {
        return new CastingRecipe(group, category, requiredMaterials, result, experience, cookingTime, toolType, needPolishing);
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        if (level.isClientSide) return false;

        // Tool cast (slot 3)
        ItemStack cast = input.getItem(1);
        if (!(cast.getItem() instanceof ToolCastItem)) return false;

        // Get cast data component
        CastData castData = cast.get(ModComponents.CAST_DATA.get());
        if (castData == null) return false;

        // Tool type check (FROM CAST)
        if (castData.toolType().isEmpty()) return false;
        if (!toolType.equals(castData.toolType().toLowerCase())) return false;

        // Material input slot (slot 0)
        ItemStack materialStack = input.getItem(0);
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

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        ItemStack cast = input.getItem(3);
        if (cast.isEmpty()) return ItemStack.EMPTY;

        CastData castData = cast.get(ModComponents.CAST_DATA.get());
        if (castData == null) return ItemStack.EMPTY;

        // Build result item
        ItemStack out = result.copy();

        // Transfer forging quality from cast
        if (!castData.quality().isEmpty() && !castData.quality().equals("none")) {
            out.set(ModComponents.FORGING_QUALITY.get(), net.stirdrem.overgeared.ForgingQuality.fromString(castData.quality()));
        }

        // Polishing flag
        if (needPolishing) {
            out.set(ModComponents.POLISHED.get(), false);
        }

        // Heated flag (used by your pipeline)
        out.set(ModComponents.HEATED_COMPONENT.get(), true);

        // Creator tooltip - check if cast has a custom name using Components API
        if (cast.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME) && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            // Get the custom name component
            var customName = cast.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
            if (customName != null) {
                out.set(ModComponents.CREATOR.get(), customName.getString());
            }
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

        // Convert Map<String, Integer> to Map<String, Integer>
        Map<String, Integer> materialsInt = new HashMap<>();
        double total = 0;
        for (var e : requiredMaterials.entrySet()) {
            materialsInt.put(e.getKey(), e.getValue().intValue());
            total += e.getValue();
        }

        CastData castData = new CastData(
                "", // quality
                toolType,
                materialsInt,
                (int) total,
                (int) total,
                List.of(), // input
                ItemStack.EMPTY, // output
                false // heated
        );

        ItemStack dummyCast = new ItemStack(net.stirdrem.overgeared.item.ModItems.CLAY_TOOL_CAST.get());
        dummyCast.set(ModComponents.CAST_DATA.get(), castData);

        list.add(Ingredient.of(dummyCast));
        return list;
    }

    /* ============================================================= */
    /* BASIC META                                                    */
    /* ============================================================= */

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
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
        return ModRecipeSerializers.CASTING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.CASTING.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
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

    public Map<String, Integer> getRequiredMaterials() {
        return requiredMaterials;
    }

    public String getToolType() {
        return toolType;
    }

    // Add these methods if your IAlloyRecipe interface requires them
    // They should return appropriate values for your alloy recipe system
    public List<Ingredient> getIngredientsList() {
        // Return actual ingredients if needed, or an empty list
        return List.of();
    }

    public CraftingBookCategory getCategory() {
        return category;
    }


    public static class Serializer implements RecipeSerializer<CastingRecipe> {

        // Codec for Map<String, Integer>
        private static final Codec<Map<String, Integer>> MATERIALS_CODEC =
                Codec.unboundedMap(Codec.STRING, Codec.INT);

        private static final MapCodec<CastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(CastingRecipe::getGroup),

                        CraftingBookCategory.CODEC
                                .optionalFieldOf("category", CraftingBookCategory.MISC)
                                .forGetter(r -> r.category),

                        MATERIALS_CODEC
                                .fieldOf("input")
                                .forGetter(CastingRecipe::getRequiredMaterials),

                        ItemStack.CODEC
                                .fieldOf("result")
                                .forGetter(r -> r.result),

                        Codec.FLOAT
                                .optionalFieldOf("experience", 0f)
                                .forGetter(CastingRecipe::getExperience),

                        Codec.INT
                                .optionalFieldOf("cookingtime", 200)
                                .forGetter(CastingRecipe::getCookingTime),

                        Codec.STRING
                                .fieldOf("tool_type")
                                .forGetter(CastingRecipe::getToolType),

                        Codec.BOOL
                                .optionalFieldOf("need_polishing", false)
                                .forGetter(CastingRecipe::requiresPolishing)
                ).apply(instance, CastingRecipe::create)
        );


        private static final StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            // group
                            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.getGroup());

                            // category
                            ByteBufCodecs.fromCodec(CraftingBookCategory.CODEC).encode(buf, recipe.category);

                            // input (materials)
                            buf.writeInt(recipe.requiredMaterials.size());
                            recipe.requiredMaterials.forEach((k, v) -> {
                                ByteBufCodecs.STRING_UTF8.encode(buf, k);
                                ByteBufCodecs.VAR_INT.encode(buf, v);
                            });

                            // result
                            ItemStack.STREAM_CODEC.encode(buf, recipe.result);

                            // experience
                            buf.writeFloat(recipe.getExperience());

                            // cooking time
                            ByteBufCodecs.VAR_INT.encode(buf, recipe.getCookingTime());

                            // tool type
                            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.toolType);

                            // need_polishing
                            ByteBufCodecs.BOOL.encode(buf, recipe.needPolishing);
                        },
                        buf -> {
                            // group
                            String group = ByteBufCodecs.STRING_UTF8.decode(buf);

                            // category
                            CraftingBookCategory category =
                                    ByteBufCodecs.fromCodec(CraftingBookCategory.CODEC).decode(buf);

                            // input (materials)
                            int size = buf.readInt();
                            Map<String, Integer> reqMaterials = new HashMap<>();
                            for (int i = 0; i < size; i++) {
                                String key = ByteBufCodecs.STRING_UTF8.decode(buf);
                                int value = ByteBufCodecs.VAR_INT.decode(buf);
                                reqMaterials.put(key, value);
                            }

                            // result
                            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);

                            // experience
                            float xp = buf.readFloat();

                            // cooking time
                            int time = ByteBufCodecs.VAR_INT.decode(buf);

                            // tool type
                            String toolType = ByteBufCodecs.STRING_UTF8.decode(buf);

                            // need_polishing
                            boolean needPolish = ByteBufCodecs.BOOL.decode(buf);

                            return CastingRecipe.create(
                                    group,
                                    category,
                                    reqMaterials,
                                    result,
                                    xp,
                                    time,
                                    toolType,
                                    needPolish
                            );
                        }
                );

        @Override
        public MapCodec<CastingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
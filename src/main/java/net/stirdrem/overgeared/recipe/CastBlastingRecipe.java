package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.ToolCastItem;

import java.util.HashMap;
import java.util.Map;

public class CastBlastingRecipe extends BlastingRecipe {

    private final Map<String, Double> requiredMaterials;
    private final String toolType;
    private final boolean needPolishing;

    public CastBlastingRecipe(String group, CookingBookCategory category,
                              ItemStack result, float xp, int time,
                              Map<String, Double> reqMaterials, String toolType, boolean needPolishing) {
        super(group, category,
                Ingredient.of(ModItems.CLAY_TOOL_CAST.get(), ModItems.NETHER_TOOL_CAST.get()),
                result, xp, time);
        this.requiredMaterials = reqMaterials;
        this.toolType = toolType;
        this.needPolishing = needPolishing;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();

        // Build CastData for JEI display
        Map<String, Integer> materials = new HashMap<>();
        int total = 0;
        for (var entry : requiredMaterials.entrySet()) {
            String mat = entry.getKey();
            int amt = (int) Math.ceil(entry.getValue());
            total += amt;
            materials.put(mat, amt);
        }

        CastData displayData = new CastData(
                "",
                toolType,
                materials,
                total,
                total,
                java.util.List.of(),
                ItemStack.EMPTY,
                false
        );

        // Create cast stacks with CastData
        ItemStack firedCast = new ItemStack(ModItems.CLAY_TOOL_CAST.get());
        firedCast.set(ModComponents.CAST_DATA, displayData);

        ItemStack netherCast = new ItemStack(ModItems.NETHER_TOOL_CAST.get());
        netherCast.set(ModComponents.CAST_DATA, displayData);

        list.add(Ingredient.of(firedCast, netherCast));
        return list;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        ItemStack stack = input.getItem(0);
        if (!(stack.getItem() instanceof ToolCastItem)) return false;
        
        CastData castData = stack.get(ModComponents.CAST_DATA);
        if (castData == null) return false;
        
        if (!toolType.equals(castData.toolType().toLowerCase())) return false;
        if (castData.amount() <= 0) return false;

        Map<String, Integer> materials = castData.materials();

        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase();
            double needed = entry.getValue();
            int available = materials.getOrDefault(material, 0);
            if (available < needed) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        ItemStack inputStack = input.getItem(0);

        // Copy the cast itself
        ItemStack cast = inputStack.copy();
        CastData castData = cast.get(ModComponents.CAST_DATA);
        if (castData == null) return ItemStack.EMPTY;

        // Build the real result item
        ItemStack result = this.result.copy();

        // Transfer quality from cast to result (convert BlueprintQuality string to ForgingQuality)
        if (!castData.quality().isEmpty() && !castData.quality().equals("none")) {
            BlueprintQuality blueprintQuality = BlueprintQuality.fromString(castData.quality());
            // Convert blueprint quality to forging quality by name
            ForgingQuality forgingQuality = ForgingQuality.fromString(blueprintQuality.getId());
            result.set(ModComponents.FORGING_QUALITY, forgingQuality);
        }

        // Polishing flag
        if (needPolishing) {
            result.set(ModComponents.POLISHED, false);
        }

        // Creator (transfer custom name from cast to output tool)
        if (inputStack.has(DataComponents.CUSTOM_NAME)) {
            result.set(ModComponents.CREATOR, inputStack.getHoverName().getString());
        }

        // Update cast data: store output, clear materials, mark as heated
        CastData updatedData = new CastData(
                castData.quality(),
                castData.toolType(),
                Map.of(),  // Clear materials
                0,         // Clear amount
                castData.maxAmount(),
                java.util.List.of(),  // Clear inputs
                result,    // Store the heated tool
                true       // Mark as heated
        );
        cast.set(ModComponents.CAST_DATA, updatedData);
        
        // Set HEATED_COMPONENT on the cast itself so players take damage when holding it
        cast.set(ModComponents.HEATED_COMPONENT, true);

        // Handle cast durability
        if (cast.isDamageableItem()) {
            if (cast.getDamageValue() + 1 >= cast.getMaxDamage()) {
                // Cast breaks, return just the result
                return result;
            } else {
                cast.setDamageValue(cast.getDamageValue() + 1);
            }
        }

        return cast;
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

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CAST_BLASTING.get();
    }

    public static class Serializer implements RecipeSerializer<CastBlastingRecipe> {
        
        // Codec for Map<String, Double>
        private static final Codec<Map<String, Double>> MATERIALS_CODEC =
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE);

        private static final MapCodec<CastBlastingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.getGroup()),
                        CookingBookCategory.CODEC.optionalFieldOf("category", CookingBookCategory.MISC)
                                .forGetter(r -> r.category()),
                        ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                        Codec.FLOAT.optionalFieldOf("experience", 0f).forGetter(r -> r.getExperience()),
                        Codec.INT.optionalFieldOf("cookingtime", 100).forGetter(r -> r.getCookingTime()),
                        MATERIALS_CODEC.fieldOf("input").forGetter(CastBlastingRecipe::getRequiredMaterials),
                        Codec.STRING.fieldOf("tool_type").forGetter(CastBlastingRecipe::getToolType),
                        Codec.BOOL.optionalFieldOf("need_polishing", false).forGetter(CastBlastingRecipe::requiresPolishing)
                ).apply(instance, CastBlastingRecipe::new)
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, CastBlastingRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            // Encode
                            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.getGroup());
                            ByteBufCodecs.fromCodec(CookingBookCategory.CODEC).encode(buf, recipe.category());
                            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
                            buf.writeFloat(recipe.getExperience());
                            ByteBufCodecs.VAR_INT.encode(buf, recipe.getCookingTime());
                            
                            // Encode materials map
                            buf.writeInt(recipe.requiredMaterials.size());
                            recipe.requiredMaterials.forEach((k, v) -> {
                                ByteBufCodecs.STRING_UTF8.encode(buf, k);
                                buf.writeDouble(v);
                            });
                            
                            ByteBufCodecs.STRING_UTF8.encode(buf, recipe.toolType);
                            ByteBufCodecs.BOOL.encode(buf, recipe.needPolishing);
                        },
                        buf -> {
                            // Decode
                            String group = ByteBufCodecs.STRING_UTF8.decode(buf);
                            CookingBookCategory category = ByteBufCodecs.fromCodec(CookingBookCategory.CODEC).decode(buf);
                            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                            float xp = buf.readFloat();
                            int time = ByteBufCodecs.VAR_INT.decode(buf);
                            
                            // Decode materials map
                            int size = buf.readInt();
                            Map<String, Double> reqMaterials = new HashMap<>();
                            for (int i = 0; i < size; i++) {
                                String key = ByteBufCodecs.STRING_UTF8.decode(buf);
                                double value = buf.readDouble();
                                reqMaterials.put(key, value);
                            }
                            
                            String toolType = ByteBufCodecs.STRING_UTF8.decode(buf);
                            boolean needPolish = ByteBufCodecs.BOOL.decode(buf);
                            
                            return new CastBlastingRecipe(group, category, result, xp, time, reqMaterials, toolType, needPolish);
                        }
                );

        @Override
        public MapCodec<CastBlastingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CastBlastingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

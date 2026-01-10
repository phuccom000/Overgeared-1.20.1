package net.stirdrem.overgeared.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record CastData(
        String quality,
        String toolType,
        Map<String, Integer> materials,
        int amount,
        int maxAmount,
        List<ItemStack> input,
        ItemStack output,
        boolean heated
) {
  public static final CastData EMPTY = new CastData(
          "",
          "",
          Map.of(),
          0,
          0,
          List.of(),
          ItemStack.EMPTY,
          false
  );

  // Custom StreamCodec for Map<String, Integer>
  private static final StreamCodec<RegistryFriendlyByteBuf, Map<String, Integer>> MATERIAL_MAP_STREAM =
          StreamCodec.of(
                  (buf, map) -> {
                    ByteBufCodecs.VAR_INT.encode(buf, map.size());
                    map.forEach((key, value) -> {
                      ByteBufCodecs.STRING_UTF8.encode(buf, key);
                      ByteBufCodecs.VAR_INT.encode(buf, value);
                    });
                  },
                  buf -> {
                    int size = ByteBufCodecs.VAR_INT.decode(buf);
                    Map<String, Integer> map = new HashMap<>();
                    for (int i = 0; i < size; i++) {
                      String key = ByteBufCodecs.STRING_UTF8.decode(buf);
                      int value = ByteBufCodecs.VAR_INT.decode(buf);
                      map.put(key, value);
                    }
                    return map;
                  }
          );

  public static final Codec<CastData> CODEC = RecordCodecBuilder.create(instance ->
          instance.group(
                  Codec.STRING.optionalFieldOf("quality", "").forGetter(CastData::quality),
                  Codec.STRING.optionalFieldOf("tool_type", "").forGetter(CastData::toolType),
                  Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("materials", Map.of()).forGetter(CastData::materials),
                  Codec.INT.optionalFieldOf("amount", 0).forGetter(CastData::amount),
                  Codec.INT.optionalFieldOf("max_amount", 0).forGetter(CastData::maxAmount),
                  ItemStack.CODEC.listOf().optionalFieldOf("input", List.of()).forGetter(CastData::input),
                  ItemStack.CODEC.optionalFieldOf("output", ItemStack.EMPTY).forGetter(CastData::output),
                  Codec.BOOL.optionalFieldOf("heated", false).forGetter(CastData::heated)
          ).apply(instance, CastData::new)
  );

  public static final StreamCodec<RegistryFriendlyByteBuf, CastData> STREAM_CODEC = StreamCodec.of(
          (buf, data) -> {
            // Encode
            ByteBufCodecs.STRING_UTF8.encode(buf, data.quality());
            ByteBufCodecs.STRING_UTF8.encode(buf, data.toolType());
            MATERIAL_MAP_STREAM.encode(buf, data.materials());
            ByteBufCodecs.VAR_INT.encode(buf, data.amount());
            ByteBufCodecs.VAR_INT.encode(buf, data.maxAmount());
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, data.input());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, data.output());
            ByteBufCodecs.BOOL.encode(buf, data.heated());
          },
          buf -> {
            // Decode
            String quality = ByteBufCodecs.STRING_UTF8.decode(buf);
            String toolType = ByteBufCodecs.STRING_UTF8.decode(buf);
            Map<String, Integer> materials = MATERIAL_MAP_STREAM.decode(buf);
            int amount = ByteBufCodecs.VAR_INT.decode(buf);
            int maxAmount = ByteBufCodecs.VAR_INT.decode(buf);
            List<ItemStack> input = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
            ItemStack output = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            boolean heated = ByteBufCodecs.BOOL.decode(buf);
            return new CastData(quality, toolType, materials, amount, maxAmount, input, output, heated);
          }
  );

  public CastData {
    if (quality == null) quality = "";
    if (toolType == null) toolType = "";
    if (materials == null) materials = Map.of();
    if (input == null) input = List.of();
    if (output == null) output = ItemStack.EMPTY;
  }

  // Helper method to add material
  public CastData withAddedMaterial(String materialKey, int value) {
    Map<String, Integer> newMaterials = new HashMap<>(materials);
    newMaterials.put(materialKey, newMaterials.getOrDefault(materialKey, 0) + value);
    return new CastData(quality, toolType, newMaterials, amount + value, maxAmount, input, output, heated);
  }

  // Helper method to add input item
  public CastData withAddedInput(ItemStack stack) {
    List<ItemStack> newInput = new java.util.ArrayList<>(input);

    // Try to merge with existing stack
    for (int i = 0; i < newInput.size(); i++) {
      ItemStack existing = newInput.get(i);
      if (ItemStack.isSameItemSameComponents(existing, stack)) {
        ItemStack merged = existing.copy();
        merged.grow(1);
        newInput.set(i, merged);
        return new CastData(quality, toolType, materials, amount, maxAmount, newInput, output, heated);
      }
    }

    // Add as new entry
    newInput.add(stack.copy());
    return new CastData(quality, toolType, materials, amount, maxAmount, newInput, output, heated);
  }

  // Helper method to set output
  public CastData withOutput(ItemStack newOutput) {
    return new CastData(quality, toolType, materials, amount, maxAmount, input, newOutput, heated);
  }

  // Helper method to clear inputs and materials
  public CastData withoutInputs() {
    return new CastData(quality, toolType, Map.of(), 0, maxAmount, List.of(), output, heated);
  }

  // Helper method to clear everything except quality, toolType, and maxAmount
  public CastData cleared() {
    return new CastData(quality, toolType, Map.of(), 0, maxAmount, List.of(), ItemStack.EMPTY, false);
  }

  // Helper method to set amount
  public CastData withAmount(int newAmount) {
    return new CastData(quality, toolType, materials, newAmount, maxAmount, input, output, heated);
  }

  // Helper method to set materials
  public CastData withMaterials(Map<String, Integer> newMaterials) {
    return new CastData(quality, toolType, newMaterials, amount, maxAmount, input, output, heated);
  }

  // Helper method to set heated status
  public CastData withHeated(boolean isHeated) {
    return new CastData(quality, toolType, materials, amount, maxAmount, input, output, isHeated);
  }

  // Check if it has output
  public boolean hasOutput() {
    return !output.isEmpty();
  }

  // Check if is empty (no inputs)
  public boolean isEmpty() {
    return input.isEmpty() && materials.isEmpty();
  }
}

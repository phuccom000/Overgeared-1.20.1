package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RockKnappingSerializer implements RecipeSerializer<RockKnappingRecipe> {
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
          ItemStack.CODEC.fieldOf("result").forGetter(RockKnappingRecipe::getOutput),
          PATTERN_CODEC.fieldOf("pattern").forGetter(RockKnappingRecipe::getPattern),
          Codec.BOOL.optionalFieldOf("mirrored", false).forGetter(RockKnappingRecipe::isMirrored)
  ).apply(instance, RockKnappingRecipe::new));

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
            RockKnappingRecipe::getOutput,
            PATTERN_STREAM_CODEC,
            RockKnappingRecipe::getPattern,
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
            RockKnappingRecipe::isMirrored,
            RockKnappingRecipe::new
    );
  }
}
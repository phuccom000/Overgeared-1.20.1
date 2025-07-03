package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.custom.SteelSmithingAnvil;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.SteelSmithingAnvilMenu;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class SteelSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {

    public SteelSmithingAnvilBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get(), pPos, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        if (!pPlayer.isCrouching()) {
            return new SteelSmithingAnvilMenu(pContainerId, pPlayerInventory, this, this.data);
        } else return null;
    }

    @Override
    protected String determineForgingQuality() {
        String quality = SteelSmithingAnvil.getQuality();
        if (quality == null) return "no_quality";
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        ForgingRecipe recipe = recipeOptional.get();
        if (!recipe.getBlueprint().isEmpty()) {

            ItemStack blueprint = this.itemHandler.getStackInSlot(11);

            // Define tool quality tiers in order of strength
            List<String> qualityTiers = List.of("poor", "well", "expert", "perfect", "master");

            // If blueprint is missing or invalid, fallback logic
            if (blueprint.isEmpty() || !blueprint.hasTag()) {
                return switch (quality.toLowerCase()) {
                    case "poor" -> ForgingQuality.POOR.getDisplayName();
                    default -> "well"; // Cap quality at 'well' without blueprint
                };
            }

            CompoundTag nbt = blueprint.getTag();
            if (nbt == null || !nbt.contains("Quality")) {
                return switch (quality.toLowerCase()) {
                    case "poor" -> ForgingQuality.POOR.getDisplayName();
                    default -> "well"; // Cap quality at 'well' without ToolType
                };
            }

            String blueprintToolType = nbt.getString("Quality").toLowerCase();

            // Determine capped quality
            int anvilTierIndex = qualityTiers.indexOf(quality.toLowerCase());
            int blueprintTierIndex = qualityTiers.indexOf(blueprintToolType);

            // Default to lowest if any tier is missing
            if (anvilTierIndex == -1 || blueprintTierIndex == -1) {
                return ForgingQuality.POOR.getDisplayName();
            }

            int finalIndex = Math.min(anvilTierIndex, blueprintTierIndex);

            return switch (qualityTiers.get(finalIndex)) {
                case "poor" -> ForgingQuality.POOR.getDisplayName();
                case "well" -> ForgingQuality.WELL.getDisplayName();
                case "expert" -> ForgingQuality.EXPERT.getDisplayName();
                case "perfect" -> {
                    Random random = new Random();
                    if ("master".equals(blueprintToolType) || ServerConfig.MASTER_QUALITY_CHANCE.get() != 0 && random.nextFloat() < ServerConfig.MASTER_QUALITY_CHANCE.get()) {
                        yield ForgingQuality.MASTER.getDisplayName();
                    } else yield ForgingQuality.PERFECT.getDisplayName();
                }
                case "master" -> ForgingQuality.MASTER.getDisplayName();
                default -> "no_quality";
            };
        }
        return quality;
    }

    @Override
    protected void craftItem() {
        super.craftItem();
        // Handle blueprint progression (slot 11)

        ItemStack blueprint = this.itemHandler.getStackInSlot(11);
        if (!blueprint.isEmpty() && blueprint.hasTag()) {
            CompoundTag tag = blueprint.getOrCreateTag();

            if (tag.contains("Quality") && tag.contains("Uses") && tag.contains("UsesToLevel")) {
                String currentQualityStr = tag.getString("Quality");
                int uses = tag.getInt("Uses");
                int usesToLevel = tag.getInt("UsesToLevel");

                BlueprintQuality currentQuality = BlueprintQuality.fromString(currentQualityStr);
                ForgingQuality resultQuality = ForgingQuality.fromString(SteelSmithingAnvil.getQuality());
                if (currentQuality != null && currentQuality != BlueprintQuality.PERFECT && currentQuality != BlueprintQuality.MASTER) {
                    if (resultQuality == ForgingQuality.PERFECT) {
                        uses += 2;
                    } else if (resultQuality == ForgingQuality.MASTER) {
                        uses += 3;
                    } else uses++;
                    // If uses reached threshold, level up
                    if (uses >= usesToLevel) {
                        BlueprintQuality nextQuality = BlueprintQuality.getNext(currentQuality);
                        if (nextQuality != null) {
                            tag.putString("Quality", nextQuality.getDisplayName());
                            tag.putInt("Uses", 0);
                            tag.putInt("UsesToLevel", nextQuality.getUse());
                        } else {
                            // Max tier reached, clamp Uses
                            tag.putInt("Uses", usesToLevel);
                        }
                    } else {
                        // Otherwise just update Uses count
                        tag.putInt("Uses", uses);
                    }

                    blueprint.setTag(tag);
                    this.itemHandler.setStackInSlot(11, blueprint); // Re-apply to update
                }
            }

        }

    }

    @Override
    public boolean hasRecipe() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        ForgingRecipe recipe = recipeOptional.get();
        ItemStack blueprint = this.itemHandler.getStackInSlot(11);

        // If blueprint is present and has a ToolType, it must match the recipe
        if (!blueprint.isEmpty() && blueprint.hasTag()) {
            CompoundTag tag = blueprint.getTag();
            if (tag != null && tag.contains("ToolType")) {
                String blueprintToolType = tag.getString("ToolType");
                if (!blueprintToolType.equals(recipe.getBlueprint())) {
                    return false; // Blueprint present but doesn't match
                }
            }
        }

        ItemStack resultStack = recipe.getResultItem(level.registryAccess());
        return canInsertItemIntoOutputSlot(resultStack)
                && canInsertAmountIntoOutputSlot(resultStack.getCount());
    }


}

package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.custom.SteelSmithingAnvil;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.SteelSmithingAnvilMenu;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SteelSmithingAnvilBlockEntity extends AbstractSmithingAnvilBlockEntity {
    private static final int BLUEPRINT_SLOT = 11;

    public SteelSmithingAnvilBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(AnvilTier.IRON, ModBlockEntities.STEEL_SMITHING_ANVIL_BE.get(), pPos, pBlockState);
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
        if (!recipe.getBlueprintTypes().isEmpty()) {

            ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);

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

        // Get the crafted output item
        ItemStack result = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        // Skip blueprint progression if crafting failed
        if (result.isEmpty()) return;

        // Handle blueprint progression (slot 11)
        ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);
        if (!blueprint.isEmpty() && blueprint.hasTag()) {
            CompoundTag tag = blueprint.getOrCreateTag();

            if (tag.contains("Quality") && tag.contains("Uses") && tag.contains("UsesToLevel")) {
                String currentQualityStr = tag.getString("Quality");
                int uses = tag.getInt("Uses");
                int usesToLevel = tag.getInt("UsesToLevel");

                BlueprintQuality currentQuality = BlueprintQuality.fromString(currentQualityStr);

                // Attempt to read the ForgingQuality from result
                CompoundTag resultTag = result.getOrCreateTag();
                String forgingQualityStr = resultTag.getString("ForgingQuality");
                ForgingQuality resultQuality = ForgingQuality.fromString(forgingQualityStr);

                if (currentQuality != null && currentQuality != BlueprintQuality.PERFECT && currentQuality != BlueprintQuality.MASTER) {
                    if (resultQuality == ForgingQuality.PERFECT) {
                        uses += 2;
                    } else if (resultQuality == ForgingQuality.MASTER) {
                        uses += 3;
                    } else {
                        uses++;
                    }

                    // Level up if threshold reached
                    if (uses >= usesToLevel) {
                        BlueprintQuality nextQuality = BlueprintQuality.getNext(currentQuality);
                        if (nextQuality != null) {
                            tag.putString("Quality", nextQuality.getDisplayName());
                            tag.putInt("Uses", 0);
                            tag.putInt("UsesToLevel", nextQuality.getUse());
                        } else {
                            tag.putInt("Uses", usesToLevel); // Clamp
                        }
                    } else {
                        tag.putInt("Uses", uses); // Just increment
                    }

                    blueprint.setTag(tag);
                    this.itemHandler.setStackInSlot(BLUEPRINT_SLOT, blueprint);
                }
            }
        }
    }


    @Override
    public boolean hasRecipe() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        ForgingRecipe recipe = recipeOptional.get();

        // Tier check
        AnvilTier requiredTier = AnvilTier.fromDisplayName(recipe.getAnvilTier());
        if (requiredTier == null || !requiredTier.isEqualOrLowerThan(this.anvilTier)) {
            return false;
        }

        ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);

        if (recipe.requiresBlueprint()) {
            // Must have a valid matching blueprint
            if (blueprint.isEmpty() || !blueprint.hasTag() || !blueprint.getTag().contains("ToolType")) {
                return false;
            }

            String blueprintToolType = blueprint.getTag().getString("ToolType").toLowerCase(Locale.ROOT);
            if (!recipe.getBlueprintTypes().contains(blueprintToolType)) {
                return false;
            }
        } else {
            // Optional blueprint: if present, it must match
            if (!blueprint.isEmpty() && blueprint.hasTag() && blueprint.getTag().contains("ToolType")) {
                String blueprintToolType = blueprint.getTag().getString("ToolType").toLowerCase(Locale.ROOT);
                if (!recipe.getBlueprintTypes().contains(blueprintToolType)) {
                    return false;
                }
            }
        }

        ItemStack resultStack = recipe.getResultItem(level.registryAccess());
        return canInsertItemIntoOutputSlot(resultStack)
                && canInsertAmountIntoOutputSlot(resultStack.getCount());
    }


}

package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.advancement.ModAdvancementTriggers;
import net.stirdrem.overgeared.block.custom.AbstractSmithingAnvil;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.event.ModEvents;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.stirdrem.overgeared.util.ItemUtils.getCooledItem;

public abstract class AbstractSmithingAnvilBlockEntity extends BlockEntity implements MenuProvider {
    protected static final int INPUT_SLOT = 0;
    protected static final int OUTPUT_SLOT = 10;
    protected final ItemStackHandler itemHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level == null || level.isClientSide()) return;
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    };
    protected final ContainerData data;
    protected int progress;
    protected int maxProgress;
    protected int hitRemains;
    protected long busyUntilGameTime = 0L;
    protected UUID ownerUUID = null;
    protected Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());
    protected AnvilTier anvilTier;
    protected long sessionStartTime = 0L; // optional, for timeout logic
    protected ItemStack failedResult;
    protected Player player;
    protected ForgingRecipe lastRecipe = null;
    protected ItemStack lastBlueprint = ItemStack.EMPTY;
    private boolean minigameOn = false;
    protected AbstractSmithingAnvil anvilBlock;
    protected static final int BLUEPRINT_SLOT = 11;

    public AbstractSmithingAnvilBlockEntity(AbstractSmithingAnvil anvilBlock, AnvilTier tier, BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
        this.anvilTier = tier;
        this.anvilBlock = anvilBlock;
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> AbstractSmithingAnvilBlockEntity.this.progress;
                    case 1 -> AbstractSmithingAnvilBlockEntity.this.maxProgress;
                    case 2 -> AbstractSmithingAnvilBlockEntity.this.hitRemains;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> AbstractSmithingAnvilBlockEntity.this.progress = pValue;
                    case 1 -> AbstractSmithingAnvilBlockEntity.this.maxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    public static void applyForgingQuality(ItemStack stack, ForgingQuality quality) {
        stack.set(ModComponents.FORGING_QUALITY, quality);
    }

    public ItemStack getRenderStack(int index) {
        return itemHandler.getStackInSlot(index);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.overgeared.smithing_anvil");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("hitRemains", hitRemains);
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.put("inventory", itemHandler.serializeNBT(registries));

        if (ownerUUID != null) {
            tag.putUUID("ownerUUID", ownerUUID);
            tag.putLong("sessionStartTime", sessionStartTime);
        }

        // Save occupiedAnvils
        ListTag occupiedList = new ListTag();
        for (Map.Entry<BlockPos, UUID> entry : occupiedAnvils.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            BlockPos pos = entry.getKey();
            entryTag.putInt("x", pos.getX());
            entryTag.putInt("y", pos.getY());
            entryTag.putInt("z", pos.getZ());
            entryTag.putUUID("uuid", entry.getValue());
            occupiedList.add(entryTag);
        }
        tag.put("occupiedAnvils", occupiedList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("hitRemains")) {
            hitRemains = tag.getInt("hitRemains");
        }
        if (tag.contains("progress")) {
            progress = tag.getInt("progress");
        }
        if (tag.contains("maxProgress")) {
            maxProgress = tag.getInt("maxProgress");
        }
        if (tag.hasUUID("ownerUUID")) {
            ownerUUID = tag.getUUID("ownerUUID");
            sessionStartTime = tag.getLong("sessionStartTime");
        } else {
            ownerUUID = null;
        }

        // Load occupiedAnvils
        occupiedAnvils.clear();
        if (tag.contains("occupiedAnvils", CompoundTag.TAG_LIST)) {
            ListTag occupiedList = tag.getList("occupiedAnvils", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < occupiedList.size(); i++) {
                CompoundTag entryTag = occupiedList.getCompound(i);
                int x = entryTag.getInt("x");
                int y = entryTag.getInt("y");
                int z = entryTag.getInt("z");
                UUID uuid = entryTag.getUUID("uuid");
                occupiedAnvils.put(new BlockPos(x, y, z), uuid);
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void increaseForgingProgress(Level pLevel, BlockPos pPos, BlockState pState) {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (hasRecipe()) {
            ForgingRecipe currentRecipe = recipe.get();
            maxProgress = currentRecipe.getHammeringRequired();
            increaseCraftingProgress();
            setChanged(pLevel, pPos, pState);

            if (hasProgressFinished()) {
                craftItem();
                resetProgress(pPos);
            }
        } else {
            resetProgress(pPos);
        }
    }

    public void resetProgress(BlockPos pos) {
        progress = 0;
        maxProgress = 0;
        lastRecipe = null;
        if (level != null && !level.isClientSide()) {
            ModEvents.resetMinigameForPlayer((ServerPlayer) player);
        }
        player = null;
    }

    protected void craftItem() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return;

        ForgingRecipe recipe = recipeOptional.get();
        ItemStack result = recipe.getResultItem(getLevel().registryAccess());
        failedResult = recipe.getFailedResultItem(getLevel().registryAccess());
        ForgingQuality minimumQuality = recipe.getMinimumQuality();
        // Only set quality if recipe supports it

        ForgingQuality maxIngredientQuality = null;
        for (int i = 0; i < 9; i++) {
            ItemStack ingredient = itemHandler.getStackInSlot(i);
            ForgingQuality quality = ingredient.get(ModComponents.FORGING_QUALITY);
            if (quality != null && (maxIngredientQuality == null || quality.ordinal() > maxIngredientQuality.ordinal())) {
                maxIngredientQuality = quality;
            }
        }

        if (recipe.hasQuality() && player != null && ServerConfig.PLAYER_AUTHOR_TOOLTIPS.get()) {
            result.set(ModComponents.CREATOR, player.getName().getString());
        }

        if (recipe.hasQuality()) {
            if (ServerConfig.ENABLE_MINIGAME.get()) {
                String qualityStr = determineForgingQuality();
                if (!Objects.equals(qualityStr, ForgingQuality.NONE.getDisplayName())) {
                    ForgingQuality quality = ForgingQuality.fromString(qualityStr);
                    if (quality != null) {
                        // Clamp to minimum quality if needed
                        if (minimumQuality != null && quality.ordinal() < minimumQuality.ordinal()) {
                            quality = minimumQuality;
                        }
                        // Clamp to maximum quality
                        if (maxIngredientQuality != null && quality.ordinal() > maxIngredientQuality.ordinal() && ServerConfig.INGREDIENTS_DEFINE_MAX_QUALITY.get()) {
                            quality = maxIngredientQuality;
                        }

                        if (quality == ForgingQuality.PERFECT) {
                            if (ServerConfig.MASTER_QUALITY_CHANCE.get() != 0 &&
                                    new Random().nextFloat() < ServerConfig.MASTER_QUALITY_CHANCE.get()) {
                                quality = ForgingQuality.MASTER;
                            }
                        }
                        result.set(ModComponents.FORGING_QUALITY, quality);

                        if (player instanceof ServerPlayer serverPlayer) {
                            ModAdvancementTriggers.FORGING_QUALITY.get()
                                    .trigger(serverPlayer, quality.getDisplayName());
                        }
                        if (!(result.getItem() instanceof ArmorItem) &&
                                !(result.getItem() instanceof ShieldItem) &&
                                recipe.hasPolishing()) {
                            result.set(ModComponents.POLISHED, false);
                        }
                        if (recipe.needQuenching() && (!result.is(ModTags.Items.HEATED_METALS) || !result.is(ModTags.Items.HOT_ITEMS))) {
                            result.set(ModComponents.HEATED_COMPONENT, true);
                        }
                    }
                }
            }
        } else if (recipe.needsMinigame()) {
            // Handle minigame result without quality
            if (ServerConfig.ENABLE_MINIGAME.get()) {
                String quality = determineForgingQuality();
                if (!Objects.equals(quality, ForgingQuality.NONE.getDisplayName())) {
                    boolean fail = false;

                    if (quality.equals(ForgingQuality.POOR.getDisplayName())) {
                        fail = true;
                    } else if (quality.equals(ForgingQuality.WELL.getDisplayName())) {
                        float failChance = ServerConfig.FAIL_ON_WELL_QUALITY_CHANCE.get().floatValue();
                        fail = new Random().nextFloat() < failChance;
                    } else if (quality.equals(ForgingQuality.EXPERT.getDisplayName())) {
                        float failChance = ServerConfig.FAIL_ON_EXPERT_QUALITY_CHANCE.get().floatValue();
                        fail = new Random().nextFloat() < failChance;
                    }

                    if (fail) {
                        result = failedResult.copy();
                    }
                }
            }
        }

        // Extract ingredients
        for (int i = 0; i < 9; i++) {
            this.itemHandler.extractItem(i, 1, false);
        }

        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        if (existing.isEmpty()) {
            // If the output slot is empty, just set the result
            this.itemHandler.setStackInSlot(OUTPUT_SLOT, result);
        } else if (ItemStack.isSameItemSameComponents(existing, result)) {
            // If the same item (with same NBT), try to stack them
            int total = existing.getCount() + result.getCount();
            int maxSize = Math.min(existing.getMaxStackSize(), this.itemHandler.getSlotLimit(OUTPUT_SLOT));

            if (total <= maxSize) {
                existing.grow(result.getCount());
                this.itemHandler.setStackInSlot(OUTPUT_SLOT, existing);
            } else {
                // If not all items fit, grow to max and optionally handle overflow
                int remainder = total - maxSize;
                existing.setCount(maxSize);
                this.itemHandler.setStackInSlot(OUTPUT_SLOT, existing);

                // Handle remainder if needed (e.g. drop, store elsewhere, etc.)
                ItemStack overflow = result.copy();
                overflow.setCount(remainder);
                // Example: drop the overflow into the world
                Containers.dropItemStack(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), overflow);
            }
        }
    }

    protected void craftItemWithBlueprint() {

        // Get the crafted output item
        ItemStack result = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        // Skip blueprint progression if crafting failed
        if (result.isEmpty()) return;

        // Handle blueprint progression (slot 11)
        ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);
        BlueprintData blueprintData = blueprint.get(ModComponents.BLUEPRINT_DATA);
        if (!blueprint.isEmpty() && blueprintData != null) {
            String currentQualityStr = blueprintData.quality();
            int uses = blueprintData.uses();
            int usesToLevel = blueprintData.usesToLevel();

            BlueprintQuality currentQuality = BlueprintQuality.fromString(currentQualityStr);

            // Attempt to read the ForgingQuality from result
            String forgingQualityStr = anvilBlock.getQuality();
            ForgingQuality resultQuality = ForgingQuality.fromString(forgingQualityStr);

            if (currentQuality != null && currentQuality != BlueprintQuality.PERFECT && currentQuality != BlueprintQuality.MASTER) {
                if (!ServerConfig.EXPERT_ABOVE_INCREASE_BLUEPRINT.get() || resultQuality.ordinal() >= ForgingQuality.EXPERT.ordinal()) {
                    uses += switch (resultQuality) {
                        case PERFECT -> 2;
                        case MASTER -> 3;
                        default -> 1;
                    };
                }

                // Level up if threshold reached
                if (uses >= usesToLevel) {
                    BlueprintQuality nextQuality = BlueprintQuality.getNext(currentQuality);
                    if (nextQuality != null) {
                        BlueprintData newData = blueprintData
                                .withQuality(nextQuality.getDisplayName())
                                .withUses(0)
                                .withUsesToLevel(nextQuality.getUse());
                        blueprint.set(ModComponents.BLUEPRINT_DATA, newData);
                        if (player instanceof ServerPlayer serverPlayer) {
                            if (nextQuality.equals(BlueprintQuality.PERFECT)
                                    || nextQuality.equals(BlueprintQuality.MASTER))
                                ModAdvancementTriggers.MAX_LEVEL_BLUEPRINT.get().trigger(serverPlayer);
                            ModAdvancementTriggers.BLUEPRINT_QUALITY.get().trigger(serverPlayer,
                                    nextQuality.getDisplayName());
                        }
                    } else {
                        blueprint.set(ModComponents.BLUEPRINT_DATA, blueprintData.withUses(usesToLevel)); // Clamp
                    }
                } else {
                    blueprint.set(ModComponents.BLUEPRINT_DATA, blueprintData.withUses(uses)); // Just increment
                }

                this.itemHandler.setStackInSlot(BLUEPRINT_SLOT, blueprint);
            }
        }
    }

    public boolean isFailedResult() {
        ItemStack result = this.itemHandler.getStackInSlot(OUTPUT_SLOT);

        return ItemStack.isSameItem(result, failedResult);
    }

    public boolean hasRecipe() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        ForgingRecipe recipe = recipeOptional.get();
        AnvilTier requiredTier = AnvilTier.fromDisplayName(recipe.getAnvilTier());

        // Safely skip if tier is invalid
        if (requiredTier == null || !requiredTier.isEqualOrLowerThan(this.anvilTier)) {
            return false;
        }

        ItemStack resultStack = recipe.getResultItem(level.registryAccess());
        return canInsertItemIntoOutputSlot(resultStack)
                && canInsertAmountIntoOutputSlot(resultStack.getCount());
    }

    public boolean hasRecipeWithBlueprint() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        ForgingRecipe recipe = recipeOptional.get();

        // Tier check
        AnvilTier requiredTier = AnvilTier.fromDisplayName(recipe.getAnvilTier());
        if (requiredTier == null || !requiredTier.isEqualOrLowerThan(this.anvilTier)) {
            return false;
        }

        ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);
        BlueprintData blueprintData = blueprint.get(ModComponents.BLUEPRINT_DATA);

        if (recipe.requiresBlueprint()) {
            // Must have a valid matching blueprint
            if (blueprint.isEmpty() || blueprintData == null) {
                return false;
            }

            String blueprintToolType = blueprintData.toolType().toLowerCase(Locale.ROOT);
            if (!recipe.getBlueprintTypes().contains(blueprintToolType)) {
                return false;
            }
        } else {
            // Optional blueprint: if present, it must match
            if (!blueprint.isEmpty() && blueprintData != null) {
                String blueprintToolType = blueprintData.toolType().toLowerCase(Locale.ROOT);
                if (!recipe.getBlueprintTypes().contains(blueprintToolType)) {
                    return false;
                }
            }
        }

        ItemStack resultStack = recipe.getResultItem(level.registryAccess());
        return canInsertItemIntoOutputSlot(resultStack)
                && canInsertAmountIntoOutputSlot(resultStack.getCount());
    }

    public Optional<ForgingRecipe> getCurrentRecipe() {
        return getCurrentRecipeHolder().map(RecipeHolder::value);
    }

    public Optional<RecipeHolder<ForgingRecipe>> getCurrentRecipeHolder() {
        // Create a wrapper that only exposes the slots needed for recipe matching
        ItemStackHandler recipeHandler = new ItemStackHandler(12);
        for (int i = 0; i < 9; i++) {
            recipeHandler.setStackInSlot(i, itemHandler.getStackInSlot(i));
        }
        recipeHandler.setStackInSlot(11, itemHandler.getStackInSlot(11));

        RecipeWrapper recipeInput = new RecipeWrapper(recipeHandler);
        return ForgingRecipe.findBestMatchHolder(level, recipeInput)
                .filter(holder -> matchesRecipeExactly(holder.value()))
                //.filter(this::hasEnoughIngredients)
                ;
    }

    protected boolean canInsertItemIntoOutputSlot(ItemStack stackToInsert) {
        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);
        return (existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, stackToInsert));
    }

    protected boolean canInsertAmountIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    public boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    public void increaseCraftingProgress() {
        progress++;

        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        if (data != null) {
            data.set(0, progress);
            data.set(1, maxProgress);
            data.set(2, hitRemains);
        }
    }

    public boolean isBusy(long currentGameTime) {
        return currentGameTime < busyUntilGameTime;
    }

    public void setBusyUntil(long time) {
        this.busyUntilGameTime = time;
        setChanged(level, worldPosition, getBlockState());
    }

    public void tick(Level lvl, BlockPos pos, BlockState st) {
        if (!pos.equals(this.worldPosition)) return; // sanity check
        try {
            // Check if blueprint changed mid-forging
            ItemStack currentBlueprint = this.itemHandler.getStackInSlot(11);
            if (!ItemStack.isSameItemSameComponents(currentBlueprint, lastBlueprint)) {
                if (progress > 0 || lastRecipe != null || isMinigameOn()) {
                    resetProgress(pos);
                    setMinigameOn(false);
                    OvergearedMod.LOGGER.debug("Blueprint changed at {}, minigame reset", pos);
                }
            }
            lastBlueprint = currentBlueprint.copy();

            Optional<ForgingRecipe> currentRecipeOpt = getCurrentRecipe();
            if (currentRecipeOpt.isEmpty()) {
                if (progress > 0 || lastRecipe != null) {
                    resetProgress(pos);
                }
                return;
            }

            ForgingRecipe currentRecipe = currentRecipeOpt.get();

            boolean recipeChanged = false;
            if (lastRecipe != null) {
                // Compare recipes by their result items since Recipe.getId() is not available in 1.21+
                // Recipes are wrapped in RecipeHolder, but we're working with the raw recipe here
                ItemStack currentResult = currentRecipe.getResultItem(level.registryAccess());
                ItemStack lastResult = lastRecipe.getResultItem(level.registryAccess());
                recipeChanged = !ItemStack.isSameItemSameComponents(currentResult, lastResult);
            } else if (maxProgress > 0) {
                recipeChanged = true;
            }

            if (recipeChanged) {
                resetProgress(pos);
                lastRecipe = currentRecipe;
                return;
            }

            lastRecipe = currentRecipe;

            if (hasRecipe()) {
                maxProgress = currentRecipe.getHammeringRequired();
                hitRemains = maxProgress - progress;
                setChanged(lvl, pos, st);

                if (hasProgressFinished()) {
                    craftItem();
                    resetProgress(pos);
                }
            } else {
                if (progress > 0 || maxProgress > 0) {
                    resetProgress(pos);
                }
            }
        } catch (Exception e) {
            OvergearedMod.LOGGER.error("Error ticking smithing anvil at {}", pos, e);
            resetProgress(pos);
        }
        tickHeatedIngredients(lvl);
    }

    public int getHitsRemaining() {
        return hitRemains;
    }

    // Add this method to ensure data sync
    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putInt("hitRemains", hitRemains);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        if (tag.contains("progress")) {
            this.progress = tag.getInt("progress");
        }
        if (tag.contains("maxProgress")) {
            this.maxProgress = tag.getInt("maxProgress");
        }
        if (tag.contains("hitRemains")) {
            this.hitRemains = tag.getInt("hitRemains");
        }
    }

    protected boolean matchesRecipeExactly(ForgingRecipe recipe) {
        // Create a wrapper for recipe matching
        ItemStackHandler recipeHandler = new ItemStackHandler(12);
        // Copy items from input slots (0-8) to our 3x3 grid
        for (int i = 0; i < 9; i++) {
            recipeHandler.setStackInSlot(i, this.itemHandler.getStackInSlot(i));
        }
        recipeHandler.setStackInSlot(11, this.itemHandler.getStackInSlot(11));
        RecipeWrapper recipeInput = new RecipeWrapper(recipeHandler);
        return recipe.matches(recipeInput, level);
    }

    protected String determineForgingQuality() {
        String quality = anvilBlock.getQuality();
        if (quality == null) return "well";
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        ForgingRecipe recipe = recipeOptional.get();
        if (!recipe.getBlueprintTypes().isEmpty()) {

            ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);
            BlueprintData blueprintData = blueprint.get(ModComponents.BLUEPRINT_DATA);

            // Define tool quality tiers in order of strength
            List<String> qualityTiers = List.of("poor", "well", "expert", "perfect", "master");

            // If blueprint is missing or invalid, fallback logic
            if (blueprint.isEmpty() || blueprintData == null) {
                return switch (quality.toLowerCase()) {
                    case "poor" -> ForgingQuality.POOR.getDisplayName();
                    default -> "well"; // Cap quality at 'well' without blueprint
                };
            }

            String blueprintQualityStr = blueprintData.quality().toLowerCase();

            // Determine capped quality
            int anvilTierIndex = qualityTiers.indexOf(quality.toLowerCase());
            int blueprintTierIndex = qualityTiers.indexOf(blueprintQualityStr);

            // Default to lowest if any tier is missing
            if (anvilTierIndex == -1 || blueprintTierIndex == -1) {
                return ForgingQuality.NONE.getDisplayName();
            }

            int finalIndex = Math.min(anvilTierIndex, blueprintTierIndex);

            switch (qualityTiers.get(finalIndex)) {
                case "poor":
                    return ForgingQuality.POOR.getDisplayName();
                case "well":
                    return ForgingQuality.WELL.getDisplayName();
                case "expert":
                    return ForgingQuality.EXPERT.getDisplayName();
                case "perfect": {
                    Random random = new Random();

                    // Check if any crafting slot contains a Master-quality ingredient
                    boolean hasMasterIngredient = false;
                    for (int i = 0; i < this.itemHandler.getSlots(); i++) {
                        if (i == OUTPUT_SLOT || i == BLUEPRINT_SLOT) continue; // skip output + blueprint
                        ItemStack stack = this.itemHandler.getStackInSlot(i);
                        ForgingQuality ingQuality = stack.get(ModComponents.FORGING_QUALITY);
                        if (!stack.isEmpty() && ingQuality == ForgingQuality.MASTER) {
                            hasMasterIngredient = true;
                            break;
                        }
                    }

                    // Normal Master roll from config
                    boolean masterRoll = ServerConfig.MASTER_QUALITY_CHANCE.get() != 0
                            && random.nextFloat() < ServerConfig.MASTER_QUALITY_CHANCE.get();

                    // Ingredient-based boost
                    boolean ingredientMasterRoll = hasMasterIngredient
                            && random.nextFloat() < ServerConfig.MASTER_FROM_INGREDIENT_CHANCE.get();

                    if ("master".equals(blueprintQualityStr) || masterRoll || ingredientMasterRoll) {
                        return ForgingQuality.MASTER.getDisplayName();
                    } else {
                        return ForgingQuality.PERFECT.getDisplayName();
                    }
                }
                case "master":
                    return ForgingQuality.MASTER.getDisplayName();
                default:
                    return ForgingQuality.WELL.getDisplayName();
            }
        }
        return quality;
    }

    public String minigameQuality() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) {
            return "none"; // no recipe = base fallback
        }

        ForgingRecipe recipe = recipeOptional.get();
        if (!recipe.getBlueprintTypes().isEmpty()) {
            if (!recipe.getQualityDifficulty().equals(ForgingQuality.NONE))
                return recipe.getQualityDifficulty().getDisplayName();
            else return blueprintQuality();
        } else return recipe.getQualityDifficulty().getDisplayName();
    }

    public String blueprintQuality() {
        String quality = anvilBlock.getQuality();
        if (quality == null) {
            return ForgingQuality.NONE.getDisplayName(); // fallback when global quality is missing
        }

        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) {
            return "poor"; // no recipe = base fallback
        }

        ForgingRecipe recipe = recipeOptional.get();
        if (!recipe.getBlueprintTypes().isEmpty()) {
            if (!recipe.getQualityDifficulty().equals(ForgingQuality.NONE))
                return recipe.getQualityDifficulty().getDisplayName();
            ItemStack blueprint = this.itemHandler.getStackInSlot(BLUEPRINT_SLOT);

            // Quality tiers in order
            List<String> qualityTiers = List.of("poor", "well", "expert", "perfect", "master");

            // Missing or invalid blueprint → cap quality
            String poor = quality.equalsIgnoreCase("poor")
                    ? ForgingQuality.POOR.getDisplayName()
                    : ForgingQuality.NONE.getDisplayName();
            BlueprintData blueprintData = blueprint.get(ModComponents.BLUEPRINT_DATA);
            if (blueprint.isEmpty() || blueprintData == null) {
                return poor;
            }

            String bpQuality = blueprintData.quality().toLowerCase();
            // ensure it’s in our tier list, otherwise default
            return qualityTiers.contains(bpQuality) ? bpQuality : ForgingQuality.NONE.getDisplayName();
        }

        return ForgingQuality.NONE.getDisplayName(); // fallback if no blueprint types
    }

    public void setProgress(int progress) {
        this.progress = progress;
        this.setChanged();

        // Force sync to client
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        if (this.data != null) {
            this.data.set(0, progress);
        }
    }

    public int getRequiredProgress() {
        return getCurrentRecipe()
                .map(ForgingRecipe::getHammeringRequired)
                .orElse(0); // default to 0 if recipe is empty
    }

    public int getProgress() {
        if (level != null && level.isClientSide() && data != null) {
            // On client, get from synced container data
            return data.get(0);
        }
        return this.progress;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    }

    public void setOwner(UUID uuid) {
        ownerUUID = uuid;
        sessionStartTime = level.getGameTime();
        setChanged();
    }

    public void clearOwner() {
        ownerUUID = null;
        sessionStartTime = 0L;
        setChanged();
    }

    public boolean isOwnedBy(Player player) {
        return ownerUUID != null && ownerUUID.equals(player.getUUID());
    }

    public boolean isOwned() {
        return ownerUUID != null;
    }

    public UUID getOccupiedAnvil(BlockPos pos) {
        return occupiedAnvils.get(pos);
    }

    public void putOccupiedAnvil(BlockPos pos, UUID me) {
        occupiedAnvils.put(pos, me);
    }

    public boolean hasQuality() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        ForgingRecipe recipe = recipeOptional.get();

        // Only set quality if recipe supports it
        return recipe.hasQuality();
    }

    public boolean needsMinigame() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return false;

        ForgingRecipe recipe = recipeOptional.get();

        // Only set quality if recipe supports it
        return !recipe.hasQuality() && recipe.needsMinigame();
    }

    public IItemHandlerModifiable getItemHandler() {
        return itemHandler;
    }

    public BlockPos getPos(ServerPlayer serverPlayer) {
        UUID playerUUID = serverPlayer.getUUID();
        for (Map.Entry<BlockPos, UUID> entry : occupiedAnvils.entrySet()) {
            if (entry.getValue().equals(playerUUID)) {
                return entry.getKey();
            }
        }
        return null; // Not found
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public boolean isMinigameOn() {
        return minigameOn;
    }

    public void setMinigameOn(boolean value) {
        this.minigameOn = value;
        setChanged(); // mark dirty for save
    }

    /**
     * Ticks heated ingredients in the anvil slots and cools them down after the configured time.
     * Uses HEATED_TIME data component to track when items were heated.
     */
    public void tickHeatedIngredients(Level level) {
        if (level.isClientSide) return;
        long tick = level.getGameTime();
        int cooldownTicks = ServerConfig.HEATED_ITEM_COOLDOWN_TICKS.get();

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (!stack.is(ModTags.Items.HEATED_METALS)) continue;

            // Check if item has HEATED_TIME component
            Long heatedSince = stack.get(ModComponents.HEATED_TIME);
            if (heatedSince == null) {
                // Item is heated but doesn't have a timestamp - set it now
                stack.set(ModComponents.HEATED_TIME, tick);
                continue;
            }

            // Check if enough time has passed to cool down
            if (tick - heatedSince >= cooldownTicks) {
                // Cool down the item - replace with cooled version
                ItemStack cooledItem = getCooledItem(stack, level);
                if (!cooledItem.isEmpty()) {
                    itemHandler.setStackInSlot(slot, cooledItem);
                    setChanged();
                }
            }
        }
    }
}

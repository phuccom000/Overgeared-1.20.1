package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.event.ModEvents;

import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractSmithingAnvilBlockEntity extends BlockEntity implements MenuProvider {
    protected static final int INPUT_SLOT = 0;
    protected static final int OUTPUT_SLOT = 10;
    protected final ItemStackHandler itemHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    protected final ContainerData data;
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected int progress = 0;
    protected int maxProgress = 0;
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

    public AbstractSmithingAnvilBlockEntity(AnvilTier tier, BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
        this.anvilTier = tier;
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    case 2 -> hitRemains;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> progress = pValue;
                    case 1 -> maxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    public static void applyForgingQuality(ItemStack stack, ForgingQuality quality) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("ForgingQuality", quality.getDisplayName());
    }

    public ItemStack getRenderStack(int index) {
        return itemHandler.getStackInSlot(index);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.of(() -> itemHandler).cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("hitRemains", hitRemains);
        tag.put("inventory", itemHandler.serializeNBT());

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
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("inventory"));
        }
        if (tag.contains("hitRemains")) {
            hitRemains = tag.getInt("hitRemains");
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
        if (!level.isClientSide()) {
            ModEvents.resetMinigameForPlayer((ServerPlayer) player);
        }
        player = null;
    }

    void triggerAdvancement(ServerPlayer player, String path) {
        // Use Minecraft's advancement system to grant the advancement
        var advancement = player.server.getAdvancements().getAdvancement(
                ResourceLocation.tryBuild(OvergearedMod.MOD_ID, path)
        );

        if (advancement != null) {
            var progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                // Grant all criteria to complete the advancement
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
            }
        }
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
            if (ingredient.hasTag() && ingredient.getTag().contains("ForgingQuality")) {
                ForgingQuality q = ForgingQuality.fromString(ingredient.getTag().getString("ForgingQuality"));
                if (q != null && (maxIngredientQuality == null || q.ordinal() > maxIngredientQuality.ordinal())) {
                    maxIngredientQuality = q;
                }
            }
        }

        if (recipe.hasQuality()) {
            if (ServerConfig.ENABLE_MINIGAME.get()) {
                String qualityStr = determineForgingQuality();
                if (!Objects.equals(qualityStr, "no_quality")) {
                    ForgingQuality quality = ForgingQuality.fromString(qualityStr);
                    if (quality != null) {
                        // Clamp to minimum quality if needed
                        if (minimumQuality != null && quality.ordinal() < minimumQuality.ordinal()) {
                            quality = minimumQuality;
                        }

                        if (maxIngredientQuality != null && quality.ordinal() > maxIngredientQuality.ordinal() && ServerConfig.INGREDIENTS_DEFINE_MAX_QUALITY.get()) {
                            quality = maxIngredientQuality;
                        }

                        CompoundTag tag = result.getOrCreateTag();

                        if (quality != ForgingQuality.PERFECT) {
                            tag.putString("ForgingQuality", quality.getDisplayName());
                        } else {
                            if (ServerConfig.MASTER_QUALITY_CHANCE.get() != 0 &&
                                    new Random().nextFloat() < ServerConfig.MASTER_QUALITY_CHANCE.get()) {
                                quality = ForgingQuality.MASTER;
                                triggerAdvancement((ServerPlayer) player, "master_forging");
                            }
                            if (quality.equals(ForgingQuality.PERFECT))
                                triggerAdvancement((ServerPlayer) player, "perfect_forging");
                            tag.putString("ForgingQuality", quality.getDisplayName());
                        }

                        if (!(result.getItem() instanceof ArmorItem) &&
                                !(result.getItem() instanceof ShieldItem) &&
                                recipe.hasPolishing()) {
                            tag.putBoolean("Polished", false);
                        }
                        if (recipe.needQuenching() && (!result.is(ModTags.Items.HEATED_METALS) || !result.is(ModTags.Items.HOT_ITEMS))) {
                            tag.putBoolean("Heated", true);
                            result.setTag(tag);
                        }
                    }
                }
            }
        } else if (recipe.needsMinigame()) {
            // Handle minigame result without quality
            if (ServerConfig.ENABLE_MINIGAME.get()) {
                String quality = determineForgingQuality();
                if (!Objects.equals(quality, "no_quality")) {
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
        } else if (ItemStack.isSameItemSameTags(existing, result)) {
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

    protected boolean hasEnoughIngredients(ForgingRecipe recipe) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        SimpleContainer inventory = new SimpleContainer(9);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        // Create a copy of the inventory to track consumed items
        SimpleContainer inventoryCopy = new SimpleContainer(9);
        for (int i = 0; i < 9; i++) {
            inventoryCopy.setItem(i, inventory.getItem(i).copy());
        }

        // Check each ingredient requirement
        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = inventoryCopy.getItem(i);
                if (ingredient.test(stack) && stack.getCount() > 0) {
                    stack.shrink(1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public Optional<ForgingRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots());
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        inventory.setItem(11, itemHandler.getStackInSlot(11));

        return ForgingRecipe.findBestMatch(level, inventory)
                .filter(this::matchesRecipeExactly)
                //.filter(this::hasEnoughIngredients)
                ;
    }

    protected boolean canInsertItemIntoOutputSlot(ItemStack stackToInsert) {
        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);
        return (existing.isEmpty() || ItemStack.isSameItemSameTags(existing, stackToInsert));
    }

    protected boolean canInsertAmountIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    public boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    protected void increaseCraftingProgress() {
        progress++;
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
            if (!ItemStack.isSameItemSameTags(currentBlueprint, lastBlueprint)) {
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
                recipeChanged = !currentRecipe.getId().equals(lastRecipe.getId());
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
    }

    public int getHitsRemaining() {
        return hitRemains;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("inventory", itemHandler.serializeNBT());
        return tag;
    }

    protected boolean matchesRecipeExactly(ForgingRecipe recipe) {
        SimpleContainer inventory = new SimpleContainer(this.itemHandler.getSlots()); // 3x3 grid
        // Copy items from input slots (0-8) to our 3x3 grid
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }
        inventory.setItem(11, this.itemHandler.getStackInSlot(11));
        return recipe.matches(inventory, level);
    }

    protected abstract String determineForgingQuality();

    public abstract String blueprintQuality();

    public void setProgress(int progress) {
        this.progress = progress;
        this.setChanged();

        // If you're using ContainerData
        if (this.data != null) {
            this.data.set(0, progress); // Assuming progress is at index 0
        }
    }

    public int getRequiredProgress() {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        ForgingRecipe currentRecipe = recipe.get();
        return currentRecipe.getHammeringRequired();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        // Ensure any players are reset
        /*ServerPlayer user = ModItemInteractEvents.getUsingPlayer(getBlockPos());
        if (user != null) {
            user.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                //minigame.resetNBTData();
                minigame.reset(user);
                //minigame.setIsVisible(false, user);
                progress = 0;
                ModItemInteractEvents.releaseAnvil(user, getBlockPos());
                //ModMessages.sendToPlayer(new MinigameSyncS2CPacket(new CompoundTag().putBoolean("isVisible", false)), user);
            });
        }*/
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
}

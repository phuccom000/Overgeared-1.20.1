package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.StartMinigameS2CPacket;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.SmithingAnvilMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class SmithingAnvilBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(11) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 10;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 0;
    private int hitRemains;
    private long busyUntilGameTime = 0L;
    private String currentQuality = "";
    @Nullable
    private UUID forgingPlayerId;
    private ForgingRecipe lastRecipe = null;

    public SmithingAnvilBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.SMITHING_ANVIL_BE.get(), pPos, pBlockState);
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
                return 2;
            }
        };
    }

    // Player tracking methods
    public boolean isPlayerForging(Player player) {
        return forgingPlayerId != null && forgingPlayerId.equals(player.getUUID());
    }

    public void startForging(Player player) {
        this.forgingPlayerId = player.getUUID();
        setChanged();
    }

    public void stopForging() {
        this.forgingPlayerId = null;
        setChanged();
    }

    public boolean isForging() {
        return forgingPlayerId != null;
    }

    private boolean isForgingPlayerInRange() {
        if (level == null || forgingPlayerId == null) return false;
        Player player = level.getPlayerByUUID(forgingPlayerId);
        return player != null && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64;
    }

    public void abortForging() {
        if (isForging()) {
            if (level != null) {
                Player player = level.getPlayerByUUID(forgingPlayerId);
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("message.overgeared.forging_aborted"));
                }
            }
            stopForging();
            resetProgress();
        }
    }

    // Minigame methods
    public void startMinigame(Player player) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;

        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) return;

        startForging(player);
        ItemStack result = recipe.get().getResultItem(level.registryAccess());
        int requiredHits = recipe.get().getHammeringRequired();
        ModMessages.sendToPlayer(new StartMinigameS2CPacket(getBlockPos(), forgingPlayerId, result, requiredHits), serverPlayer);
    }

    public void registerHit(String quality) {
        this.currentQuality = quality;
        increaseCraftingProgress();
        setChanged();

        if (hasProgressFinished()) {
            craftItem();
            resetProgress();
        }
    }

    // Crafting methods
    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (pLevel.isClientSide) return;

        if (isForging() && !isForgingPlayerInRange()) {
            abortForging();
            return;
        }

        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (hasRecipe()) {
            ForgingRecipe currentRecipe = recipe.get();
            maxProgress = currentRecipe.getHammeringRequired();
            setChanged(pLevel, pPos, pState);

            if (hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    public void craftItem() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return;

        ForgingRecipe recipe = recipeOptional.get();
        ItemStack result = recipe.getResultItem(getLevel().registryAccess());

        if (recipe.hasQuality()) {
            String quality = determineForgingQuality();
            if (!quality.isEmpty()) {
                CompoundTag tag = result.getOrCreateTag();
                tag.putString("ForgingQuality", quality);
                result.setTag(tag);
            }
        }

        for (int i = 0; i < this.itemHandler.getSlots() - 1; i++) {
            this.itemHandler.extractItem(i, 1, false);
        }

        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            this.itemHandler.setStackInSlot(OUTPUT_SLOT, result);
        } else if (ItemStack.isSameItemSameTags(existing, result)) {
            int total = existing.getCount() + result.getCount();
            int maxSize = Math.min(existing.getMaxStackSize(), this.itemHandler.getSlotLimit(OUTPUT_SLOT));

            if (total <= maxSize) {
                existing.grow(result.getCount());
            } else {
                existing.setCount(maxSize);
                ItemStack overflow = result.copy();
                overflow.setCount(total - maxSize);
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), overflow);
            }
        }
    }

    private String determineForgingQuality() {
        switch (currentQuality.toLowerCase()) {
            case "perfect":
                return ForgingQuality.PERFECT.getDisplayName();
            case "expert":
                return ForgingQuality.EXPERT.getDisplayName();
            case "well":
                return ForgingQuality.WELL.getDisplayName();
            case "poor":
                return ForgingQuality.POOR.getDisplayName();
            default:
                return ForgingQuality.WELL.getDisplayName();
        }
    }

    private void resetProgress() {
        progress = 0;
        maxProgress = 0;
        currentQuality = "";
        lastRecipe = null;
        AnvilMinigameOverlay.endMinigame();
        setChanged();
    }

    public boolean hasRecipe() {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (recipe.isPresent()) {
            ItemStack resultStack = recipe.get().getResultItem(level.registryAccess());
            return canInsertItemIntoOutputSlot(resultStack) && canInsertAmountIntoOutputSlot(resultStack.getCount());
        }
        return false;
    }

    public Optional<ForgingRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(9);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        return ForgingRecipe.findBestMatch(level, inventory)
                .filter(this::matchesRecipeExactly);
    }

    private boolean matchesRecipeExactly(ForgingRecipe recipe) {
        SimpleContainer inventory = new SimpleContainer(9);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }
        return recipe.matches(inventory, level);
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack stackToInsert) {
        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);
        return existing.isEmpty() || ItemStack.isSameItemSameTags(existing, stackToInsert);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        ItemStack outputStack = this.itemHandler.getStackInSlot(OUTPUT_SLOT);
        return outputStack.getCount() + count <= outputStack.getMaxStackSize();
    }

    private boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
        setChanged();
    }

    public boolean isBusy(long currentGameTime) {
        return currentGameTime < busyUntilGameTime;
    }

    public void setBusyUntil(long time) {
        this.busyUntilGameTime = time;
        setChanged();
    }

    public void updateHitsRemaining(Level lvl, BlockPos pos, BlockState st) {
        Optional<ForgingRecipe> currentRecipeOpt = getCurrentRecipe();
        boolean recipeChanged = false;

        if (currentRecipeOpt.isPresent()) {
            ForgingRecipe currentRecipe = currentRecipeOpt.get();
            if (lastRecipe != null) {
                recipeChanged = !currentRecipe.getId().equals(lastRecipe.getId());
            } else if (maxProgress > 0) {
                recipeChanged = true;
            }
            lastRecipe = currentRecipe;
        } else {
            recipeChanged = (lastRecipe != null || maxProgress > 0);
            lastRecipe = null;
        }

        if (recipeChanged) {
            resetProgress();
            return;
        }

        if (hasRecipe()) {
            ForgingRecipe recipe = currentRecipeOpt.get();
            maxProgress = recipe.getHammeringRequired();
            hitRemains = maxProgress - progress;
            setChanged();

            if (hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    public int getHitsRemaining() {
        return hitRemains;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        setChanged();
        if (this.data != null) {
            this.data.set(0, progress);
        }
    }

    public int getRequiredProgress() {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        return recipe.map(forgingRecipe -> forgingRecipe.getHammeringRequired() - progress).orElse(0);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("smithing_anvil.progress", progress);
        pTag.putString("current_quality", currentQuality);
        if (forgingPlayerId != null) {
            pTag.putUUID("forging_player", forgingPlayerId);
        }
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("smithing_anvil.progress");
        currentQuality = pTag.getString("current_quality");
        if (pTag.hasUUID("forging_player")) {
            forgingPlayerId = pTag.getUUID("forging_player");
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        if (!pPlayer.isCrouching()) {
            return new SmithingAnvilMenu(pContainerId, pPlayerInventory, this, this.data);
        }
        return null;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public ItemStack getRenderStack(int index) {
        return itemHandler.getStackInSlot(index);
    }

    public static void applyForgingQuality(ItemStack stack, ForgingQuality quality) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("ForgingQuality", quality.getDisplayName());
    }
}
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.custom.SmithingAnvil;
import net.stirdrem.overgeared.item.custom.SmithingHammer;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.SmithingAnvilMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private UUID ownerUUID = null;
    private Map<BlockPos, UUID> occupiedAnvils = Collections.synchronizedMap(new HashMap<>());

    private long sessionStartTime = 0L; // optional, for timeout logic


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
                return 3;
            }
        };
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        if (!pPlayer.isCrouching()) {
            return new SmithingAnvilMenu(pContainerId, pPlayerInventory, this, this.data);
        } else return null;

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


    public void increaseForgingProgress(Level pLevel, BlockPos pPos, BlockState pState) {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (hasRecipe()) {
            ForgingRecipe currentRecipe = recipe.get();
            maxProgress = currentRecipe.getHammeringRequired();
            increaseCraftingProgress();
            setChanged(pLevel, pPos, pState);

            if (hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private void resetProgress() {
        progress = 0;
        maxProgress = 0;
        lastRecipe = null;
        //ClientAnvilMinigameData.setHitsRemaining(0);
        ServerPlayer user = SmithingHammer.getUsingPlayer(getBlockPos());
        if (user != null) {
            user.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                minigame.resetNBTData();
                //minigame.reset(user);
                //minigame.setIsVisible(false, user);
                SmithingHammer.releaseAnvil(user, getBlockPos());
                //ModMessages.sendToPlayer(new MinigameSyncS2CPacket(new CompoundTag().putBoolean("isVisible", false)), user);
            });
        }
        //AnvilMinigameOverlay.endMinigame();
    }

    private void craftItem() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isEmpty()) return;

        ForgingRecipe recipe = recipeOptional.get();
        ItemStack result = recipe.getResultItem(getLevel().registryAccess());

        // Only set quality if recipe supports it
        if (recipe.hasQuality()) {
            String quality = determineForgingQuality();
            if (!Objects.equals(quality, "no_quality")) { // Additional safety check
                CompoundTag tag = result.getOrCreateTag();
                tag.putString("ForgingQuality", quality);
                result.setTag(tag);
            }
        }

        // Extract ingredients
        for (int i = 0; i < this.itemHandler.getSlots() - 1; i++) {
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


    public boolean hasRecipe() {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (recipe.isPresent()) {
            ItemStack resultStack = recipe.get().getResultItem(level.registryAccess());

            return canInsertItemIntoOutputSlot(resultStack)
                    && canInsertAmountIntoOutputSlot(resultStack.getCount());
        }

        return false;
    }


    private boolean hasEnoughIngredients(ForgingRecipe recipe) {
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
        SimpleContainer inventory = new SimpleContainer(9);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        return ForgingRecipe.findBestMatch(level, inventory)
                .filter(this::matchesRecipeExactly)
                //.filter(this::hasEnoughIngredients)
                ;
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack stackToInsert) {
        ItemStack existing = this.itemHandler.getStackInSlot(OUTPUT_SLOT);
        return (existing.isEmpty() || ItemStack.isSameItemSameTags(existing, stackToInsert));
    }


    private boolean canInsertAmountIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }


    public boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }


    public boolean isBusy(long currentGameTime) {
        return currentGameTime < busyUntilGameTime;
    }

    public void setBusyUntil(long time) {
        this.busyUntilGameTime = time;
        setChanged(level, worldPosition, getBlockState());
    }


    private ForgingRecipe lastRecipe = null;

    public void tick(Level lvl, BlockPos pos, BlockState st) {
        try {
            Optional<ForgingRecipe> currentRecipeOpt = getCurrentRecipe();
            // Check if recipe has changed by comparing with last known recipe
            boolean recipeChanged = false;
            if (currentRecipeOpt.isEmpty()) {
                resetProgress();
                return;
            }


            ForgingRecipe currentRecipe = currentRecipeOpt.get();

            if (lastRecipe != null) {
                // Compare recipes by their IDs or other unique properties
                recipeChanged = !currentRecipe.getId().equals(lastRecipe.getId());
            } else if (maxProgress > 0) {
                // We had progress but no last recipe (shouldn't normally happen)
                recipeChanged = true;
            }

            lastRecipe = currentRecipe;

            if (recipeChanged) {
                resetProgress();
                return;
            }

            if (hasRecipe()) {
                ForgingRecipe recipe = currentRecipeOpt.get();
                maxProgress = recipe.getHammeringRequired();
                hitRemains = maxProgress - progress;
                setChanged(lvl, pos, st);

                if (hasProgressFinished()) {
                    craftItem();
                    resetProgress();
                }
            }
            /*else {
                resetProgress();
            }*/
        } catch (Exception e) {
            OvergearedMod.LOGGER.error("Error updating anvil hits remaining", e);
            resetProgress();
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

    public static void applyForgingQuality(ItemStack stack, ForgingQuality quality) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("ForgingQuality", quality.getDisplayName());
    }

    private boolean matchesRecipeExactly(ForgingRecipe recipe) {
        SimpleContainer inventory = new SimpleContainer(9); // 3x3 grid
        // Copy items from input slots (0-8) to our 3x3 grid
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }
        return recipe.matches(inventory, level);
    }

   /* public void startForgingMinigame(Player player, ItemStack result) {
        if (player.level().isClientSide) {
            Minecraft.getInstance().setScreen(new ForgingMinigameScreen(result));
        }
    }*/

    private String determineForgingQuality() {
        // Get quality from anvil or use default if null
        String quality = SmithingAnvil.getQuality();
        if (quality == null) {
            return "no_quality"; // Default quality
        }

        // Use switch expression for better null safety
        return switch (quality.toLowerCase()) {
            case "poor" -> ForgingQuality.POOR.getDisplayName();
            case "expert" -> ForgingQuality.EXPERT.getDisplayName();
            case "perfect" -> ForgingQuality.PERFECT.getDisplayName();
            case "well" -> ForgingQuality.WELL.getDisplayName();
            default -> "no_quality";// Fallback
        };
    }


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
        return currentRecipe.getHammeringRequired() - progress;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        // Ensure any players are reset
        ServerPlayer user = SmithingHammer.getUsingPlayer(getBlockPos());
        if (user != null) {
            user.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                //minigame.resetNBTData();
                minigame.reset(user);
                //minigame.setIsVisible(false, user);
                progress = 0;
                SmithingHammer.releaseAnvil(user, getBlockPos());
                //ModMessages.sendToPlayer(new MinigameSyncS2CPacket(new CompoundTag().putBoolean("isVisible", false)), user);
            });
        }
    }

  /*  public void completeForgingWithQuality(String quality) {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        if (recipeOptional.isPresent()) {
            ForgingRecipe recipe = recipeOptional.get();
            ItemStack result = recipe.getResultItem(level.registryAccess()).copy();

            // Apply quality
            result.getOrCreateTag().putString("ForgingQuality", quality);

            // Clear inputs
            for (int i = 0; i < 9; i++) {
                itemHandler.extractItem(i, 1, false);
            }

            // Set output
            ItemStack currentOutput = itemHandler.getStackInSlot(OUTPUT_SLOT);
            if (currentOutput.isEmpty()) {
                itemHandler.setStackInSlot(OUTPUT_SLOT, result);
            } else {
                currentOutput.grow(1);
            }

            resetForgingState();
        }
    }*/

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


}

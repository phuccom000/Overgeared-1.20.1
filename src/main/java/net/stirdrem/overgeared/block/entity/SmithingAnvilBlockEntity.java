package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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
import net.stirdrem.overgeared.block.custom.SmithingAnvil;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.minigame.AnvilMinigame;
import net.stirdrem.overgeared.item.custom.SmithingHammer;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.screen.SmithingAnvilMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
    private static int progress = 0;
    private static int maxProgress = 0;
    private static int hitRemains;
    private static int perfectHits = ClientAnvilMinigameData.getPerfectHits();
    private static int goodHits = ClientAnvilMinigameData.getGoodHits();
    private static int missedHits = ClientAnvilMinigameData.getMissedHits();
    private static float arrowPosition = ClientAnvilMinigameData.getArrowPosition();
    private static float arrowSpeed = ServerConfig.DEFAULT_ARROW_SPEED.get().floatValue();
    private long busyUntilGameTime = 0L;


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

    public ItemStack getRenderStack(int index) {
        return itemHandler.getStackInSlot(index);
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
        } else return null;

    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("progress", progress);
        pTag.putInt("maxProgress", maxProgress);
        pTag.putInt("hitsRemaining", hitRemains);
        pTag.putInt("perfectHits", perfectHits);
        pTag.putInt("goodHits", goodHits);
        pTag.putInt("missedHits", missedHits);
        pTag.putFloat("arrowPosition", arrowPosition);
        pTag.putFloat("missedHits", arrowSpeed);
        pTag.putLong("busyUntil", busyUntilGameTime);

        // Save recipe ID if we have a last recipe
        if (lastRecipe != null) {
            pTag.putString("lastRecipe", lastRecipe.getId().toString());
        }

        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("progress");
        maxProgress = pTag.getInt("maxProgress");
        hitRemains = pTag.getInt("hitsRemaining");
        perfectHits = pTag.getInt("perfectHits");
        goodHits = pTag.getInt("goodHits");
        missedHits = pTag.getInt("missedHits");
        arrowPosition = pTag.getFloat("arrowPosition");
        arrowSpeed = pTag.getFloat("arrowSpeed");
        busyUntilGameTime = pTag.getLong("busyUntil");

        // Load last recipe if present
        if (pTag.contains("lastRecipe")) {
            ResourceLocation recipeId = ResourceLocation.tryParse(pTag.getString("lastRecipe"));
            if (level != null) {
                level.getRecipeManager().byKey(recipeId).ifPresent(recipe -> {
                    if (recipe instanceof ForgingRecipe) {
                        lastRecipe = (ForgingRecipe) recipe;
                    }
                });
            }
        }
    }

    // Helper method to save minimal data for sync


    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (hasRecipe()) {
            ForgingRecipe currentRecipe = recipe.get();
            maxProgress = currentRecipe.getHammeringRequired();
            increaseCraftingProgress();
            hitRemains = maxProgress - progress;
            ClientAnvilMinigameData.setHitsRemaining(hitRemains);
            setChanged(pLevel, pPos, pState);

            if (hasProgressFinished()) {
                craftItem();
                resetProgress(pPos);
            }
        } else {
            resetProgress(pPos);
        }
    }

    private void resetProgress(BlockPos pos) {
        progress = 0;
        maxProgress = 0;
        lastRecipe = null;
        System.out.println("Minigame reset progress");
        //AnvilMinigame.end();
        SmithingHammer.releaseAnvil(pos);
    }

    private void craftItem() {
        Optional<ForgingRecipe> recipeOptional = getCurrentRecipe();
        ForgingRecipe recipe = recipeOptional.get();
        ItemStack result = recipe.getResultItem(null);

        // Determine quality based on performance
        String quality = determineForgingQuality();
        //String quality = null;
        // Only set the NBT tag if quality is meaningful
        if (quality != null && !quality.isEmpty() && recipe.hasQuality() && !quality.equals("none")) {
            CompoundTag tag = result.getOrCreateTag();
            tag.putString("ForgingQuality", quality);
            result.setTag(tag);
        }
        int test = this.itemHandler.getSlots();
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


    private boolean hasProgressFinished() {
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

    public void updateHitsRemaining(Level lvl, BlockPos pos, BlockState st) {

        Optional<ForgingRecipe> currentRecipeOpt = getCurrentRecipe();

/*
        // If no recipe but we have progress, reset
        if (currentRecipeOpt.isEmpty() && maxProgress > 0) {
            progress = 0;
            maxProgress = 0;
            lastRecipe = null;
            return;
        }
*/

        // If we have a recipe
        if (currentRecipeOpt.isPresent()) {
            ForgingRecipe currentRecipe = currentRecipeOpt.get();

            // Check if recipe changed
            if (lastRecipe != null && !currentRecipe.getId().equals(lastRecipe.getId())) {
                progress = 0;
                maxProgress = 0;
                lastRecipe = currentRecipe;
                return;
            }

            lastRecipe = currentRecipe;
            maxProgress = currentRecipe.getHammeringRequired();
            hitRemains = maxProgress - progress;

            if (hasProgressFinished()) {
                hitRemains = 0;
            }
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
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
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
        // Implement your logic here to assess performance
        // For demonstration, we'll return a random quality
        String quality = SmithingAnvil.getQuality();
        return switch (quality) {
            case "poor" -> ForgingQuality.POOR.getDisplayName();
            case "expert" -> ForgingQuality.EXPERT.getDisplayName();
            case "perfect" -> ForgingQuality.PERFECT.getDisplayName();
            case "well" -> ForgingQuality.WELL.getDisplayName();
            default -> "none";
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
        int required = currentRecipe.getHammeringRequired() - progress;
        return currentRecipe.getHammeringRequired() - progress;
    }

    public ItemStack getResultItem() {
        Optional<ForgingRecipe> recipe = getCurrentRecipe();
        if (recipe.isPresent())
            return recipe.get().getResultItem(level.registryAccess());
        return null;
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


}

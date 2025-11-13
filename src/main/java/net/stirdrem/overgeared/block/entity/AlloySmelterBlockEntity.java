package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.stirdrem.overgeared.block.entity.ModBlockEntities;
import net.stirdrem.overgeared.recipe.AlloySmeltingRecipe;
import net.stirdrem.overgeared.recipe.ShapedAlloySmeltingRecipe;
import net.stirdrem.overgeared.screen.AlloySmelterMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AlloySmelterBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private final ItemStackHandler itemHandler = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ContainerData data;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final LazyOptional<? extends IItemHandler>[] sidedHandlers = SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

    private int burnTime;
    private int maxBurnTime;
    private int cookTime;
    private int cookTimeTotal;
    private float storedExperience = 0.0F;

    public AlloySmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALLOY_FURNACE_BE.get(), pos, state);

        this.data = new ContainerData() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> burnTime;
                    case 1 -> maxBurnTime;
                    case 2 -> cookTime;
                    case 3 -> cookTimeTotal;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> burnTime = value;
                    case 1 -> maxBurnTime = value;
                    case 2 -> cookTime = value;
                    case 3 -> cookTimeTotal = value;
                }
            }

            public int getCount() {
                return 4;
            }
        };
    }

    // --------------------------------------------------
    // Tick logic
    // --------------------------------------------------
    public static void tick(Level level, BlockPos pos, BlockState state, AlloySmelterBlockEntity be) {
        boolean wasLit = be.burnTime > 0;
        boolean dirty = false;

        if (be.burnTime > 0) be.burnTime--;

        ItemStack fuel = be.itemHandler.getStackInSlot(4);

        if (be.burnTime == 0 && be.canSmelt()) {
            be.maxBurnTime = be.burnTime = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
            if (be.burnTime > 0 && !fuel.isEmpty()) {
                Item fuelContainer = fuel.getItem().getCraftingRemainingItem();
                fuel.shrink(1);
                if (fuel.isEmpty() && fuelContainer != null)
                    be.itemHandler.setStackInSlot(4, new ItemStack(fuelContainer));
                dirty = true;
            }
        }

        if (be.isLit() && be.canSmelt()) {
            be.cookTime++;
            if (be.cookTime >= be.cookTimeTotal) {
                be.cookTime = 0;
                be.smelt();
                dirty = true;
            }
        } else if (!be.canSmelt()) {
            be.cookTime = 0;
        }

        if (wasLit != be.isLit()) {
            state = state.setValue(BlockStateProperties.LIT, be.isLit());
            level.setBlock(pos, state, 3);
            dirty = true;
        }

        if (dirty) be.setChanged();
    }

    // --------------------------------------------------
    // Capability
    // --------------------------------------------------
    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        for (LazyOptional<? extends IItemHandler> handler : sidedHandlers) handler.invalidate();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) return lazyItemHandler.cast();
            if (side == Direction.UP) return sidedHandlers[0].cast();
            if (side == Direction.DOWN) return sidedHandlers[1].cast();
            return sidedHandlers[2].cast();
        }
        return super.getCapability(cap, side);
    }

    // --------------------------------------------------
    // Smelting logic
    // --------------------------------------------------
    private boolean canSmelt() {
        SimpleContainer inv = new SimpleContainer(4);
        for (int i = 0; i < 4; i++) inv.setItem(i, itemHandler.getStackInSlot(i));

        // Try shapeless recipe first
        Optional<AlloySmeltingRecipe> shapelessRecipe =
                level.getRecipeManager().getRecipeFor(AlloySmeltingRecipe.Type.INSTANCE, inv, level);

        // Try shaped recipe if shapeless not found
        Optional<ShapedAlloySmeltingRecipe> shapedRecipe =
                level.getRecipeManager().getRecipeFor(ShapedAlloySmeltingRecipe.Type.INSTANCE, inv, level);

        if (shapelessRecipe.isEmpty() && shapedRecipe.isEmpty()) return false;

        cookTimeTotal = shapelessRecipe.map(AlloySmeltingRecipe::getCookingTime)
                .orElseGet(() -> shapedRecipe.get().getCookingTime());

        ItemStack result = shapelessRecipe.map(r -> r.getResultItem(level.registryAccess()))
                .orElseGet(() -> shapedRecipe.get().getResultItem(level.registryAccess()));

        ItemStack output = itemHandler.getStackInSlot(5);
        return !result.isEmpty() &&
                (output.isEmpty() || (output.is(result.getItem()) &&
                        output.getCount() + result.getCount() <= output.getMaxStackSize()));
    }


    private void smelt() {
        if (!canSmelt()) return;

        SimpleContainer inv = new SimpleContainer(4);
        for (int i = 0; i < 4; i++) inv.setItem(i, itemHandler.getStackInSlot(i));

        Optional<AlloySmeltingRecipe> shapelessRecipe =
                level.getRecipeManager().getRecipeFor(AlloySmeltingRecipe.Type.INSTANCE, inv, level);
        Optional<ShapedAlloySmeltingRecipe> shapedRecipe =
                level.getRecipeManager().getRecipeFor(ShapedAlloySmeltingRecipe.Type.INSTANCE, inv, level);

        ItemStack result;
        float xp;

        if (shapelessRecipe.isPresent()) {
            AlloySmeltingRecipe recipe = shapelessRecipe.get();
            result = recipe.getResultItem(level.registryAccess());
            xp = recipe.getExperience();
        } else if (shapedRecipe.isPresent()) {
            ShapedAlloySmeltingRecipe recipe = shapedRecipe.get();
            result = recipe.getResultItem(level.registryAccess());
            xp = recipe.getExperience();
        } else return;

        ItemStack output = itemHandler.getStackInSlot(5);
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(5, result.copy());
        } else if (output.is(result.getItem())) {
            output.grow(result.getCount());
        }

        for (int i = 0; i < 4; i++) {
            ItemStack input = itemHandler.getStackInSlot(i);
            if (!input.isEmpty()) input.shrink(1);
        }

        if (!level.isClientSide && xp > 0.0F) {
            storedExperience += xp;
        }
    }


    // --------------------------------------------------
    // Experience logic (vanilla accurate)
    // --------------------------------------------------
    private void spawnExperience(float xp) {
        if (this.level == null || this.level.isClientSide) return;

        int i = Mth.floor(xp);
        float f = xp - i;
        if (f > 0.0F && Math.random() < f) i++;

        while (i > 0) {
            int split = ExperienceOrb.getExperienceValue(i);
            i -= split;
            this.level.addFreshEntity(new ExperienceOrb(this.level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    split));
        }
    }

    private boolean isLit() {
        return burnTime > 0;
    }

    // --------------------------------------------------
    // Container & UI
    // --------------------------------------------------
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overgeared.alloy_smelter");
    }

    @Override
    protected Component getDefaultName() {
        return null;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new AlloySmelterMenu(id, playerInv, this, this.data);
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return null;
    }

    public void awardStoredExperience(Player player) {
        if (this.level == null || this.level.isClientSide) return;
        if (storedExperience > 0 && player != null) {
            int total = (int) storedExperience;
            float fractional = storedExperience - total;
            if (fractional > 0.0F && Math.random() < fractional) total++;

            // Give the XP to the player
            player.giveExperiencePoints(total);

            // Play the XP pickup sound at the smelter's position
            this.level.playSound(
                    null, // null = heard by all nearby players
                    worldPosition,
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.5F,
                    this.level.random.nextFloat() * 0.1F + 0.9F
            );

            storedExperience = 0;
            setChanged();
        }
    }


    // --------------------------------------------------
    // NBT
    // --------------------------------------------------
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurnTime", maxBurnTime);
        tag.putInt("cookTime", cookTime);
        tag.putInt("cookTimeTotal", cookTimeTotal);
        tag.putFloat("storedXp", storedExperience);

    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurnTime");
        cookTime = tag.getInt("cookTime");
        cookTimeTotal = tag.getInt("cookTimeTotal");
        storedExperience = tag.getFloat("storedXp");
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
        spawnExperience(storedExperience);
    }

    // --------------------------------------------------
    // Hopper automation
    // --------------------------------------------------
    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) return new int[]{0, 1, 2, 3};
        else if (side == Direction.DOWN) return new int[]{5};
        else return new int[]{4};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        if (index == 5) return false;
        if (index == 4) return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index == 5;
    }

    // --------------------------------------------------
    // Basic container methods
    // --------------------------------------------------
    @Override
    public int getContainerSize() {
        return itemHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++)
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            ItemStack result = stack.split(amount);
            setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr(
                (double) this.worldPosition.getX() + 0.5D,
                (double) this.worldPosition.getY() + 0.5D,
                (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

}

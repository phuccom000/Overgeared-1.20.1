package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import net.stirdrem.overgeared.recipe.AlloySmeltingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.recipe.ShapedAlloySmeltingRecipe;
import net.stirdrem.overgeared.screen.AlloySmelterMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AlloySmelterBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {

  private final int inventorySize = 6;
  private final NonNullList<ItemStack> inventory = NonNullList.withSize(inventorySize, ItemStack.EMPTY);

  private final ItemStackHandler itemHandler = new ItemStackHandler(inventory) {
    @Override
    protected void onContentsChanged(int slot) {
      setChanged();
    }
  };

  private final ContainerData data;

  private int burnTime;
  private int maxBurnTime;
  private int cookTime;
  private int cookTimeTotal;
  private float storedExperience;

  public AlloySmelterBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.ALLOY_FURNACE_BE.get(), pos, state);

    this.data = new ContainerData() {
      @Override
      public int get(int index) {
        return switch (index) {
          case 0 -> burnTime;
          case 1 -> maxBurnTime;
          case 2 -> cookTime;
          case 3 -> cookTimeTotal;
          default -> 0;
        };
      }

      @Override
      public void set(int index, int value) {
        switch (index) {
          case 0 -> burnTime = value;
          case 1 -> maxBurnTime = value;
          case 2 -> cookTime = value;
          case 3 -> cookTimeTotal = value;
        }
      }

      @Override
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
    boolean changed = false;

    if (be.burnTime > 0) {
      be.burnTime--;
      changed = true;
    }

    ItemStack fuel = be.itemHandler.getStackInSlot(4);

    if (be.burnTime == 0 && be.canSmelt()) {
      be.maxBurnTime = be.burnTime = fuel.getBurnTime(RecipeType.SMELTING);
      if (be.burnTime > 0 && !fuel.isEmpty()) {
        ItemStack fuelContainer = fuel.getCraftingRemainingItem();
        fuel.shrink(1);
        if (fuel.isEmpty() && !fuelContainer.isEmpty())
          be.itemHandler.setStackInSlot(4, fuelContainer);
        changed = true;
      }
    }

    if (be.isLit() && be.canSmelt()) {
      be.cookTime++;
      if (be.cookTime >= be.cookTimeTotal) {
        be.cookTime = 0;
        be.smelt();
        changed = true;
      }
    } else if (!be.isLit() && be.cookTime > 0) {  // Only reset if not lit
      be.cookTime = 0;
      changed = true;  // Mark dirty since we changed cookTime
    }

    if (wasLit != be.isLit()) {
      state = state.setValue(BlockStateProperties.LIT, be.isLit());
      level.setBlock(pos, state, 3);
      changed = true;
    }

    if (changed) be.setChanged();
  }

  public Optional<RecipeHolder<?>> getCurrentRecipe() {
    if (level == null) return Optional.empty();

    // we have to copy our item handler to one that is of size 4
    // so that we don't include the fuel and output slots in the recipe
    ItemStackHandler input = new ItemStackHandler(4);
    for (int i = 0; i < input.getSlots(); i++) {
      input.setStackInSlot(i, itemHandler.getStackInSlot(i));
    }

    RecipeInput recipeInput = new RecipeWrapper(input);
    RecipeManager rm = level.getRecipeManager();

    Optional<RecipeHolder<?>> result = rm.getRecipeFor(ModRecipeTypes.ALLOY_SMELTING.get(), recipeInput, level).map(r -> r);
    if (result.isEmpty()) {
      result = rm.getRecipeFor(ModRecipeTypes.SHAPED_ALLOY_SMELTING.get(), recipeInput, level).map(r -> r);
    }
    return result;
  }

  public IItemHandler getItemHandler() {
    return itemHandler;
  }

  // --------------------------------------------------
  // Smelting logic
  // --------------------------------------------------
  private boolean canSmelt() {
    Optional<RecipeHolder<?>> recipe = getCurrentRecipe();
    if (recipe.isEmpty()) return false;

    ItemStack result;
    if (recipe.get().value() instanceof AlloySmeltingRecipe shapeless) {
      cookTimeTotal = shapeless.getCookingTime();
      result = shapeless.getResultItem();
    } else if (recipe.get().value() instanceof ShapedAlloySmeltingRecipe shaped) {
      cookTimeTotal = shaped.getCookingTime();
      result = shaped.getResultItem();
    } else return false;

    ItemStack output = itemHandler.getStackInSlot(5);
    return (output.isEmpty() || (output.is(result.getItem()) &&
                    output.getCount() + result.getCount() <= output.getMaxStackSize()));
  }

  private void smelt() {
    if (!canSmelt()) return;

    Optional<RecipeHolder<?>> recipe = getCurrentRecipe();
    if (recipe.isEmpty()) return;

    ItemStack result;
    float xp;

    if (recipe.get().value() instanceof AlloySmeltingRecipe shapeless) {
      result = shapeless.getResultItem();
      xp = shapeless.getExperience();
    } else if (recipe.get().value() instanceof ShapedAlloySmeltingRecipe shaped) {
      result = shaped.getResultItem();
      xp = shaped.getExperience();
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

    if (level != null && !level.isClientSide && xp > 0.0F) {
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

  @Override
  protected NonNullList<ItemStack> getItems() {
    return inventory;
  }

  @Override
  protected void setItems(NonNullList<ItemStack> items) {
    if (items.size() != itemHandler.getSlots()) return;

    for (int i = 0; i < itemHandler.getSlots(); i++) {
      itemHandler.setStackInSlot(i, items.get(i));
    }
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
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    ContainerHelper.saveAllItems(tag, this.inventory, registries);
    tag.putInt("burnTime", burnTime);
    tag.putInt("maxBurnTime", maxBurnTime);
    tag.putInt("cookTime", cookTime);
    tag.putInt("cookTimeTotal", cookTimeTotal);
    tag.putFloat("storedXp", storedExperience);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    ContainerHelper.loadAllItems(tag, this.inventory, registries);
    burnTime = tag.getInt("burnTime");
    maxBurnTime = tag.getInt("maxBurnTime");
    cookTime = tag.getInt("cookTime");
    cookTimeTotal = tag.getInt("cookTimeTotal");
    storedExperience = tag.getFloat("storedXp");
  }

  public void drops() {
    if (this.level == null) return;
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
    if (index == 4) return stack.getBurnTime(RecipeType.SMELTING) > 0;
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
    if (this.level != null && this.level.getBlockEntity(this.worldPosition) != this) return false;
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

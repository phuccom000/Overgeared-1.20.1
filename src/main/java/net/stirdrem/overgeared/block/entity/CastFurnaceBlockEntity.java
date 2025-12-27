package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
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
import net.stirdrem.overgeared.recipe.CastingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.screen.CastFurnaceMenu;
import net.stirdrem.overgeared.util.ConfigHelper;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class CastFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_CAST = 3;

    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private final LazyOptional<? extends IItemHandler>[] sidedHandlers =
            SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

    private int burnTime;
    private int maxBurnTime;
    private int cookTime;
    private int cookTimeTotal;
    private float storedExperience;

    private final ContainerData data = new ContainerData() {
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

    public CastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAST_FURNACE_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CastFurnaceBlockEntity be) {
        boolean wasLit = be.isLit();
        boolean dirty = false;

        if (be.burnTime > 0) be.burnTime--;

        ItemStack fuel = be.itemHandler.getStackInSlot(SLOT_FUEL);

        if (be.burnTime == 0 && be.canSmelt()) {
            be.maxBurnTime = be.burnTime = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
            if (be.burnTime > 0 && !fuel.isEmpty()) {
                Item remainder = fuel.getItem().getCraftingRemainingItem();
                fuel.shrink(1);
                if (fuel.isEmpty() && remainder != null)
                    be.itemHandler.setStackInSlot(SLOT_FUEL, new ItemStack(remainder));
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
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, be.isLit()), 3);
            dirty = true;
        }

        if (dirty) be.setChanged();
    }


    private boolean isLit() {
        return burnTime > 0;
    }

    private boolean canSmelt() {
        if (level == null) return false;

        SimpleContainer inv = new SimpleContainer(2);
        inv.setItem(0, itemHandler.getStackInSlot(SLOT_INPUT));
        inv.setItem(1, itemHandler.getStackInSlot(SLOT_CAST));

        Optional<CastingRecipe> recipeOpt =
                level.getRecipeManager().getRecipeFor(ModRecipeTypes.CASTING.get(), inv, level);

        if (recipeOpt.isEmpty()) return false;

        CastingRecipe recipe = recipeOpt.get();

        ItemStack previewOutput = buildResultStack(recipe);
        if (previewOutput.isEmpty()) return false;

        ItemStack outputSlot = itemHandler.getStackInSlot(SLOT_OUTPUT);

        cookTimeTotal = recipe.getCookingTime();

        // Empty output slot → OK
        if (outputSlot.isEmpty()) {
            return true;
        }

        // Must be EXACT same item + EXACT same NBT
        if (!ItemStack.isSameItemSameTags(outputSlot, previewOutput)) {
            return false;
        }

        // Must fit stack size
        return outputSlot.getCount() + previewOutput.getCount()
                <= outputSlot.getMaxStackSize();
    }

    private ItemStack buildResultStack(CastingRecipe recipe) {
        ItemStack output = recipe.getResultItem(level.registryAccess()).copy();

        ItemStack cast = itemHandler.getStackInSlot(SLOT_CAST);
        CompoundTag castTag = cast.getTag();
        CompoundTag outTag = output.getTag(); // DO NOT create yet

        // Transfer forging quality
        if (castTag != null && castTag.contains("Quality")) {
            String q = castTag.getString("Quality");
            if (!"none".equals(q)) {
                if (outTag == null) outTag = new CompoundTag();
                outTag.putString("ForgingQuality", q);
            }
        }

        // Polishing flag
        if (recipe.requiresPolishing()) {
            if (outTag == null) outTag = new CompoundTag();
            outTag.putBoolean("Polished", false);
        }

        // Heated flag (always)
        if (outTag == null) outTag = new CompoundTag();
        outTag.putBoolean("Heated", true);
        output.setTag(outTag);
        

        return output;
    }

    private void smelt() {
        if (!canSmelt()) return;

        SimpleContainer inv = new SimpleContainer(2);
        inv.setItem(0, itemHandler.getStackInSlot(SLOT_INPUT));
        inv.setItem(1, itemHandler.getStackInSlot(SLOT_CAST));
        ItemStack cast = itemHandler.getStackInSlot(SLOT_CAST);
        CompoundTag castTag = cast.getOrCreateTag();

        CastingRecipe recipe =
                level.getRecipeManager()
                        .getRecipeFor(ModRecipeTypes.CASTING.get(), inv, level)
                        .orElse(null);

        if (recipe == null) return;

        ItemStack result = recipe.getResultItem(level.registryAccess());
        float xp = recipe.getExperience();
        boolean needPolishing = recipe.requiresPolishing();


        ItemStack output = result.copy();
        CompoundTag outTag = output.getTag();

        // Transfer forging quality from cast
        if (castTag.contains("Quality")) {
            String q = castTag.getString("Quality");
            if (!q.equals("none")) {
                if (outTag == null) outTag = new CompoundTag();
                outTag.putString("ForgingQuality", q);
            }
        }
        // Polishing flag
        if (needPolishing) {
            if (outTag == null) outTag = new CompoundTag();
            outTag.putBoolean("Polished", false);
        }

        if (outTag == null) outTag = new CompoundTag();
        outTag.putBoolean("Heated", true);
        output.setTag(outTag);

        if (itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            itemHandler.setStackInSlot(SLOT_OUTPUT, output);
        } else {
            itemHandler.getStackInSlot(SLOT_OUTPUT).grow(1);
        }
        Map<String, Integer> availableMaterials =
                ConfigHelper.getMaterialValuesForItem(itemHandler.getStackInSlot(SLOT_INPUT));
        Map<String, Double> requiredMaterials = recipe.getRequiredMaterials();
        int itemconsumeamount = 1;
        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase();
            double needed = entry.getValue();
            double available = availableMaterials
                    .getOrDefault(material, (int) needed);

            itemconsumeamount = (int) Math.max(1, Math.ceil(needed / available));
        }

        itemHandler.getStackInSlot(SLOT_INPUT).shrink(itemconsumeamount);


        // Damage cast
        if (cast.isDamageableItem()) {
            cast.hurt(1, level.random, null);

            if (cast.getDamageValue() >= cast.getMaxDamage()) {
                itemHandler.setStackInSlot(SLOT_CAST, ItemStack.EMPTY);
            }
        }
        if (!level.isClientSide && xp > 0)
            storedExperience += xp;
    }

    private void spawnExperience(float xp) {
        if (level == null || level.isClientSide) return;

        int i = Mth.floor(xp);
        float f = xp - i;
        if (f > 0 && Math.random() < f) i++;

        while (i > 0) {
            int split = ExperienceOrb.getExperienceValue(i);
            i -= split;
            level.addFreshEntity(new ExperienceOrb(level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    split));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overgeared.casting_furnace");
    }

    @Override
    protected Component getDefaultName() {
        return null;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new CastFurnaceMenu(id, inv, this, data);
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return null;
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
        for (LazyOptional<? extends IItemHandler> h : sidedHandlers) h.invalidate();
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
        SimpleContainer inv = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++)
            inv.setItem(i, itemHandler.getStackInSlot(i));
        Containers.dropContents(level, worldPosition, inv);
        spawnExperience(storedExperience);
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

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) return new int[]{SLOT_INPUT, SLOT_CAST};
        if (side == Direction.DOWN) return new int[]{SLOT_OUTPUT};
        return new int[]{SLOT_FUEL};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == SLOT_OUTPUT) return false;
        if (slot == SLOT_FUEL) return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
        if (slot == SLOT_CAST) return stack.is(ModTags.Items.TOOL_CAST);
        return ConfigHelper.isValidMaterial(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == SLOT_OUTPUT;
    }

    @Override
    public int getContainerSize() {
        return itemHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = stack.split(amount);
        if (!result.isEmpty()) setChanged();
        return result;
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
        if (level == null) return false;
        if (level.getBlockEntity(worldPosition) != this) return false;

        return player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}

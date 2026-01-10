package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.recipe.CastingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;
import net.stirdrem.overgeared.util.ConfigHelper;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class CastingSmelterBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_CAST = 3;
    private static final int[] SLOTS_UP = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_SIDE = new int[]{SLOT_FUEL};
    private static final int[] SLOTS_DOWN = new int[]{SLOT_OUTPUT};

    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int burnTime;
    private int maxBurnTime;
    private int cookTime;
    private int cookTimeTotal;
    private float storedExperience;

    public CastingSmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAST_FURNACE_BE.get(), pos, state);
    }

    // ================= TICK =================

    public static void tick(Level level, BlockPos pos, BlockState state, CastingSmelterBlockEntity be) {
        boolean wasLit = be.isLit();
        boolean dirty = false;

        if (be.burnTime > 0) be.burnTime--;

        ItemStack fuel = be.itemHandler.getStackInSlot(SLOT_FUEL);

        if (be.burnTime == 0 && be.canSmelt()) {
            be.maxBurnTime = be.burnTime = fuel.getBurnTime(RecipeType.SMELTING);
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

    // ================= RECIPE =================
    public Optional<RecipeHolder<CastingRecipe>> getCurrentRecipe() {
        if (level == null) return Optional.empty();

        // Build recipe input layout expected by CastingRecipe
        ItemStackHandler recipeInput = new ItemStackHandler(4);

        recipeInput.setStackInSlot(0, itemHandler.getStackInSlot(SLOT_INPUT)); // material
        recipeInput.setStackInSlot(1, ItemStack.EMPTY);                       // unused
        recipeInput.setStackInSlot(2, ItemStack.EMPTY);                       // unused
        recipeInput.setStackInSlot(3, itemHandler.getStackInSlot(SLOT_CAST)); // cast

        RecipeInput wrapper = new RecipeWrapper(recipeInput);

        return level.getRecipeManager().getRecipeFor(
                ModRecipeTypes.CASTING.get(),
                wrapper,
                level
        );
    }

    private boolean canSmelt() {
        Optional<RecipeHolder<CastingRecipe>> recipeOpt = getCurrentRecipe();
        if (recipeOpt.isEmpty()) return false;

        CastingRecipe recipe = recipeOpt.get().value();
        ItemStack previewOutput = buildResultStack(recipe);
        if (previewOutput.isEmpty()) return false;

        ItemStack outputSlot = itemHandler.getStackInSlot(SLOT_OUTPUT);

        cookTimeTotal = recipe.getCookingTime();

        if (outputSlot.isEmpty()) return true;

        if (!ItemStack.isSameItemSameComponents(outputSlot, previewOutput)) return false;

        return outputSlot.getCount() + previewOutput.getCount() <= outputSlot.getMaxStackSize();
    }


    private ItemStack buildResultStack(CastingRecipe recipe) {
        ItemStack output = recipe.getResultItem(level.registryAccess()).copy();
        ItemStack cast = itemHandler.getStackInSlot(SLOT_CAST);

        // Transfer forging quality
        if (cast.has(ModComponents.FORGING_QUALITY.get())) {
            output.set(ModComponents.FORGING_QUALITY.get(), cast.get(ModComponents.FORGING_QUALITY.get()));
        }

        // Polishing flag
        if (recipe.requiresPolishing()) {
            output.set(ModComponents.POLISHED.get(), false);
        }

        // Heated
        output.set(ModComponents.HEATED_COMPONENT.get(), true);

        return output;
    }

    private void smelt() {
        Optional<RecipeHolder<CastingRecipe>> recipeOpt = getCurrentRecipe();
        if (recipeOpt.isEmpty()) return;

        CastingRecipe recipe = recipeOpt.get().value();
        ItemStack output = buildResultStack(recipe);

        if (itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            itemHandler.setStackInSlot(SLOT_OUTPUT, output);
        } else {
            itemHandler.getStackInSlot(SLOT_OUTPUT).grow(output.getCount());
        }

        // Consume materials
        Map<String, Integer> availableMaterials =
                ConfigHelper.getMaterialValuesForItem(itemHandler.getStackInSlot(SLOT_INPUT));
        Map<String, Double> requiredMaterials = recipe.getRequiredMaterials();

        int itemConsumeAmount = 1;
        for (var entry : requiredMaterials.entrySet()) {
            String material = entry.getKey().toLowerCase();
            double needed = entry.getValue();
            double available = availableMaterials.getOrDefault(material, 1);
            itemConsumeAmount = (int) Math.max(1, Math.ceil(needed / available));
        }

        itemHandler.getStackInSlot(SLOT_INPUT).shrink(itemConsumeAmount);

        // Damage cast
        ItemStack cast = itemHandler.getStackInSlot(SLOT_CAST);
        if (cast.isDamageableItem()) {
            cast.setDamageValue(cast.getDamageValue() + 1);
            if (cast.getDamageValue() >= cast.getMaxDamage()) {
                itemHandler.setStackInSlot(SLOT_CAST, ItemStack.EMPTY);
            }
        }

        // XP
        if (!level.isClientSide && recipe.getExperience() > 0) {
            storedExperience += recipe.getExperience();
        }
    }


    // ================= XP =================

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

    public void awardStoredExperience(Player player) {
        if (this.level == null || this.level.isClientSide) return;
        if (storedExperience > 0 && player != null) {
            int total = (int) storedExperience;
            float fractional = storedExperience - total;
            if (fractional > 0.0F && Math.random() < fractional) total++;

            player.giveExperiencePoints(total);

            this.level.playSound(
                    null,
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

    // ================= CONTAINER =================

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

    // ================= MENU =================

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.overgeared.casting_furnace");
    }

    @Override
    protected Component getDefaultName() {
        return null;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return null;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {

    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        //return new CastingSmelterMenu(id, inv, this);
        return null;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return null;
    }

    // ================= CAPS =================

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return SLOTS_DOWN;
        }
        if (side == Direction.UP) {
            return SLOTS_UP;
        }
        return SLOTS_SIDE;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        if (direction == null) return false;

        // Prevent inserting into output or cast slot
        if (slot == SLOT_OUTPUT || slot == SLOT_CAST) return false;

        // Fuel slot
        if (slot == SLOT_FUEL) {
            return stack.getBurnTime(RecipeType.SMELTING) > 0;
        }

        // Input slot
        if (slot == SLOT_INPUT) {
            return ConfigHelper.isValidMaterial(stack);
        }

        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        // Only extract from output slot (bottom)
        return direction == Direction.DOWN && slot == SLOT_OUTPUT;
    }


    public void drops() {
        SimpleContainer simpleContainer = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            simpleContainer.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, simpleContainer);
        spawnExperience(storedExperience);
    }
}

package net.stirdrem.overgeared.screen;

import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.FletchingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class FletchingStationMenu extends AbstractContainerMenu {
    private static final int INPUT_SLOT_TIP = 0;
    private static final int INPUT_SLOT_SHAFT = 1;
    private static final int INPUT_SLOT_FEATHER = 2;
    private static final int INPUT_SLOT_POTION = 3;
    private static final int OUTPUT_SLOT = 4;
    private static final int PLAYER_INVENTORY_START = 5;
    private static final int PLAYER_INVENTORY_END = 32;
    private static final int PLAYER_HOTBAR_START = 33;
    private static final int PLAYER_HOTBAR_END = 40;

    private final Level level;
    private final ContainerLevelAccess access;
    private final Container input;
    private final ResultContainer result = new ResultContainer();
    private final RecipeManager recipeManager;
    private final Player player;

    public FletchingStationMenu(int id, Inventory playerInv) {
        this(id, playerInv, ContainerLevelAccess.NULL);
    }

    public FletchingStationMenu(int id, Inventory playerInv, ContainerLevelAccess access) {
        super(ModMenuTypes.FLETCHING_STATION_MENU.get(), id);
        this.access = access;
        this.recipeManager = playerInv.player.level().getRecipeManager();
        this.player = playerInv.player;
        this.level = playerInv.player.level();
        this.input = new SimpleContainer(4) {
            @Override
            public void setItem(int i, ItemStack stack) {
                super.setItem(i, stack);
                FletchingStationMenu.this.slotsChanged(this);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                FletchingStationMenu.this.slotsChanged(this);
            }
        };

        // Input slots
        addSlot(new Slot(input, INPUT_SLOT_TIP, 66, 17) {
            @Override
            public void onTake(Player pPlayer, ItemStack pStack) {
                updateResultSlot();
                super.onTake(pPlayer, pStack);
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }
        });
        addSlot(new Slot(input, INPUT_SLOT_SHAFT, 48, 35) {
            @Override
            public void onTake(Player pPlayer, ItemStack pStack) {
                updateResultSlot();
                super.onTake(pPlayer, pStack);
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }
        });
        addSlot(new Slot(input, INPUT_SLOT_FEATHER, 30, 53) {
            @Override
            public void onTake(Player pPlayer, ItemStack pStack) {
                updateResultSlot();
                super.onTake(pPlayer, pStack);
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }
        });

        addSlot(new Slot(input, INPUT_SLOT_POTION, 92, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isPotion(stack);
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }

            @Override
            public int getMaxStackSize() {
                return 1; // Maximum stack size of 1
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return 1; // Maximum stack size of 1 for any item
            }
        });

        // Output slot
        addSlot(new Slot(result, OUTPUT_SLOT, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return true;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                consumeInputs(stack);
                super.onTake(player, stack);
            }


        });

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    private boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Blocks.FLETCHING_TABLE);
    }

    @Override
    public void slotsChanged(Container container) {
        updateResultSlot();
        super.slotsChanged(container);
    }

    private boolean isUpgradeableArrow(ItemStack stack) {
        return stack.is(ModItems.IRON_UPGRADE_ARROW.get())
                || stack.is(ModItems.STEEL_UPGRADE_ARROW.get())
                || stack.is(ModItems.DIAMOND_UPGRADE_ARROW.get());
    }

    /**
     * Creates a RecipeInput wrapper from the input container for recipe matching
     */
    private RecipeInput createRecipeInput() {
        return new RecipeInput() {
            @Override
            public ItemStack getItem(int slot) {
                return input.getItem(slot);
            }

            @Override
            public int size() {
                return input.getContainerSize();
            }
        };
    }


    private void updateResultSlot() {
        // If all input slots are empty, clear the output slot
        if (level.isClientSide()) return;

        boolean hasInput = false;
        for (int i = 0; i < 3; i++) {
            if (!input.getItem(i).isEmpty()) {
                hasInput = true;
                break;
            }
        }
        if (!hasInput) {
            result.setItem(OUTPUT_SLOT, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }
        RecipeInput recipeInput = createRecipeInput();
        Optional<RecipeHolder<FletchingRecipe>> optHolder = recipeManager.getRecipeFor(ModRecipeTypes.FLETCHING.get(), recipeInput, level);
        ItemStack resultStack = ItemStack.EMPTY;
        ItemStack potion = input.getItem(INPUT_SLOT_POTION);
        // Check for arrow stack + potion conversion case
        boolean allowUpgradeableArrowConversion = ServerConfig.UPGRADE_ARROW_POTION_TOGGLE.get();
        if (!potion.isEmpty()) {
            int arrowSlots = 0;
            int arrowCount = 0;
            int slotNumber = -1;
            // Count how many slots contain arrows
            for (int i = 0; i < 3; i++) {
                ItemStack slotStack = input.getItem(i);
                if (slotStack.is(Items.ARROW) || (allowUpgradeableArrowConversion && isUpgradeableArrow(slotStack))) {
                    arrowSlots++;
                    arrowCount = slotStack.getCount();
                    slotNumber = i;
                }
            }

            // If exactly one slot has arrows (regardless of stack size)
            if (arrowSlots == 1) {
                ItemStack arrowStack = input.getItem(slotNumber);
                boolean isUpgradeable = isUpgradeableArrow(arrowStack);

                // Skip if trying to convert upgradeable arrow when not allowed
                if (isUpgradeable && !allowUpgradeableArrowConversion) {
                    result.setItem(OUTPUT_SLOT, ItemStack.EMPTY);
                    broadcastChanges();
                    return;
                }
                if (potion.is(Items.POTION)) {
                    ItemStack tippedArrows;
                    if (isUpgradeableArrow(input.getItem(slotNumber)))
                        tippedArrows = input.getItem(slotNumber).copy();
                    else tippedArrows = new ItemStack(Items.TIPPED_ARROW, arrowCount);
                    PotionContents potionContents = potion.get(DataComponents.POTION_CONTENTS);
                    if (potionContents != null) {
                        tippedArrows.set(DataComponents.POTION_CONTENTS, potionContents);
                    }
                    resultStack = tippedArrows;
                } else if (potion.is(Items.LINGERING_POTION)) {
                    ItemStack lingeringArrows;
                    if (isUpgradeableArrow(input.getItem(slotNumber))) {
                        lingeringArrows = input.getItem(slotNumber).copy();
                    } else {
                        lingeringArrows = new ItemStack(ModItems.LINGERING_ARROW.get(), arrowCount);
                    }
                    PotionContents potionContents = potion.get(DataComponents.POTION_CONTENTS);
                    if (potionContents != null) {
                        lingeringArrows.set(DataComponents.POTION_CONTENTS, potionContents);
                    }
                    if (isUpgradeableArrow(input.getItem(slotNumber))) {
                        lingeringArrows.set(ModComponents.LINGERING_STATUS, true);
                    }
                    resultStack = lingeringArrows;
                }
            }
        }

        if (optHolder.isPresent()) {
            FletchingRecipe recipe = optHolder.get().value();

            int tipCount = input.getItem(INPUT_SLOT_TIP).getCount();
            int shaftCount = input.getItem(INPUT_SLOT_SHAFT).getCount();
            int featherCount = input.getItem(INPUT_SLOT_FEATHER).getCount();

            int craftCount = Math.max(Math.min(Math.min(tipCount, shaftCount), featherCount), 1);
            ItemStack baseResult = recipe.assemble(recipeInput, level.registryAccess());

            if (!potion.isEmpty()) {
                boolean isUpgradeable = isUpgradeableArrow(baseResult);
                if ((isUpgradeable && !allowUpgradeableArrowConversion)) {
                    result.setItem(OUTPUT_SLOT, ItemStack.EMPTY);
                    broadcastChanges();
                    return;
                }
                PotionContents potionContents = potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

                if ((potion.is(Items.POTION) || potion.is(Items.SPLASH_POTION)) && !recipe.getTippedResult().isEmpty()) {
                    resultStack = recipe.getTippedResult().copy();
                    // Transfer potion contents to result
                    if (potionContents != PotionContents.EMPTY) {
                        resultStack.set(DataComponents.POTION_CONTENTS, potionContents);
                    }

                } else if (potion.is(Items.LINGERING_POTION) && !recipe.getLingeringResult().isEmpty()) {
                    resultStack = recipe.getLingeringResult().copy();

                    if (isUpgradeableArrow(resultStack)) {
                        resultStack.set(ModComponents.LINGERING_STATUS, true);
                    }

                    // Transfer potion contents to result
                    if (potionContents != PotionContents.EMPTY) {
                        resultStack.set(DataComponents.POTION_CONTENTS, potionContents);
                    }
                } else {
                    result.setItem(OUTPUT_SLOT, ItemStack.EMPTY);
                    broadcastChanges();
                    return;
                }
            } else {
                resultStack = baseResult.copy();
            }

            if (!resultStack.isEmpty()) {
                int outPer = baseResult.getCount();
                int maxStack = resultStack.getMaxStackSize();
                int maxCraftCount = Math.min(maxStack / outPer, craftCount);
                resultStack.setCount(outPer * maxCraftCount);
            }
        }
        result.setItem(OUTPUT_SLOT, resultStack);
        broadcastChanges();
    }

    private void consumeInputs(ItemStack result) {
        if (result.isEmpty()) return;

        RecipeInput recipeInput = createRecipeInput();
        Optional<RecipeHolder<FletchingRecipe>> optHolder = recipeManager.getRecipeFor(ModRecipeTypes.FLETCHING.get(), recipeInput, level);
        if (optHolder.isPresent()) {
            FletchingRecipe recipe = optHolder.get().value();
            ItemStack baseResult = recipe.assemble(recipeInput, level.registryAccess());
            int baseCount = baseResult.getCount();
            int tookCount = result.getCount();
            int batchesTaken = Math.max(1, tookCount / baseCount);

            // Consume input items
            for (int i = 0; i < 3; i++) {
                ItemStack stack = input.getItem(i);
                if (!stack.isEmpty()) {
                    stack.shrink(batchesTaken);
                    input.setItem(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
                }
            }

            // Consume potion if used

        } else {
            int tookCount = result.getCount();
            for (int i = 0; i < 3; i++) {
                ItemStack stack = input.getItem(i);
                if (!stack.isEmpty()) {
                    stack.shrink(tookCount);
                    input.setItem(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
                }
            }
        }
        ItemStack potionStack = input.getItem(INPUT_SLOT_POTION);
        if (!potionStack.isEmpty()) {
            potionStack.shrink(1);
            input.setItem(INPUT_SLOT_POTION, potionStack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : potionStack);
        }
        updateResultSlot();
    }


    private ItemStack applyPotionEffects(ItemStack result, ItemStack potion) {
        PotionContents potionContents = potion.get(DataComponents.POTION_CONTENTS);
        if (result.is(Items.ARROW)) {
            ItemStack tippedArrow = new ItemStack(Items.TIPPED_ARROW, result.getCount());
            if (potionContents != null) {
                tippedArrow.set(DataComponents.POTION_CONTENTS, potionContents);
            }
            return tippedArrow;
        } else if (result.is(ModItems.LINGERING_ARROW.get())) {
            if (potionContents != null) {
                result.set(DataComponents.POTION_CONTENTS, potionContents);
            }
            return result;
        }
        return result;
    }


    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copiedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            copiedStack = slotStack.copy();

            // Handling output slot
            if (index == OUTPUT_SLOT) {
                while (canStillCraft()) {
                    ItemStack result = slot.getItem().copy();
                    int maxTransfer = result.getMaxStackSize();

                    // Adjust count if larger than what's actually available
                    int craftCount = Math.min(result.getCount(), maxTransfer);
                    result.setCount(craftCount);

                    // Try to move result to player inventory
                    if (!moveItemStackTo(result, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END + 1, true)) {
                        break; // Stop if inventory is full
                    }
                    slot.onQuickCraft(result, copiedStack);
                    consumeInputs(copiedStack); // Consume only once per iteration

                    if (slot.getItem().isEmpty()) break; // Stop if no more result
                }

                slot.setChanged();
            }

            // Moving from player inventory to input slots
            else if (index >= PLAYER_INVENTORY_START && index <= PLAYER_HOTBAR_END) {
                // Handle potions separately
                if (isPotion(slotStack)) {
                    if (!moveItemStackTo(slotStack, INPUT_SLOT_POTION, INPUT_SLOT_POTION + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // Try to move to any input slot
                else if (!moveItemStackTo(slotStack, INPUT_SLOT_TIP, INPUT_SLOT_POTION, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Moving from input slots to player inventory
            else if (index >= INPUT_SLOT_TIP && index <= INPUT_SLOT_POTION) {
                if (!moveItemStackTo(slotStack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == copiedStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return copiedStack;
    }

    private boolean canStillCraft() {
        RecipeInput recipeInput = createRecipeInput();
        Optional<RecipeHolder<FletchingRecipe>> optHolder = recipeManager.getRecipeFor(ModRecipeTypes.FLETCHING.get(), recipeInput, level);
        return optHolder.isPresent();
    }

    private boolean simulateInsertIntoPlayerInventory(ItemStack stack) {
        for (int i = PLAYER_INVENTORY_START; i <= PLAYER_HOTBAR_END; i++) {
            Slot slot = this.slots.get(i);
            ItemStack existing = slot.getItem();

            // Empty slot — can insert
            if (existing.isEmpty()) return true;

            // Stackable and space remains
            if (ItemStack.isSameItemSameComponents(stack, existing) && existing.getCount() < existing.getMaxStackSize()) {
                return true;
            }
        }
        return false; // No space found
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.access != ContainerLevelAccess.NULL)
            for (int i = 0; i < input.getContainerSize(); i++) {
                ItemStack stack = input.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                }
            }
    }


    public static Holder<Potion> getPotion(@Nullable PotionContents potionContents) {
        if (potionContents == null || potionContents == PotionContents.EMPTY) {
            return Potions.WATER;
        }
        return potionContents.potion().orElse(Potions.WATER);
    }

    public static Holder<Potion> getPotionByName(String name) {
        if (name == null || name.isEmpty()) {
            return Potions.WATER;
        }
        ResourceLocation location = ResourceLocation.parse(name);
        Potion potion = BuiltInRegistries.POTION.get(location);
        if (potion == null) {
            return Potions.WATER;
        }
        return BuiltInRegistries.POTION.wrapAsHolder(potion);
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable PotionContents potionContents) {
        List<MobEffectInstance> list = Lists.newArrayList();
        if (potionContents != null) {
            potionContents.getAllEffects().forEach(list::add);
        }
        return list;
    }
}
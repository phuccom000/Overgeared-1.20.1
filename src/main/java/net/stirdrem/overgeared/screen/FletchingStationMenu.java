package net.stirdrem.overgeared.screen;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
        Optional<FletchingRecipe> opt = recipeManager.getRecipeFor(ModRecipeTypes.FLETCHING.get(), input, level);
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
                    PotionUtils.setPotion(tippedArrows, PotionUtils.getPotion(potion));
                    if (potion.hasTag()) {
                        tippedArrows.setTag(potion.getTag().copy());
                    }
                    resultStack = tippedArrows;
                } else if (potion.is(Items.LINGERING_POTION)) {
                    ItemStack lingeringArrows;
                    if (isUpgradeableArrow(input.getItem(slotNumber))) {
                        lingeringArrows = input.getItem(slotNumber).copy();
                    } else {
                        lingeringArrows = new ItemStack(ModItems.LINGERING_ARROW.get(), arrowCount);
                    }
                    PotionUtils.setPotion(lingeringArrows, PotionUtils.getPotion(potion));
                    if (potion.hasTag()) {
                        lingeringArrows.setTag(potion.getTag().copy());
                    }
                    if (isUpgradeableArrow(input.getItem(slotNumber))) {
                        lingeringArrows.getOrCreateTag().putBoolean("LingeringPotion", true);
                    }
                    resultStack = lingeringArrows;
                }
            }
        }

        if (opt.isPresent()) {
            FletchingRecipe recipe = opt.get();

            int tipCount = input.getItem(INPUT_SLOT_TIP).getCount();
            int shaftCount = input.getItem(INPUT_SLOT_SHAFT).getCount();
            int featherCount = input.getItem(INPUT_SLOT_FEATHER).getCount();

            int craftCount = Math.max(Math.min(Math.min(tipCount, shaftCount), featherCount), 1);
            ItemStack baseResult = recipe.assemble(input, level.registryAccess());

            if (!potion.isEmpty()) {
                boolean isUpgradeable = isUpgradeableArrow(baseResult);
                if ((isUpgradeable && !allowUpgradeableArrowConversion)) {
                    result.setItem(OUTPUT_SLOT, ItemStack.EMPTY);
                    broadcastChanges();
                    return;
                }
                String potionEffect = PotionUtils.getPotion(potion).getName(""); // Gets "minecraft:strong_strength" etc

                if ((potion.is(Items.POTION) || potion.is(Items.SPLASH_POTION)) && !recipe.getTippedResult().isEmpty()) {
                    resultStack = recipe.getTippedResult().copy();
                    CompoundTag resultTag = resultStack.getOrCreateTag();
                    /*if (recipe.getTippedTag() != null) {
                        // Add recipe-defined tipped tag without replacing everything
                        resultTag.putString(recipe.getTippedTag(), potionEffect);
                    }*/
                    if (potion.hasTag()) {
                        resultTag.merge(potion.getTag().copy());
                    }
                    resultStack.setTag(potion.getTag().copy());

                } else if (potion.is(Items.LINGERING_POTION) && !recipe.getLingeringResult().isEmpty()) {
                    resultStack = recipe.getLingeringResult().copy();

                    CompoundTag resultTag = resultStack.getOrCreateTag();

                    if (isUpgradeableArrow(resultStack)) {
                        resultTag.putBoolean("LingeringPotion", true);
                    } else if (recipe.getLingeringTag() != null) {
                        // Add recipe-defined lingering tag without replacing everything
                        resultTag.putString(recipe.getLingeringTag(), potionEffect);
                    }

                    // Always set potion effect onto the stack
                    //PotionUtils.setPotion(resultStack, PotionUtils.getPotion(potion));

                    // Merge potion’s tag (if present) instead of overwriting
                    if (potion.hasTag()) {
                        resultTag.merge(potion.getTag().copy());
                    }

                    resultStack.setTag(resultTag);
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

        Optional<FletchingRecipe> opt = recipeManager.getRecipeFor(ModRecipeTypes.FLETCHING.get(), input, level);
        if (opt.isPresent()) {
            FletchingRecipe recipe = opt.get();
            ItemStack baseResult = recipe.assemble(input, level.registryAccess());
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
        if (result.is(Items.ARROW)) {
            ItemStack tippedArrow = new ItemStack(Items.TIPPED_ARROW, result.getCount());
            PotionUtils.setPotion(tippedArrow, PotionUtils.getPotion(potion));
            tippedArrow.setTag(potion.getTag());
            return tippedArrow;
        } else if (result.is(ModItems.LINGERING_ARROW.get())) {
            PotionUtils.setPotion(result, PotionUtils.getPotion(potion));
            result.setTag(potion.getTag());
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
        Optional<FletchingRecipe> opt = recipeManager.getRecipeFor(ModRecipeTypes.FLETCHING.get(), input, level);
        return opt.isPresent();
    }

    private boolean simulateInsertIntoPlayerInventory(ItemStack stack) {
        for (int i = PLAYER_INVENTORY_START; i <= PLAYER_HOTBAR_END; i++) {
            Slot slot = this.slots.get(i);
            ItemStack existing = slot.getItem();

            // Empty slot — can insert
            if (existing.isEmpty()) return true;

            // Stackable and space remains
            if (ItemStack.isSameItemSameTags(stack, existing) && existing.getCount() < existing.getMaxStackSize()) {
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


    public static Potion getPotion(@Nullable CompoundTag tag) {
        if (tag == null) return Potions.EMPTY;

        if (tag.contains("Potion", 8)) {
            return Potion.byName(tag.getString("Potion"));
        }

        return Potions.EMPTY;
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag pCompoundTag) {
        List<MobEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(pCompoundTag).getEffects());
        getCustomEffects(pCompoundTag, list);
        return list;
    }

    public static void getCustomEffects(@Nullable CompoundTag pCompoundTag, List<MobEffectInstance> pEffectList) {
        if (pCompoundTag != null && pCompoundTag.contains("CustomPotionEffects", 9)) {
            ListTag listtag = pCompoundTag.getList("CustomPotionEffects", 10);

            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag);
                if (mobeffectinstance != null) {
                    pEffectList.add(mobeffectinstance);
                }
            }
        }

    }
}
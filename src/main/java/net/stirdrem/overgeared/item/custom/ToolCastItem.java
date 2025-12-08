package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.util.ConfigHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ToolCastItem extends Item {
    private final Supplier<Integer> durabilitySupplier;
    private final boolean allowMaterialInsert;

    // Fired/Nether cast (damageable) — can choose if it accepts materials
    public ToolCastItem(Supplier<Integer> durabilitySupplier, boolean allowMaterialInsert, Properties props) {
        super(props);
        this.durabilitySupplier = durabilitySupplier;
        this.allowMaterialInsert = allowMaterialInsert;
    }

    // Unfired cast (non-damageable) — usually should ALLOW insert
    public ToolCastItem(boolean allowMaterialInsert, Properties props) {
        super(props);
        this.durabilitySupplier = null;
        this.allowMaterialInsert = allowMaterialInsert;
    }


    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false; // No enchanting table
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false; // No enchanted books
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0; // Enchantability level for fishing/looting
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player.isCrouching()) {
            return calculateAndReturnMaterials(stack, player);
        }

        return super.use(level, player, hand);
    }

    private InteractionResultHolder<ItemStack> calculateAndReturnMaterials(ItemStack castStack, Player player) {
        CompoundTag tag = castStack.getTag();
        if (tag == null) {
            player.displayClientMessage(Component.translatable("message.overgeared.cast_empty"), true);
            return InteractionResultHolder.fail(castStack);
        }

        // Check if we have input items to return
        List<ItemStack> inputItems = getInputItemsFromCast(castStack);
        if (inputItems.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.overgeared.cast_empty"), true);
            return InteractionResultHolder.fail(castStack);
        }

        boolean returnedAny = false;

        // Return all the original input items with their NBT
        for (ItemStack inputItem : inputItems) {
            if (!inputItem.isEmpty()) {
                // Add to player inventory or drop if full
                if (!player.getInventory().add(inputItem.copy())) {
                    player.drop(inputItem.copy(), false);
                }
                returnedAny = true;
            }
        }

        if (returnedAny) {
            // Clear all data from the cast
            tag.put("Materials", new CompoundTag()); // Empty compound instead of remove
            tag.putInt("Amount", 0);
            tag.remove("input");

            player.displayClientMessage(Component.translatable("message.overgeared.materials_returned"), true);
            return InteractionResultHolder.sidedSuccess(castStack, player.level().isClientSide());
        } else {
            player.displayClientMessage(Component.translatable("message.overgeared.no_materials"), true);
            return InteractionResultHolder.fail(castStack);
        }
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack castStack, Slot slot, ClickAction action, Player player) {
        if (!allowMaterialInsert) return false;
        if (action != ClickAction.SECONDARY) return false;
        if (!slot.allowModification(player)) return false;

        ItemStack slotStack = slot.getItem();
        if (slotStack.isEmpty()) return false;

        // Try insert material into cast
        if (insertMaterial(castStack, slotStack, player)) {
            slot.remove(1); // remove one item from slot
            return true;
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack castStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess slotAccess) {
        if (!allowMaterialInsert) return false;
        if (action != ClickAction.SECONDARY) return false;

        // Try insert the held item (cursor stack) into the cast
        if (insertMaterial(castStack, otherStack, player)) {
            otherStack.shrink(1); // remove from cursor
            return true;
        }

        return false;
    }

    private boolean insertMaterial(ItemStack cast, ItemStack material, Player player) {
        if (material.isEmpty()) return false;

        CompoundTag tag = cast.getOrCreateTag();
        ListTag list = tag.getList("input", Tag.TAG_COMPOUND);

        String itemId = BuiltInRegistries.ITEM.getKey(material.getItem()).toString();

        // Validate material type
        if (!ConfigHelper.isValidMaterial(material)) {
            player.displayClientMessage(Component.translatable("message.overgeared.invalid_material"), true);
            return false;
        }

        int value = ConfigHelper.getMaterialValue(material);
        if (value <= 0) return false;

        int amount = tag.getInt("Amount");
        int maxAmount = tag.contains("MaxAmount") ? tag.getInt("MaxAmount") : Integer.MAX_VALUE;

        // Block overfilling
        if (amount + value > maxAmount) {
            //player.displayClientMessage(Component.translatable("message.overgeared.cast_full"), true);
            return false;
        }

        // === Update Materials NBT correctly ===
        String mat = ConfigHelper.getMaterialForItem(material);
        CompoundTag mats = tag.contains("Materials") ? tag.getCompound("Materials") : new CompoundTag();

        int prev = mats.getInt(mat); // ✅ read using material key (mat)
        mats.putInt(mat, prev + value); // ✅ store using material key
        tag.put("Materials", mats);


        // === Try merge into existing input entry ===
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ItemStack entryStack = ItemStack.of(entry);

            if (ItemStack.isSameItemSameTags(entryStack, material)) {
                entryStack.grow(1);
                entryStack.save(entry);
                list.set(i, entry);

                tag.put("input", list);
                tag.putInt("Amount", amount + value);
                playInsertSound(player);
                return true;
            }
        }

        // === No merge found, add new entry ===
        ItemStack stored = material.copy();
        stored.setCount(1);
        list.add(stored.save(new CompoundTag()));

        tag.put("input", list);
        tag.putInt("Amount", amount + value);

        playInsertSound(player);
        return true;
    }


    private void playInsertSound(Player player) {
        player.level().playSound(
                player,
                player.blockPosition(),
                SoundEvents.BUNDLE_INSERT,
                SoundSource.PLAYERS,
                0.7F, 1.1F
        );
    }

    /**
     * Get all input items from the cast's "input" NBT list
     */
    private List<ItemStack> getInputItemsFromCast(ItemStack cast) {
        List<ItemStack> items = new ArrayList<>();
        CompoundTag tag = cast.getTag();

        if (tag != null && tag.contains("input", Tag.TAG_LIST)) {
            ListTag inputList = tag.getList("input", Tag.TAG_COMPOUND);

            for (Tag inputTag : inputList) {
                if (inputTag instanceof CompoundTag) {
                    ItemStack item = ItemStack.of((CompoundTag) inputTag);
                    if (!item.isEmpty()) {
                        items.add(item);
                    }
                }
            }
        }

        return items;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return durabilitySupplier != null;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return durabilitySupplier != null ? durabilitySupplier.get() : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        // === Quality ===
        if (tag.contains("Quality", Tag.TAG_STRING)) {
            String quality = tag.getString("Quality");
            ChatFormatting color = BlueprintQuality.getColor(quality);
            if (!quality.equals("NONE"))
                tooltip.add(
                        Component.translatable("tooltip.overgeared.tool_cast.quality")
                                .append(" ")
                                .append(
                                        Component.translatable("quality.overgeared." + quality.toLowerCase())
                                                .withStyle(color)
                                )
                                .withStyle(ChatFormatting.GRAY)
                );
        }

        // === Tool Type Display ===
        if (tag.contains("ToolType", Tag.TAG_STRING)) {
            String toolType = tag.getString("ToolType");
            tooltip.add(
                    Component.translatable("tooltip.overgeared.tool_cast.type")
                            .append(" ")
                            .append(Component.translatable("tooltype.overgeared." + toolType.toLowerCase()).withStyle(ChatFormatting.BLUE)
                            )
                            .withStyle(ChatFormatting.GRAY)
            );
        }

        // === Material List ===
        if (tag.contains("Materials", Tag.TAG_COMPOUND)) {
            CompoundTag materials = tag.getCompound("Materials");
            if (!materials.isEmpty()) {
                tooltip.add(
                        Component.translatable("tooltip.overgeared.tool_cast.materials")
                                .withStyle(ChatFormatting.GRAY)
                );

                for (String key : materials.getAllKeys()) {
                    int amount = materials.getInt(key);

                    Component display = Component.translatable("material.overgeared." + key.toLowerCase());
                    if (display.getString().equals("material.overgeared." + key.toLowerCase())) {
                        display = Component.literal(key);
                    }

                    tooltip.add(
                            Component.literal("  • ").append(display)
                                    .append(Component.literal(": " + amount))
                                    .withStyle(ChatFormatting.WHITE)
                    );
                }
            }
        }

        // === Amount ===
        if (tag.contains("Amount")) {
            int raw = tag.getInt("Amount");
            double amt = raw / 9.0;

            int maxRaw = tag.contains("MaxAmount") ? tag.getInt("MaxAmount") : raw;
            double maxAmt = maxRaw / 9.0;

            tooltip.add(
                    Component.translatable("tooltip.overgeared.tool_cast.amount")
                            .append(" ")
                            .append(
                                    Component.literal(String.format("%.2f", amt))
                                            .withStyle(ChatFormatting.YELLOW)
                            )
                            .append(" / ")
                            .append(
                                    Component.literal(String.format("%.2f", maxAmt))
                                            .withStyle(ChatFormatting.WHITE)
                            )
                            .withStyle(ChatFormatting.GRAY)
            );
            if (amt / maxAmt != 1)
                tooltip.add(
                        Component.translatable("tooltip.overgeared.add_materials")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
        }

        // Add usage hint (only if cast has materials)
        if ((tag.contains("Materials", Tag.TAG_COMPOUND) && !tag.getCompound("Materials").isEmpty()) ||
                (tag.contains("input", Tag.TAG_LIST) && !tag.getList("input", Tag.TAG_COMPOUND).isEmpty())) {
            tooltip.add(
                    Component.translatable("tooltip.overgeared.cast_right_click")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
            );
        }
    }

    // Helper class for counting items with same name and NBT
    private static class ItemCount {
        final ItemStack itemStack;
        final String displayName;
        int count;

        ItemCount(ItemStack itemStack, String displayName, int count) {
            this.itemStack = itemStack;
            this.displayName = displayName;
            this.count = count;
        }
    }
}
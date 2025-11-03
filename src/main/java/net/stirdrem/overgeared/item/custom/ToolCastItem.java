package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.CastingConfigHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ToolCastItem extends Item {
    private final Supplier<Integer> durabilitySupplier;

    public ToolCastItem(Supplier<Integer> durabilitySupplier, Properties props) {
        super(props);
        this.durabilitySupplier = durabilitySupplier;
    }

    // Unfired cast (non-damageable)
    public ToolCastItem(Properties props) {
        super(props);
        this.durabilitySupplier = null;
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

    /**
     * Legacy support: Fallback to material-based return if no input list exists
     */
    private InteractionResultHolder<ItemStack> calculateAndReturnMaterialsLegacy(ItemStack castStack, Player player) {
        CompoundTag tag = castStack.getTag();
        if (tag == null || !tag.contains("Materials", Tag.TAG_COMPOUND)) {
            player.displayClientMessage(Component.translatable("message.overgeared.cast_empty"), true);
            return InteractionResultHolder.fail(castStack);
        }

        CompoundTag materials = tag.getCompound("Materials");
        if (materials.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.overgeared.cast_empty"), true);
            return InteractionResultHolder.fail(castStack);
        }

        boolean returnedAny = false;

        for (String materialId : materials.getAllKeys()) {
            int amount = materials.getInt(materialId);
            if (amount <= 0) continue;

            // Calculate how many items to return based on material value
            ItemStack materialStack = createMaterialStack(materialId, amount);
            if (!materialStack.isEmpty()) {
                // Add to player inventory or drop if full
                if (!player.getInventory().add(materialStack)) {
                    player.drop(materialStack, false);
                }
                returnedAny = true;
            }
        }

        if (returnedAny) {
            // Clear the materials from the cast
            tag.remove("Materials");
            tag.remove("Amount");

            player.displayClientMessage(Component.translatable("message.overgeared.materials_returned"), true);
            return InteractionResultHolder.sidedSuccess(castStack, player.level().isClientSide());
        } else {
            player.displayClientMessage(Component.translatable("message.overgeared.no_materials"), true);
            return InteractionResultHolder.fail(castStack);
        }
    }

    private ItemStack createMaterialStack(String materialId, int totalValue) {
        // Find which item corresponds to this material
        String itemId = getItemIdForMaterial(materialId);
        if (itemId.equals("none")) {
            return ItemStack.EMPTY;
        }

        // Get the value per item of this material
        int valuePerItem = CastingConfigHelper.getMaterialValue(itemId);
        if (valuePerItem <= 0) {
            return ItemStack.EMPTY;
        }

        // Calculate how many items to return
        int itemCount = totalValue / valuePerItem;
        if (itemCount <= 0) {
            return ItemStack.EMPTY;
        }

        // Create the item stack
        ItemStack result = getItemStackFromId(itemId);
        if (!result.isEmpty()) {
            result.setCount(itemCount);
        }

        return result;
    }

    private String getItemIdForMaterial(String materialId) {
        // Find the first item that matches this material type
        for (var entry : ServerConfig.MATERIAL_SETTING.get()) {
            List<?> row = (List<?>) entry;
            if (row.size() >= 3 && materialId.equals(row.get(1))) {
                return (String) row.get(0);
            }
        }
        return "none";
    }

    private ItemStack getItemStackFromId(String itemId) {
        try {
            return new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(itemId)));
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
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
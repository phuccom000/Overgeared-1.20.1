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
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ConfigHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ToolCastItem extends Item {
    private final boolean allowMaterialInsert;
    private final boolean haveDurability;

    public ToolCastItem(boolean allowMaterialInsert, boolean haveDurability, Properties props) {
        super(props);
        this.allowMaterialInsert = allowMaterialInsert;
        this.haveDurability = haveDurability;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getTag();

        if (!level.isClientSide) {
            return calculateAndReturnMaterials(stack, player, hand);
        }

        return super.use(level, player, hand);
    }

    private InteractionResultHolder<ItemStack> calculateAndReturnMaterials(ItemStack castStack, Player player, InteractionHand hand) {
        CompoundTag tag = castStack.getTag();
        if (tag == null) {
            return InteractionResultHolder.fail(castStack);
        }

        if (tag.contains("Output", Tag.TAG_COMPOUND)) {
            ItemStack output = ItemStack.of(tag.getCompound("Output"));

            if (!output.isEmpty()) {
                if (!player.getInventory().add(output.copy())) {
                    player.drop(output.copy(), false);
                }

                tag.remove("Output");
                tag.remove("Materials");
                tag.remove("input");
                tag.remove("Heated");
                tag.putInt("Amount", 0);

                player.level().playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS,
                        0.8F,
                        1.2F
                );
                ItemStack stack2 = castStack.copy();
                return InteractionResultHolder.sidedSuccess(
                        castStack,
                        player.level().isClientSide()
                );

            }
        }

        List<ItemStack> inputItems = getInputItemsFromCast(castStack);
        if (inputItems.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.overgeared.cast_empty"),
                    true
            );
            return InteractionResultHolder.fail(castStack);
        }

        for (ItemStack inputItem : inputItems) {
            if (!player.getInventory().add(inputItem.copy())) {
                player.drop(inputItem.copy(), false);
            }
        }

        tag.put("Materials", new CompoundTag());
        tag.putInt("Amount", 0);
        tag.remove("input");

        return InteractionResultHolder.sidedSuccess(
                castStack,
                player.level().isClientSide()
        );
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack castStack, Slot slot, ClickAction action, Player player) {
        if (!allowMaterialInsert) return false;
        if (action != ClickAction.SECONDARY) return false;
        if (!slot.allowModification(player)) return false;

        ItemStack slotStack = slot.getItem();
        if (slotStack.isEmpty()) return false;

        if (insertMaterial(castStack, slotStack, player)) {
            slot.remove(1);
            return true;
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack castStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess slotAccess) {
        if (!allowMaterialInsert) return false;
        if (action != ClickAction.SECONDARY) return false;

        if (insertMaterial(castStack, otherStack, player)) {
            otherStack.shrink(1);
            return true;
        }

        return false;
    }

    private boolean insertMaterial(ItemStack cast, ItemStack material, Player player) {
        if (cast.hasTag() && cast.getTag().contains("Output")) {
            return false;
        }
        if (material.isEmpty()) return false;

        CompoundTag tag = cast.getOrCreateTag();
        ListTag list = tag.getList("input", Tag.TAG_COMPOUND);

        String itemId = BuiltInRegistries.ITEM.getKey(material.getItem()).toString();

        if (!ConfigHelper.isValidMaterial(material)) {
            player.displayClientMessage(Component.translatable("message.overgeared.invalid_material"), true);
            return false;
        }

        int value = ConfigHelper.getMaterialValue(material);
        if (value <= 0) return false;

        int amount = tag.getInt("Amount");
        int maxAmount = tag.contains("MaxAmount") ? tag.getInt("MaxAmount") : Integer.MAX_VALUE;

        if (amount + value > maxAmount) {
            return false;
        }

        String mat = ConfigHelper.getMaterialForItem(material);
        CompoundTag mats = tag.contains("Materials") ? tag.getCompound("Materials") : new CompoundTag();

        int prev = mats.getInt(mat);
        mats.putInt(mat, prev + value);
        tag.put("Materials", mats);

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
        return haveDurability;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return haveDurability ? ServerConfig.FIRED_CAST_DURABILITY.get() : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag tag = stack.getTag();
        if (tag == null) return;

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
        if (tag.contains("Output", Tag.TAG_COMPOUND)) {
            ItemStack output = ItemStack.of(tag.getCompound("Output"));

            tooltip.add(
                    Component.translatable("tooltip.overgeared.tool_cast.contains")
                            .withStyle(ChatFormatting.GRAY)
            );

            tooltip.add(
                    Component.literal("  • ")
                            .append(output.getHoverName())
                            .withStyle(ChatFormatting.GOLD)
            );
        }
        if ((tag.contains("Materials", Tag.TAG_COMPOUND) && !tag.getCompound("Materials").isEmpty()) ||
                (tag.contains("input", Tag.TAG_LIST) && !tag.getList("input", Tag.TAG_COMPOUND).isEmpty()) ||
                tag.contains("Output", Tag.TAG_COMPOUND)) {
            tooltip.add(
                    Component.translatable("tooltip.overgeared.cast_right_click")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
            );
        }

    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }
}
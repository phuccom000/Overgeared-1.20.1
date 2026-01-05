package net.stirdrem.overgeared.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.util.ConfigHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (!level.isClientSide) {
            return calculateAndReturnMaterials(stack, player, hand);
        }

        return super.use(level, player, hand);
    }

    private InteractionResultHolder<ItemStack> calculateAndReturnMaterials(ItemStack castStack, Player player, InteractionHand hand) {
        CastData data = castStack.getOrDefault(ModComponents.CAST_DATA, CastData.EMPTY);

        if (data.hasOutput()) {
            ItemStack output = data.output();

            if (!player.getInventory().add(output.copy())) {
                player.drop(output.copy(), false);
            }

            castStack.set(ModComponents.CAST_DATA, data.cleared());

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS,
                    0.8F,
                    1.2F
            );
            return InteractionResultHolder.sidedSuccess(
                    castStack,
                    player.level().isClientSide()
            );
        }

        List<ItemStack> inputItems = data.input();
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

        castStack.set(ModComponents.CAST_DATA, data.withoutInputs());

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
        CastData data = cast.getOrDefault(ModComponents.CAST_DATA, CastData.EMPTY);
        
        if (data.hasOutput()) {
            return false;
        }
        if (material.isEmpty()) return false;

        if (!ConfigHelper.isValidMaterial(material)) {
            player.displayClientMessage(Component.translatable("message.overgeared.invalid_material"), true);
            return false;
        }

        int value = ConfigHelper.getMaterialValue(material);
        if (value <= 0) return false;

        int amount = data.amount();
        int maxAmount = data.maxAmount() > 0 ? data.maxAmount() : Integer.MAX_VALUE;

        if (amount + value > maxAmount) {
            return false;
        }

        String mat = ConfigHelper.getMaterialForItem(material);
        
        // Update materials map and add input
        ItemStack singleMaterial = material.copy();
        singleMaterial.setCount(1);
        
        CastData newData = data.withAddedMaterial(mat, value).withAddedInput(singleMaterial);
        cast.set(ModComponents.CAST_DATA, newData);

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
        CastData data = cast.getOrDefault(ModComponents.CAST_DATA, CastData.EMPTY);
        return data.input();
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        CastData data = stack.getOrDefault(ModComponents.CAST_DATA, CastData.EMPTY);

        if (!data.quality().isEmpty()) {
            String quality = data.quality();
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

        if (!data.toolType().isEmpty()) {
            String toolType = data.toolType();
            tooltip.add(
                    Component.translatable("tooltip.overgeared.tool_cast.type")
                            .append(" ")
                            .append(Component.translatable("tooltype.overgeared." + toolType.toLowerCase()).withStyle(ChatFormatting.BLUE)
                            )
                            .withStyle(ChatFormatting.GRAY)
            );
        }

        if (!data.materials().isEmpty()) {
            Map<String, Integer> materials = data.materials();
            tooltip.add(
                    Component.translatable("tooltip.overgeared.tool_cast.materials")
                            .withStyle(ChatFormatting.GRAY)
            );

            for (Map.Entry<String, Integer> entry : materials.entrySet()) {
                String key = entry.getKey();
                int amount = entry.getValue();

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

        if (data.amount() > 0 || data.maxAmount() > 0) {
            int raw = data.amount();
            double amt = raw / 9.0;

            int maxRaw = data.maxAmount() > 0 ? data.maxAmount() : raw;
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
        if (data.hasOutput()) {
            ItemStack output = data.output();

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
        if (!data.materials().isEmpty() || !data.input().isEmpty() || data.hasOutput()) {
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
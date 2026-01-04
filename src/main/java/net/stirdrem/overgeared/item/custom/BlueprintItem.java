package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;

import java.util.List;

public class BlueprintItem extends Item {

    public BlueprintItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) return;

        // Only show quality/progress if both tags are present
        if (customData.contains("Quality")) {
            BlueprintQuality quality = getQuality(stack);

            tooltip.add(Component.translatable("tooltip.overgeared.blueprint.quality")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(quality.getTranslationKey()).withStyle(quality.getColor())));

            if (quality == BlueprintQuality.PERFECT || quality == BlueprintQuality.MASTER) {
                tooltip.add(Component.translatable("tooltip.overgeared.blueprint.maxlevel")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }

        if (customData.contains("Uses")) {
            int uses = getUses(stack);
            int usesToLevel = getUsesToNextLevel(stack);

            if (!customData.contains("Quality") || (getQuality(stack) != BlueprintQuality.PERFECT && getQuality(stack) != BlueprintQuality.MASTER)) {
                tooltip.add(Component.translatable("tooltip.overgeared.blueprint.progress", uses, usesToLevel)
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        // ToolType line only if present
        if (customData.contains("ToolType")) {
            ToolType toolType = getToolType(stack);
            tooltip.add(Component.translatable("tooltip.overgeared.blueprint.tool_type").withStyle(ChatFormatting.GRAY)
                    .append(toolType.getDisplayName().withStyle(ChatFormatting.BLUE)));
        }

        if (customData.contains("Required")) {
            boolean required = customData.copyTag().getBoolean("Required");

            tooltip.add(Component.translatable(
                    required
                            ? "tooltip.overgeared.blueprint.required"
                            : "tooltip.overgeared.blueprint.optional"
            ).withStyle(required ? ChatFormatting.RED : ChatFormatting.GRAY));
        }
    }

    public static BlueprintQuality getQuality(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty() || !customData.contains("Quality")) {
            return BlueprintQuality.POOR; // Default to POOR if not set
        }
        try {
            return BlueprintQuality.fromString(customData.copyTag().getString("Quality"));
        } catch (IllegalArgumentException e) {
            return BlueprintQuality.POOR; // Default to POOR if invalid
        }
    }

    public static int getUses(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.isEmpty() ? 0 : customData.copyTag().getInt("Uses");
    }

    public static int getUsesToNextLevel(ItemStack stack) {
        return getUsesToNextLevel(getQuality(stack));
    }

    public static ToolType getToolType(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        if (customData.isEmpty() || !customData.contains("ToolType")) {
            List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
            return !types.isEmpty() ? types.get(0) : ToolType.SWORD;
        }

        String id = customData.copyTag().getString("ToolType");
        return ToolTypeRegistry.byId(id).orElse(ToolType.SWORD);
    }

    public static void setDefaultData(ItemStack stack) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData -> {
            var tag = customData.copyTag();

            // Set default quality to POOR
            tag.putString("Quality", BlueprintQuality.POOR.name());
            tag.putInt("Uses", 0);
            tag.putInt("UsesToLevel", getUsesToNextLevel(BlueprintQuality.POOR));

            // Set default tool type to first available or SWORD
            List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
            tag.putString("ToolType", !types.isEmpty() ? types.get(0).getId() : "SWORD");

            return CustomData.of(tag);
        });
    }

    public static void cycleToolType(ItemStack stack) {
        List<ToolType> available = ToolTypeRegistry.getRegisteredTypesAll();
        if (available.isEmpty()) return;

        ToolType current = getToolType(stack);
        int currentIndex = available.indexOf(current);
        int nextIndex = (currentIndex + 1) % available.size();

        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData -> {
            var tag = customData.copyTag();
            tag.putString("ToolType", available.get(nextIndex).getId());
            return CustomData.of(tag);
        });
    }

    private static int getUsesToNextLevel(BlueprintQuality quality) {
        return switch (quality) {
            case POOR -> BlueprintQuality.POOR.getUse();
            case WELL -> BlueprintQuality.WELL.getUse();
            case EXPERT -> BlueprintQuality.EXPERT.getUse();
            case PERFECT -> BlueprintQuality.PERFECT.getUse();
            case MASTER -> BlueprintQuality.MASTER.getUse();
        };
    }
}
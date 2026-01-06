package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
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

        BlueprintData data = stack.get(ModComponents.BLUEPRINT_DATA.get());
        if (data == null) return;

        // Show quality
        BlueprintQuality quality = data.getQualityEnum();
        tooltip.add(Component.translatable("tooltip.overgeared.blueprint.quality")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(quality.getTranslationKey()).withStyle(quality.getColor())));

        if (quality == BlueprintQuality.PERFECT || quality == BlueprintQuality.MASTER) {
            tooltip.add(Component.translatable("tooltip.overgeared.blueprint.maxlevel")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            // Show progress
            tooltip.add(Component.translatable("tooltip.overgeared.blueprint.progress", data.uses(), data.usesToLevel())
                    .withStyle(ChatFormatting.GRAY));
        }

        // Show tool type
        ToolType toolType = getToolType(stack);
        tooltip.add(Component.translatable("tooltip.overgeared.blueprint.tool_type").withStyle(ChatFormatting.GRAY)
                .append(toolType.getDisplayName().withStyle(ChatFormatting.BLUE)));

        // Show required status
        if (data.required()) {
            tooltip.add(Component.translatable("tooltip.overgeared.blueprint.required")
                    .withStyle(ChatFormatting.RED));
        }
    }

    public static BlueprintQuality getQuality(ItemStack stack) {
        BlueprintData data = stack.get(ModComponents.BLUEPRINT_DATA.get());
        if (data == null) return BlueprintQuality.POOR;
        return data.getQualityEnum();
    }

    public static int getUses(ItemStack stack) {
        BlueprintData data = stack.get(ModComponents.BLUEPRINT_DATA.get());
        return data == null ? 0 : data.uses();
    }

    public static int getUsesToNextLevel(ItemStack stack) {
        return getUsesToNextLevel(getQuality(stack));
    }

    public static ToolType getToolType(ItemStack stack) {
        BlueprintData data = stack.get(ModComponents.BLUEPRINT_DATA.get());
        if (data == null || data.toolType().isEmpty()) {
            List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
            return !types.isEmpty() ? types.getFirst() : ToolType.SWORD;
        }
        return ToolTypeRegistry.byId(data.toolType()).orElse(ToolType.SWORD);
    }

    public static void setDefaultData(ItemStack stack) {
        List<ToolType> types = ToolTypeRegistry.getRegisteredTypesAll();
        String defaultToolType = !types.isEmpty() ? types.getFirst().getId() : "sword";
        
        BlueprintData data = new BlueprintData(
                BlueprintQuality.POOR.name(),
                defaultToolType,
                0,
                getUsesToNextLevel(BlueprintQuality.POOR),
                false
        );
        stack.set(ModComponents.BLUEPRINT_DATA.get(), data);
    }

    public static void cycleToolType(ItemStack stack) {
        List<ToolType> available = ToolTypeRegistry.getRegisteredTypesAll();
        if (available.isEmpty()) return;

        ToolType current = getToolType(stack);
        int currentIndex = available.indexOf(current);
        int nextIndex = (currentIndex + 1) % available.size();

        BlueprintData data = stack.getOrDefault(ModComponents.BLUEPRINT_DATA.get(), BlueprintData.createDefault());
        stack.set(ModComponents.BLUEPRINT_DATA.get(), data.withToolType(available.get(nextIndex).getId()));
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
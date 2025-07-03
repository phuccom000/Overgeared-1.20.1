package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.BlueprintQuality;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlueprintItem extends Item {

    public BlueprintItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        CompoundTag tag = stack.getOrCreateTag();

        // Set default quality to POOR
        tag.putString("Quality", BlueprintQuality.POOR.name());
        tag.putInt("Uses", 0);
        tag.putInt("UsesToLevel", getUsesToNextLevel(BlueprintQuality.POOR));

        // Set default tool type to first available or SWORD
        List<ToolType> types = ToolTypeRegistry.getRegisteredTypes();
        tag.putString("ToolType", !types.isEmpty() ? types.get(0).getId() : "SWORD");

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag tag = stack.getTag();
        if (tag == null) return;

        BlueprintQuality quality = getQuality(stack);
        int uses = getUses(stack);
        int usesToLevel = getUsesToNextLevel(stack);
        ToolType toolType = getToolType(stack);

        // Quality line
        tooltip.add(Component.translatable("tooltip.overgeared.blueprint.quality").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(quality.getTranslationKey()).withStyle(quality.getColor())));

        // Progress line
        if (quality != BlueprintQuality.PERFECT && quality != BlueprintQuality.MASTER) {
            tooltip.add(Component.translatable("tooltip.overgeared.blueprint.progress",
                    uses, usesToLevel).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.overgeared.blueprint.maxlevel")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        // Tool type line
        tooltip.add(Component.translatable("tooltip.overgeared.blueprint.tool_type").withStyle(ChatFormatting.GRAY)
                .append(toolType.getDisplayName().withStyle(ChatFormatting.BLUE)));

        // Instruction line
        if (flag.isAdvanced()) {
            tooltip.add(Component.translatable("tooltip.overgeared.blueprint.instructions")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.overgeared.blueprint.progress", uses, usesToLevel).withStyle(ChatFormatting.GRAY));
        }
    }

    public static BlueprintQuality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Quality")) {
            return BlueprintQuality.POOR; // Default to POOR if not set
        }
        try {
            return BlueprintQuality.fromString(tag.getString("Quality"));
        } catch (IllegalArgumentException e) {
            return BlueprintQuality.POOR; // Default to POOR if invalid
        }
    }

    public static int getUses(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt("Uses") : 0; // Default to 0 uses
    }

    public static int getUsesToNextLevel(ItemStack stack) {
        return getUsesToNextLevel(getQuality(stack));
    }

    public static ToolType getToolType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("ToolType")) {
            List<ToolType> types = ToolTypeRegistry.getRegisteredTypes();
            return !types.isEmpty() ? types.get(0) : ToolType.SWORD; // Default to first available or SWORD
        }
        return ToolTypeRegistry.byId(tag.getString("ToolType"))
                .orElse(ToolType.SWORD); // Default to SWORD if invalid
    }

    public static void cycleToolType(ItemStack stack) {
        List<ToolType> available = ToolTypeRegistry.getRegisteredTypes();
        if (available.isEmpty()) return;

        ToolType current = getToolType(stack);
        int currentIndex = available.indexOf(current);
        int nextIndex = (currentIndex + 1) % available.size();

        stack.getOrCreateTag().putString("ToolType", available.get(nextIndex).getId());
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
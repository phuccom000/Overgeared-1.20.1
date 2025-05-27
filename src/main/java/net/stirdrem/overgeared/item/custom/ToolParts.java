package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ToolParts extends Item {
    public ToolParts(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        String quality = stack.getOrCreateTag().getString("ForgingQuality");
        if (!quality.isEmpty()) {
            tooltip.add(Component.literal(getDisplayQuality(quality)).withStyle(ChatFormatting.GRAY));
        }
    }

    private String getDisplayQuality(String key) {
        return switch (key) {
            case "poor" -> "Poorly Forged";
            case "well" -> "Well Forged";
            case "expert" -> "Expertly Forged";
            case "perfect" -> "Perfectly Forged";
            default -> "";
        };
    }
}

package net.stirdrem.overgeared.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.stirdrem.overgeared.screen.RockKnappingMenuProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KnappableRockItem extends Item {
    public KnappableRockItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Check if both hands have this rock item
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (!(mainHand.getItem() instanceof KnappableRockItem && offHand.getItem() instanceof KnappableRockItem)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.STONE_PLACE,
                    SoundSource.PLAYERS, 0.6f, 1.0f);

            // Only open GUI if both hands have rocks
            if (mainHand.getItem() instanceof KnappableRockItem && offHand.getItem() instanceof KnappableRockItem) {
                NetworkHooks.openScreen((ServerPlayer) player, new RockKnappingMenuProvider(), buf -> {
                    // Pass both items to the menu
                    buf.writeItem(mainHand);
                    buf.writeItem(offHand);
                });
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("tooltip.overgeared.knappable_rock.tooltip").withStyle(ChatFormatting.GRAY)
        );
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
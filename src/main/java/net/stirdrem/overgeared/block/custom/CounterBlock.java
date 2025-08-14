package net.stirdrem.overgeared.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.PacketSendCounterC2SPacket;

public class CounterBlock extends Block {
    public CounterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos,
                                 Player player, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (world.isClientSide()) {
            if (!player.isCrouching()) {
                if (!AnvilMinigameEvents.isIsVisible()) return InteractionResult.SUCCESS;
                // Read the current counter at the moment of right-click:
                int currentCount = (int) AnvilMinigameEvents.getArrowPosition();
                String quality = AnvilMinigameEvents.handleHit();
                player.displayClientMessage(Component.literal("Client Count: " + currentCount), false);
                player.displayClientMessage(Component.literal("Client Quality: " + quality), false);
                ModMessages.sendToServer(new PacketSendCounterC2SPacket(pos, quality));
                AnvilMinigameEvents.speedUp();
                return InteractionResult.SUCCESS;
            } else {
                AnvilMinigameEvents.reset();
                AnvilMinigameEvents.setIsVisible(pos, !AnvilMinigameEvents.isIsVisible());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}

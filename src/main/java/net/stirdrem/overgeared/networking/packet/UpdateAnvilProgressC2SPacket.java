package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.block.entity.SmithingAnvilBlockEntity;
import net.stirdrem.overgeared.client.AnvilMinigameOverlay;
import net.stirdrem.overgeared.screen.SmithingAnvilMenu;

import java.util.function.Supplier;

public class UpdateAnvilProgressC2SPacket {
    private final int progress;

    public UpdateAnvilProgressC2SPacket(int progress) {
        this.progress = progress;
    }

    public UpdateAnvilProgressC2SPacket(FriendlyByteBuf buf) {
        this.progress = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(progress);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                if (player.containerMenu instanceof SmithingAnvilMenu menu) {
                    SmithingAnvilBlockEntity anvil = menu.getBlockEntity();
                    anvil.setProgress(this.progress);

                }
            }
        });
        return true;
    }
}

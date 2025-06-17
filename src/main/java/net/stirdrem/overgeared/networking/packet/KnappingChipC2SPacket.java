package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.screen.RockKnappingMenu;

import java.util.function.Supplier;

public class KnappingChipC2SPacket {
    private final int index;

    public KnappingChipC2SPacket(int index) {
        this.index = index;
    }

    public KnappingChipC2SPacket(FriendlyByteBuf buf) {
        this.index = buf.readInt();
    }

    public static void encode(KnappingChipC2SPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.index);
    }

    public static KnappingChipC2SPacket decode(FriendlyByteBuf buf) {
        return new KnappingChipC2SPacket(buf.readInt());
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Server-side handling
            ServerPlayer player = context.getSender();
            if (player != null && player.containerMenu instanceof RockKnappingMenu menu) {
                // Validate the index is within bounds
                if (index >= 0 && index < 9) {
                    menu.setChip(index);
                    OvergearedMod.LOGGER.debug("Player {} chipped spot {} in knapping grid", player.getName().getString(), index);
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
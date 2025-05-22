package net.stirdrem.overgeared.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HitsRemainingPacket {
    private final int hitsRemaining;

    public HitsRemainingPacket(int hitsRemaining) {
        this.hitsRemaining = hitsRemaining;
    }

    public static void encode(HitsRemainingPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.hitsRemaining);
    }

    public static HitsRemainingPacket decode(FriendlyByteBuf buf) {
        return new HitsRemainingPacket(buf.readInt());
    }

    public static void handle(HitsRemainingPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update client-side data here
        });
        ctx.get().setPacketHandled(true);
    }
}


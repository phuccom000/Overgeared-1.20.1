package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.screen.BlueprintWorkbenchMenu;

import java.util.function.Supplier;

public class SelectToolTypeC2SPacket {
    private final String toolTypeId;

    public SelectToolTypeC2SPacket(String toolTypeId) {
        this.toolTypeId = toolTypeId;
    }

    public SelectToolTypeC2SPacket(FriendlyByteBuf buf) {
        this.toolTypeId = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(toolTypeId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.containerMenu instanceof BlueprintWorkbenchMenu menu) {
                boolean toolExists = ToolTypeRegistry.byId(toolTypeId).isPresent();
                ToolTypeRegistry.byId(toolTypeId).ifPresent(menu::createBlueprint);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}
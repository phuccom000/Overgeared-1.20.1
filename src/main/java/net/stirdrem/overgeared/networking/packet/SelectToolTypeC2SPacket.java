package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ToolType;
import net.stirdrem.overgeared.item.ToolTypeRegistry;
import net.stirdrem.overgeared.screen.BlueprintWorkbenchMenu;

import java.util.Optional;
import java.util.function.Supplier;

public class SelectToolTypeC2SPacket {
    private final String toolTypeId;
    private final int containerId;  // Add container ID field

    public SelectToolTypeC2SPacket(String toolTypeId, int containerId) {
        this.toolTypeId = toolTypeId;
        this.containerId = containerId;
    }

    public SelectToolTypeC2SPacket(FriendlyByteBuf buf) {
        this.toolTypeId = buf.readUtf();
        this.containerId = buf.readInt();  // Read container ID from buffer
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(toolTypeId);
        buf.writeInt(containerId);  // Write container ID to buffer
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                OvergearedMod.LOGGER.warn("SelectToolTypeC2SPacket received from null sender (toolTypeId='{}')", toolTypeId);
            } else {
                Optional<ToolType> optional = ToolTypeRegistry.byId(toolTypeId);
                if (optional.isPresent()) {
                    OvergearedMod.LOGGER.debug("ToolType '{}' found. Proceeding to create blueprint.", toolTypeId);
                    if (player.containerMenu instanceof BlueprintWorkbenchMenu menu) {
                        menu.createBlueprint(optional.get());
                        menu.broadcastChanges(); // ensure client sync
                    } else {
                        OvergearedMod.LOGGER.warn("Player '{}' is not in BlueprintWorkbenchMenu, but in {}",
                                player.getGameProfile().getName(),
                                player.containerMenu.getClass().getSimpleName());
                    }
                } else {
                    OvergearedMod.LOGGER.error("ToolTypeRegistry.byId('{}') returned empty; cannot create blueprint.", toolTypeId);
                }
            }
        });
        context.setPacketHandled(true);
        return true;
    }

}
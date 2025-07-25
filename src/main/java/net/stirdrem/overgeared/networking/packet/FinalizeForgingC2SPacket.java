package net.stirdrem.overgeared.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;

import java.util.function.Supplier;

public class FinalizeForgingC2SPacket {
    private final String quality;

    public FinalizeForgingC2SPacket(String quality) {
        this.quality = quality;
    }

    public FinalizeForgingC2SPacket(FriendlyByteBuf buf) {
        this.quality = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(quality);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // HERE WE ARE ON THE SERVER!
            ServerPlayer player = context.getSender();
            /*if (player != null && player.containerMenu instanceof SmithingAnvilMenu menu) {
                SmithingAnvilBlockEntity anvil = menu.getBlockEntity();
                //anvil.completeForgingWithQuality(quality);
            }*/
            //player.sendSystemMessage(Component.literal(quality));
            player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {

            });
            OvergearedMod.LOGGER.info("Smithing Anvil Shift Right Clicked");

        });
        return true;
    }
}
package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;

import java.util.function.Supplier;

public class StartMinigameC2SPacket {
    private final ItemStack result;
    private final int hitsRequired;
    private final BlockPos pos;

    public StartMinigameC2SPacket(ItemStack result, int hitsRequired, BlockPos pos) {
        this.result = result;
        this.hitsRequired = hitsRequired;
        this.pos = pos;
    }

    public StartMinigameC2SPacket(FriendlyByteBuf buf) {
        this.result = buf.readItem();
        this.hitsRequired = buf.readInt();
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(result);
        buf.writeInt(hitsRequired);
        buf.writeBlockPos(pos);
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
                minigame.start(result, hitsRequired, pos);
                ModMessages.sendToPlayer(new MinigameSyncS2CPacket(
                        minigame.isVisible(),
                        minigame.isForging(),
                        minigame.getResultItem(),
                        minigame.getHitsRemaining(),
                        minigame.getArrowPosition(),
                        minigame.getArrowSpeed(),
                        ServerConfig.MAX_SPEED.get().floatValue(),
                        ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue(),
                        minigame.getMovingRight(),
                        minigame.getPerfectHits(),
                        minigame.getGoodHits(),
                        minigame.getMissedHits(),
                        minigame.getPerfectZoneStart(),
                        minigame.getPerfectZoneEnd(),
                        minigame.getGoodZoneStart(),
                        minigame.getGoodZoneEnd(),
                        ServerConfig.ZONE_SHRINK_FACTOR.get().floatValue(),
                        15.0f, // zoneShiftAmount
                        minigame.getAnvilPos()
                ), player);
            });
            OvergearedMod.LOGGER.info("Smithing Anvil Right Clicked");

        });
        return true;
    }
}
package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;

import java.util.function.Supplier;

public class StartMinigameC2SPacket {
    private final CompoundTag minigameData;

    public StartMinigameC2SPacket(ItemStack result, int hitsRequired, BlockPos pos) {
        this.minigameData = new CompoundTag();
        minigameData.put("result", result.save(new CompoundTag()));
        minigameData.putInt("hitsRequired", hitsRequired);
        minigameData.putInt("posX", pos.getX());
        minigameData.putInt("posY", pos.getY());
        minigameData.putInt("posZ", pos.getZ());
    }

    public StartMinigameC2SPacket(FriendlyByteBuf buf) {
        this.minigameData = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(minigameData);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerLevel level = context.getSender().serverLevel();
            if (player == null) return;

            ItemStack result = ItemStack.of(minigameData.getCompound("result"));
            int hitsRequired = minigameData.getInt("hitsRequired");
            BlockPos pos = new BlockPos(
                    minigameData.getInt("posX"),
                    minigameData.getInt("posY"),
                    minigameData.getInt("posZ")
            );

            player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                minigame.start(result, hitsRequired, pos, player);

                // Create sync packet data
                CompoundTag syncData = new CompoundTag();
                minigame.saveNBTData(syncData);
                syncData.putFloat("maxArrowSpeed", ServerConfig.MAX_ARROW_SPEED.get().floatValue());
                syncData.putFloat("speedIncreasePerHit", ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue());
                syncData.putFloat("zoneShrinkFactor", ServerConfig.ZONE_SHRINK_FACTOR.get().floatValue());

                ModMessages.sendToPlayer(new MinigameSyncS2CPacket(syncData), player);
            });

            OvergearedMod.LOGGER.debug("Started minigame for player {}", player.getName().getString());
        });
        return true;
    }
}
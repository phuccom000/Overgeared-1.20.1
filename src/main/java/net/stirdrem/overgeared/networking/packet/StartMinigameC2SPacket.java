package net.stirdrem.overgeared.networking.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ClientAnvilMinigameData;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;

import java.util.function.Supplier;

public class StartMinigameC2SPacket {
    private final CompoundTag minigameData;

    public StartMinigameC2SPacket(ItemStack result, int hitsRemaining, BlockPos pos) {
        this.minigameData = new CompoundTag();
        minigameData.put("result", result.save(new CompoundTag()));
        minigameData.putInt("hitsRemaining", hitsRemaining);
        minigameData.putInt("posX", pos.getX());
        minigameData.putInt("posY", pos.getY());
        minigameData.putInt("posZ", pos.getZ());
    }

    public StartMinigameC2SPacket(ItemStack result, CompoundTag nbt, BlockPos pos) {
        this.minigameData = nbt;
        minigameData.putInt("hitsRemaining", nbt.getInt("hitsRemaining"));
        minigameData.put("result", result.save(new CompoundTag()));
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
            if (player == null) return;

            ItemStack result = ItemStack.of(minigameData.getCompound("result"));
            int hitsRequired = minigameData.getInt("hitsRemaining");
            BlockPos pos = new BlockPos(
                    minigameData.getInt("posX"),
                    minigameData.getInt("posY"),
                    minigameData.getInt("posZ")
            );

            player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                ClientAnvilMinigameData.loadFromNBT(minigameData);
                //minigame.start(result, hitsRequired, pos, player);
                minigame.start(result, minigameData, pos, player);
                // Create sync packet data
                CompoundTag syncData = new CompoundTag();
                minigame.saveNBTData(syncData);
                syncData.putFloat("maxArrowSpeed", ServerConfig.MAX_SPEED.get().floatValue());
                syncData.putFloat("speedIncreasePerHit", ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue());
                syncData.putFloat("zoneShrinkFactor", ServerConfig.ZONE_SHRINK_FACTOR.get().floatValue());

                ModMessages.sendToPlayer(new MinigameSyncS2CPacket(syncData), player);
            });

            OvergearedMod.LOGGER.debug("Started minigame for player {}", player.getName().getString());
        });
        return true;
    }
}
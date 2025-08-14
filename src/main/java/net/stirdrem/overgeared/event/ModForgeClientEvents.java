package net.stirdrem.overgeared.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.minigame.AnvilMinigame;
import net.stirdrem.overgeared.minigame.AnvilMinigameProvider;
import net.stirdrem.overgeared.networking.ModMessages;
import net.stirdrem.overgeared.networking.packet.MinigameSyncS2CPacket;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModForgeClientEvents {


    /*@SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
            AnvilMinigameOverlay.pauseMinigame();
        }
    }*/
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            Level level = player.level();

            if (level.isClientSide()) return;

            //handleHeatedItems(player, level);
            handleAnvilMinigameSync(event, player);
        }
    }

    private static void handleAnvilMinigameSync(TickEvent.PlayerTickEvent event, Player player) {
        if (event.side == LogicalSide.CLIENT) {
            player.getCapability(AnvilMinigameProvider.ANVIL_MINIGAME).ifPresent(minigame -> {
                if (minigame.isVisible()) {
                    updateArrowPosition(minigame);
                    //syncMinigameData(minigame, (ServerPlayer) player);
                }
            });
        }
    }

    private static void updateArrowPosition(AnvilMinigame minigame) {
        float arrowPosition = minigame.getArrowPosition();
        float arrowSpeed = minigame.getArrowSpeed();
        boolean movingRight = minigame.getMovingRight();

        if (movingRight) {
            minigame.setArrowPosition(Math.min(100, arrowPosition + arrowSpeed));
            if (arrowPosition >= 100) {
                minigame.setMovingRight(false);
            }
        } else {
            minigame.setArrowPosition(Math.max(0, arrowPosition - arrowSpeed));
            if (arrowPosition <= 0) {
                minigame.setMovingRight(true);
            }
        }
    }

    private static void syncMinigameData(AnvilMinigame minigame, ServerPlayer player) {
        try {
            // Create a CompoundTag to hold all minigame data
            CompoundTag minigameData = new CompoundTag();
            minigame.saveNBTData(minigameData); // Use your existing NBT serialization

            // Add server config values that the client needs
            minigameData.putFloat("maxArrowSpeed", ServerConfig.MAX_ARROW_SPEED.get().floatValue());
            minigameData.putFloat("speedIncreasePerHit", ServerConfig.DEFAULT_ARROW_SPEED_INCREASE.get().floatValue());
            minigameData.putFloat("zoneShrinkFactor", ServerConfig.ZONE_SHRINK_FACTOR.get().floatValue());

            // Send the packet
            ModMessages.sendToPlayer(new MinigameSyncS2CPacket(minigameData), player);

            // Optional debug logging
            //OvergearedMod.LOGGER.debug("Sent minigame sync packet to player {}", player.getName().getString());
        } catch (Exception e) {
            OvergearedMod.LOGGER.error("Failed to sync minigame data to player {}", player.getName().getString(), e);
        }
    }
}

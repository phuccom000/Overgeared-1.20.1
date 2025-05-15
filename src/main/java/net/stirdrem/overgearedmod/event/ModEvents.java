package net.stirdrem.overgearedmod.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stirdrem.overgearedmod.OvergearedMod;
import net.stirdrem.overgearedmod.heat.HeatCapability;
import net.stirdrem.overgearedmod.heat.HeatCapabilityProvider;
import net.stirdrem.overgearedmod.item.ModItems;
import net.stirdrem.overgearedmod.util.ModTags;

@Mod.EventBusSubscriber(modid = OvergearedMod.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;

            boolean hasHeatedIngot = false;
            // Check inventory
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && stack.is(ModTags.Items.HEATED_METALS)) {
                    hasHeatedIngot = true;
                    break;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffhandItem();
            if (!offhandStack.isEmpty() && offhandStack.is(ModTags.Items.HEATED_METALS)) {
                hasHeatedIngot = true;
            }

            // Apply effect if player has heated ingot
            if (hasHeatedIngot) {
                boolean hasTongs = false;
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty() &&
                        (offhand.is(ModTags.Items.TONGS))) {
                    hasTongs = true;
                }

                if (!hasTongs) {
                    //player.sendSystemMessage(Component.literal("Player does not have tongs!"));
                    player.hurt(player.damageSources().hotFloor(), 1.0F); // Apply burn damage
                }
            }
        }
    }
}

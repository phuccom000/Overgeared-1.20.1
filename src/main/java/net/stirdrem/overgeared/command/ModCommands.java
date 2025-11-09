package net.stirdrem.overgeared.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.CastingConfigHelper;

public class ModCommands {

    private static final String[] QUALITIES = {"poor", "well", "expert", "perfect", "master"};

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        // /setforgingquality <quality>
        dispatcher.register(
                Commands.literal("setforgingquality")
                        .then(Commands.argument("quality", StringArgumentType.string())
                                .suggests((c, b) -> {
                                    for (String q : QUALITIES) b.suggest(q);
                                    return b.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayer();
                                    String quality = StringArgumentType.getString(ctx, "quality").toLowerCase();

                                    ItemStack inHand = player.getMainHandItem();
                                    if (inHand.isEmpty()) {
                                        ctx.getSource().sendFailure(Component.literal("You must hold an item"));
                                        return 0;
                                    }

                                    CompoundTag tag = inHand.getOrCreateTag();
                                    tag.putString("ForgingQuality", quality);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Set ForgingQuality to " + quality), false);

                                    return 1;
                                })
                        )
        );

        // /givecast <toolType> [quality] [material]
        dispatcher.register(
                Commands.literal("givecast")
                        .then(Commands.argument("toolType", StringArgumentType.string())
                                .then(Commands.argument("quality", StringArgumentType.string())
                                        .suggests((c, b) -> {
                                            b.suggest("none");
                                            for (String q : QUALITIES) b.suggest(q);
                                            return b.buildFuture();
                                        })
                                        .then(Commands.argument("material", StringArgumentType.string())
                                                .suggests((c, b) -> {
                                                    b.suggest("clay");
                                                    b.suggest("nether");
                                                    return b.buildFuture();
                                                })
                                                .executes(ctx -> giveCast(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "toolType"),
                                                        StringArgumentType.getString(ctx, "quality"),
                                                        StringArgumentType.getString(ctx, "material")
                                                ))
                                        )
                                        .executes(ctx -> giveCast(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "toolType"),
                                                StringArgumentType.getString(ctx, "quality"),
                                                "clay"
                                        ))
                                )
                                .executes(ctx -> giveCast(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "toolType"),
                                        "none",
                                        "clay"
                                ))
                        )
        );
    }

    private static int giveCast(CommandSourceStack source, String toolType, String quality, String material) {
        ServerPlayer player = source.getPlayer();

        ItemStack stack = material.equalsIgnoreCase("nether") ?
                new ItemStack(ModItems.NETHER_TOOL_CAST.get()) :
                new ItemStack(ModItems.CLAY_TOOL_CAST.get());

        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("ToolType", toolType.toLowerCase());
        tag.putInt("Amount", 0);
        tag.putInt("MaxAmount", CastingConfigHelper.getMaxMaterialAmount(toolType));
        tag.put("Materials", new CompoundTag()); // Empty compound instead of remove

        if (!quality.equalsIgnoreCase("none"))
            tag.putString("Quality", quality.toLowerCase());

        player.addItem(stack);

        source.sendSuccess(
                () -> Component.literal("Gave cast: " + toolType +
                        (quality.equals("none") ? "" : " (" + quality + ")") +
                        " [" + material + "]"), false
        );

        return 1;
    }
}

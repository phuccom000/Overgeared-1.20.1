package net.stirdrem.overgeared.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.util.ConfigHelper;

public class ModCommands {

    private static final String[] QUALITIES = {"poor", "well", "expert", "perfect", "master"};

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        // /setforgingquality <quality>
        dispatcher.register(
                Commands.literal("setforgingquality")
                        .requires(source -> source.hasPermission(2)) // Requires operator level 2 or higher
                        .then(Commands.argument("quality", StringArgumentType.string())
                                .suggests((c, b) -> {
                                    for (String q : QUALITIES) b.suggest(q);
                                    return b.buildFuture();
                                })
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                        ctx.getSource().sendFailure(Component.literal("This command can only be run by a player"));
                                        return 0;
                                    }
                                    String quality = StringArgumentType.getString(ctx, "quality").toLowerCase();

                                    ItemStack inHand = player.getMainHandItem();
                                    if (inHand.isEmpty()) {
                                        ctx.getSource().sendFailure(Component.literal("You must hold an item"));
                                        return 0;
                                    }

                                    inHand.set(ModComponents.FORGING_QUALITY, ForgingQuality.fromString(quality));

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Set ForgingQuality to " + quality), false);

                                    return 1;
                                })
                        )
        );

        // /givecast <toolType> [quality] [material]
        dispatcher.register(
                Commands.literal("givecast")
                        .requires(source -> source.hasPermission(2)) // Requires operator level 2 or higher
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
        if (!(source.getPlayer() instanceof ServerPlayer player)) return 0;

        ItemStack stack = material.equalsIgnoreCase("nether") ?
                new ItemStack(ModItems.NETHER_TOOL_CAST.get()) :
                new ItemStack(ModItems.CLAY_TOOL_CAST.get());

        stack.set(ModComponents.CAST_DATA, new CastData(
                quality.equalsIgnoreCase("none") ? null : quality.toLowerCase(),
                toolType.toLowerCase(),
                null, 0,
                ConfigHelper.getMaxMaterialAmount(toolType),
                null,
                null,
                false));

        player.addItem(stack);

        source.sendSuccess(
                () -> Component.literal("Gave cast: " + toolType +
                        (quality.equals("none") ? "" : " (" + quality + ")") +
                        " [" + material + "]"), false
        );

        return 1;
    }
}

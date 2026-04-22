package com.agesmining.agesmining.command;

import com.agesmining.agesmining.config.AgesMiningConfig;
import com.agesmining.agesmining.util.StabilityEngine;
import com.agesmining.agesmining.util.SupportDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import static com.agesmining.agesmining.AgesMining.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AgesMiningCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("agesmining")
                .requires(src -> src.hasPermission(2))

                // /agesmining status
                .then(Commands.literal("status")
                    .executes(AgesMiningCommand::cmdStatus))

                // /agesmining check <pos>
                .then(Commands.literal("check")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(AgesMiningCommand::cmdCheck)))

                // /agesmining collapse <pos>
                .then(Commands.literal("collapse")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(AgesMiningCommand::cmdForceCollapse)))

                // /agesmining toggle
                .then(Commands.literal("toggle")
                    .executes(AgesMiningCommand::cmdToggle))

                // /agesmining debug <radius>
                .then(Commands.literal("debug")
                    .then(Commands.argument("radius", IntegerArgumentType.integer(1, 16))
                        .executes(AgesMiningCommand::cmdDebugRadius)))

                // /agesmining reload
                .then(Commands.literal("reload")
                    .executes(AgesMiningCommand::cmdReload))
        );
    }

    // ── /agesmining status ────────────────────────────────────────────────────
    private static int cmdStatus(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();

        StabilityEngine engine = StabilityEngine.get(level);
        boolean enabled = AgesMiningConfig.INSTANCE.CAVE_INS_ENABLED.get();

        src.sendSuccess(() -> Component.literal("=== Ages Mining Status ===")
            .withStyle(ChatFormatting.GOLD), false);

        src.sendSuccess(() -> Component.literal("Cave-ins: ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(enabled ? "ENABLED" : "DISABLED")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);

        src.sendSuccess(() -> Component.literal("Pending checks: ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.valueOf(engine.getPendingCheckCount()))
                .withStyle(ChatFormatting.YELLOW)), false);

        src.sendSuccess(() -> Component.literal("Pending collapses: ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.valueOf(engine.getPendingCollapseCount()))
                .withStyle(ChatFormatting.RED)), false);

        src.sendSuccess(() -> Component.literal("Support mode: ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal("TFC (supports only)")
                .withStyle(ChatFormatting.GREEN)), false);

        src.sendSuccess(() -> Component.literal("Trigger chance: ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(
                String.format("%.0f%%",
                    AgesMiningConfig.INSTANCE.COLLAPSE_TRIGGER_CHANCE.get() * 100))
                .withStyle(ChatFormatting.AQUA)), false);

        src.sendSuccess(() -> Component.literal("Propagation chance: ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(
                String.format("%.0f%%",
                    AgesMiningConfig.INSTANCE.COLLAPSE_PROPAGATE_CHANCE.get() * 100))
                .withStyle(ChatFormatting.AQUA)), false);

        SupportDataManager.SupportRange range = SupportDataManager.INSTANCE.getSupportCheckRange();
        src.sendSuccess(() -> Component.literal("Support range (h/up/down): ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(range.horizontal() + "/" + range.up() + "/" + range.down())
                .withStyle(ChatFormatting.YELLOW)), false);

        src.sendSuccess(() -> Component.literal("Min depth (Y): ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(
                String.valueOf(AgesMiningConfig.INSTANCE.MIN_DEPTH_FOR_COLLAPSE.get()))
                .withStyle(ChatFormatting.AQUA)), false);

        return 1;
    }

    // ── /agesmining check <pos> ───────────────────────────────────────────────
    private static int cmdCheck(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();

        BlockPos pos;
        try {
            pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        } catch (Exception e) {
            src.sendFailure(Component.literal("Position not loaded or invalid."));
            return 0;
        }

        StabilityEngine engine = StabilityEngine.get(level);
        boolean supported = engine.isSupported(level, pos);
        var blockName = level.getBlockState(pos).getBlock().getDescriptionId();

        src.sendSuccess(() -> Component.literal("Block at " + pos.toShortString() + " (")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.translatable(blockName).withStyle(ChatFormatting.WHITE))
            .append(Component.literal("):").withStyle(ChatFormatting.GRAY)), false);

        src.sendSuccess(() -> Component.literal("  Structural support: ")
            .withStyle(ChatFormatting.GRAY)
            .append(supported
                ? Component.literal("SUPPORTED ✓").withStyle(ChatFormatting.GREEN)
                : Component.literal("UNSUPPORTED ✗").withStyle(ChatFormatting.RED)), false);

        return 1;
    }

    // ── /agesmining collapse <pos> ─────────────────────────────────────────────
    private static int cmdForceCollapse(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();

        BlockPos pos;
        try {
            pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        } catch (Exception e) {
            src.sendFailure(Component.literal("Position not loaded or invalid."));
            return 0;
        }

        if (level.getBlockState(pos).isAir()) {
            src.sendFailure(Component.literal("No block at that position."));
            return 0;
        }

        // Force-start a collapse wave at this position
        StabilityEngine engine = StabilityEngine.get(level);
        boolean started = engine.startCollapse(level, pos);

        src.sendSuccess(() -> Component.literal(started
                ? "Force-started collapse at "
                : "No collapsible wave could start at ")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal(pos.toShortString()).withStyle(ChatFormatting.WHITE)), false);

        return started ? 1 : 0;
    }

    // ── /agesmining toggle ─────────────────────────────────────────────────────
    private static int cmdToggle(CommandContext<CommandSourceStack> ctx) {
        boolean current = AgesMiningConfig.INSTANCE.CAVE_INS_ENABLED.get();
        AgesMiningConfig.INSTANCE.CAVE_INS_ENABLED.set(!current);

        boolean newVal = !current;
        ctx.getSource().sendSuccess(() -> Component.literal("Cave-ins are now ")
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(newVal ? "ENABLED" : "DISABLED")
                .withStyle(newVal ? ChatFormatting.GREEN : ChatFormatting.RED)), true);

        return 1;
    }

    // ── /agesmining debug <radius> ─────────────────────────────────────────────
    private static int cmdDebugRadius(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("Must be run by a player."));
            return 0;
        }
        if (!(player.level() instanceof ServerLevel level)) return 0;

        int radius = IntegerArgumentType.getInteger(ctx, "radius");
        StabilityEngine engine = StabilityEngine.get(level);
        BlockPos origin = player.blockPosition();

        int unsupported = 0;
        int total = 0;

        for (BlockPos pos : BlockPos.betweenClosed(
            origin.offset(-radius, -2, -radius),
            origin.offset(radius, 4, radius))) {

            var state = level.getBlockState(pos);
            if (state.isAir()) continue;
            total++;
            if (!engine.isSupported(level, pos)) unsupported++;
        }

        int finalUnsupported = unsupported;
        int finalTotal = total;
        src.sendSuccess(() -> Component.literal("Debug scan (r=" + radius + "): ")
            .withStyle(ChatFormatting.GOLD)
            .append(Component.literal(finalUnsupported + "/" + finalTotal + " blocks unsupported")
                .withStyle(finalUnsupported > 0 ? ChatFormatting.RED : ChatFormatting.GREEN)), false);

        return 1;
    }

    // ── /agesmining reload ─────────────────────────────────────────────────────
    private static int cmdReload(CommandContext<CommandSourceStack> ctx) {
        // Clear engine caches so new config values take effect immediately
        StabilityEngine.clearAll();

        ctx.getSource().sendSuccess(() -> Component.literal(
            "Ages Mining engine reset. Config changes will take effect.")
            .withStyle(ChatFormatting.GREEN), true);

        return 1;
    }
}

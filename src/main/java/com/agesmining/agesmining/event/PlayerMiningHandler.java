package com.agesmining.agesmining.event;

import com.agesmining.agesmining.AgesMining;
import com.agesmining.agesmining.config.AgesMiningConfig;
import com.agesmining.agesmining.util.StabilityEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Provides player-facing feedback: stability warnings and action bar messages.
 */
public class PlayerMiningHandler {

    // Cooldown to avoid spamming stability warnings (per-player, in ms)
    private final Map<UUID, Long> warnCooldowns = new HashMap<>();
    private static final long WARN_COOLDOWN_MS = 4000;

    @SubscribeEvent
    public void onPlayerMine(PlayerEvent.BreakSpeed event) {
        if (!AgesMiningConfig.INSTANCE.CAVE_INS_ENABLED.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        BlockPos pos = event.getPosition().orElse(null);
        if (pos == null) return;
        if (pos.getY() > AgesMiningConfig.INSTANCE.MIN_DEPTH_FOR_COLLAPSE.get()) return;

        // Check if the block above this one is supported
        BlockPos ceiling = pos.above();
        StabilityEngine engine = StabilityEngine.get(level);

        BlockState ceilingState = level.getBlockState(ceiling);
        if (!ceilingState.isAir() && !engine.isSupported(level, ceiling)) {
            sendWarning(player, ceiling);
        }
    }

    private void sendWarning(ServerPlayer player, BlockPos unstablePos) {
        UUID id = player.getUUID();
        long now = System.currentTimeMillis();

        if (warnCooldowns.getOrDefault(id, 0L) + WARN_COOLDOWN_MS > now) return;
        warnCooldowns.put(id, now);

        player.displayClientMessage(
            Component.literal("⚠ Потолок нестабилен! Установите опоры!")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
            true // true = action bar overlay (not chat)
        );
    }

    /** Remove player data on logout */
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        warnCooldowns.remove(event.getEntity().getUUID());
    }
}

package com.agesmining.agesmining.event;

import com.agesmining.agesmining.AgesMining;
import com.agesmining.agesmining.config.AgesMiningConfig;
import com.agesmining.agesmining.util.StabilityEngine;
import com.agesmining.agesmining.util.SupportDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

/**
 * Handles Forge events to integrate the cave-in mechanic with the game loop.
 */
public class CaveInEventHandler {

    /** Called every server tick to drive StabilityEngine processing. */
    @SubscribeEvent
    public void onServerTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;
        if (!AgesMiningConfig.INSTANCE.CAVE_INS_ENABLED.get()) return;

        StabilityEngine.get(level).tick(level);
    }

    /** Triggered when any block is broken in the world. */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!AgesMiningConfig.INSTANCE.CAVE_INS_ENABLED.get()) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        StabilityEngine.get(level).tryTriggerCollapse(level, pos);
        AgesMining.LOGGER.debug("Block broken at {} ({}), attempting collapse trigger",
            pos, state.getBlock().getDescriptionId());
    }

    /** Register datapack-driven support definitions. */
    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(SupportDataManager.INSTANCE);
    }

    /** Clean up engine state when a world unloads. */
    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel) {
            StabilityEngine.clearAll();
        }
    }
}

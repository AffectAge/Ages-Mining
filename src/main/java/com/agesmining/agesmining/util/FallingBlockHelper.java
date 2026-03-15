package com.agesmining.agesmining.util;

import com.agesmining.agesmining.config.AgesMiningConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Spawns FallingBlockEntity instances for the cave-in collapse mechanic.
 * Reuses vanilla Minecraft's falling block system for correct physics.
 */
public class FallingBlockHelper {

    /**
     * Replaces a block with a falling block entity.
     * The entity will deal damage to players it lands on if configured.
     */
    public static void startFalling(ServerLevel level, BlockPos pos, BlockState state) {
        FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);

        // Configure the falling entity
        falling.time = 1; // Skip first-tick grace period
        float damagePerBlock = AgesMiningConfig.INSTANCE.COLLAPSE_DAMAGE_PER_BLOCK.get().floatValue();
        falling.setHurtsEntities(
            damagePerBlock,
            (int) (damagePerBlock * 4.0f)
        );

        if (AgesMiningConfig.INSTANCE.DESTROY_ITEMS_ON_COLLAPSE.get()) {
            falling.dropItem = false;
        }
    }

    /**
     * Deals damage to players caught in the collapse area.
     * Called directly when we want immediate damage (before falling blocks settle).
     */
    public static void damagePlayersInArea(ServerLevel level, BlockPos center, int radius, DamageSource source) {
        if (!AgesMiningConfig.INSTANCE.DAMAGE_PLAYERS.get()) return;

        AABB aabb = new AABB(
            center.getX() - radius, center.getY() - 1, center.getZ() - radius,
            center.getX() + radius, center.getY() + radius, center.getZ() + radius
        );

        List<Entity> entities = level.getEntities(null, aabb);
        float damage = (float)(AgesMiningConfig.INSTANCE.COLLAPSE_DAMAGE_PER_BLOCK.get() * 2.0);

        for (Entity entity : entities) {
            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                player.hurt(source, damage);
            }
        }
    }
}

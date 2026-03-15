package com.agesmining.agesmining.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;

import java.util.Random;

/**
 * Handles visual and audio feedback for cave-in events.
 */
public class EffectsHelper {

    private static final Random RANDOM = new Random();

    /**
     * Spawns falling dust particles as a warning before collapse.
     */
    public static void spawnDustWarning(ServerLevel level, BlockPos pos) {
        BlockParticleOption particle = new BlockParticleOption(
            ParticleTypes.BLOCK, Blocks.GRAVEL.defaultBlockState()
        );

        // Emit from bottom face of the unstable block
        double cx = pos.getX() + 0.5;
        double cy = pos.getY();
        double cz = pos.getZ() + 0.5;

        for (int i = 0; i < 6; i++) {
            double ox = (RANDOM.nextDouble() - 0.5) * 0.8;
            double oz = (RANDOM.nextDouble() - 0.5) * 0.8;
            level.sendParticles(particle, cx + ox, cy, cz + oz,
                1, 0.05, -0.1, 0.05, 0.02);
        }
    }

    /**
     * Plays a cave rumble/crack sound at the collapse origin.
     */
    public static void playCaveInSound(ServerLevel level, BlockPos pos) {
        // Play gravel break sound as primary collapse sound
        level.playSound(null, pos,
            SoundEvents.GRAVEL_BREAK,
            SoundSource.BLOCKS,
            2.5f,
            0.6f + RANDOM.nextFloat() * 0.3f
        );

        // Play a secondary rumble (ambient cave sound)
        level.playSound(null, pos,
            SoundEvents.STONE_BREAK,
            SoundSource.BLOCKS,
            1.8f,
            0.4f + RANDOM.nextFloat() * 0.2f
        );
    }

    /**
     * Plays a soft creak warning sound to alert nearby players.
     */
    public static void playCreakWarning(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos,
            SoundEvents.GRAVEL_STEP,
            SoundSource.BLOCKS,
            0.8f,
            0.5f + RANDOM.nextFloat() * 0.2f
        );
    }
}

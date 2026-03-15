package com.agesmining.agesmining.util;

import com.agesmining.agesmining.config.AgesMiningConfig;
import com.agesmining.agesmining.registry.ModTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides per-block stability values used to determine how quickly
 * a ceiling section deteriorates without support.
 * Higher stability = harder to collapse.
 */
public class StabilityChecker {

    public static int getStability(BlockState state) {
        Block block = state.getBlock();

        // Support structures are infinitely stable
        if (state.is(ModTags.Blocks.MINE_SUPPORTS)) return Integer.MAX_VALUE;

        // Non-structural blocks have zero stability
        if (state.is(ModTags.Blocks.NON_STRUCTURAL)) return 0;

        // Bedrock / end stone — extremely stable
        if (block == Blocks.BEDROCK) return Integer.MAX_VALUE;
        if (block == Blocks.END_STONE || block == Blocks.END_STONE_BRICKS) return 20;

        // Deepslate variants
        if (block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE
            || block == Blocks.DEEPSLATE_BRICKS || block == Blocks.DEEPSLATE_TILES) {
            return AgesMiningConfig.INSTANCE.DEEPSLATE_STABILITY.get();
        }

        // Ores — slightly weaker due to hollow pockets
        if (state.is(ModTags.Blocks.ALL_ORES)) {
            return AgesMiningConfig.INSTANCE.ORE_STABILITY.get();
        }

        // Stone variants
        if (block == Blocks.STONE || block == Blocks.COBBLESTONE
            || block == Blocks.STONE_BRICKS || block == Blocks.MOSSY_COBBLESTONE
            || block == Blocks.MOSSY_STONE_BRICKS || block == Blocks.ANDESITE
            || block == Blocks.DIORITE || block == Blocks.GRANITE) {
            return AgesMiningConfig.INSTANCE.STONE_STABILITY.get();
        }

        // Gravel — very unstable
        if (block == Blocks.GRAVEL) {
            return AgesMiningConfig.INSTANCE.GRAVEL_STABILITY.get();
        }

        // Sand
        if (block == Blocks.SAND || block == Blocks.RED_SAND || block == Blocks.SANDSTONE) {
            return AgesMiningConfig.INSTANCE.SAND_STABILITY.get();
        }

        // Dirt/soil
        if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK
            || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT) {
            return AgesMiningConfig.INSTANCE.DIRT_STABILITY.get();
        }

        // Default for any other solid block
        if (state.isSolidRender(null, null)) {
            return AgesMiningConfig.INSTANCE.STONE_STABILITY.get();
        }

        return 0;
    }

    public static boolean isVeryUnstable(BlockState state) {
        return getStability(state) <= 3;
    }
}

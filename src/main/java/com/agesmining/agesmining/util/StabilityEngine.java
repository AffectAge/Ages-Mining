package com.agesmining.agesmining.util;

import com.agesmining.agesmining.AgesMining;
import com.agesmining.agesmining.config.AgesMiningConfig;
import com.agesmining.agesmining.network.NetworkHandler;
import com.agesmining.agesmining.registry.ModBlocks;
import com.agesmining.agesmining.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Collapse simulation modelled after TFC:
 * - mining/chiseling attempts a local unsupported scan
 * - one position starts a collapse wave
 * - wave propagates upwards in random ticks and shrinks each step
 */
public class StabilityEngine {

    private static final Map<ServerLevel, StabilityEngine> INSTANCES = new WeakHashMap<>();

    public static StabilityEngine get(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level, ignored -> new StabilityEngine());
    }

    public static void clearAll() {
        INSTANCES.clear();
    }

    private final List<CollapseWave> collapsesInProgress = new ArrayList<>();

    public void tick(ServerLevel level) {
        if (collapsesInProgress.isEmpty() || level.getRandom().nextInt(10) != 0) {
            return;
        }

        for (CollapseWave wave : collapsesInProgress) {
            Set<BlockPos> updatedPositions = new HashSet<>();

            for (BlockPos posAt : wave.nextPositions) {
                BlockState stateAt = level.getBlockState(posAt);
                if (stateAt.is(ModTags.Blocks.CAN_COLLAPSE)
                    && canFallDown(level, posAt)
                    && posAt.distSqr(wave.centerPos) < wave.radiusSquared
                    && level.getRandom().nextDouble() < AgesMiningConfig.INSTANCE.COLLAPSE_PROPAGATE_CHANCE.get()) {
                    if (collapseBlock(level, posAt, stateAt, false)) {
                        updatedPositions.add(posAt.above().immutable());
                    }
                }
            }

            wave.nextPositions.clear();
            if (!updatedPositions.isEmpty()) {
                triggerCollapseEffects(level, wave.centerPos, 2.4f);
                wave.nextPositions.addAll(updatedPositions);
                wave.radiusSquared *= 0.8D;
            }
        }

        collapsesInProgress.removeIf(wave -> wave.nextPositions.isEmpty());
    }

    public boolean tryTriggerCollapse(ServerLevel level, BlockPos pos) {
        if (!AgesMiningConfig.INSTANCE.CAVE_INS_ENABLED.get()) return false;
        if (pos.getY() > AgesMiningConfig.INSTANCE.MIN_DEPTH_FOR_COLLAPSE.get()) return false;

        BlockState state = level.getBlockState(pos);
        if (!state.is(ModTags.Blocks.CAN_TRIGGER_COLLAPSE)) return false;

        boolean realCollapse = level.getRandom().nextDouble() < AgesMiningConfig.INSTANCE.COLLAPSE_TRIGGER_CHANCE.get();
        boolean fakeCollapse = !realCollapse && level.getRandom().nextDouble() < AgesMiningConfig.INSTANCE.COLLAPSE_FAKE_TRIGGER_CHANCE.get();
        if (!realCollapse && !fakeCollapse) return false;

        int radX = (level.getRandom().nextInt(5) + 4) / 2;
        int radY = (level.getRandom().nextInt(3) + 2) / 2;
        int radZ = (level.getRandom().nextInt(5) + 4) / 2;

        List<BlockPos> fakeCollapseStarts = new ArrayList<>();
        Set<BlockPos> unsupported = SupportDataManager.INSTANCE.findUnsupportedPositions(
            level,
            pos.offset(-radX, -radY, -radZ),
            pos.offset(radX, radY, radZ)
        );

        for (BlockPos checking : unsupported) {
            if (checking.equals(pos)) continue;
            if (!canStartCollapse(level, checking)) continue;

            if (fakeCollapse) {
                fakeCollapseStarts.add(checking.immutable());
                continue;
            }

            startCollapse(level, checking);
            return true;
        }

        if (!fakeCollapseStarts.isEmpty()) {
            for (BlockPos start : fakeCollapseStarts) {
                if (AgesMiningConfig.INSTANCE.ENABLE_WARNING_PARTICLES.get()) {
                    EffectsHelper.spawnDustWarning(level, start);
                }
            }
        }
        return false;
    }

    public boolean startCollapse(ServerLevel level, BlockPos centerPos) {
        int variance = Math.max(1, AgesMiningConfig.INSTANCE.COLLAPSE_RADIUS_VARIANCE.get());
        int radius = AgesMiningConfig.INSTANCE.COLLAPSE_MIN_RADIUS.get() + level.getRandom().nextInt(variance);
        int radiusSquared = radius * radius;
        Set<BlockPos> secondaryPositions = new HashSet<>();

        AgesMining.LOGGER.debug("Collapse started at {}", centerPos);

        for (BlockPos pos : BlockPos.betweenClosed(
            centerPos.offset(-radius, -4, -radius),
            centerPos.offset(radius, -4, radius)
        )) {
            boolean foundEmpty = false;
            for (int y = 0; y <= 8; y++) {
                BlockPos posAt = pos.above(y);
                BlockState stateAt = level.getBlockState(posAt);

                if (foundEmpty && stateAt.is(ModTags.Blocks.CAN_COLLAPSE)) {
                    if (posAt.distSqr(centerPos) < radiusSquared
                        && level.getRandom().nextDouble() < AgesMiningConfig.INSTANCE.COLLAPSE_PROPAGATE_CHANCE.get()) {
                        if (collapseBlock(level, posAt, stateAt, true)) {
                            secondaryPositions.add(posAt.above().immutable());
                            break;
                        }
                    }
                }

                foundEmpty = !stateAt.isCollisionShapeFullBlock(level, posAt);
            }
        }

        if (!secondaryPositions.isEmpty()) {
            collapsesInProgress.add(new CollapseWave(centerPos.immutable(), secondaryPositions, radiusSquared));
            triggerCollapseEffects(level, centerPos, 3.5f);
            return true;
        }
        return false;
    }

    public boolean isSupported(ServerLevel level, BlockPos pos) {
        return SupportDataManager.INSTANCE.isSupported(level, pos);
    }

    public int getPendingCollapseCount() {
        int total = 0;
        for (CollapseWave wave : collapsesInProgress) {
            total += wave.nextPositions.size();
        }
        return total;
    }

    public int getPendingCheckCount() {
        return 0;
    }

    private boolean canStartCollapse(LevelAccessor level, BlockPos pos) {
        BlockPos posBelow = pos.below();
        BlockState state = level.getBlockState(pos);
        BlockState stateBelow = level.getBlockState(posBelow);

        return state.is(ModTags.Blocks.CAN_START_COLLAPSE)
            && (canFallThrough(level, posBelow, stateBelow, Direction.DOWN)
            || !stateBelow.isCollisionShapeFullBlock(level, posBelow)
            || stateBelow.is(ModTags.Blocks.NON_STRUCTURAL));
    }

    private boolean collapseBlock(ServerLevel level, BlockPos pos, BlockState state, boolean destroyBlockBelow) {
        if (!isCollapsible(state)) return false;

        BlockPos posBelow = pos.below();
        if (destroyBlockBelow && !canFallThrough(level, posBelow, level.getBlockState(posBelow), Direction.DOWN)) {
            level.destroyBlock(posBelow, true);
        }

        FallingBlockHelper.startFalling(level, pos, state);
        level.removeBlock(pos, false);
        return true;
    }

    private boolean canFallDown(ServerLevel level, BlockPos pos) {
        return canFallThrough(level, pos.below(), level.getBlockState(pos.below()), Direction.DOWN);
    }

    private boolean canFallThrough(LevelAccessor level, BlockPos pos, BlockState state, Direction fallingDirection) {
        return !state.isFaceSturdy(level, pos, fallingDirection.getOpposite());
    }

    private boolean isCollapsible(BlockState state) {
        if (state.isAir()) return false;
        if (state.is(ModBlocks.MINE_SUPPORT_PILLAR.get())) return false;
        if (state.is(ModBlocks.MINE_SUPPORT_BEAM.get())) return false;
        if (state.is(ModTags.Blocks.NON_COLLAPSIBLE)) return false;
        return true;
    }

    private void triggerCollapseEffects(ServerLevel level, BlockPos pos, float shakeIntensity) {
        if (AgesMiningConfig.INSTANCE.ENABLE_SOUNDS.get()) {
            EffectsHelper.playCaveInSound(level, pos);
        }
        if (AgesMiningConfig.INSTANCE.ENABLE_SCREEN_SHAKE.get()) {
            NetworkHandler.sendShakeToNear(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                24.0,
                shakeIntensity
            );
        }
    }

    private static final class CollapseWave {
        private final BlockPos centerPos;
        private final Set<BlockPos> nextPositions;
        private double radiusSquared;

        private CollapseWave(BlockPos centerPos, Set<BlockPos> nextPositions, double radiusSquared) {
            this.centerPos = centerPos;
            this.nextPositions = nextPositions;
            this.radiusSquared = radiusSquared;
        }
    }
}

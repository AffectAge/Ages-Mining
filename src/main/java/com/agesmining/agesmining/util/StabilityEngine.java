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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Collapse simulation modelled after TFC:
 * - mining/chiseling attempts a local unsupported scan
 * - one position starts a collapse wave
 * - wave propagates upwards and to nearby positions
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
    private final ArrayDeque<PendingCollapseStart> pendingCollapseStarts = new ArrayDeque<>();

    public void tick(ServerLevel level) {
        processPendingStarts(level);
        if (collapsesInProgress.isEmpty()) {
            return;
        }

        int propagationRadius = Math.max(0, AgesMiningConfig.INSTANCE.COLLAPSE_PROPAGATION_RADIUS.get());
        int maxBlocksPerCollapse = Math.max(1, AgesMiningConfig.INSTANCE.MAX_BLOCKS_PER_COLLAPSE.get());

        for (CollapseWave wave : collapsesInProgress) {
            if (wave.collapsedBlocks >= maxBlocksPerCollapse) {
                wave.nextPositions.clear();
                continue;
            }

            Set<BlockPos> updatedPositions = new HashSet<>();
            for (BlockPos posAt : wave.nextPositions) {
                if (wave.collapsedBlocks >= maxBlocksPerCollapse) {
                    break;
                }

                BlockState stateAt = level.getBlockState(posAt);
                if (!stateAt.is(ModTags.Blocks.CAN_COLLAPSE)) {
                    continue;
                }
                if (isSupported(level, posAt)) {
                    continue;
                }
                if (!canFallDown(level, posAt)) {
                    continue;
                }
                if (posAt.distSqr(wave.centerPos) >= wave.radiusSquared) {
                    continue;
                }
                if (level.getRandom().nextDouble() >= AgesMiningConfig.INSTANCE.COLLAPSE_PROPAGATE_CHANCE.get()) {
                    continue;
                }

                if (collapseBlock(level, posAt, stateAt, false)) {
                    wave.collapsedBlocks++;
                    addPropagationTargets(updatedPositions, posAt, propagationRadius);
                }
            }

            wave.nextPositions.clear();
            if (!updatedPositions.isEmpty() && wave.collapsedBlocks < maxBlocksPerCollapse) {
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
        if (isSupported(level, pos)) return false;

        boolean realCollapse = level.getRandom().nextDouble() < AgesMiningConfig.INSTANCE.COLLAPSE_TRIGGER_CHANCE.get();
        boolean fakeCollapse = !realCollapse && level.getRandom().nextDouble() < AgesMiningConfig.INSTANCE.COLLAPSE_FAKE_TRIGGER_CHANCE.get();
        if (!realCollapse && !fakeCollapse) return false;

        int checkRadius = Math.max(1, AgesMiningConfig.INSTANCE.CHECK_RADIUS.get());
        double baseCollapseChance = AgesMiningConfig.INSTANCE.BASE_COLLAPSE_CHANCE.get();

        List<BlockPos> fakeCollapseStarts = new ArrayList<>();
        Set<BlockPos> unsupported = SupportDataManager.INSTANCE.findUnsupportedPositions(
            level,
            pos.offset(-checkRadius, -checkRadius, -checkRadius),
            pos.offset(checkRadius, checkRadius, checkRadius)
        );

        for (BlockPos checking : unsupported) {
            if (checking.equals(pos)) continue;
            if (!canStartCollapse(level, checking)) continue;

            if (fakeCollapse) {
                fakeCollapseStarts.add(checking.immutable());
                continue;
            }

            if (level.getRandom().nextDouble() >= baseCollapseChance) {
                continue;
            }

            if (startCollapse(level, checking)) {
                return true;
            }
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

    public boolean tryTriggerSupportBreakCollapse(ServerLevel level, BlockPos pos) {
        if (!AgesMiningConfig.INSTANCE.CAVE_INS_ENABLED.get()) return false;
        if (pos.getY() > AgesMiningConfig.INSTANCE.MIN_DEPTH_FOR_COLLAPSE.get()) return false;

        int radius = 4 + level.getRandom().nextInt(5); // 4..8
        double chancePerCandidate = AgesMiningConfig.INSTANCE.SUPPORT_BREAK_COLLAPSE_CHANCE.get();

        Set<BlockPos> unsupported = SupportDataManager.INSTANCE.findUnsupportedPositions(
            level,
            pos.offset(-radius, -radius, -radius),
            pos.offset(radius, radius, radius)
        );

        for (BlockPos candidate : unsupported) {
            if (!canStartCollapse(level, candidate)) continue;
            if (isSupported(level, candidate)) continue;
            if (level.getRandom().nextDouble() >= chancePerCandidate) continue;

            if (startCollapse(level, candidate)) {
                return true;
            }
        }
        return false;
    }

    public boolean startCollapse(ServerLevel level, BlockPos centerPos) {
        int delayTicks = Math.max(0, AgesMiningConfig.INSTANCE.COLLAPSE_DELAY_TICKS.get());
        if (delayTicks == 0) {
            return startCollapseNow(level, centerPos);
        }

        long dueTime = level.getGameTime() + delayTicks;
        pendingCollapseStarts.addLast(new PendingCollapseStart(centerPos.immutable(), dueTime));

        if (AgesMiningConfig.INSTANCE.ENABLE_WARNING_PARTICLES.get()) {
            EffectsHelper.spawnDustWarning(level, centerPos);
        }
        return true;
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
        return pendingCollapseStarts.size();
    }

    private void processPendingStarts(ServerLevel level) {
        if (pendingCollapseStarts.isEmpty()) {
            return;
        }

        long now = level.getGameTime();
        Iterator<PendingCollapseStart> iterator = pendingCollapseStarts.iterator();
        while (iterator.hasNext()) {
            PendingCollapseStart pending = iterator.next();
            if (pending.dueTime > now) {
                continue;
            }
            iterator.remove();
            startCollapseNow(level, pending.centerPos);
        }
    }

    private boolean startCollapseNow(ServerLevel level, BlockPos centerPos) {
        int variance = Math.max(1, AgesMiningConfig.INSTANCE.COLLAPSE_RADIUS_VARIANCE.get());
        int radius = AgesMiningConfig.INSTANCE.COLLAPSE_MIN_RADIUS.get() + level.getRandom().nextInt(variance);
        int radiusSquared = radius * radius;
        int maxBlocksPerCollapse = Math.max(1, AgesMiningConfig.INSTANCE.MAX_BLOCKS_PER_COLLAPSE.get());
        int propagationRadius = Math.max(0, AgesMiningConfig.INSTANCE.COLLAPSE_PROPAGATION_RADIUS.get());

        Set<BlockPos> secondaryPositions = new HashSet<>();
        int collapsedBlocks = 0;

        AgesMining.LOGGER.debug("Collapse started at {}", centerPos);

        for (BlockPos pos : BlockPos.betweenClosed(
            centerPos.offset(-radius, -4, -radius),
            centerPos.offset(radius, -4, radius)
        )) {
            if (collapsedBlocks >= maxBlocksPerCollapse) {
                break;
            }

            boolean foundEmpty = false;
            for (int y = 0; y <= 8; y++) {
                if (collapsedBlocks >= maxBlocksPerCollapse) {
                    break;
                }

                BlockPos posAt = pos.above(y);
                BlockState stateAt = level.getBlockState(posAt);

                if (foundEmpty && stateAt.is(ModTags.Blocks.CAN_COLLAPSE)) {
                    if (isSupported(level, posAt)) {
                        continue;
                    }
                    if (posAt.distSqr(centerPos) < radiusSquared
                        && level.getRandom().nextDouble() < AgesMiningConfig.INSTANCE.COLLAPSE_PROPAGATE_CHANCE.get()) {
                        if (collapseBlock(level, posAt, stateAt, true)) {
                            collapsedBlocks++;
                            addPropagationTargets(secondaryPositions, posAt, propagationRadius);
                            break;
                        }
                    }
                }

                foundEmpty = !stateAt.isCollisionShapeFullBlock(level, posAt);
            }
        }

        if (!secondaryPositions.isEmpty() && collapsedBlocks > 0) {
            collapsesInProgress.add(new CollapseWave(centerPos.immutable(), secondaryPositions, radiusSquared, collapsedBlocks));
            triggerCollapseEffects(level, centerPos, 3.5f);
            return true;
        }
        return false;
    }

    private void addPropagationTargets(Set<BlockPos> out, BlockPos collapsedPos, int radius) {
        BlockPos above = collapsedPos.above();
        out.add(above.immutable());
        if (radius <= 0) {
            return;
        }

        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if ((dx * dx + dz * dz) > r2) {
                    continue;
                }
                out.add(above.offset(dx, 0, dz).immutable());
            }
        }
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

    private record PendingCollapseStart(BlockPos centerPos, long dueTime) {}

    private static final class CollapseWave {
        private final BlockPos centerPos;
        private final Set<BlockPos> nextPositions;
        private double radiusSquared;
        private int collapsedBlocks;

        private CollapseWave(BlockPos centerPos, Set<BlockPos> nextPositions, double radiusSquared, int collapsedBlocks) {
            this.centerPos = centerPos;
            this.nextPositions = nextPositions;
            this.radiusSquared = radiusSquared;
            this.collapsedBlocks = collapsedBlocks;
        }
    }
}

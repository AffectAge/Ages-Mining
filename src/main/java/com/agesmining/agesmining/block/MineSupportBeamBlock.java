package com.agesmining.agesmining.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Mine Support Beam — a horizontal structural beam.
 * Stretches across the ceiling to provide lateral support.
 * Placed at ceiling level; connects to pillars at ends.
 */
public class MineSupportBeamBlock extends Block {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty CONNECTED_NEGATIVE = BooleanProperty.create("connected_negative");
    public static final BooleanProperty CONNECTED_POSITIVE = BooleanProperty.create("connected_positive");

    // Beam shapes: a flat plank along each horizontal axis
    // 16x4x4 along X, 4x4x16 along Z, placed near top of block
    private static final VoxelShape SHAPE_X = Block.box(0, 12, 6, 16, 16, 10);
    private static final VoxelShape SHAPE_Z = Block.box(6, 12, 0, 10, 16, 16);

    public MineSupportBeamBlock() {
        super(Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.0f, 2.5f)
            .sound(SoundType.WOOD)
            .noOcclusion()
            .requiresCorrectToolForDrops()
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(AXIS, Direction.Axis.X)
            .setValue(CONNECTED_NEGATIVE, false)
            .setValue(CONNECTED_POSITIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, CONNECTED_NEGATIVE, CONNECTED_POSITIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Align beam to player's facing horizontal axis
        Direction facing = ctx.getHorizontalDirection();
        Direction.Axis axis = facing.getAxis();
        // Default to X if not horizontal (shouldn't happen)
        if (axis == Direction.Axis.Y) axis = Direction.Axis.X;
        BlockState base = this.defaultBlockState().setValue(AXIS, axis);
        return updateConnections(base, ctx.getLevel(), ctx.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                   net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if (!facing.getAxis().isHorizontal()) {
            return state;
        }
        return updateConnections(state, level, pos);
    }

    private BlockState updateConnections(BlockState state, net.minecraft.world.level.LevelAccessor level, BlockPos pos) {
        Direction.Axis axis = state.getValue(AXIS);
        Direction negative = axis == Direction.Axis.X ? Direction.WEST : Direction.NORTH;
        Direction positive = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;

        boolean negConnected = connectsTo(level.getBlockState(pos.relative(negative)), axis);
        boolean posConnected = connectsTo(level.getBlockState(pos.relative(positive)), axis);

        return state
            .setValue(CONNECTED_NEGATIVE, negConnected)
            .setValue(CONNECTED_POSITIVE, posConnected);
    }

    private boolean connectsTo(BlockState state, Direction.Axis axis) {
        if (state.getBlock() instanceof MineSupportPillarBlock) return true;
        return state.is(this) && state.getValue(AXIS) == axis;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}

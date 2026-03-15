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
            .setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Align beam to player's facing horizontal axis
        Direction facing = ctx.getHorizontalDirection();
        Direction.Axis axis = facing.getAxis();
        // Default to X if not horizontal (shouldn't happen)
        if (axis == Direction.Axis.Y) axis = Direction.Axis.X;
        return this.defaultBlockState().setValue(AXIS, axis);
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

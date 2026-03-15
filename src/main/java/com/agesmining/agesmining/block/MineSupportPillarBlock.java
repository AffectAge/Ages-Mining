package com.agesmining.agesmining.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * Mine Support Pillar — a vertical structural support.
 * Provides stability in a column above and below the placed block.
 * Visually: a thick wooden post (8x16x8 px center column).
 */
public class MineSupportPillarBlock extends RotatedPillarBlock {

    public static final BooleanProperty CONNECTED_UP = BooleanProperty.create("connected_up");
    public static final BooleanProperty CONNECTED_DOWN = BooleanProperty.create("connected_down");

    // Pillar shape: 6x16x6 pixel column centered
    private static final VoxelShape SHAPE_Y = Block.box(5, 0, 5, 11, 16, 11);
    private static final VoxelShape SHAPE_X = Block.box(0, 5, 5, 16, 11, 11);
    private static final VoxelShape SHAPE_Z = Block.box(5, 5, 0, 11, 11, 16);

    public MineSupportPillarBlock() {
        super(Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.5f, 3.0f)
            .sound(SoundType.WOOD)
            .noOcclusion()
            .requiresCorrectToolForDrops()
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(AXIS, Direction.Axis.Y)
            .setValue(CONNECTED_UP, false)
            .setValue(CONNECTED_DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, CONNECTED_UP, CONNECTED_DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction.Axis axis = ctx.getClickedFace().getAxis();
        BlockPos pos = ctx.getClickedPos();
        BlockGetter level = ctx.getLevel();

        boolean connUp = level.getBlockState(pos.above()).is(this);
        boolean connDown = level.getBlockState(pos.below()).is(this);

        return this.defaultBlockState()
            .setValue(AXIS, axis)
            .setValue(CONNECTED_UP, connUp)
            .setValue(CONNECTED_DOWN, connDown);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                   net.minecraft.world.level.LevelAccessor level,
                                   BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.UP) {
            return state.setValue(CONNECTED_UP, facingState.is(this));
        }
        if (facing == Direction.DOWN) {
            return state.setValue(CONNECTED_DOWN, facingState.is(this));
        }
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(AXIS)) {
            case X -> SHAPE_X;
            case Z -> SHAPE_Z;
            default -> SHAPE_Y;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}

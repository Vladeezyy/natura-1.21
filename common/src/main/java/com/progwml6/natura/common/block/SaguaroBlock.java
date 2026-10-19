package com.progwml6.natura.common.block;

import java.util.Map;

import com.mojang.serialization.MapCodec;
import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockSaguaro: a cactus that connects to its neighbours (other saguaro and opaque full blocks) on all six sides,
 * hurts what touches it (1 damage) and, while it rains with air above it, grows saguaro fruit on a free side (1 in 20
 * random ticks). It is placed on sand or saguaro, but does not break when that goes away (1.12 checked placement only).
 */
public class SaguaroBlock extends Block {
    public static final Map<Direction, BooleanProperty> PROPERTIES = PipeBlock.PROPERTY_BY_DIRECTION;
    private static final VoxelShape NORMAL = Block.box(2, 0, 2, 14, 14, 14);
    private static final VoxelShape UP = Block.box(2, 0, 2, 14, 16, 14);

    public SaguaroBlock(Properties properties) {
        super(properties);
        BlockState state = this.stateDefinition.any();
        for (BooleanProperty p : PROPERTIES.values()) {
            state = state.setValue(p, false);
        }
        this.registerDefaultState(state);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROPERTIES.values().toArray(new BooleanProperty[0]));
    }

    /** 1.12 canConnectTo: another saguaro, or an opaque full block (not a barrier, pumpkin or melon). */
    private static boolean connects(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.BARRIER)) {
            return false;
        }
        if (state.getBlock() instanceof SaguaroBlock) {
            return true;
        }
        return state.isSolidRender(level, pos) && !state.is(Blocks.PUMPKIN) && !state.is(Blocks.CARVED_PUMPKIN) && !state.is(Blocks.JACK_O_LANTERN)
                && !state.is(Blocks.MELON);
    }

    public BlockState withConnections(BlockGetter level, BlockPos pos) {
        BlockState state = this.defaultBlockState();
        for (Direction dir : Direction.values()) {
            state = state.setValue(PROPERTIES.get(dir), connects(level, pos.relative(dir)));
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!this.canSurvive(this.defaultBlockState(), context.getLevel(), context.getClickedPos())) {
            return null;
        }
        return this.withConnections(context.getLevel(), context.getClickedPos());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof SaguaroBlock || below.is(Blocks.SAND) || below.is(Blocks.RED_SAND);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(PROPERTIES.get(direction), connects(level, neighborPos));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(PipeBlock.UP) ? UP : NORMAL;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return NORMAL;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof ItemEntity)) {
            entity.hurt(level.damageSources().cactus(), 1.0F);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getLevelData().isRaining() && level.isEmptyBlock(pos.above())) {
            boolean canGrow = random.nextInt(20) == 0;
            if (com.progwml6.natura.platform.Services.PLATFORM.canCropGrow(level, pos, state, canGrow)) {
                Direction side = switch (random.nextInt(4)) {
                    case 0 -> Direction.NORTH;
                    case 1 -> Direction.EAST;
                    case 2 -> Direction.SOUTH;
                    default -> Direction.WEST;
                };
                if (level.isEmptyBlock(pos.relative(side))) {
                    level.setBlock(pos.relative(side), NaturaBlocks.SAGUARO_FRUIT.get().defaultBlockState()
                            .setValue(SaguaroFruitBlock.FACING, side.getOpposite()), Block.UPDATE_ALL);
                }
                com.progwml6.natura.platform.Services.PLATFORM.cropGrown(level, pos, state);
            }
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState out = state;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            out = out.setValue(PROPERTIES.get(rotation.rotate(dir)), state.getValue(PROPERTIES.get(dir)));
        }
        return out;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState out = state;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            out = out.setValue(PROPERTIES.get(mirror.mirror(dir)), state.getValue(PROPERTIES.get(dir)));
        }
        return out;
    }
}

package com.progwml6.natura.common.block;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockSaguaroFruit: the fruit hanging on a saguaro side (FACING = towards the cactus). Placed by hand it faces the
 * way the player looks (1.12 onBlockPlacedBy). It drops the saguaro fruit food.
 */
public class SaguaroFruitBlock extends HorizontalDirectionalBlock {
    private static final VoxelShape EAST = Block.box(10, 3, 4, 18, 12, 12);
    private static final VoxelShape WEST = Block.box(-2, 3, 4, 6, 12, 12);
    private static final VoxelShape SOUTH = Block.box(4, 3, 10, 12, 12, 18);
    private static final VoxelShape NORTH = Block.box(4, 3, -2, 12, 12, 6);

    public SaguaroFruitBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (!facing.getAxis().isHorizontal()) {
            facing = Direction.NORTH;
        }
        return this.defaultBlockState().setValue(FACING, facing.getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            level.setBlock(pos, state.setValue(FACING, Direction.fromYRot(placer.getYRot())), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> NORTH;
        };
    }
}

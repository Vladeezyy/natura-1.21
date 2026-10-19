package com.progwml6.natura.common.block;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;

/**
 * 1.12 BlockNetherLog2 metas 0-11: a quarter of the 2x2 bloodwood trunk. PIECE = 1.12 meta: 0-3 upright (Y), 4-7
 * lying east-west, 8-11 lying north-south, the low two bits pick the quarter. Placing picks the piece from where the
 * clicked face was hit, with the 1.12 coordinates (hitX / hitZ relative to the clicked block, also on side faces where
 * one of them is fixed). Metas 12-15 (all bark) are the separate bloodwood_bark block.
 */
public class BloodwoodLogBlock extends Block {
    public static final IntegerProperty PIECE = IntegerProperty.create("piece", 0, 11);

    public BloodwoodLogBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PIECE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PIECE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        BlockPos clicked = context.replacingClickedOnBlock() ? context.getClickedPos() : context.getClickedPos().relative(face.getOpposite());
        Vec3 hit = context.getClickLocation().subtract(clicked.getX(), clicked.getY(), clicked.getZ());
        float hitX = (float) hit.x;
        float hitZ = (float) hit.z;
        int add = 0;
        switch (face) {
            case DOWN, UP -> {
                if (hitX > 0.5F) add += 1;
                if (hitZ > 0.5F) add += 2;
            }
            case NORTH, SOUTH -> {
                add = 8;
                if (hitX > 0.5F) add += 1;
                if (hitZ < 0.5F) add += 2;
            }
            case WEST, EAST -> {
                add = 4;
                if (hitX < 0.5F) add += 1;
                if (hitZ < 0.5F) add += 2;
            }
        }
        return this.defaultBlockState().setValue(PIECE, add);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        RedwoodLeavesBlock.beginLeavesDecay(level, pos, 4);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}

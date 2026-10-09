package com.progwml6.natura.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 BlockEnumLog: a rotatable log. Breaking it starts the leaves decay check around it (BlockLog.breakBlock). */
public class NaturaLogBlock extends RotatedPillarBlock {
    public NaturaLogBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            RedwoodLeavesBlock.beginLeavesDecay(level, pos, 4);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}

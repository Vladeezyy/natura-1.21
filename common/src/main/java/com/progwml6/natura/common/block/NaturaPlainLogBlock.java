package com.progwml6.natura.common.block;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** A log block without an axis (1.12 redwood bark / heart / root, bloodwood bark), still a log for leaves. */
public class NaturaPlainLogBlock extends Block {
    public NaturaPlainLogBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        RedwoodLeavesBlock.beginLeavesDecay(level, pos, 4);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}

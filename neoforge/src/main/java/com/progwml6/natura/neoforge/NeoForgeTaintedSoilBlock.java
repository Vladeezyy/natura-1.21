package com.progwml6.natura.neoforge;

import com.progwml6.natura.common.block.TaintedSoilBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.TriState;

/** Tainted soil on NeoForge: it sustains the nether plants through IBlockExtension.canSustainPlant. */
public class NeoForgeTaintedSoilBlock extends TaintedSoilBlock {
    public NeoForgeTaintedSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return sustains(plant) ? TriState.TRUE : TriState.DEFAULT;
    }
}

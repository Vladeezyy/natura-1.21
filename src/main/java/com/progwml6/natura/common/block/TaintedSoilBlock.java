package com.progwml6.natura.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;

/** 1.12 BlockTaintedSoil (soil, dry and heated farmland): nether plants grow on it and it burns forever like netherrack. */
public class TaintedSoilBlock extends Block {
    public TaintedSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        if (plant.getBlock() instanceof net.minecraft.world.level.block.NetherWartBlock || plant.getBlock() instanceof NaturaGlowshroomBlock
                || plant.getBlock() instanceof net.minecraft.world.level.block.FungusBlock || plant.getBlock() instanceof net.minecraft.world.level.block.RootsBlock) {
            return TriState.TRUE;
        }
        return TriState.DEFAULT;
    }
}

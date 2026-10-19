package com.progwml6.natura.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.RootsBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 BlockTaintedSoil (soil, dry and heated farmland): nether plants grow on it and it burns forever like netherrack
 * (also in the infiniburn tags). NeoForge: canSustainPlant in its subclass; Fabric: mixins into the nether plants.
 */
public class TaintedSoilBlock extends Block {
    public TaintedSoilBlock(Properties properties) {
        super(properties);
    }

    /** NeoForge IBlockExtension.isFireSource (same signature, no NeoForge types). */
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return true;
    }

    /** The plants tainted soil sustains: nether wart, glowshrooms, fungi and roots. */
    public static boolean sustains(BlockState plant) {
        return plant.getBlock() instanceof NetherWartBlock || plant.getBlock() instanceof NaturaGlowshroomBlock
                || plant.getBlock() instanceof FungusBlock || plant.getBlock() instanceof RootsBlock;
    }
}

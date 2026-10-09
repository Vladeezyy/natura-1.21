package com.progwml6.natura.world.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 GhostwoodTreeGenerator: the same big tree as sakura, on nether soil. */
public class GhostwoodTreeGenerator extends SakuraTreeGenerator {
    public final boolean seekHeight;

    public GhostwoodTreeGenerator(BlockState log, BlockState leaves, boolean seekHeight) {
        super(log, leaves, false, false);
        this.seekHeight = seekHeight;
    }

    @Override
    protected boolean isSoil(BlockState state) {
        return TreeWorld.isNetherSoil(state);
    }

    /** Down to the first netherrack / soul sand / tainted soil (or below the world). */
    @Override
    BlockPos findGround(TreeWorld world, BlockPos pos) {
        boolean foundGround = false;
        BlockPos position = pos;
        do {
            position = position.below();
            BlockState under = world.getBlockState(position);
            if (TreeWorld.isNetherSoil(under) || position.getY() < world.bottom()) {
                foundGround = true;
            }
        } while (!foundGround);
        return position.above();
    }

    @Override
    protected BlockPos startPos(TreeWorld world, BlockPos position) {
        return this.seekHeight ? this.findGround(world, position) : position;
    }
}

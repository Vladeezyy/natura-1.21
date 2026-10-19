package com.progwml6.natura.world.tree;

import java.util.Random;

import net.minecraft.core.BlockPos;

/** 1.12 BaseTreeGenerator. */
public abstract class NaturaTreeGenerator {
    public abstract void generateTree(Random random, TreeWorld world, BlockPos pos);

    /** 1.12 BlockPos.add(double, double, double): floors the sum. */
    static BlockPos add(BlockPos pos, double x, double y, double z) {
        return x == 0.0D && y == 0.0D && z == 0.0D ? pos : BlockPos.containing(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }
}

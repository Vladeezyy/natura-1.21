package com.progwml6.natura.world.gen;

import java.util.Random;

import com.progwml6.natura.world.tree.TreeWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 CloudsGenerator: a lumpy cloud of `cloudSize` overlapping puffs, only into air. */
public class CloudsGenerator {
    public final int cloudSize;
    public final BlockState cloud;
    public final boolean flatCloud;

    public CloudsGenerator(int cloudSize, BlockState cloud) {
        this.cloudSize = cloudSize;
        this.cloud = cloud;
        this.flatCloud = false;
    }

    public void generateCloud(Random random, TreeWorld world, BlockPos pos) {
        int xRand = random.nextInt(3) - 1;
        int zRand = random.nextInt(3) - 1;
        for (int block = 0; block < this.cloudSize; block++) {
            int xIter = pos.getX() + (random.nextInt(3) - 1) + xRand;
            int yIter = pos.getY();
            int zIter = pos.getZ() + (random.nextInt(3) - 1) + zRand;
            if (random.nextBoolean() && !this.flatCloud || this.flatCloud && random.nextInt(10) == 0) {
                yIter = pos.getY() + random.nextInt(3) - 1;
            }
            for (int x = xIter; x < xIter + random.nextInt(4) + 3 * (this.flatCloud ? 3 : 1); x++) {
                int mathX = xIter - x;
                for (int y = yIter; y < yIter + random.nextInt(1) + 2; y++) {
                    int mathY = yIter - y;
                    for (int z = zIter; z < zIter + random.nextInt(4) + 3 * (this.flatCloud ? 3 : 1); z++) {
                        int mathZ = zIter - z;
                        if (Math.abs(mathX) + Math.abs(mathY) + Math.abs(mathZ) < 4 * (this.flatCloud ? 3 : 1) + random.nextInt(2)) {
                            BlockPos blockpos = new BlockPos(x, y, z);
                            if (world.getBlockState(blockpos).isAir()) {
                                world.setBlockState(blockpos, this.cloud);
                            }
                        }
                    }
                }
            }
        }
    }
}

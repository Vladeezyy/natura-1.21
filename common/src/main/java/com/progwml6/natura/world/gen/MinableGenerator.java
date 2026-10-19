package com.progwml6.natura.world.gen;

import java.util.Random;
import java.util.function.Predicate;

import com.progwml6.natura.world.tree.TreeWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

/** The 1.12 vanilla WorldGenMinable vein (tainted soil in netherrack, heat sand in soul sand). */
public class MinableGenerator {
    private final BlockState ore;
    private final int numberOfBlocks;
    private final Predicate<BlockState> target;

    public MinableGenerator(BlockState ore, int numberOfBlocks, Predicate<BlockState> target) {
        this.ore = ore;
        this.numberOfBlocks = numberOfBlocks;
        this.target = target;
    }

    public void generate(TreeWorld world, Random rand, BlockPos position) {
        float f = rand.nextFloat() * (float) Math.PI;
        double d0 = (float) (position.getX() + 8) + Mth.sin(f) * (float) this.numberOfBlocks / 8.0F;
        double d1 = (float) (position.getX() + 8) - Mth.sin(f) * (float) this.numberOfBlocks / 8.0F;
        double d2 = (float) (position.getZ() + 8) + Mth.cos(f) * (float) this.numberOfBlocks / 8.0F;
        double d3 = (float) (position.getZ() + 8) - Mth.cos(f) * (float) this.numberOfBlocks / 8.0F;
        double d4 = position.getY() + rand.nextInt(3) - 2;
        double d5 = position.getY() + rand.nextInt(3) - 2;
        for (int i = 0; i < this.numberOfBlocks; ++i) {
            float f1 = (float) i / (float) this.numberOfBlocks;
            double d6 = d0 + (d1 - d0) * f1;
            double d7 = d4 + (d5 - d4) * f1;
            double d8 = d2 + (d3 - d2) * f1;
            double d9 = rand.nextDouble() * this.numberOfBlocks / 16.0D;
            double d10 = (Mth.sin((float) Math.PI * f1) + 1.0F) * d9 + 1.0D;
            double d11 = (Mth.sin((float) Math.PI * f1) + 1.0F) * d9 + 1.0D;
            int j = Mth.floor(d6 - d10 / 2.0D);
            int k = Mth.floor(d7 - d11 / 2.0D);
            int l = Mth.floor(d8 - d10 / 2.0D);
            int i1 = Mth.floor(d6 + d10 / 2.0D);
            int j1 = Mth.floor(d7 + d11 / 2.0D);
            int k1 = Mth.floor(d8 + d10 / 2.0D);
            for (int l1 = j; l1 <= i1; ++l1) {
                double d12 = (l1 + 0.5D - d6) / (d10 / 2.0D);
                if (d12 * d12 < 1.0D) {
                    for (int i2 = k; i2 <= j1; ++i2) {
                        double d13 = (i2 + 0.5D - d7) / (d11 / 2.0D);
                        if (d12 * d12 + d13 * d13 < 1.0D) {
                            for (int j2 = l; j2 <= k1; ++j2) {
                                double d14 = (j2 + 0.5D - d8) / (d10 / 2.0D);
                                if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) {
                                    BlockPos blockpos = new BlockPos(l1, i2, j2);
                                    if (this.target.test(world.getBlockState(blockpos))) {
                                        world.setBlockState(blockpos, this.ore);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

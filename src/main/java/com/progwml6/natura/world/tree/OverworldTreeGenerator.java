package com.progwml6.natura.world.tree;

import java.util.Random;

import com.progwml6.natura.common.NaturaConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 OverworldTreeGenerator (maple, silverbell, amaranth, tiger): a vanilla-like small tree. Its space check never
 * fails (1.12 set canGrowTree = true in every branch), so it grows through whatever is there.
 */
public class OverworldTreeGenerator extends NaturaTreeGenerator {
    public final int minTreeHeight;
    public final int treeHeightRange;
    public final BlockState log;
    public final BlockState leaves;
    public final boolean seekHeight;
    public final boolean isSapling;

    public OverworldTreeGenerator(int treeHeight, int treeRange, BlockState log, BlockState leaves, boolean seekHeight, boolean isSapling) {
        this.minTreeHeight = treeHeight;
        this.treeHeightRange = treeRange;
        this.log = log;
        this.leaves = leaves;
        this.seekHeight = seekHeight;
        this.isSapling = isSapling;
    }

    @Override
    public void generateTree(Random rand, TreeWorld world, BlockPos position) {
        int heightRange = rand.nextInt(this.treeHeightRange) + this.minTreeHeight;
        if (this.seekHeight) {
            position = this.findGround(world, position);
            if (position.getY() < world.bottom()) {
                return;
            }
        }
        if (position.getY() >= world.bottom() + 1 && position.getY() + heightRange + 1 <= world.top()) {
            BlockState soil = world.getBlockState(position.below());
            if (TreeWorld.isPlainsSoil(soil)) {
                world.onPlantGrow(position.below());
                this.placeCanopy(world, rand, position, heightRange);
                this.placeTrunk(world, position, heightRange);
            }
        }
    }

    /** Down from the sapling to the first dirt / grass with a non-full block above, not lower than the sea level. */
    BlockPos findGround(TreeWorld world, BlockPos pos) {
        int returnHeight = world.bottom();
        int height = pos.getY();
        int seaLevel = world.isFlat() && this.isSapling ? NaturaConfig.flatSeaLevel(world) : NaturaConfig.SEA_LEVEL.get();
        do {
            BlockPos position = new BlockPos(pos.getX(), height, pos.getZ());
            BlockState state = world.getBlockState(position);
            if (TreeWorld.isDirtOrGrass(state) && !world.isFullBlock(world.getBlockState(position.above()), position.above())) {
                returnHeight = height + 1;
                break;
            }
            height--;
        } while (height > seaLevel);
        return new BlockPos(pos.getX(), returnHeight, pos.getZ());
    }

    protected void placeCanopy(TreeWorld world, Random random, BlockPos pos, int height) {
        for (int y = pos.getY() - 3 + height; y <= pos.getY() + height; ++y) {
            int subract = y - (pos.getY() + height);
            int subract2 = 1 - subract / 2;
            for (int x = pos.getX() - subract2; x <= pos.getX() + subract2; ++x) {
                int mathX = x - pos.getX();
                for (int z = pos.getZ() - subract2; z <= pos.getZ() + subract2; ++z) {
                    int mathZ = z - pos.getZ();
                    if (Math.abs(mathX) != subract2 || Math.abs(mathZ) != subract2 || random.nextInt(2) != 0 && subract != 0) {
                        BlockPos blockpos = new BlockPos(x, y, z);
                        BlockState state = world.getBlockState(blockpos);
                        if (TreeWorld.isAir(state) || world.canBeReplacedByLeaves(state, blockpos)) {
                            world.setBlockState(blockpos, this.leaves);
                        }
                    }
                }
            }
        }
    }

    protected void placeTrunk(TreeWorld world, BlockPos pos, int height) {
        for (int localHeight = 0; localHeight < height; ++localHeight) {
            BlockPos blockpos = new BlockPos(pos.getX(), pos.getY() + localHeight, pos.getZ());
            BlockState state = world.getBlockState(blockpos);
            if (TreeWorld.isAir(state) || TreeWorld.isLeaves(state) || TreeWorld.isReplaceable(state)) {
                world.setBlockState(blockpos, this.log);
            }
        }
    }
}

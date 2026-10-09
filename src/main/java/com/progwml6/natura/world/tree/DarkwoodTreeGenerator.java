package com.progwml6.natura.world.tree;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 DarkwoodTreeGenerator: a small nether tree whose leaves are sometimes flowering (1 in 15) or fruiting (1 in 25). */
public class DarkwoodTreeGenerator extends NaturaTreeGenerator {
    public final int minTreeHeight;
    public final BlockState log;
    public final BlockState leaves;
    public final BlockState flowering;
    public final BlockState fruiting;
    public final boolean seekHeight;

    public DarkwoodTreeGenerator(int treeHeight, BlockState log, BlockState leaves, BlockState flowering, BlockState fruiting, boolean seekHeight) {
        this.minTreeHeight = treeHeight;
        this.log = log;
        this.leaves = leaves;
        this.flowering = flowering;
        this.fruiting = fruiting;
        this.seekHeight = seekHeight;
    }

    BlockPos findGround(TreeWorld world, BlockPos pos) {
        boolean foundGround = false;
        BlockPos position = pos;
        do {
            position = position.below();
            if (TreeWorld.isNetherSoil(world.getBlockState(position)) || position.getY() < world.bottom()) {
                foundGround = true;
            }
        } while (!foundGround);
        return position.above();
    }

    @Override
    public void generateTree(Random rand, TreeWorld world, BlockPos position) {
        int heightRange = rand.nextInt(3) + this.minTreeHeight;
        if (heightRange < 4) {
            heightRange = 4;
        }
        if (this.seekHeight) {
            position = this.findGround(world, position);
            if (position.getY() < world.bottom()) {
                return;
            }
        }
        if (position.getY() >= world.bottom() + 1 && position.getY() + heightRange + 1 <= world.top()) {
            if (!this.checkIfCanGrow(position, heightRange, world)) {
                return;
            }
            BlockState soil = world.getBlockState(position.below());
            if (TreeWorld.isNetherSoil(soil) && position.getY() < world.top() - heightRange - 1) {
                world.onPlantGrow(position.below());
                this.placeCanopy(world, rand, position, heightRange);
                this.placeTrunk(world, position, heightRange);
            }
        }
    }

    private boolean checkIfCanGrow(BlockPos position, int heightRange, TreeWorld world) {
        boolean canGrowTree = true;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = position.getY(); y <= position.getY() + 1 + heightRange; ++y) {
            int range = 1;
            if (y == position.getY()) {
                range = 0;
            }
            if (y >= position.getY() + 1 + heightRange - 2) {
                range = 2;
            }
            for (int x = position.getX() - range; x <= position.getX() + range && canGrowTree; ++x) {
                for (int z = position.getZ() - range; z <= position.getZ() + range && canGrowTree; ++z) {
                    if (y >= world.bottom() && y < world.top()) {
                        pos.set(x, y, z);
                        BlockState state = world.getBlockState(pos);
                        if (!TreeWorld.isAir(state) && !TreeWorld.isLeaves(state) && !TreeWorld.isNetherSoil(state) && !TreeWorld.isWood(state)) {
                            canGrowTree = false;
                        }
                    } else {
                        canGrowTree = false;
                    }
                }
            }
        }
        return canGrowTree;
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
                        if (world.canBeReplacedByLeaves(world.getBlockState(blockpos), blockpos)) {
                            world.setBlockState(blockpos, this.getRandomizedLeaves(random));
                        }
                    }
                }
            }
        }
    }

    protected void placeTrunk(TreeWorld world, BlockPos pos, int height) {
        for (int localHeight = 0; localHeight < height; ++localHeight) {
            BlockPos blockpos = pos.above(localHeight);
            BlockState state = world.getBlockState(blockpos);
            if (TreeWorld.isAir(state) || TreeWorld.isLeaves(state)) {
                world.setBlockState(blockpos, this.log);
            }
        }
    }

    protected BlockState getRandomizedLeaves(Random random) {
        return random.nextInt(25) == 0 ? this.fruiting : random.nextInt(15) == 0 ? this.flowering : this.leaves;
    }
}

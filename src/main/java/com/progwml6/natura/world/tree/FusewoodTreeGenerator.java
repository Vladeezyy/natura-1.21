package com.progwml6.natura.world.tree;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 FusewoodTreeGenerator: a small nether tree that only grows where the space around it is clear. */
public class FusewoodTreeGenerator extends NaturaTreeGenerator {
    public final int minTreeHeight;
    public final BlockState log;
    public final BlockState leaves;
    public final boolean seekHeight;

    public FusewoodTreeGenerator(int treeHeight, BlockState log, BlockState leaves, boolean seekHeight) {
        this.minTreeHeight = treeHeight;
        this.log = log;
        this.leaves = leaves;
        this.seekHeight = seekHeight;
    }

    BlockPos findGround(TreeWorld world, BlockPos pos) {
        int returnHeight = world.bottom();
        int y = pos.getY() - 1;
        do {
            BlockPos position = new BlockPos(pos.getX(), y, pos.getZ());
            if (TreeWorld.isNetherSoil(world.getBlockState(position)) || position.getY() < world.bottom()) {
                returnHeight = y + 1;
                break;
            }
            y--;
        } while (y > world.bottom());
        return new BlockPos(pos.getX(), returnHeight, pos.getZ());
    }

    /** 1.12 name, inverted meaning: true when the block is in the way. */
    public boolean isReplaceable(TreeWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !TreeWorld.isAir(state) && !TreeWorld.isLeaves(state) && !TreeWorld.isNetherSoil(state) && !TreeWorld.isWood(state);
    }

    @Override
    public void generateTree(Random rand, TreeWorld world, BlockPos position) {
        int height = rand.nextInt(3) + this.minTreeHeight;
        if (height < 4) {
            height = 4;
        }
        if (this.seekHeight) {
            position = this.findGround(world, position);
            if (position.getY() < world.bottom()) {
                return;
            }
        }
        if (position.getY() >= world.bottom() + 1 && position.getY() + height + 1 <= world.top()) {
            if (!this.checkClear(world, position, height)) {
                return;
            }
            BlockPos down = position.below();
            if (TreeWorld.isNetherSoil(world.getBlockState(down)) && position.getY() < world.top() - height - 1) {
                world.onPlantGrow(down);
                this.placeCanopy(world, rand, position, height);
                this.placeTrunk(world, position, height);
            }
        }
    }

    boolean checkClear(TreeWorld world, BlockPos position, int height) {
        boolean flag = true;
        for (int j = position.getY(); j <= position.getY() + 1 + height; ++j) {
            int range = 1;
            if (j == position.getY()) {
                range = 0;
            }
            if (j >= position.getY() + 1 + height - 2) {
                range = 2;
            }
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (int l = position.getX() - range; l <= position.getX() + range && flag; ++l) {
                for (int i1 = position.getZ() - range; i1 <= position.getZ() + range && flag; ++i1) {
                    if (j >= world.bottom() && j < world.top()) {
                        if (this.isReplaceable(world, mutable.set(l, j, i1))) {
                            flag = false;
                        }
                    } else {
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    protected void placeCanopy(TreeWorld world, Random rand, BlockPos position, int height) {
        for (int y = position.getY() - 3 + height; y <= position.getY() + height; ++y) {
            int subract = y - (position.getY() + height);
            int subract2 = 1 - subract / 2;
            for (int x = position.getX() - subract2; x <= position.getX() + subract2; ++x) {
                int mathX = x - position.getX();
                for (int z = position.getZ() - subract2; z <= position.getZ() + subract2; ++z) {
                    int mathZ = z - position.getZ();
                    if (Math.abs(mathX) != subract2 || Math.abs(mathZ) != subract2 || rand.nextInt(2) != 0 && subract != 0) {
                        BlockPos blockpos = new BlockPos(x, y, z);
                        if (world.isAirBlock(blockpos)) {
                            world.setBlockState(blockpos, this.leaves);
                        }
                    }
                }
            }
        }
    }

    protected void placeTrunk(TreeWorld world, BlockPos position, int height) {
        for (int localHeight = 0; localHeight < height; ++localHeight) {
            BlockPos upN = position.above(localHeight);
            BlockState state = world.getBlockState(upN);
            if (TreeWorld.isAir(state) || TreeWorld.isLeaves(state)) {
                world.setBlockState(upN, this.log);
            }
        }
    }
}

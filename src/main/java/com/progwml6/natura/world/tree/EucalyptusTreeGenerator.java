package com.progwml6.natura.world.tree;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 EucalyptusTreeGenerator: a trunk with eight branches ending in leaf nodes. */
public class EucalyptusTreeGenerator extends NaturaTreeGenerator {
    public final int minTreeHeight;
    public final int treeHeightRange;
    public final BlockState log;
    public final BlockState leaves;
    public final boolean seekHeight;

    public EucalyptusTreeGenerator(int treeHeight, int treeRange, BlockState log, BlockState leaves, boolean seekHeight) {
        this.minTreeHeight = treeHeight;
        this.treeHeightRange = treeRange;
        this.log = log;
        this.leaves = leaves;
        this.seekHeight = seekHeight;
    }

    @Override
    public void generateTree(Random random, TreeWorld world, BlockPos pos) {
        int height = random.nextInt(this.treeHeightRange) + this.minTreeHeight;
        if (this.seekHeight) {
            pos = this.findGround(world, pos);
            if (pos.getY() < world.bottom()) {
                return;
            }
        }
        boolean flag = true;
        if (pos.getY() >= world.bottom() + 1 && pos.getY() + height + 1 <= world.top()) {
            for (int j = pos.getY(); j <= pos.getY() + 1 + height; ++j) {
                int k = 1;
                if (j == pos.getY()) {
                    k = 0;
                }
                if (j >= pos.getY() + 1 + height - 2) {
                    k = 3;
                }
                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                for (int l = pos.getX() - k; l <= pos.getX() + k && flag; ++l) {
                    for (int i1 = pos.getZ() - k; i1 <= pos.getZ() + k && flag; ++i1) {
                        if (j >= world.bottom() && j < world.top()) {
                            BlockState state = world.getBlockState(mutable.set(l, j, i1));
                            if (!TreeWorld.isAir(state) && !TreeWorld.isLeaves(state)) {
                                flag = false;
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }
            if (flag) {
                BlockState soil = world.getBlockState(pos.below());
                if (TreeWorld.isPlainsSoil(soil)) {
                    world.onPlantGrow(pos.below());
                    this.placeTrunk(world, pos, height);
                    this.genBranch(world, random, pos, height, 1);
                    this.genBranch(world, random, pos, height, 2);
                    this.genBranch(world, random, pos, height, 3);
                    this.genBranch(world, random, pos, height, 4);
                    this.genStraightBranch(world, random, pos, height, 1);
                    this.genStraightBranch(world, random, pos, height, 2);
                    this.genStraightBranch(world, random, pos, height, 3);
                    this.genStraightBranch(world, random, pos, height, 4);
                    this.generateNode(world, random, pos.above(height));
                }
            }
        }
    }

    /** The given position when it stands on dirt / grass, else the first dirt / grass from y 96 down to 32. */
    BlockPos findGround(TreeWorld world, BlockPos pos) {
        int returnHeight = world.bottom();
        BlockState stateDown = world.getBlockState(pos.below());
        if (!world.isFullBlock(world.getBlockState(pos), pos) && TreeWorld.isDirtOrGrass(stateDown)) {
            return pos;
        }
        int y = 96;
        do {
            BlockPos position = new BlockPos(pos.getX(), y, pos.getZ());
            if (y < 32) {
                break;
            }
            BlockState state = world.getBlockState(position);
            if (TreeWorld.isDirtOrGrass(state) && !world.isFullBlock(world.getBlockState(position.above()), position.above())) {
                returnHeight = y + 1;
                break;
            }
            y--;
        } while (y > 0);
        return new BlockPos(pos.getX(), returnHeight, pos.getZ());
    }

    private void genBranch(TreeWorld world, Random random, BlockPos pos, int height, int direction) {
        int posX = pos.getX();
        int posY = pos.getY() + height - 3;
        int posZ = pos.getZ();
        int byte0 = 0;
        int byte1 = 0;
        switch (direction) {
            case 1 -> { byte0 = 1; byte1 = 1; }
            case 2 -> { byte0 = -1; byte1 = 1; }
            case 3 -> { byte0 = 1; byte1 = -1; }
            case 4 -> { byte0 = -1; byte1 = -1; }
        }
        int heightShift = random.nextInt(6);
        for (int bIter = 4; bIter > 0; bIter--) {
            if (heightShift % 3 != 0) {
                posX += byte0;
            }
            if (heightShift % 3 != 1) {
                posZ += byte1;
            }
            int branch = heightShift % 3;
            posY += branch;
            BlockPos blockpos = new BlockPos(posX, posY, posZ);
            if (branch == 2) {
                this.setBlockAndMetadata(world, blockpos.below(), this.log);
            }
            this.setBlockAndMetadata(world, blockpos, this.log);
            if (bIter == 1) {
                this.generateNode(world, random, blockpos);
            }
            heightShift = random.nextInt(6);
        }
    }

    private void genStraightBranch(TreeWorld world, Random random, BlockPos pos, int height, int direction) {
        int posX = pos.getX();
        int posY = pos.getY() + height - 3;
        int posZ = pos.getZ();
        int xShift = 0;
        int zShift = 0;
        switch (direction) {
            case 1 -> { xShift = 1; zShift = 0; }
            case 2 -> { xShift = 0; zShift = 1; }
            case 3 -> { xShift = -1; zShift = 0; }
            case 4 -> { xShift = 0; zShift = -1; }
        }
        int heightShift = random.nextInt(6);
        for (int j2 = 4; j2 > 0; j2--) {
            if (xShift == 0) {
                posX = (posX + random.nextInt(3)) - 1;
                posZ += zShift;
            }
            if (zShift == 0) {
                posX += xShift;
                posZ = (posZ + random.nextInt(3)) - 1;
            }
            int branch = heightShift % 3;
            posY += branch;
            BlockPos blockpos = new BlockPos(posX, posY, posZ);
            if (branch == 2) {
                this.setBlockAndMetadata(world, blockpos.below(), this.log);
            }
            this.setBlockAndMetadata(world, blockpos, this.log);
            if (j2 == 1) {
                this.generateNode(world, random, blockpos);
            }
            heightShift = random.nextInt(6);
        }
    }

    /** A log with a plus-shaped layer of leaves around it and a 3x3 one above (1.12 skipped its own leaves block). */
    public void generateNode(TreeWorld world, Random random, BlockPos pos) {
        this.setBlockAndMetadata(world, pos, this.log);
        this.leafLayer(world, pos.getX() - 2, pos.getX() + 2, pos.getZ() - 1, pos.getZ() + 1, pos.getY());
        this.leafLayer(world, pos.getX() - 1, pos.getX() + 1, pos.getZ() - 2, pos.getZ() + 2, pos.getY());
        this.leafLayer(world, pos.getX() - 1, pos.getX() + 1, pos.getZ() - 1, pos.getZ() + 1, pos.getY() + 1);
    }

    private void leafLayer(TreeWorld world, int x0, int x1, int z0, int z1, int y) {
        for (int xIter = x0; xIter <= x1; xIter++) {
            for (int zIter = z0; zIter <= z1; zIter++) {
                BlockPos newPos = new BlockPos(xIter, y, zIter);
                BlockState state = world.getBlockState(newPos);
                // 1.12: block != overworldLeaves2 (the block of every eucalyptus / willow / hopseed / sakura leaf)
                if (!isLeaves2(state) && !world.isFullBlock(state, newPos)) {
                    this.setBlockAndMetadata(world, newPos, this.leaves);
                }
            }
        }
    }

    static boolean isLeaves2(BlockState state) {
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        return path.equals("natura:willow_leaves") || path.equals("natura:eucalyptus_leaves") || path.equals("natura:hopseed_leaves")
                || path.equals("natura:sakura_leaves");
    }

    protected void placeTrunk(TreeWorld world, BlockPos pos, int height) {
        while (height >= 0) {
            BlockState state = world.getBlockState(pos);
            if (TreeWorld.isAir(state) || TreeWorld.isReplaceable(state) || TreeWorld.isLeaves(state)) {
                this.setBlockAndMetadata(world, pos, this.log);
            }
            pos = pos.above();
            height--;
        }
    }

    protected void setBlockAndMetadata(TreeWorld world, BlockPos pos, BlockState stateNew) {
        BlockState state = world.getBlockState(pos);
        if (TreeWorld.isAir(state) || TreeWorld.canPlaceBlockAt(state) || state == this.leaves) {
            world.setBlockState(pos, stateNew);
        }
    }
}

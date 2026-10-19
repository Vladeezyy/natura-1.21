package com.progwml6.natura.world.tree;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 BloodwoodTreeGenerator: a tree hanging from the nether ceiling, a 2x2 trunk of bloodwood quarters growing down
 * with bark branches and blood leaves at the bottom. It places over anything (no space check).
 */
public class BloodwoodTreeGenerator extends NaturaTreeGenerator {
    public final BlockState full;
    public final BlockState trunk1;
    public final BlockState trunk2;
    public final BlockState trunk3;
    public final BlockState trunk4;
    public final BlockState leaves;

    public BloodwoodTreeGenerator(BlockState full, BlockState trunk1, BlockState trunk2, BlockState trunk3, BlockState trunk4, BlockState leaves) {
        this.full = full;
        this.trunk1 = trunk1;
        this.trunk2 = trunk2;
        this.trunk3 = trunk3;
        this.trunk4 = trunk4;
        this.leaves = leaves;
    }

    /** Up to the first netherrack / soul sand / tainted soil with a non-full block below it, at most y 120. */
    BlockPos findCeiling(TreeWorld world, BlockPos pos) {
        int returnHeight = world.bottom();
        int height = pos.getY();
        do {
            BlockPos position = new BlockPos(pos.getX(), height, pos.getZ());
            if (TreeWorld.isNetherSoil(world.getBlockState(position)) && !world.isFullBlock(world.getBlockState(position.below()), position.below())) {
                returnHeight = height - 1;
                break;
            }
            height++;
        } while (height <= 120);
        return new BlockPos(pos.getX(), returnHeight, pos.getZ());
    }

    @Override
    public void generateTree(Random random, TreeWorld world, BlockPos pos) {
        int treeHeight = random.nextInt(8) + 8;
        pos = this.findCeiling(world, pos);
        if (pos.getY() < world.bottom()) {
            return;
        }
        for (int heightIter = 0; heightIter < treeHeight; heightIter++) {
            BlockState local = world.getBlockState(pos.offset(0, -heightIter, 0));
            if (local.isAir() || isNetherLeaves(local)) {
                this.setBlockAndMetadata(world, pos.offset(0, -heightIter, 0), this.trunk1);
                this.setBlockAndMetadata(world, pos.offset(1, -heightIter, 0), this.trunk2);
                this.setBlockAndMetadata(world, pos.offset(0, -heightIter, 1), this.trunk3);
                this.setBlockAndMetadata(world, pos.offset(1, -heightIter, 1), this.trunk4);
            }
        }
        this.genBranch(world, random, pos, -treeHeight, 1);
        this.genBranch(world, random, pos.offset(1, 0, 0), -treeHeight, 2);
        this.genBranch(world, random, pos.offset(0, 0, 1), -treeHeight, 3);
        this.genBranch(world, random, pos.offset(1, 0, 1), -treeHeight, 4);
        this.genStraightBranch(world, random, pos, -treeHeight, 1);
        this.genStraightBranch(world, random, pos.offset(1, 0, 0), -treeHeight, 2);
        this.genStraightBranch(world, random, pos.offset(0, 0, 1), -treeHeight, 3);
        this.genStraightBranch(world, random, pos.offset(1, 0, 1), -treeHeight, 4);
    }

    private void genBranch(TreeWorld world, Random random, BlockPos pos, int height, int direction) {
        int posX = pos.getX();
        int posY = pos.getY() + height;
        int posZ = pos.getZ();
        int offsetX = 0;
        int offsetZ = 0;
        switch (direction) {
            case 1 -> { offsetX = 1; offsetZ = 1; }
            case 2 -> { offsetX = -1; offsetZ = 1; }
            case 3 -> { offsetX = 1; offsetZ = -1; }
            case 4 -> { offsetX = -1; offsetZ = -1; }
        }
        int heightShift = random.nextInt(6);
        for (int bIter = 4; bIter > 0; bIter--) {
            if (heightShift % 3 != 0) {
                posX += offsetX;
            }
            if (heightShift % 3 != 1) {
                posZ += offsetZ;
            }
            int branch = heightShift % 3 - 1;
            posY += branch;
            this.generateNode(world, random, new BlockPos(posX, posY, posZ));
            heightShift = random.nextInt(6);
        }
    }

    private void genStraightBranch(TreeWorld world, Random random, BlockPos pos, int height, int direction) {
        int posX = pos.getX();
        int posY = pos.getY() + height;
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
            int branch = heightShift % 3 - 1;
            posY += branch;
            this.generateNode(world, random, new BlockPos(posX, posY, posZ));
            heightShift = random.nextInt(6);
        }
    }

    public void generateNode(TreeWorld world, Random random, BlockPos pos) {
        this.setBlockAndMetadata(world, pos, this.full);
        this.leafLayer(world, pos.getX() - 1, pos.getX() + 1, pos.getZ() - 1, pos.getZ() + 1, pos.getY());
        this.leafLayer(world, pos.getX() - 1, pos.getX() + 1, pos.getZ() - 2, pos.getZ() + 2, pos.getY());
        this.leafLayer(world, pos.getX() - 2, pos.getX() + 2, pos.getZ() - 1, pos.getZ() + 1, pos.getY() + 1);
    }

    /** 1.12: block != netherLeaves (ghostwood / blood / fusewood leaves) and not a full block. */
    private void leafLayer(TreeWorld world, int x0, int x1, int z0, int z1, int y) {
        for (int xIter = x0; xIter <= x1; xIter++) {
            for (int zIter = z0; zIter <= z1; zIter++) {
                BlockPos newPos = new BlockPos(xIter, y, zIter);
                BlockState state = world.getBlockState(newPos);
                if (!isNetherLeaves(state) && !world.isFullBlock(state, newPos)) {
                    this.setBlockAndMetadata(world, newPos, this.leaves);
                }
            }
        }
    }

    private static boolean isNetherLeaves(BlockState state) {
        String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        return id.equals("natura:ghostwood_leaves") || id.equals("natura:bloodwood_leaves") || id.equals("natura:fusewood_leaves");
    }

    protected void setBlockAndMetadata(TreeWorld world, BlockPos pos, BlockState stateNew) {
        world.setBlockState(pos, stateNew);
    }
}

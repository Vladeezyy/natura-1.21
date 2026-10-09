package com.progwml6.natura.world.tree;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 HopseedTreeGenerator: a 2x2 trunk under a wide flat crown. */
public class HopseedTreeGenerator extends NaturaTreeGenerator {
    public final int minTreeHeight;
    public final int treeHeightRange;
    public final BlockState log;
    public final BlockState leaves;
    public final boolean seekHeight;
    public final boolean isSapling;

    public HopseedTreeGenerator(int treeHeight, int treeRange, BlockState log, BlockState leaves, boolean seekHeight, boolean isSapling) {
        this.minTreeHeight = treeHeight;
        this.treeHeightRange = treeRange;
        this.log = log;
        this.leaves = leaves;
        this.seekHeight = seekHeight;
        this.isSapling = isSapling;
    }

    @Override
    public void generateTree(Random random, TreeWorld world, BlockPos position) {
        int height = random.nextInt(this.treeHeightRange) + this.minTreeHeight;
        if (this.seekHeight) {
            while (position.getY() > world.bottom() + 1 && world.isAirBlock(position) || TreeWorld.isLeaves(world.getBlockState(position))) {
                position = position.below();
            }
            if (position.getY() < world.bottom()) {
                return;
            }
        }
        if (!TreeWorld.isDirtOrGrass(world.getBlockState(position))) {
            return;
        }
        position = position.above();
        boolean hasSpace = true;
        if (position.getY() >= world.bottom() + 1 && position.getY() + height + 1 <= world.top()) {
            for (int y = position.getY(); y <= position.getY() + 1 + height; ++y) {
                int radius = 1;
                if (y == position.getY()) {
                    radius = 0;
                }
                if (y >= position.getY() + 1 + height - 2) {
                    radius = 2;
                }
                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                for (int x = position.getX() - radius; x <= position.getX() + radius && hasSpace; ++x) {
                    for (int z = position.getZ() - radius; z <= position.getZ() + radius && hasSpace; ++z) {
                        if (y >= world.bottom() && y < world.top()) {
                            if (!this.isReplaceable(world, mutable.set(x, y, z))) {
                                hasSpace = false;
                            }
                        } else {
                            hasSpace = false;
                        }
                    }
                }
            }
            if (hasSpace) {
                BlockPos downPosition = position.below();
                BlockState state = world.getBlockState(downPosition);
                if (TreeWorld.isPlainsSoil(state) && position.getY() < world.top() - height - 1) {
                    world.onPlantGrow(downPosition);
                    world.onPlantGrow(downPosition.east());
                    world.onPlantGrow(downPosition.south());
                    world.onPlantGrow(downPosition.south().east());
                    this.growLogs(world, position);
                    this.growLeaves(world, random, position, height);
                    for (int l = 0; l < height; ++l) {
                        this.trunk(world, position.above(l));
                        if (l < height - 1) {
                            this.trunk(world, position.offset(1, l, 0));
                            this.trunk(world, position.offset(1, l, 1));
                            this.trunk(world, position.offset(0, l, 1));
                        }
                    }
                }
            }
        }
    }

    private void trunk(TreeWorld world, BlockPos blockpos) {
        BlockState state = world.getBlockState(blockpos);
        if (TreeWorld.isAir(state) || TreeWorld.isLeaves(state)) {
            world.setBlockState(blockpos, this.log);
        }
    }

    private void growLogs(TreeWorld world, BlockPos positionIn) {
        this.setBlockAndMetadata(world, positionIn, this.log);
        this.setBlockAndMetadata(world, positionIn.offset(1, 0, 0), this.log);
        this.setBlockAndMetadata(world, positionIn.offset(0, 0, 1), this.log);
        this.setBlockAndMetadata(world, positionIn.offset(1, 0, 1), this.log);
        this.setBlockAndMetadata(world, positionIn.offset(0, 1, 0), this.log);
        this.setBlockAndMetadata(world, positionIn.offset(1, 1, 0), this.log);
        this.setBlockAndMetadata(world, positionIn.offset(0, 1, 1), this.log);
        this.setBlockAndMetadata(world, positionIn.offset(1, 1, 1), this.log);
    }

    protected void growLeaves(TreeWorld world, Random random, BlockPos positionIn, int height) {
        for (int y = positionIn.getY() - 2 + height; y <= positionIn.getY() + height; ++y) {
            int subract = y - (positionIn.getY() + height);
            int subract2 = 2 + 1 - subract;
            for (int x = positionIn.getX() - subract2; x <= positionIn.getX() + subract2; ++x) {
                int mathX = x - positionIn.getX();
                for (int z = positionIn.getZ() - subract2; z <= positionIn.getZ() + subract2; ++z) {
                    int mathZ = z - positionIn.getZ();
                    BlockPos blockpos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(blockpos);
                    if ((mathX >= 0 || mathZ >= 0 || mathX * mathX + mathZ * mathZ <= subract2 * subract2)
                            && (mathX <= 0 && mathZ <= 0 || mathX * mathX + mathZ * mathZ <= (subract2 + 1) * (subract2 + 1))
                            && (random.nextInt(4) != 0 || mathX * mathX + mathZ * mathZ <= (subract2 - 1) * (subract2 - 1))
                            && (TreeWorld.isAir(state) || TreeWorld.isLeaves(state) || world.canBeReplacedByLeaves(state, blockpos))) {
                        this.setBlockAndMetadata(world, blockpos, this.leaves);
                    }
                }
            }
        }
    }

    protected void setBlockAndMetadata(TreeWorld world, BlockPos positionIn, BlockState stateNew) {
        BlockState state = world.getBlockState(positionIn);
        if (TreeWorld.isAir(state) || TreeWorld.canPlaceBlockAt(state) || state == this.leaves) {
            world.setBlockState(positionIn, stateNew);
        }
    }

    public boolean isReplaceable(TreeWorld world, BlockPos positionIn) {
        BlockState state = world.getBlockState(positionIn);
        return TreeWorld.isAir(state) || TreeWorld.isLeaves(state) || TreeWorld.isWood(state);
    }
}

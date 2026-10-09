package com.progwml6.natura.world.tree;

import java.util.Random;

import com.progwml6.natura.common.NaturaConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 WillowTreeGenerator: a swamp-oak-like tree that can stand in water, with leaves hanging down. Only below y 128. */
public class WillowTreeGenerator extends NaturaTreeGenerator {
    public final int minTreeHeight;
    public final int treeHeightRange;
    public final BlockState log;
    public final BlockState leaves;
    public final boolean seekHeight;
    public final boolean isSapling;

    public WillowTreeGenerator(int treeHeight, int treeRange, BlockState log, BlockState leaves, boolean seekHeight, boolean isSapling) {
        this.minTreeHeight = treeHeight;
        this.treeHeightRange = treeRange;
        this.log = log;
        this.leaves = leaves;
        this.seekHeight = seekHeight;
        this.isSapling = isSapling;
    }

    private static boolean isWater(BlockState state) {
        return state.getFluidState().is(FluidTags.WATER) && (state.is(Blocks.WATER));
    }

    @Override
    public void generateTree(Random random, TreeWorld world, BlockPos position) {
        int height;
        if (this.seekHeight) {
            position = this.findGround(world, position);
            if (position.getY() < world.bottom()) {
                return;
            }
        }
        for (height = random.nextInt(this.treeHeightRange) + this.minTreeHeight; isWater(world.getBlockState(position.below())); position = position.below()) {
        }
        boolean flag = true;
        if (position.getY() >= world.bottom() + 1 && position.getY() + height + 1 <= 128) {
            for (int j = position.getY(); j <= position.getY() + 1 + height; ++j) {
                int k = 1;
                if (j == position.getY()) {
                    k = 0;
                }
                if (j >= position.getY() + 1 + height - 2) {
                    k = 3;
                }
                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                for (int l = position.getX() - k; l <= position.getX() + k && flag; ++l) {
                    for (int i1 = position.getZ() - k; i1 <= position.getZ() + k && flag; ++i1) {
                        if (j >= world.bottom() && j < 128) {
                            BlockState state = world.getBlockState(mutable.set(l, j, i1));
                            if (!TreeWorld.isAir(state) && !TreeWorld.isLeaves(state)) {
                                if (!isWater(state)) {
                                    flag = false;
                                } else if (j > position.getY()) {
                                    flag = false;
                                }
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }
            if (flag) {
                BlockPos down = position.below();
                BlockState state = world.getBlockState(down);
                if (TreeWorld.isPlainsSoil(state) && position.getY() < 128 - height - 1) {
                    world.onPlantGrow(down);
                    for (int k1 = position.getY() - 3 + height; k1 <= position.getY() + height; ++k1) {
                        int j2 = k1 - (position.getY() + height);
                        int l2 = 2 - j2 / 2;
                        for (int j3 = position.getX() - l2; j3 <= position.getX() + l2; ++j3) {
                            int k3 = j3 - position.getX();
                            for (int i4 = position.getZ() - l2; i4 <= position.getZ() + l2; ++i4) {
                                int j1 = i4 - position.getZ();
                                if (Math.abs(k3) != l2 || Math.abs(j1) != l2 || random.nextInt(2) != 0 && j2 != 0) {
                                    BlockPos blockpos = new BlockPos(j3, k1, i4);
                                    state = world.getBlockState(blockpos);
                                    if (world.canBeReplacedByLeaves(state, blockpos)) {
                                        this.setBlockAndMetadata(world, blockpos, this.leaves);
                                    }
                                }
                            }
                        }
                    }
                    for (int l1 = 0; l1 < height; ++l1) {
                        BlockPos upN = position.above(l1);
                        BlockState state1 = world.getBlockState(upN);
                        if (TreeWorld.isAir(state1) || TreeWorld.isLeaves(state1) || TreeWorld.isReplaceable(state1) || isWater(state1)) {
                            this.setBlockAndMetadata(world, position.above(l1), this.log);
                        }
                    }
                    for (int i2 = position.getY() - 3 + height; i2 <= position.getY() + height; ++i2) {
                        int k2 = i2 - (position.getY() + height);
                        int i3 = 2 - k2 / 2;
                        BlockPos.MutableBlockPos mutable1 = new BlockPos.MutableBlockPos();
                        for (int l3 = position.getX() - i3; l3 <= position.getX() + i3; ++l3) {
                            for (int j4 = position.getZ() - i3; j4 <= position.getZ() + i3; ++j4) {
                                mutable1.set(l3, i2, j4);
                                if (TreeWorld.isLeaves(world.getBlockState(mutable1))) {
                                    BlockPos blockpos3 = mutable1.west();
                                    BlockPos blockpos4 = mutable1.east();
                                    BlockPos blockpos1 = mutable1.north();
                                    BlockPos blockpos2 = mutable1.south();
                                    if (random.nextInt(4) == 0 && world.isAirBlock(blockpos3)) {
                                        this.addDownLeaves(world, blockpos3);
                                    }
                                    if (random.nextInt(4) == 0 && world.isAirBlock(blockpos4)) {
                                        this.addDownLeaves(world, blockpos4);
                                    }
                                    if (random.nextInt(4) == 0 && world.isAirBlock(blockpos1)) {
                                        this.addDownLeaves(world, blockpos1);
                                    }
                                    if (random.nextInt(4) == 0 && world.isAirBlock(blockpos2)) {
                                        this.addDownLeaves(world, blockpos2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    BlockPos findGround(TreeWorld world, BlockPos pos) {
        int returnHeight = world.bottom();
        int height = pos.getY();
        int seaLevel = world.isFlat() && this.isSapling ? NaturaConfig.flatSeaLevel(world) : NaturaConfig.SEA_LEVEL.get();
        do {
            BlockPos position = new BlockPos(pos.getX(), height, pos.getZ());
            BlockState state = world.getBlockState(position);
            if ((TreeWorld.isDirtOrGrass(state) || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND))
                    && !world.isFullBlock(world.getBlockState(position.above()), position.above())) {
                returnHeight = height + 1;
                break;
            }
            height--;
        } while (height > seaLevel);
        return new BlockPos(pos.getX(), returnHeight, pos.getZ());
    }

    private void addDownLeaves(TreeWorld world, BlockPos pos) {
        this.setBlockAndMetadata(world, pos, this.leaves);
        int i = 4;
        for (pos = pos.below(); world.isAirBlock(pos) && i > 0; --i) {
            this.setBlockAndMetadata(world, pos, this.leaves);
            pos = pos.below();
        }
    }

    protected void setBlockAndMetadata(TreeWorld world, BlockPos pos, BlockState stateNew) {
        BlockState state = world.getBlockState(pos);
        if (TreeWorld.isAir(state) || TreeWorld.canPlaceBlockAt(state) || state == this.leaves) {
            world.setBlockState(pos, stateNew);
        }
    }
}

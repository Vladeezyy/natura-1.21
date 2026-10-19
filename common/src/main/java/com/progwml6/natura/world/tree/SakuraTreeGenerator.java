package com.progwml6.natura.world.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.progwml6.natura.common.NaturaConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 SakuraTreeGenerator: the 1.12 vanilla big oak (WorldGenBigTree) with sakura wood. */
public class SakuraTreeGenerator extends NaturaTreeGenerator {
    protected Random rand;
    protected TreeWorld world;
    protected BlockPos basePos = BlockPos.ZERO;
    int heightLimit;
    private java.util.concurrent.atomic.AtomicInteger sharedHeightLimit;
    int height;
    double heightAttenuation = 0.618D;
    double branchSlope = 0.381D;
    double scaleWidth = 1.0D;
    double leafDensity = 1.0D;
    int trunkSize = 1;
    int heightLimitLimit = 12;
    int leafDistanceLimit = 4;
    List<FoliageCoordinates> foliageCoords;
    public final BlockState log;
    public final BlockState leaves;
    public final boolean findGround;
    public final boolean isSapling;

    public SakuraTreeGenerator(BlockState log, BlockState leaves, boolean findGround, boolean isSapling) {
        this.findGround = findGround;
        this.isSapling = isSapling;
        this.log = log;
        this.leaves = leaves;
    }

    void generateLeafNodeList() {
        this.height = (int) (this.heightLimit * this.heightAttenuation);
        if (this.height >= this.heightLimit) {
            this.height = this.heightLimit - 1;
        }
        int i = (int) (1.382D + Math.pow(this.leafDensity * this.heightLimit / 13.0D, 2.0D));
        if (i < 1) {
            i = 1;
        }
        int j = this.basePos.getY() + this.height;
        int k = this.heightLimit - this.leafDistanceLimit;
        this.foliageCoords = new ArrayList<>();
        this.foliageCoords.add(new FoliageCoordinates(this.basePos.above(k), j));
        for (; k >= 0; --k) {
            float f = this.layerSize(k);
            if (f >= 0.0F) {
                for (int l = 0; l < i; ++l) {
                    double d0 = this.scaleWidth * f * (this.rand.nextFloat() + 0.328D);
                    double d1 = this.rand.nextFloat() * 2.0F * Math.PI;
                    double d2 = d0 * Math.sin(d1) + 0.5D;
                    double d3 = d0 * Math.cos(d1) + 0.5D;
                    BlockPos blockpos = add(this.basePos, d2, k - 1, d3);
                    BlockPos blockpos1 = blockpos.above(this.leafDistanceLimit);
                    if (this.checkBlockLine(blockpos, blockpos1) == -1) {
                        int i1 = this.basePos.getX() - blockpos.getX();
                        int j1 = this.basePos.getZ() - blockpos.getZ();
                        double d4 = blockpos.getY() - Math.sqrt(i1 * i1 + j1 * j1) * this.branchSlope;
                        int k1 = d4 > j ? j : (int) d4;
                        BlockPos blockpos2 = new BlockPos(this.basePos.getX(), k1, this.basePos.getZ());
                        if (this.checkBlockLine(blockpos2, blockpos) == -1) {
                            this.foliageCoords.add(new FoliageCoordinates(blockpos, blockpos2.getY()));
                        }
                    }
                }
            }
        }
    }

    void crosSection(BlockPos pos, float size, BlockState state) {
        int i = (int) (size + 0.618D);
        for (int j = -i; j <= i; ++j) {
            for (int k = -i; k <= i; ++k) {
                if (Math.pow(Math.abs(j) + 0.5D, 2.0D) + Math.pow(Math.abs(k) + 0.5D, 2.0D) <= size * size) {
                    BlockPos blockpos = pos.offset(j, 0, k);
                    BlockState here = this.world.getBlockState(blockpos);
                    if (TreeWorld.isAir(here) || TreeWorld.isLeaves(here)) {
                        this.setBlockAndMetadata(this.world, blockpos, state);
                    }
                }
            }
        }
    }

    float layerSize(int y) {
        if (y < this.heightLimit * 0.3F) {
            return -1.0F;
        }
        float f = this.heightLimit / 2.0F;
        float f1 = f - y;
        float f2 = Mth.sqrt(f * f - f1 * f1);
        if (f1 == 0.0F) {
            f2 = f;
        } else if (Math.abs(f1) >= f) {
            return 0.0F;
        }
        return f2 * 0.5F;
    }

    float leafSize(int y) {
        return y >= 0 && y < this.leafDistanceLimit ? (y != 0 && y != this.leafDistanceLimit - 1 ? 3.0F : 2.0F) : -1.0F;
    }

    void generateLeafNode(BlockPos pos) {
        for (int i = 0; i < this.leafDistanceLimit; ++i) {
            this.crosSection(pos.above(i), this.leafSize(i), this.leaves);
        }
    }

    void limb(BlockPos from, BlockPos to, BlockState state) {
        BlockPos blockpos = to.offset(-from.getX(), -from.getY(), -from.getZ());
        int i = getGreatestDistance(blockpos);
        float f = (float) blockpos.getX() / (float) i;
        float f1 = (float) blockpos.getY() / (float) i;
        float f2 = (float) blockpos.getZ() / (float) i;
        for (int j = 0; j <= i; ++j) {
            BlockPos blockpos1 = add(from, 0.5F + j * f, 0.5F + j * f1, 0.5F + j * f2);
            this.setBlockAndMetadata(this.world, blockpos1, this.withAxis(state, from, blockpos1));
        }
    }

    protected BlockState withAxis(BlockState state, BlockPos from, BlockPos to) {
        return state.hasProperty(RotatedPillarBlock.AXIS) ? state.setValue(RotatedPillarBlock.AXIS, getLogAxis(from, to)) : state;
    }

    static int getGreatestDistance(BlockPos posIn) {
        int i = Math.abs(posIn.getX());
        int j = Math.abs(posIn.getY());
        int k = Math.abs(posIn.getZ());
        return k > i && k > j ? k : (j > i ? j : i);
    }

    private static Direction.Axis getLogAxis(BlockPos a, BlockPos b) {
        Direction.Axis axis = Direction.Axis.Y;
        int i = Math.abs(b.getX() - a.getX());
        int j = Math.abs(b.getZ() - a.getZ());
        int k = Math.max(i, j);
        if (k > 0) {
            if (i == k) {
                axis = Direction.Axis.X;
            } else if (j == k) {
                axis = Direction.Axis.Z;
            }
        }
        return axis;
    }

    void generateLeaves() {
        for (FoliageCoordinates coords : this.foliageCoords) {
            this.generateLeafNode(coords.pos());
        }
    }

    boolean leafNodeNeedsBase(int y) {
        return y >= this.heightLimit * 0.2D;
    }

    void generateTrunk() {
        BlockPos blockpos = this.basePos;
        BlockPos blockpos1 = this.basePos.above(this.height);
        this.limb(blockpos, blockpos1, this.log);
        if (this.trunkSize == 2) {
            this.limb(blockpos.east(), blockpos1.east(), this.log);
            this.limb(blockpos.east().south(), blockpos1.east().south(), this.log);
            this.limb(blockpos.south(), blockpos1.south(), this.log);
        }
    }

    void generateLeafNodeBases() {
        for (FoliageCoordinates coords : this.foliageCoords) {
            int i = coords.branchBase();
            BlockPos blockpos = new BlockPos(this.basePos.getX(), i, this.basePos.getZ());
            if (!blockpos.equals(coords.pos()) && this.leafNodeNeedsBase(i - this.basePos.getY())) {
                this.limb(blockpos, coords.pos(), this.log);
            }
        }
    }

    int checkBlockLine(BlockPos posOne, BlockPos posTwo) {
        BlockPos blockpos = posTwo.offset(-posOne.getX(), -posOne.getY(), -posOne.getZ());
        int i = getGreatestDistance(blockpos);
        float f = (float) blockpos.getX() / (float) i;
        float f1 = (float) blockpos.getY() / (float) i;
        float f2 = (float) blockpos.getZ() / (float) i;
        if (i == 0) {
            return -1;
        }
        for (int j = 0; j <= i; ++j) {
            BlockPos blockpos1 = add(posOne, 0.5F + j * f, 0.5F + j * f1, 0.5F + j * f2);
            if (!this.isReplaceable(this.world, blockpos1)) {
                return j;
            }
        }
        return -1;
    }

    protected boolean isSoil(BlockState state) {
        return TreeWorld.isPlainsSoil(state);
    }

    private boolean validTreeLocation() {
        if (!this.isSoil(this.world.getBlockState(this.basePos.below()))) {
            return false;
        }
        int i = this.checkBlockLine(this.basePos, this.basePos.above(this.heightLimit - 1));
        if (i == -1) {
            return true;
        } else if (i < 6) {
            return false;
        }
        this.heightLimit = i;
        return true;
    }

    protected void setBlockAndMetadata(TreeWorld world, BlockPos pos, BlockState stateNew) {
        BlockState state = world.getBlockState(pos);
        if (TreeWorld.isAir(state) || TreeWorld.canPlaceBlockAt(state) || state == this.leaves) {
            world.setBlockState(pos, stateNew);
        }
    }

    BlockPos findGround(TreeWorld world, BlockPos pos) {
        int seaLevel = world.isFlat() && this.isSapling ? NaturaConfig.flatSeaLevel(world) : NaturaConfig.SEA_LEVEL.get();
        boolean foundGround = false;
        int height = seaLevel + 64;
        do {
            height--;
            BlockState under = world.getBlockState(new BlockPos(pos.getX(), height, pos.getZ()));
            if (TreeWorld.isDirtOrGrass(under) || height < seaLevel) {
                foundGround = true;
            }
        } while (!foundGround);
        return new BlockPos(pos.getX(), height + 1, pos.getZ());
    }

    public boolean isReplaceable(TreeWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return TreeWorld.isAir(state) || TreeWorld.isLeaves(state);
    }

    protected BlockPos startPos(TreeWorld world, BlockPos position) {
        return this.findGround ? this.findGround(world, position) : position;
    }

    @Override
    public void generateTree(Random random, TreeWorld worldIn, BlockPos position) {
        this.world = worldIn;
        this.basePos = this.startPos(worldIn, position);
        this.rand = new Random(random.nextLong());
        if (this.sharedHeightLimit != null) {
            this.heightLimit = this.sharedHeightLimit.get();
        }
        if (this.heightLimit == 0) {
            this.heightLimit = 5 + this.rand.nextInt(this.heightLimitLimit);
        }
        boolean valid = this.validTreeLocation();
        if (this.sharedHeightLimit != null) {
            this.sharedHeightLimit.set(this.heightLimit);
        }
        if (valid) {
            this.generateLeafNodeList();
            this.generateLeaves();
            this.generateTrunk();
            this.generateLeafNodeBases();
        }
        this.world = null;
    }

    /**
     * The world generators: 1.12 kept one generator for the whole game, so its height limit (rolled once, then only
     * lowered where a tree did not fit) was shared by every tree it grew.
     */
    public SakuraTreeGenerator shareHeightLimit(java.util.concurrent.atomic.AtomicInteger heightLimit) {
        this.sharedHeightLimit = heightLimit;
        return this;
    }

    record FoliageCoordinates(BlockPos pos, int branchBase) {}
}

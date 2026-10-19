package com.progwml6.natura.world.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.progwml6.natura.common.NaturaConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 RedwoodTreeGenerator: an 80-140 block tall redwood with a heart-and-bark trunk shrinking in rings, big-oak style
 * branches and roots reaching down to 18 blocks. The ring and root shapes are in {@link RedwoodShapes}.
 */
public class RedwoodTreeGenerator extends NaturaTreeGenerator {
    private Random rand;
    private TreeWorld world;
    private BlockPos basePos = BlockPos.ZERO;
    int heightLimit;
    int height;
    double heightAttenuation = 0.618D;
    double branchSlope = 0.381D;
    double scaleWidth = 1.0D;
    double leafDensity = 1.0D;
    int trunkSize = 1;
    int heightLimitLimit = 12;
    int leafDistanceLimit = 4;
    List<SakuraTreeGenerator.FoliageCoordinates> foliageCoords;
    public final BlockState bark;
    public final BlockState heart;
    public final BlockState root;
    public final BlockState leaves;
    public final boolean useHeight;
    public final boolean isSapling;

    public RedwoodTreeGenerator(BlockState bark, BlockState heart, BlockState root, BlockState leaves, boolean useHeight, boolean isSapling) {
        this.bark = bark;
        this.heart = heart;
        this.root = root;
        this.leaves = leaves;
        this.useHeight = useHeight;
        this.isSapling = isSapling;
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
        this.foliageCoords.add(new SakuraTreeGenerator.FoliageCoordinates(this.basePos.above(k), j));
        for (; k >= 0; --k) {
            float f = this.layerSize(k);
            if (f >= 0.0F) {
                for (int l = 0; l < i; ++l) {
                    double d0 = this.scaleWidth * f * (this.rand.nextFloat() + 0.328D);
                    double d1 = this.rand.nextFloat() * 2.0F * Math.PI;
                    double d2 = d0 * Math.sin(d1) + 0.5D;
                    double d3 = d0 * Math.cos(d1) + 0.5D;
                    // 1.12 redwood: y - 1 for every layer (the big oak used k - 1)
                    BlockPos blockpos = add(this.basePos, d2, -1, d3);
                    BlockPos blockpos1 = blockpos.above(this.leafDistanceLimit);
                    if (this.checkBlockLine(blockpos, blockpos1) == -1) {
                        int i1 = this.basePos.getX() - blockpos.getX();
                        int j1 = this.basePos.getZ() - blockpos.getZ();
                        double d4 = blockpos.getY() - Math.sqrt(i1 * i1 + j1 * j1) * this.branchSlope;
                        int k1 = d4 > j ? j : (int) d4;
                        BlockPos blockpos2 = new BlockPos(this.basePos.getX(), k1, this.basePos.getZ());
                        if (this.checkBlockLine(blockpos2, blockpos) == -1) {
                            this.foliageCoords.add(new SakuraTreeGenerator.FoliageCoordinates(blockpos, blockpos2.getY()));
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

    void limb(BlockPos pos1, BlockPos pos2, BlockState state) {
        BlockPos blockpos = pos2.offset(-pos1.getX(), -pos1.getY(), -pos1.getZ());
        int i = SakuraTreeGenerator.getGreatestDistance(blockpos);
        float f = (float) blockpos.getX() / (float) i;
        float f1 = (float) blockpos.getY() / (float) i;
        float f2 = (float) blockpos.getZ() / (float) i;
        for (int j = 0; j <= i; ++j) {
            this.setBlockAndMetadata(this.world, add(pos1, 0.5F + j * f, 0.5F + j * f1, 0.5F + j * f2), state);
        }
    }

    void generateLeaves() {
        for (var coords : this.foliageCoords) {
            this.generateLeafNode(coords.pos());
        }
    }

    boolean leafNodeNeedsBase(int y) {
        return y >= this.heightLimit * 0.2D;
    }

    void generateLeafNodeBases() {
        for (var coords : this.foliageCoords) {
            int i = coords.branchBase();
            BlockPos blockpos = new BlockPos(this.basePos.getX(), i, this.basePos.getZ());
            if (!blockpos.equals(coords.pos()) && this.leafNodeNeedsBase(i - this.basePos.getY())) {
                this.limb(blockpos, coords.pos(), this.bark);
            }
        }
    }

    int checkBlockLine(BlockPos posOne, BlockPos posTwo) {
        BlockPos blockpos = posTwo.offset(-posOne.getX(), -posOne.getY(), -posOne.getZ());
        int i = SakuraTreeGenerator.getGreatestDistance(blockpos);
        float f = (float) blockpos.getX() / (float) i;
        float f1 = (float) blockpos.getY() / (float) i;
        float f2 = (float) blockpos.getZ() / (float) i;
        if (i == 0) {
            return -1;
        }
        for (int j = 0; j <= i; ++j) {
            if (!this.isReplaceable(this.world, add(posOne, 0.5F + j * f, 0.5F + j * f1, 0.5F + j * f2))) {
                return j;
            }
        }
        return -1;
    }

    protected void setBlockAndMetadata(TreeWorld world, BlockPos pos, BlockState stateNew) {
        if (this.isReplaceable(world, pos)) {
            world.setBlockState(pos, stateNew);
        }
    }

    /** Air, leaves, replaceable blocks, logs, grass, dirt, saplings and vines: the roots grow through the ground. */
    public boolean isReplaceable(TreeWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return TreeWorld.isAir(state) || TreeWorld.isLeaves(state) || TreeWorld.isReplaceable(state) || TreeWorld.isWood(state) || canGrowInto(state);
    }

    protected static boolean canGrowInto(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || TreeWorld.isDirtOrGrass(state) || state.is(BlockTags.LOGS) || state.is(BlockTags.SAPLINGS)
                || state.is(Blocks.VINE);
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
        return new BlockPos(pos.getX(), height, pos.getZ());
    }

    public boolean isValidSpawn(TreeWorld world, BlockPos pos) {
        boolean ground = TreeWorld.isDirtOrGrass(world.getBlockState(pos));
        boolean transparent = !world.isFullBlock(world.getBlockState(pos.above()), pos.above());
        return ground && transparent;
    }

    @Override
    public void generateTree(Random random, TreeWorld world, BlockPos pos) {
        BlockPos groundPosition = pos;
        if (!this.useHeight) {
            groundPosition = this.findGround(world, pos);
            if (!this.isValidSpawn(world, groundPosition)) {
                return;
            }
        }
        int treeHeight = random.nextInt(60) + 80;
        this.world = world;
        this.basePos = groundPosition;
        this.rand = new Random(random.nextLong());
        this.heightLimit = 5 + this.rand.nextInt(this.heightLimitLimit);
        if (treeHeight > 120) {
            for (int currentHeight = 0; currentHeight < treeHeight; currentHeight++) {
                BlockPos at = groundPosition.above(currentHeight);
                if (currentHeight < treeHeight * 1 / 10) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_13);
                } else if (currentHeight < treeHeight * 2 / 10) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_12);
                } else if (currentHeight < treeHeight * 3 / 10) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_11);
                } else if (currentHeight < treeHeight * 4 / 10) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_10);
                } else if (currentHeight < treeHeight * 5 / 10) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_9);
                    this.growLowBranch(random, at);
                } else if (currentHeight < treeHeight * 6 / 10) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_8);
                    this.growLowBranch(random, at);
                } else if (currentHeight < treeHeight * 7 / 10) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_7);
                    this.growMiddleBranch(random, at);
                } else if (currentHeight < treeHeight * 8 / 10) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_6);
                    this.growMiddleBranch(random, at);
                } else if (currentHeight < treeHeight * 9 / 10) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_5);
                    this.growHighBranch(random, at);
                } else {
                    this.shape(world, at, RedwoodShapes.GEN_RING_3);
                    this.growHighBranch(random, at);
                }
            }
            this.growRoots(world, groundPosition.below(), RedwoodShapes.GEN_RING_13, BIG_ROOTS);
            this.growTop(groundPosition.above(this.height));
        } else if (treeHeight > 100) {
            for (int currentHeight = 0; currentHeight < treeHeight; currentHeight++) {
                BlockPos at = groundPosition.above(currentHeight);
                if (currentHeight < treeHeight * 1 / 8) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_11);
                } else if (currentHeight < treeHeight * 2 / 8) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_10);
                } else if (currentHeight < treeHeight * 3 / 8) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_9);
                } else if (currentHeight < treeHeight * 4 / 8) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_8);
                } else if (currentHeight < treeHeight * 5 / 8) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_7);
                    this.growMiddleBranch(random, at);
                } else if (currentHeight < treeHeight * 6 / 8) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_6);
                    this.growMiddleBranch(random, at);
                } else if (currentHeight < treeHeight * 7 / 8) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_5);
                    this.growHighBranch(random, at);
                } else {
                    this.shape(world, at, RedwoodShapes.GEN_RING_3);
                    this.growHighBranch(random, at);
                }
            }
            this.growRoots(world, groundPosition.below(), RedwoodShapes.GEN_RING_11, MEDIUM_ROOTS);
            this.growTop(groundPosition.above(this.height));
        } else {
            for (int currentHeight = 0; currentHeight < treeHeight; currentHeight++) {
                BlockPos at = groundPosition.above(currentHeight);
                if (currentHeight < treeHeight * 1 / 6) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_9);
                } else if (currentHeight < treeHeight * 2 / 6) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_8);
                } else if (currentHeight < treeHeight * 3 / 6) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_7);
                } else if (currentHeight < treeHeight * 4 / 6) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_6);
                    this.growMiddleBranch(random, at);
                } else if (currentHeight < treeHeight * 5 / 6) {
                    this.shape(world, at, RedwoodShapes.GEN_RING_5);
                    this.growHighBranch(random, at);
                } else {
                    this.shape(world, at, RedwoodShapes.GEN_RING_3);
                    this.growHighBranch(random, at);
                }
            }
            this.growRoots(world, groundPosition.below(), RedwoodShapes.GEN_RING_9, SMALL_ROOTS);
            this.growTop(groundPosition.above(this.height));
        }
    }

    /** 1.12 growTop: three big-oak crowns, 4 above and at the given position. */
    private void growTop(BlockPos pos) {
        this.basePos = pos.above(4);
        this.crown();
        this.basePos = pos;
        this.crown();
        this.basePos = pos;
        this.crown();
    }

    private void crown() {
        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateLeafNodeBases();
    }

    private void growHighBranch(Random random, BlockPos pos) {
        for (int iter = 0; iter < 3; iter++) {
            this.basePos = new BlockPos((pos.getX() + random.nextInt(21)) - 10, pos.getY(), (pos.getZ() + random.nextInt(21)) - 10);
            this.crown();
        }
    }

    private void growMiddleBranch(Random random, BlockPos pos) {
        for (int iter = 0; iter < 6; iter++) {
            this.basePos = new BlockPos((pos.getX() + random.nextInt(31)) - 15, pos.getY(), (pos.getZ() + random.nextInt(31)) - 15);
            this.crown();
        }
    }

    private void growLowBranch(Random random, BlockPos pos) {
        this.basePos = new BlockPos((pos.getX() + random.nextInt(17)) - 8, pos.getY(), (pos.getZ() + random.nextInt(17)) - 8);
        this.crown();
        if (random.nextInt(2) == 0) {
            this.basePos = new BlockPos((pos.getX() + random.nextInt(17)) - 8, pos.getY(), (pos.getZ() + random.nextInt(17)) - 8);
            this.crown();
        }
        this.basePos = new BlockPos((pos.getX() + random.nextInt(17)) - 8, pos.getY(), (pos.getZ() + random.nextInt(17)) - 8);
        this.crown();
    }

    private static final int[][][] SMALL_ROOTS = {RedwoodShapes.SMALL_ROOT_1, RedwoodShapes.SMALL_ROOT_1, RedwoodShapes.SMALL_ROOT_1,
            RedwoodShapes.SMALL_ROOT_2, RedwoodShapes.SMALL_ROOT_2, RedwoodShapes.SMALL_ROOT_3, RedwoodShapes.SMALL_ROOT_3, RedwoodShapes.SMALL_ROOT_3,
            RedwoodShapes.SMALL_ROOT_3, RedwoodShapes.SMALL_ROOT_4, RedwoodShapes.SMALL_ROOT_4};
    private static final int[][][] MEDIUM_ROOTS = {RedwoodShapes.MEDIUM_ROOT_1, RedwoodShapes.MEDIUM_ROOT_1, RedwoodShapes.MEDIUM_ROOT_1,
            RedwoodShapes.MEDIUM_ROOT_2, RedwoodShapes.MEDIUM_ROOT_2, RedwoodShapes.MEDIUM_ROOT_3, RedwoodShapes.MEDIUM_ROOT_3,
            RedwoodShapes.MEDIUM_ROOT_3, RedwoodShapes.MEDIUM_ROOT_3, RedwoodShapes.MEDIUM_ROOT_4, RedwoodShapes.MEDIUM_ROOT_4,
            RedwoodShapes.MEDIUM_ROOT_5, RedwoodShapes.MEDIUM_ROOT_5, RedwoodShapes.MEDIUM_ROOT_5};
    private static final int[][][] BIG_ROOTS = {RedwoodShapes.BIG_ROOT_1, RedwoodShapes.BIG_ROOT_1, RedwoodShapes.BIG_ROOT_1,
            RedwoodShapes.BIG_ROOT_2, RedwoodShapes.BIG_ROOT_2, RedwoodShapes.BIG_ROOT_3, RedwoodShapes.BIG_ROOT_3, RedwoodShapes.BIG_ROOT_3,
            RedwoodShapes.BIG_ROOT_3, RedwoodShapes.BIG_ROOT_4, RedwoodShapes.BIG_ROOT_4, RedwoodShapes.BIG_ROOT_5, RedwoodShapes.BIG_ROOT_5,
            RedwoodShapes.BIG_ROOT_5, RedwoodShapes.BIG_ROOT_6, RedwoodShapes.BIG_ROOT_6, RedwoodShapes.BIG_ROOT_6, RedwoodShapes.BIG_ROOT_6};

    /** 1.12 growSmallRoots / growMediumRoots / growBigRoots: a ring at the ground, then one root layer per block down. */
    private void growRoots(TreeWorld world, BlockPos pos, int[][] ring, int[][][] layers) {
        this.shape(world, pos, ring);
        for (int i = 0; i < layers.length; i++) {
            this.shape(world, pos.below(i + 1), layers[i]);
        }
    }

    /** One of the 1.12 shape methods: skipped on bedrock and at the world bottom. */
    private void shape(TreeWorld world, BlockPos pos, int[][] shape) {
        if (!world.getBlockState(pos).is(Blocks.BEDROCK) && pos.getY() > world.bottom()) {
            for (int[] s : shape) {
                BlockState state = switch (s[3]) {
                    case 0 -> this.bark;
                    case 1 -> this.heart;
                    case 2 -> this.root;
                    default -> this.leaves;
                };
                this.setBlockAndMetadata(world, pos.offset(s[0], s[1], s[2]), state);
            }
        }
    }
}

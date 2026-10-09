package com.progwml6.natura.world.tree;

import java.util.Random;

import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.common.NaturaConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 SaguaroGenerator: a small saguaro (3-6 high, up to four arms) or, 1 in 20, a big candelabra one. Used by the
 * baby saguaro and the world generation. Placed blocks get their connections afterwards (1.12 computed them on render).
 */
public class SaguaroGenerator {
    private Random rand;
    private TreeWorld world;
    private BlockPos basePos = BlockPos.ZERO;
    public final BlockState saguaro;
    public final boolean findGround;
    public final boolean isBaby;
    private final java.util.List<BlockPos> placed = new java.util.ArrayList<>();

    public SaguaroGenerator(BlockState saguaro, boolean findGround, boolean isBaby) {
        this.saguaro = saguaro;
        this.findGround = findGround;
        this.isBaby = isBaby;
    }

    public void generateSaguaro(Random random, TreeWorld worldIn, BlockPos position) {
        this.world = worldIn;
        if (this.findGround) {
            this.basePos = this.findGround(worldIn, position);
            // 1.12 checked the original position here, not the ground found
            if (position.getY() < worldIn.bottom()) {
                return;
            }
            if (!isSand(worldIn.getBlockState(position.below()))) {
                return;
            }
        } else {
            this.basePos = position;
        }
        this.rand = new Random(random.nextLong());
        if (this.validSaguaroLocation()) {
            if (this.rand.nextInt(20) == 0) {
                this.generateCactusTree();
            } else {
                this.generateSmallCactus();
            }
            var block = (com.progwml6.natura.common.block.SaguaroBlock) this.saguaro.getBlock();
            for (BlockPos pos : this.placed) {
                if (worldIn.getBlockState(pos).is(block)) {
                    worldIn.setBlockState(pos, block.withConnections(worldIn.level(), pos));
                }
            }
        }
        this.placed.clear();
        this.world = null;
    }

    private static boolean isSand(BlockState state) {
        return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND);
    }

    /** 1.12 canSustainPlant for a desert plant: sand, terracotta. */
    private static boolean isDesertSoil(BlockState state) {
        return isSand(state) || state.is(net.minecraft.tags.BlockTags.TERRACOTTA);
    }

    private boolean validSaguaroLocation() {
        BlockState current = this.world.getBlockState(this.basePos);
        BlockState below = this.world.getBlockState(this.basePos.below());
        if (!current.isAir()) {
            return current.is(NaturaBlocks.SAGUARO_BABY.get()) && isDesertSoil(below);
        }
        return isDesertSoil(below);
    }

    void generateCactusTree() {
        for (int height = 0; height < 6; height++) {
            this.setBlock(this.basePos.above(height));
        }
        this.setBlock(this.basePos.above(2).east());
        this.setBlock(this.basePos.above(2).west());
        this.setBlock(this.basePos.above(2).south());
        this.setBlock(this.basePos.above(2).north());
        for (int height = 0; height < 2; height++) {
            BlockPos pos = this.basePos.above(height + 2);
            this.setBlock(pos.east(2));
            this.setBlock(pos.west(2));
            this.setBlock(pos.south(2));
            this.setBlock(pos.north(2));
            pos = this.basePos.above(height + 3);
            this.setBlock(pos.east(3));
            this.setBlock(pos.west(3));
            this.setBlock(pos.south(3));
            this.setBlock(pos.north(3));
            pos = this.basePos.above(height + 5);
            this.setBlock(pos.east());
            this.setBlock(pos.west());
            this.setBlock(pos.south());
            this.setBlock(pos.north());
        }
    }

    void generateSmallCactus() {
        int height = this.rand.nextInt(4) + 3;
        for (int i = 0; i < height; i++) {
            this.setBlock(this.basePos.above(i));
        }
        int branchY = height >= 5 ? 2 : 1;
        BlockPos pos = this.basePos.above(branchY);
        for (int side = 0; side < 4; side++) {
            if (this.rand.nextBoolean()) {
                int size = this.rand.nextInt(height - branchY) + branchY - this.rand.nextInt(3);
                for (int branch = 0; branch < size; branch++) {
                    BlockPos up = pos.above(branch);
                    this.setBlock(switch (side) {
                        case 0 -> up.east();
                        case 1 -> up.west();
                        case 2 -> up.south();
                        default -> up.north();
                    });
                }
            }
        }
    }

    void setBlock(BlockPos pos) {
        if (!this.world.isFullBlock(this.world.getBlockState(pos), pos)) {
            this.world.setBlockState(pos, this.saguaro);
            this.placed.add(pos.immutable());
        }
    }

    BlockPos findGround(TreeWorld world, BlockPos pos) {
        int returnHeight = world.bottom();
        int height = pos.getY();
        int seaLevel = world.isFlat() && this.isBaby ? NaturaConfig.flatSeaLevel(world) : NaturaConfig.SEA_LEVEL.get();
        do {
            BlockPos position = new BlockPos(pos.getX(), height, pos.getZ());
            if (isSand(world.getBlockState(position)) && !world.isFullBlock(world.getBlockState(position.above()), position.above())) {
                returnHeight = height + 1;
                break;
            }
            height--;
        } while (height > seaLevel);
        return new BlockPos(pos.getX(), returnHeight, pos.getZ());
    }
}

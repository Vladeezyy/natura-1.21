package com.progwml6.natura.world.gen;

import java.util.Random;

import com.progwml6.natura.common.NaturaConfig;
import com.progwml6.natura.common.block.NaturaBerryBushBlock;
import com.progwml6.natura.world.tree.TreeWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 OverworldBerryBushGenerator / NetherBerryBushGenerator: a large node (1 in 10), a shrub (2 in 10), a small node
 * (4 in 10) or a single bush, grown (stage 2, 1 in 5 ripe) or younger. The nether one searched its ground from the x
 * coordinate as its height (1.12 bug: `int height = pos.getX()`), so it only works where x is positive; kept.
 */
public class BerryBushGenerator {
    public final int spawnHeight;
    public final BlockState berryBush;
    public final boolean nether;

    public BerryBushGenerator(int spawnHeight, BlockState berryBush, boolean nether) {
        this.spawnHeight = spawnHeight;
        this.berryBush = berryBush;
        this.nether = nether;
    }

    private BlockState age(int age) {
        return this.berryBush.setValue(NaturaBerryBushBlock.AGE, age);
    }

    public void generateBush(Random random, TreeWorld world, BlockPos pos) {
        pos = this.nether ? this.findNetherGround(world, pos) : this.findGround(world, pos);
        if (pos.getY() < world.bottom()) {
            return;
        }
        if (pos.getY() >= world.bottom() + 1) {
            int size = random.nextInt(10);
            if (size == 9) {
                this.generateLargeNode(random, world, pos);
            } else if (size >= 7) {
                this.generateShrub(random, world, pos);
            } else if (size >= 3) {
                this.generateSmallNode(random, world, pos);
            } else {
                this.setBlockAndMetadata(world, pos, this.age(this.randomAge(random)));
            }
        }
    }

    protected void generateLargeNode(Random random, TreeWorld world, BlockPos pos) {
        for (int iterX = pos.getX() - 2; iterX <= pos.getX() + 2; iterX++) {
            for (int iterZ = pos.getZ() - 1; iterZ <= pos.getZ() + 1; iterZ++) {
                for (int iterY = pos.getY() - 1; iterY <= pos.getY(); iterY++) {
                    this.setBlockAndMetadata(world, new BlockPos(iterX, iterY, iterZ), this.age(this.randomFullAge(random)));
                }
            }
        }
        for (int iterX = pos.getX() - 1; iterX <= pos.getX() + 1; iterX++) {
            for (int iterY = pos.getY() - 1; iterY <= pos.getY(); iterY++) {
                this.setBlockAndMetadata(world, new BlockPos(iterX, iterY, pos.getZ() - 2), this.age(this.randomFullAge(random)));
            }
        }
        for (int iterX = pos.getX() - 1; iterX <= pos.getX() + 1; iterX++) {
            for (int iterY = pos.getY() - 1; iterY <= pos.getY(); iterY++) {
                this.setBlockAndMetadata(world, new BlockPos(iterX, iterY, pos.getZ() + 2), this.age(this.randomFullAge(random)));
            }
        }
        for (int iterX = pos.getX() - 1; iterX <= pos.getX() + 1; iterX++) {
            for (int iterZ = pos.getZ() - 1; iterZ <= pos.getZ() + 1; iterZ++) {
                this.setBlockAndMetadata(world, new BlockPos(iterX, pos.getY() + 1, iterZ), this.age(this.randomFullAge(random)));
                this.setBlockAndMetadata(world, new BlockPos(iterX, pos.getY() - 2, iterZ), this.age(this.randomFullAge(random)));
            }
        }
    }

    protected void generateShrub(Random random, TreeWorld world, BlockPos pos) {
        do {
            BlockState state = world.getBlockState(pos);
            if (!TreeWorld.isAir(state) && !TreeWorld.isLeaves(state)) {
                break;
            }
            pos = pos.below();
        } while (pos.getY() > world.bottom());
        BlockState ground = world.getBlockState(pos);
        boolean soil = this.nether ? ground.is(Blocks.NETHERRACK) || ground.is(Blocks.SOUL_SAND) : TreeWorld.isDirtOrGrass(ground);
        if (soil) {
            // 1.12 called pos.up() without using it: the shrub starts in the ground block
            for (int y = pos.getY(); y <= pos.getY() + 2; ++y) {
                int subract = y - pos.getY();
                int subract2 = 2 - subract;
                for (int x = pos.getX() - subract2; x <= pos.getX() + subract2; ++x) {
                    int mathX = x - pos.getX();
                    for (int z = pos.getZ() - subract2; z <= pos.getZ() + subract2; ++z) {
                        int mathZ = z - pos.getZ();
                        BlockPos blockpos = new BlockPos(x, y, z);
                        BlockState blockState = world.getBlockState(blockpos);
                        if (Math.abs(mathX) != subract2 || Math.abs(mathZ) != subract2
                                || random.nextInt(2) != 0 && world.canBeReplacedByLeaves(blockState, blockpos)) {
                            this.setBlockAndMetadata(world, blockpos, this.age(this.randomFullAge(random)));
                        }
                    }
                }
            }
        }
    }

    protected void generateSmallNode(Random random, TreeWorld world, BlockPos pos) {
        this.setBlockAndMetadata(world, pos, this.age(this.randomFullAge(random)));
        if (random.nextBoolean()) {
            this.setBlockAndMetadata(world, pos.east(), this.age(this.randomAge(random)));
        }
        if (random.nextBoolean()) {
            this.setBlockAndMetadata(world, pos.west(), this.age(this.randomAge(random)));
        }
        if (random.nextBoolean()) {
            this.setBlockAndMetadata(world, pos.south(), this.age(this.randomAge(random)));
        }
        if (random.nextBoolean()) {
            this.setBlockAndMetadata(world, pos.north(), this.age(this.randomAge(random)));
        }
    }

    /** 1.12: anything that is not an opaque cube is replaced. */
    protected void setBlockAndMetadata(TreeWorld world, BlockPos pos, BlockState state) {
        if (!world.isFullBlock(world.getBlockState(pos), pos)) {
            world.setBlockState(pos, state);
        }
    }

    BlockPos findGround(TreeWorld world, BlockPos pos) {
        int returnHeight = world.bottom() - 1;
        BlockState down = world.getBlockState(pos.below());
        if (!world.isFullBlock(world.getBlockState(pos), pos) && TreeWorld.isDirtOrGrass(down)) {
            return pos;
        }
        int height = this.spawnHeight;
        do {
            BlockPos position = new BlockPos(pos.getX(), height, pos.getZ());
            if (height < NaturaConfig.SEA_LEVEL.get()) {
                break;
            }
            if (TreeWorld.isDirtOrGrass(world.getBlockState(position))) {
                if (!world.isFullBlock(world.getBlockState(position.above()), position.above())) {
                    returnHeight = height + 1;
                }
                break;
            }
            height--;
        } while (height > 0);
        return new BlockPos(pos.getX(), returnHeight, pos.getZ());
    }

    BlockPos findNetherGround(TreeWorld world, BlockPos pos) {
        int returnHeight = world.bottom() - 1;
        BlockState down = world.getBlockState(pos.below());
        if (!world.isAirBlock(pos.below()) && !world.isFullBlock(world.getBlockState(pos), pos) && (down.is(Blocks.NETHERRACK) || down.is(Blocks.SOUL_SAND))) {
            return pos;
        }
        int height = pos.getX();
        BlockPos position = new BlockPos(pos.getX(), height, pos.getZ());
        do {
            BlockState state = world.getBlockState(position);
            if (!state.isAir()) {
                if (state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND)) {
                    if (!world.isFullBlock(world.getBlockState(position.above()), position.above())) {
                        returnHeight = height + 1;
                    }
                    break;
                }
            }
            height--;
            position = position.below();
        } while (height > 0);
        return new BlockPos(pos.getX(), returnHeight, pos.getZ());
    }

    int randomFullAge(Random random) {
        return 2 + (random.nextInt(5) == 0 ? 1 : 0);
    }

    int randomAge(Random random) {
        return random.nextInt(3);
    }
}

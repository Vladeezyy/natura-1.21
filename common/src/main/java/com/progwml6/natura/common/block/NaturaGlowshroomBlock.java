package com.progwml6.natura.common.block;


import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.world.tree.GlowshroomGenerator;
import com.progwml6.natura.world.tree.TreeWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockNetherGlowshroom: a mushroom that glows (9). It spreads like a mushroom (1 in 25, at most 5 in its 9x3x9),
 * stays on mycelium, netherrack, soul sand or tainted soil in any light, elsewhere on a solid top in light below 13, and
 * bone meal (40%) grows its huge glowshroom.
 */
public class NaturaGlowshroomBlock extends BushBlock implements BonemealableBlock {
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 6, 11);
    private final String color;

    public NaturaGlowshroomBlock(String color, Properties properties) {
        super(properties);
        this.color = color;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isSolidRender(level, pos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.MYCELIUM) || below.is(Blocks.NETHERRACK) || below.is(Blocks.SOUL_SAND) || below.getBlock() instanceof TaintedSoilBlock) {
            return true;
        }
        return level.getRawBrightness(pos, 0) < 13 && below.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(25) == 0) {
            int i = 5;
            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
                if (level.getBlockState(p).is(this)) {
                    --i;
                    if (i <= 0) {
                        return;
                    }
                }
            }
            BlockPos blockpos1 = pos.offset(rand.nextInt(3) - 1, rand.nextInt(2) - rand.nextInt(2), rand.nextInt(3) - 1);
            BlockState current = level.getBlockState(pos);
            for (int k = 0; k < 4; ++k) {
                if (level.isEmptyBlock(blockpos1) && current.canSurvive(level, blockpos1)) {
                    pos = blockpos1;
                }
                blockpos1 = pos.offset(rand.nextInt(3) - 1, rand.nextInt(2) - rand.nextInt(2), rand.nextInt(3) - 1);
            }
            current = level.getBlockState(pos);
            if (level.isEmptyBlock(blockpos1) && current.canSurvive(level, blockpos1)) {
                level.setBlock(blockpos1, current, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.4D;
    }

    /** 1.12 generateBigMushroom: removed, the huge one grown, put back if nothing grew. */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
        Block large = NaturaBlocks.LARGE_GLOWSHROOMS.get(this.color).get();
        new GlowshroomGenerator(this.color, large.defaultBlockState()).generateShroom(new java.util.Random(random.nextLong()), new TreeWorld(level), pos);
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, state, Block.UPDATE_INVISIBLE);
        }
    }
}

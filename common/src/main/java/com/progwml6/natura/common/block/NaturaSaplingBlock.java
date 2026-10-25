package com.progwml6.natura.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Natura saplings (1.12 BlockSapling subclasses): vanilla ticking (light 9, 1 in 7, two stages) and bone meal (45%),
 * the tree comes from the Natura generator instead of a tree grower. Overworld saplings use the vanilla soil rules,
 * nether ones grow on {@link #soil} (soul sand, netherrack, tainted soil), and the bloodwood sapling hangs from it.
 */
public class NaturaSaplingBlock extends SaplingBlock {
    /** Grows the tree; true when it grew. */
    @FunctionalInterface
    public interface Tree {
        boolean grow(ServerLevel level, BlockPos pos, BlockState sapling, RandomSource random);
    }

    private final Tree tree;
    private final TagKey<Block> soil;
    private final boolean hanging;

    public NaturaSaplingBlock(String name, Tree tree, TagKey<Block> soil, boolean hanging, Properties properties) {
        // the tree comes from advanceTree; the vanilla grower has no feature
        super(new AbstractTreeGrower() {
            @Override
            protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> getConfiguredFeature(
                    RandomSource random, boolean flowers) {
                return null;
            }
        }, properties);
        this.tree = tree;
        this.soil = soil;
        this.hanging = hanging;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return this.soil == null ? super.mayPlaceOn(state, level, pos) : state.is(this.soil);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (this.soil == null) {
            return super.canSurvive(state, level, pos);
        }
        BlockPos support = this.hanging ? pos.above() : pos.below();
        return this.mayPlaceOn(level.getBlockState(support), level, support);
    }

    @Override
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.cycle(STAGE), Block.UPDATE_NONE);
        } else if (this.tree != null) {
            this.tree.grow(level, pos, state, random);
        }
    }
}

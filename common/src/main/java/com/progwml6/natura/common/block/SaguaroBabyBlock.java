package com.progwml6.natura.common.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.level.redstone.Orientation;

import net.minecraft.world.entity.InsideBlockEffectApplier;

import com.mojang.serialization.MapCodec;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.world.tree.SaguaroGenerator;
import com.progwml6.natura.world.tree.TreeWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockSaguaroBaby: grows (1 in 200 random ticks) to stage 1, then into a saguaro (SaguaroGenerator). It hurts
 * like the saguaro, needs sand or saguaro below and breaks (dropping itself) without it.
 */
public class SaguaroBabyBlock extends Block {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 1);
    private static final VoxelShape COLLISION = Block.box(5.2, 0, 5.2, 10.8, 10.8, 10.8);
    private static final VoxelShape SHAPE = Block.box(5.2, 0, 5.2, 10.8, 8, 10.8);

    public SaguaroBabyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) || below.is(Blocks.SAND) || below.is(Blocks.RED_SAND);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level instanceof ServerLevel server && !(entity instanceof ItemEntity)) {
            entity.hurtServer(server, level.damageSources().cactus(), 1.0F);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        boolean canGrow = random.nextInt(200) == 0;
        if (com.progwml6.natura.platform.Services.PLATFORM.canCropGrow(level, pos, state, canGrow)) {
            if (age == 0) {
                level.setBlock(pos, state.setValue(AGE, 1), Block.UPDATE_ALL);
            } else {
                TreeWorld world = new TreeWorld(level);
                new SaguaroGenerator(NaturaBlocks.SAGUARO.get().defaultBlockState(), true, true).generateSaguaro(new java.util.Random(random.nextLong()), world, pos);
            }
            com.progwml6.natura.platform.Services.PLATFORM.cropGrown(level, pos, state);
        }
    }
}

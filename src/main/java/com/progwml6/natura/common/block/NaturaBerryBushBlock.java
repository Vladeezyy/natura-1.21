package com.progwml6.natura.common.block;

import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockEnumBerryBush: a leafy bush that grows from small (0) to ripe (3), stacks up to three high, and is picked
 * by clicking it (left or right) when ripe: one berry at the player's feet, back to stage 2. Overworld bushes grow 1 in
 * 20 random ticks with light 8, nether ones 1 in 75 in any light. Bone meal makes a small bush 1-2 stages bigger and
 * a grown one (stage 2 or 3) sometimes start a new bush above (it never ripens it). It can be placed anywhere.
 */
public class NaturaBerryBushBlock extends Block implements BonemealableBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    private static final VoxelShape SMALL = Block.box(4, 0, 4, 12, 8, 12);
    private static final VoxelShape MEDIUM = Block.box(2, 0, 2, 14, 12, 14);
    private final boolean nether;
    private final Supplier<? extends ItemLike> berry;

    public NaturaBerryBushBlock(boolean nether, Supplier<? extends ItemLike> berry, Properties properties) {
        super(properties);
        this.nether = nether;
        this.berry = berry;
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
        return switch (state.getValue(AGE)) {
            case 0 -> SMALL;
            case 1 -> MEDIUM;
            default -> Shapes.block();
        };
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int height;
        for (height = 1; level.getBlockState(pos.below(height)).is(this); ++height) {
        }
        boolean canGrow = random.nextInt(this.nether ? 75 : 20) == 0;
        if (!this.nether && level.getMaxLocalRawBrightness(pos) < 8) {
            return;
        }
        if (net.neoforged.neoforge.common.CommonHooks.canCropGrow(level, pos, state, canGrow)) {
            int age = state.getValue(AGE);
            if (age < 3) {
                level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            }
            if (random.nextInt(3) == 0 && height < 3 && level.getBlockState(pos.above()).isAir() && age >= 2) {
                level.setBlock(pos.above(), this.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 2) {
            level.setBlock(pos, this.defaultBlockState().setValue(AGE, random.nextInt(2) + 1 + age), Block.UPDATE_CLIENTS);
            return;
        }
        if (level.isEmptyBlock(pos.above()) && random.nextInt(3) == 0) {
            level.setBlock(pos.above(), this.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private boolean pick(BlockState state, Level level, BlockPos pos, Player player) {
        if (state.getValue(AGE) != 3) {
            return false;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, this.defaultBlockState().setValue(AGE, 2), Block.UPDATE_CLIENTS);
            giveAtFeet(level, player, new ItemStack(this.berry.get()));
        }
        return true;
    }

    /** 1.12: an item entity at the player's feet (y - 1), collided with the player at once (picked up). */
    static void giveAtFeet(Level level, Player player, ItemStack stack) {
        ItemEntity item = new ItemEntity(level, player.getX(), player.getY() - 1.0D, player.getZ(), stack);
        level.addFreshEntity(item);
        if (!(player instanceof net.neoforged.neoforge.common.util.FakePlayer)) {
            item.setNoPickUpDelay();
            item.playerTouch(player);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return this.pick(state, level, pos, player) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        this.pick(state, level, pos, player);
    }
}

package com.progwml6.natura.common.block;

import java.util.function.Supplier;

import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockOverworldCrops (barley 0-3, cotton 0-4): grows like a vanilla crop, bone meal adds 2-5 stages, and a ripe
 * crop is picked by clicking it (left or right), dropping one crop at the player and going back to stage 1 (barley) /
 * 2 (cotton). Placed only on farmland; once planted it also stays on grass and dirt (1.12 canBlockStay used the plains
 * soil). Its drops (loot table) never get fortune: 1.12 dropped crop blocks with fortune 0.
 */
public abstract class NaturaCropBlock extends BushBlock implements BonemealableBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);
    private final IntegerProperty age;
    private final int maxAge;
    private final int pickedAge;
    private final Supplier<? extends ItemLike> crop;
    private final Supplier<? extends ItemLike> seed;

    protected NaturaCropBlock(IntegerProperty age, int maxAge, int pickedAge, Supplier<? extends ItemLike> crop, Supplier<? extends ItemLike> seed,
                              Properties properties) {
        super(properties);
        this.age = age;
        this.maxAge = maxAge;
        this.pickedAge = pickedAge;
        this.crop = crop;
        this.seed = seed;
        this.registerDefaultState(this.stateDefinition.any().setValue(age, 0));
    }

    public BlockState withAge(int age) {
        return this.defaultBlockState().setValue(this.age, age);
    }

    public int getAge(BlockState state) {
        return state.getValue(this.age);
    }

    /** The crop item (1.12's Waila plugin showed it instead of the seeds). */
    public ItemLike crop() {
        return this.crop.get();
    }

    public static final IntegerProperty AGE_3 = IntegerProperty.create("age", 0, 3);
    public static final IntegerProperty AGE_4 = IntegerProperty.create("age", 0, 4);

    /** Barley: stages 0-3, picked back to 1. */
    public static class Barley extends NaturaCropBlock {
        public Barley(Supplier<? extends ItemLike> crop, Supplier<? extends ItemLike> seed, Properties properties) {
            super(AGE_3, 3, 1, crop, seed, properties);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(AGE_3);
        }
    }

    /** Cotton: stages 0-4, picked back to 2. */
    public static class Cotton extends NaturaCropBlock {
        public Cotton(Supplier<? extends ItemLike> crop, Supplier<? extends ItemLike> seed, Properties properties) {
            super(AGE_4, 4, 2, crop, seed, properties);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(AGE_4);
        }
    }

    public IntegerProperty getAgeProperty() {
        return this.age;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos.below());
        return (level.getRawBrightness(pos, 0) >= 8 || level.canSeeSky(pos)) && (soil.is(Blocks.FARMLAND) || soil.is(Blocks.DIRT)
                || soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.COARSE_DIRT) || soil.is(Blocks.PODZOL));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        // a grown crop has nothing to do (as vanilla crops): no random ticks for whole fields of ripe barley and cotton
        return state.getValue(this.age) < this.maxAge;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9) {
            int i = state.getValue(this.age);
            if (i < this.maxAge) {
                float f = growthChance(this, level, pos);
                if (com.progwml6.natura.platform.Services.PLATFORM.canCropGrow(level, pos, state, random.nextInt((int) (25.0F / f) + 1) == 0)) {
                    level.setBlock(pos, state.setValue(this.age, i + 1), Block.UPDATE_CLIENTS);
                    com.progwml6.natura.platform.Services.PLATFORM.cropGrown(level, pos, state);
                }
            }
        }
    }

    /** 1.12 / vanilla BlockCrops.getGrowthChance. */
    static float growthChance(Block block, BlockGetter level, BlockPos pos) {
        float f = 1.0F;
        BlockPos below = pos.below();
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                float f1 = 0.0F;
                BlockState soil = level.getBlockState(below.offset(i, 0, j));
                if (soil.is(Blocks.FARMLAND) || soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK)) {
                    f1 = 1.0F;
                    if (soil.is(Blocks.FARMLAND) && soil.getValue(net.minecraft.world.level.block.FarmBlock.MOISTURE) > 0) {
                        f1 = 3.0F;
                    }
                }
                if (i != 0 || j != 0) {
                    f1 /= 4.0F;
                }
                f += f1;
            }
        }
        boolean flag = level.getBlockState(pos.west()).is(block) || level.getBlockState(pos.east()).is(block);
        boolean flag1 = level.getBlockState(pos.north()).is(block) || level.getBlockState(pos.south()).is(block);
        if (flag && flag1) {
            f /= 2.0F;
        } else if (level.getBlockState(pos.west().north()).is(block) || level.getBlockState(pos.east().north()).is(block)
                || level.getBlockState(pos.east().south()).is(block) || level.getBlockState(pos.west().south()).is(block)) {
            f /= 2.0F;
        }
        return f;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(this.age) < this.maxAge;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int i = Math.min(state.getValue(this.age) + Mth.nextInt(random, 2, 5), this.maxAge);
        level.setBlock(pos, state.setValue(this.age, i), Block.UPDATE_CLIENTS);
    }

    /** 1.12: a ripe crop is picked, one crop item spawned at the player's feet and picked up at once. */
    private boolean pick(BlockState state, Level level, BlockPos pos, Player player) {
        if (state.getValue(this.age) != this.maxAge) {
            return false;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, this.defaultBlockState().setValue(this.age, this.pickedAge), Block.UPDATE_ALL);
            NaturaBerryBushBlock.giveAtFeet(level, player, new ItemStack(this.crop.get()));
        }
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS; // 1.21 useWithoutItem: main hand only
        }
        return this.pick(state, level, pos, player) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        this.pick(state, level, pos, player);
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(this.seed.get());
    }

    public Item seedItem() {
        return this.seed.get().asItem();
    }
}

package com.progwml6.natura.common.item;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 ItemSaguaroFruit: food (3, 0.3) that plants a baby saguaro on top of sand or terracotta. */
public class SaguaroFruitItem extends NaturaFoodItem {
    private final Supplier<? extends Block> baby;

    public SaguaroFruitItem(Supplier<? extends Block> baby, Properties properties) {
        super(properties, 32);
        this.baby = baby;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        var player = context.getPlayer();
        if (player != null && (!player.mayUseItemAt(pos, Direction.UP, context.getItemInHand()) || !player.mayUseItemAt(pos.above(), Direction.UP, context.getItemInHand()))) {
            return InteractionResult.FAIL;
        }
        BlockState soil = level.getBlockState(pos);
        if ((soil.is(Blocks.SAND) || soil.is(Blocks.RED_SAND) || soil.is(BlockTags.TERRACOTTA)) && level.isEmptyBlock(pos.above())) {
            if (!level.isClientSide) {
                level.setBlock(pos.above(), this.baby.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            if (!((Object) player instanceof net.minecraft.world.entity.player.Player consumer && consumer.getAbilities().instabuild)) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }
}

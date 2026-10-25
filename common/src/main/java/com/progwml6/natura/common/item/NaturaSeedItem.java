package com.progwml6.natura.common.item;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 ItemSeeds (barley / cotton seeds): plants the crop on the top of farmland with air above. */
public class NaturaSeedItem extends NaturaItem {
    private final Supplier<? extends Block> crop;

    public NaturaSeedItem(Supplier<? extends Block> crop, Properties properties) {
        super(properties);
        this.crop = crop;
    }

    public Block getCrop() {
        return this.crop.get();
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
        if (soil.is(Blocks.FARMLAND) && level.isEmptyBlock(pos.above())) {
            if (!level.isClientSide) {
                level.setBlock(pos.above(), this.crop.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            if (!((Object) player instanceof net.minecraft.world.entity.player.Player consumer && consumer.getAbilities().instabuild)) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }
}

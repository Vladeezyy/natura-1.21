package com.progwml6.natura.common.item;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 ItemSeedBag: plants its crop on every fitting block of the 3x3 square around the clicked one (same height),
 * farmland for the crops, soul sand for nether wart, using one bag.
 */
public class NaturaSeedBagItem extends NaturaItem {
    private final Supplier<? extends Block> crop;

    public NaturaSeedBagItem(Supplier<? extends Block> crop, Properties properties) {
        super(properties);
        this.crop = crop;
    }

    private boolean sustains(BlockState soil) {
        return this.crop.get() == Blocks.NETHER_WART ? soil.is(Blocks.SOUL_SAND) : soil.is(Blocks.FARMLAND);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        boolean planted = false;
        BlockPos last = pos;
        for (int x = pos.getX() - 1; x <= pos.getX() + 1; x++) {
            for (int z = pos.getZ() - 1; z <= pos.getZ() + 1; z++) {
                BlockPos position = new BlockPos(x, pos.getY(), z);
                last = position;
                if (player != null && (!player.mayUseItemAt(position, Direction.UP, stack) || !player.mayUseItemAt(position.above(), Direction.UP, stack))) {
                    continue;
                }
                if (this.sustains(level.getBlockState(position)) && level.isEmptyBlock(position.above())) {
                    planted = true;
                    if (!level.isClientSide) {
                        level.setBlock(position.above(), this.crop.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        if (planted) {
            if (player == null || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (stack.isEmpty() && !level.isClientSide) {
                level.levelEvent(2001, last, Block.getId(this.crop.get().defaultBlockState()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}

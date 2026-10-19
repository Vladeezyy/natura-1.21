package com.progwml6.natura.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;

/** 1.12 ItemBoneBag: bone meal on every block of the 3x3 square around the clicked one, using one bag. */
public class NaturaBoneBagItem extends NaturaItem {
    public NaturaBoneBagItem(Properties properties) {
        super(properties);
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
        for (int x = pos.getX() - 1; x <= pos.getX() + 1; x++) {
            for (int z = pos.getZ() - 1; z <= pos.getZ() + 1; z++) {
                BlockPos position = new BlockPos(x, pos.getY(), z);
                if (player != null && (!player.mayUseItemAt(position, Direction.UP, stack) || !player.mayUseItemAt(position.above(), Direction.UP, stack))) {
                    continue;
                }
                // one bone meal for each block (the vanilla call uses up the stack it is given)
                if (com.progwml6.natura.platform.Services.PLATFORM.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, position, player)) {
                    planted = true;
                    if (!level.isClientSide()) {
                        level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, position, 15);
                    }
                }
            }
        }
        if (planted) {
            if (player == null || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}

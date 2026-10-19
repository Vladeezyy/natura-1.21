package com.progwml6.natura.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 BlockNetherLog (fusewood): harvesting it sets off a small explosion that hurts but breaks no blocks (1.12
 * createExplosion(..., isSmoking = false)), 2.0 on Hard, 1.75 on Easy / Normal, none on Peaceful. Centred on the block
 * corner like 1.12 (pos.getX(), not getX() + 0.5).
 */
public class FusewoodLogBlock extends NaturaLogBlock {
    public FusewoodLogBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            Difficulty difficulty = level.getDifficulty();
            if (difficulty == Difficulty.HARD) {
                level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 2.0F, Level.ExplosionInteraction.NONE);
            } else if (difficulty == Difficulty.NORMAL || difficulty == Difficulty.EASY) {
                level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 1.75F, Level.ExplosionInteraction.NONE);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}

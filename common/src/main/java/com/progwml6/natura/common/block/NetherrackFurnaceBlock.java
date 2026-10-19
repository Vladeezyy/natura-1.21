package com.progwml6.natura.common.block;

import com.mojang.serialization.MapCodec;
import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 BlockNetherrackFurnace: a furnace whose fuel burns twice as long (one block with a lit state, 1.12 had two). */
public class NetherrackFurnaceBlock extends AbstractFurnaceBlock {
    public NetherrackFurnaceBlock(Properties properties) {
        super(properties);
    }


    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof NetherrackFurnaceBlockEntity furnace) {
            player.openMenu(furnace);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NetherrackFurnaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createFurnaceTicker(level, type, NaturaBlocks.NETHERRACK_FURNACE_BE.get());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (state.getValue(LIT)) {
            double d0 = pos.getX() + 0.5D;
            double d1 = pos.getY() + rand.nextDouble() * 6.0D / 16.0D;
            double d2 = pos.getZ() + 0.5D;
            double d4 = rand.nextDouble() * 0.6D - 0.3D;
            if (rand.nextDouble() < 0.1D) {
                level.playLocalSound(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            Direction facing = state.getValue(FACING);
            double x = d0 + (facing == Direction.WEST ? -0.52D : facing == Direction.EAST ? 0.52D : d4);
            double z = d2 + (facing == Direction.NORTH ? -0.52D : facing == Direction.SOUTH ? 0.52D : d4);
            level.addParticle(ParticleTypes.SMOKE, x, d1, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, x, d1, z, 0.0D, 0.0D, 0.0D);
        }
    }
}

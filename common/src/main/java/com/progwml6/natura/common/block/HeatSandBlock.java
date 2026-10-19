package com.progwml6.natura.common.block;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.entity.InsideBlockEffectApplier;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockHeatSand: falling sand you sink into a little (collision 14/16) that burns (1, "in fire") players without
 * boots and mobs that are not fire immune.
 */
public class HeatSandBlock extends FallingBlock {
    static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

    public HeatSandBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        burn(level, entity);
    }

    /** 1.12: players with an empty boots slot, or mobs (EntityLiving) that do not resist fire. */
    static void burn(Level level, Entity entity) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (entity instanceof Player player) {
            if (player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
                entity.hurtServer(server, level.damageSources().inFire(), 1);
            }
        } else if (entity instanceof Mob && !entity.fireImmune()) {
            entity.hurtServer(server, level.damageSources().inFire(), 1);
        }
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return 0x7f4c33;
    }
}

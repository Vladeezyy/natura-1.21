package com.progwml6.natura.common.block;

import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.InsideBlockEffectApplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockCloud (white, dark, ash, sulfur): you sink into it (only its bottom 1/16 is solid, nothing when a cloud is
 * under it), falling slows to a crawl (x0.005) and causes no fall damage. A sulfur cloud explodes (1, breaking blocks)
 * when lit with flint and steel or hit by a burning arrow. Clouds never drop from explosions.
 */
public class CloudBlock extends Block {
    private static final VoxelShape BOTTOM = Block.box(0, 0, 0, 16, 1, 16);
    private final boolean sulfur;

    public CloudBlock(boolean sulfur, Properties properties) {
        super(properties);
        this.sulfur = sulfur;
    }

    @Override
    public boolean dropFromExplosion(net.minecraft.world.level.Explosion explosion) {
        return false;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return level.getBlockState(pos.below()).getBlock() instanceof CloudBlock ? Shapes.empty() : BOTTOM;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.getBlock() instanceof CloudBlock || super.skipRendering(state, adjacent, direction);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (this.sulfur && entity instanceof AbstractArrow arrow && !level.isClientSide() && arrow.isOnFire()) {
            explode(level, pos, arrow.getOwner() instanceof LivingEntity living ? living : null);
            level.removeBlock(pos, false);
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y < 0.0D) {
            entity.setDeltaMovement(motion.x, motion.y * 0.005D, motion.z);
        }
        entity.fallDistance = 0.0F;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (this.sulfur && stack.is(Items.FLINT_AND_STEEL)) {
            level.removeBlock(pos, false);
            explode(level, pos, player);
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    /** 1.12 createExplosion(entity, corner, size, smoking = true): breaks blocks, no fire. */
    private static void explode(Level level, BlockPos pos, LivingEntity cause) {
        if (!level.isClientSide()) {
            level.explode(cause, pos.getX(), pos.getY(), pos.getZ(), 1, Level.ExplosionInteraction.BLOCK);
        }
    }
}

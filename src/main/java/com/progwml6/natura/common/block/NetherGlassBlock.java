package com.progwml6.natura.common.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockNetherGlass: soul glass (no collision, slowness II for a second to whoever is in it) and heat glass
 * (collision 14/16, burns like heat sand). Both very blast resistant and dropped only with Silk Touch.
 */
public class NetherGlassBlock extends HalfTransparentBlock {
    private final boolean soul;

    public NetherGlassBlock(boolean soul, Properties properties) {
        super(properties);
        this.soul = soul;
    }

    @Override
    protected MapCodec<? extends HalfTransparentBlock> codec() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.soul ? Shapes.empty() : HeatSandBlock.SHAPE;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity living) {
            if (this.soul) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1));
            } else {
                HeatSandBlock.burn(level, entity);
            }
        }
    }
}

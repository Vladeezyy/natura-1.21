package com.progwml6.natura.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

/** 1.12 blaze rails: rails with a 0.65 top speed (vanilla 0.4); the golden one also pushes and brakes in its own way. */
public final class BlazeRailBlocks {
    private BlazeRailBlocks() {}

    public static class Normal extends RailBlock {
        public Normal(Properties properties) {
            super(properties);
        }

        @Override
        public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
            return 0.65F;
        }
    }

    public static class Detector extends DetectorRailBlock {
        public Detector(Properties properties) {
            super(properties);
        }

        @Override
        public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
            return 0.65F;
        }
    }

    /**
     * 1.12 BlockBlazeRailPowered: powered, the golden rail pushes the cart sideways off a solid block (0.2, on the axis
     * across the rail as 1.12 did) and speeds it up (x1.2 under 0.2, else x1.05); unpowered it halves the speed. The
     * activator rail does what the vanilla one does.
     */
    public static class Powered extends PoweredRailBlock {
        private final boolean activator;

        public Powered(Properties properties, boolean activator) {
            super(properties, !activator);
            this.activator = activator;
        }

        @Override
        public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
            return 0.65F;
        }

        @Override
        public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
            RailShape shape = this.getRailDirection(state, level, pos, cart);
            Vec3 m = cart.getDeltaMovement();
            double mx = m.x, mz = m.z;
            if (state.getValue(POWERED)) {
                if (this.activator) {
                    return;
                }
                if (shape == RailShape.EAST_WEST) {
                    double speed = 0;
                    if (level.getBlockState(pos.west()).isRedstoneConductor(level, pos.west())) {
                        speed -= 0.2F;
                    } else if (level.getBlockState(pos.east()).isRedstoneConductor(level, pos.east())) {
                        speed += 0.2F;
                    }
                    mz += speed;
                } else if (shape == RailShape.NORTH_SOUTH) {
                    double speed = 0;
                    if (level.getBlockState(pos.north()).isRedstoneConductor(level, pos.north())) {
                        speed += 0.2F;
                    } else if (level.getBlockState(pos.south()).isRedstoneConductor(level, pos.south())) {
                        speed -= 0.2F;
                    }
                    mx += speed;
                }
                mx *= Math.abs(mx) < 0.2 ? 1.2 : 1.05;
                mz *= Math.abs(mz) < 0.2 ? 1.2 : 1.05;
            } else if (!this.activator) {
                mx *= 0.5;
                mz *= 0.5;
            }
            cart.setDeltaMovement(mx, m.y, mz);
        }
    }
}

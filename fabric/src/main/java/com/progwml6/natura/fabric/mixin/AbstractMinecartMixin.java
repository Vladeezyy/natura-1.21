package com.progwml6.natura.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.progwml6.natura.common.block.BlazeRailBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The blaze rails on Fabric (NeoForge: its rail API): a 0.65 top speed, the golden one boosts like the powered rail and
 * then runs its own push (NeoForge calls onMinecartPass just before the boost), the activator one activates carts.
 */
@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean natura$activatorRail(BlockState state, Block block, Operation<Boolean> original) {
        return original.call(state, block)
                || block == Blocks.ACTIVATOR_RAIL && state.getBlock() instanceof BlazeRailBlocks.Powered rail && rail.isActivatorRail();
    }

    @WrapOperation(method = "moveAlongTrack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean natura$poweredRail(BlockState state, Block block, Operation<Boolean> original) {
        return original.call(state, block)
                || block == Blocks.POWERED_RAIL && state.getBlock() instanceof BlazeRailBlocks.Powered rail && !rail.isActivatorRail();
    }

    @WrapOperation(method = "moveAlongTrack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;getMaxSpeed()D"))
    private double natura$maxSpeed(AbstractMinecart cart, Operation<Double> original, @Local(argsOnly = true) BlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlazeRailBlocks.Normal || block instanceof BlazeRailBlocks.Detector || block instanceof BlazeRailBlocks.Powered) {
            return BlazeRailBlocks.MAX_SPEED;
        }
        return original.call(cart);
    }

    @Inject(method = "moveAlongTrack", at = @At("TAIL"))
    private void natura$pass(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (state.getBlock() instanceof BlazeRailBlocks.Powered rail) {
            AbstractMinecart cart = (AbstractMinecart) (Object) this;
            rail.onMinecartPass(state, cart.level(), pos, cart);
        }
    }
}

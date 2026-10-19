package com.progwml6.natura.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.progwml6.natura.common.block.BlazeRailBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The blaze rails (26.x: NeoForge has no rail speed / minecart pass hooks any more, so on both loaders): a 0.65 top
 * speed, and the golden one runs its own push after the track move (NeoForge 1.21.1 ran it just before the boost).
 */
@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin extends MinecartBehavior {
    protected OldMinecartBehaviorMixin(AbstractMinecart minecart) {
        super(minecart);
    }

    @WrapOperation(method = "moveAlongTrack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;getMaxSpeed(Lnet/minecraft/server/level/ServerLevel;)D"))
    private double natura$maxSpeed(AbstractMinecart cart, ServerLevel level, Operation<Double> original, @Local(ordinal = 0) BlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlazeRailBlocks.Normal || block instanceof BlazeRailBlocks.Detector || block instanceof BlazeRailBlocks.Powered) {
            return BlazeRailBlocks.MAX_SPEED;
        }
        return original.call(cart, level);
    }

    @Inject(method = "moveAlongTrack", at = @At("RETURN"))
    private void natura$pass(ServerLevel level, CallbackInfo ci, @Local(ordinal = 0) BlockPos pos, @Local(ordinal = 0) BlockState state) {
        if (state.getBlock() instanceof BlazeRailBlocks.Powered rail) {
            rail.onMinecartPass(state, level, pos, this.minecart);
        }
    }
}

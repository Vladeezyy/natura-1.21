package com.progwml6.natura.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.progwml6.natura.common.block.BlazeRailBlocks;

import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The blaze rails on Fabric: the golden one acts as a powered rail, the activator one as an activator rail (vanilla
 * checks those two blocks by identity; NeoForge asks PoweredRailBlock.isActivatorRail). The speed and the golden rail's
 * push are in the common mixin. 26.x: BlockState.is(Block) is is(Object) in the bytecode (TypedInstance).
 */
@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorFabricMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private boolean natura$activatorRail(BlockState state, Object block, Operation<Boolean> original) {
        return original.call(state, block)
                || block == Blocks.ACTIVATOR_RAIL && state.getBlock() instanceof BlazeRailBlocks.Powered rail && rail.isActivatorRail();
    }

    @WrapOperation(method = "moveAlongTrack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private boolean natura$poweredRail(BlockState state, Object block, Operation<Boolean> original) {
        return original.call(state, block)
                || block == Blocks.POWERED_RAIL && state.getBlock() instanceof BlazeRailBlocks.Powered rail && !rail.isActivatorRail();
    }
}

package com.progwml6.natura.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.progwml6.natura.common.block.TaintedSoilBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.NetherFungusBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.NetherRootsBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Nether wart, fungi and roots grow on tainted soil (NeoForge: TaintedSoilBlock canSustainPlant). */
@Mixin({NetherWartBlock.class, NetherFungusBlock.class, NetherRootsBlock.class})
public abstract class TaintedSoilPlantMixin {
    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void natura$taintedSoil(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof TaintedSoilBlock) {
            cir.setReturnValue(true);
        }
    }
}

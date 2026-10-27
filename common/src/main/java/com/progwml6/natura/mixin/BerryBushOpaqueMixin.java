package com.progwml6.natura.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.progwml6.natura.common.block.NaturaBerryBushBlock;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 BlockEnumBerryBush.getBlockLayer: berry bushes are drawn opaque like the leaves when cutout leaves are off
 * (26.x: vanilla only does that for a LeavesBlock).
 */
@Mixin(ModelBlockRenderer.class)
public abstract class BerryBushOpaqueMixin {
    @Inject(method = "forceOpaque", at = @At("HEAD"), cancellable = true)
    private static void natura$bushLikeLeaves(boolean cutoutLeaves, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof NaturaBerryBushBlock) {
            cir.setReturnValue(!cutoutLeaves);
        }
    }
}

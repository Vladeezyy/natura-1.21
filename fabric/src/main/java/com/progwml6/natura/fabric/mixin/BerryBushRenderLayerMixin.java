package com.progwml6.natura.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.progwml6.natura.common.block.NaturaBerryBushBlock;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 BlockEnumBerryBush.getBlockLayer: berry bushes take the leaves' layer, cutout_mipped with fancy graphics and solid
 * with fast ones (Forge: NaturaForgeClient.bushRenderTypes).
 */
@Mixin(ItemBlockRenderTypes.class)
public abstract class BerryBushRenderLayerMixin {
    @Inject(method = "getChunkRenderType", at = @At("HEAD"), cancellable = true)
    private static void natura$bushLikeLeaves(BlockState state, CallbackInfoReturnable<RenderType> cir) {
        if (state.getBlock() instanceof NaturaBerryBushBlock) {
            cir.setReturnValue(ItemBlockRenderTypes.getChunkRenderType(Blocks.OAK_LEAVES.defaultBlockState()));
        }
    }
}

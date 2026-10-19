package com.progwml6.natura.fabric.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.progwml6.natura.common.block.RespawnObeliskBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;

/** The player respawns on an active obelisk (NeoForge: IBlockExtension.getRespawnPosition for a non-forced spawn block). */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "findRespawnAndUseSpawnBlock", at = @At("HEAD"), cancellable = true)
    private static void natura$obelisk(ServerLevel level, BlockPos pos, float angle, boolean forced, boolean keepInventory,
                                       CallbackInfoReturnable<Optional<ServerPlayer.RespawnPosAngle>> cir) {
        BlockState state = level.getBlockState(pos);
        if (!forced && state.getBlock() instanceof RespawnObeliskBlock obelisk) {
            cir.setReturnValue(obelisk.getRespawnPosition(state, EntityType.PLAYER, level, pos, angle));
        }
    }
}

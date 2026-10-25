package com.progwml6.natura.fabric.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.progwml6.natura.common.block.RespawnObeliskBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** The player respawns on an active obelisk (Forge: IForgeBlock.getRespawnPosition for a non-forced spawn block). */
@Mixin(Player.class)
public abstract class PlayerRespawnMixin {
    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("HEAD"), cancellable = true)
    private static void natura$obelisk(ServerLevel level, BlockPos pos, float angle, boolean forced, boolean keepInventory,
                                       CallbackInfoReturnable<Optional<Vec3>> cir) {
        BlockState state = level.getBlockState(pos);
        if (!forced && state.getBlock() instanceof RespawnObeliskBlock obelisk) {
            cir.setReturnValue(obelisk.getRespawnPosition(state, EntityType.PLAYER, level, pos, angle, null));
        }
    }
}

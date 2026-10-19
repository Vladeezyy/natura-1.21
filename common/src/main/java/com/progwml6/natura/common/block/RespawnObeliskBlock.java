package com.progwml6.natura.common.block;

import java.util.Optional;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 1.12 BlockRespawnObelisk: right click (not sneaking) makes it the player's respawn point (any dimension, the nether
 * too), lights it up (7, portal and witch particles) and says "&lt;name&gt;: Spawn point set!". The player respawns on top
 * of an active obelisk. Breaking one clears the breaker's respawn point (1.12 reset the spawn dimension).
 */
public class RespawnObeliskBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public RespawnObeliskBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Random rand = new Random();
        rand.setSeed(2 ^ 16 + 2 ^ 8 + (4 * 3 * 271));
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.setRespawnPosition(level.dimension(), pos, 0.0F, false, false);
            serverPlayer.sendSystemMessage(player.getDisplayName().copy().append(": Spawn point set!"));
        }
        level.setBlock(pos, this.defaultBlockState().setValue(ACTIVE, true), Block.UPDATE_ALL);
        level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F,
                rand.nextFloat() * 0.4F + 0.8F, false);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** NeoForge IBlockExtension.isBed (same signature); Fabric: a mixin into the respawn lookup calls it. */
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, LivingEntity sleeper) {
        return state.getValue(ACTIVE);
    }

    /** NeoForge IBlockExtension.getRespawnPosition (same signature); Fabric: the same mixin. */
    public Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader level, BlockPos pos, float orientation) {
        if (!state.getValue(ACTIVE)) {
            return Optional.empty();
        }
        return Optional.of(new ServerPlayer.RespawnPosAngle(new Vec3(pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D), orientation));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (state.getValue(ACTIVE)) {
            for (int i = 0; i < 2; i++) {
                double x = pos.getX() + rand.nextFloat() * 3 - 1;
                double y = pos.getY() + rand.nextFloat() * 2;
                double z = pos.getZ() + rand.nextFloat() * 3 - 1;
                level.addParticle(ParticleTypes.PORTAL, x, y, z, 0.0D, 0.0D, 0.0D);
                level.addParticle(ParticleTypes.WITCH, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.setRespawnPosition(Level.OVERWORLD, null, 0.0F, false, false);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}

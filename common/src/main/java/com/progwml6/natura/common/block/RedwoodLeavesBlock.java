package com.progwml6.natura.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * 1.12 BlockRedwoodLeaves: not the vanilla distance decay. A leaf only checks once something near it broke (1.12
 * CHECK_DECAY, set by a log breaking within 4 blocks or a leaf within 2), and then decays if no log is left in the 9x9x9
 * cube around it. The flag is cleared again when a log is found (1.12 kept checking every random tick).
 */
public class RedwoodLeavesBlock extends NaturaLeavesBlock {
    public static final BooleanProperty CHECK_DECAY = BooleanProperty.create("check_decay");

    public RedwoodLeavesBlock(Properties properties) {
        super(properties, false);
        this.registerDefaultState(this.defaultBlockState().setValue(CHECK_DECAY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CHECK_DECAY);
    }

    /** 1.12 Block.beginLeavesDecay for every redwood leaf in the cube of the given radius around pos. */
    public static void beginLeavesDecay(Level level, BlockPos pos, int radius) {
        if (level.isClientSide || !level.hasChunksAt(pos.offset(-radius - 1, -radius - 1, -radius - 1), pos.offset(radius + 1, radius + 1, radius + 1))) {
            return;
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
            BlockState state = level.getBlockState(p);
            if (state.getBlock() instanceof RedwoodLeavesBlock && !state.getValue(CHECK_DECAY)) {
                level.setBlock(p, state.setValue(CHECK_DECAY, true), Block.UPDATE_INVISIBLE);
            }
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(CHECK_DECAY) && !state.getValue(BlockStateProperties.PERSISTENT);
    }

    @Override
    protected boolean decaying(BlockState state) {
        return false;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(CHECK_DECAY) || state.getValue(BlockStateProperties.PERSISTENT)) {
            return;
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -4, -4), pos.offset(4, 4, 4))) {
            if (level.getBlockState(p).is(BlockTags.LOGS)) {
                level.setBlock(pos, state.setValue(CHECK_DECAY, false), Block.UPDATE_INVISIBLE);
                return;
            }
        }
        dropResources(state, level, pos);
        level.removeBlock(pos, false);
    }
}

package com.progwml6.natura.client;

import java.util.function.BiConsumer;

import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.color.block.BlockTintSource;

/**
 * Client registrations: the 1.12 LeavesColorizer tints; the loaders call these. 26.x: the item tints and the bow
 * "pulling" models are item model definitions (assets/natura/items, tools/convert_26.py).
 */
public final class NaturaClient {
    private static final int MAPLE = 0xcc5718;
    private static final int AMARANTH = 10079569;
    private static final int NO_COLOR = 16777215;
    private static final int LEAVES2 = 7842607;

    private NaturaClient() {}

    /** 1.12 LeavesColorizer: the colour in the world, or the static one without a world (items). RGB. */
    static int leavesColor(String leaves, BlockAndTintGetter level, BlockPos pos) {
        boolean world = level != null && pos != null;
        return switch (leaves) {
            case "maple_leaves" -> MAPLE;
            // 1.12 added 0x222222 to the packed colour (each channel brighter, carrying into the next one past 0xff)
            case "amaranth_leaves" -> world ? (BiomeColors.getAverageFoliageColor(level, pos) & 0xFFFFFF) + 0x222222 : AMARANTH;
            case "eucalyptus_leaves", "hopseed_leaves", "redwood_leaves" -> world ? BiomeColors.getAverageFoliageColor(level, pos) & 0xFFFFFF : LEAVES2;
            default -> NO_COLOR;
        };
    }

    /** 0 topiary, 1 bluegrass, 2 autumnal, for the colored grass blocks, slabs and stairs. */
    private static int grassType(String id) {
        return id.startsWith("bluegrass") ? 1 : id.startsWith("autumnal") ? 2 : 0;
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    /** The block tints (26.x BlockTintSource: one source per tint index). */
    public static void blockColors(BiConsumer<BlockTintSource, Block> event) {
        NaturaBlocks.LEAVES.forEach((id, block) -> event.accept(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return opaque(leavesColor(id, null, null));
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return opaque(leavesColor(id, level, pos));
            }
        }, block.get()));
        NaturaBlocks.COLORED_GRASS.forEach((id, block) -> event.accept(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return opaque(GrassColorizer.colorStatic(grassType(id)));
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                return opaque(GrassColorizer.colorForPos(level, pos, grassType(id)));
            }
        }, block.get()));
    }
}

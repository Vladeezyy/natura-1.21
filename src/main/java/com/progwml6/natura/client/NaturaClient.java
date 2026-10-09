package com.progwml6.natura.client;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/** Client registrations: the 1.12 LeavesColorizer tints. */
@EventBusSubscriber(modid = Natura.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NaturaClient {
    private static final int MAPLE = 0xcc5718;
    private static final int AMARANTH = 10079569;
    private static final int NO_COLOR = 16777215;
    private static final int LEAVES2 = 7842607;

    private NaturaClient() {}

    /** 1.12 LeavesColorizer: the colour in the world, or the static one without a world (items). */
    static int leavesColor(String leaves, BlockAndTintGetter level, BlockPos pos) {
        boolean world = level != null && pos != null;
        return switch (leaves) {
            case "maple_leaves" -> MAPLE;
            // 1.12 added 0x222222 to the packed colour (each channel brighter, carrying into the next one past 0xff)
            case "amaranth_leaves" -> world ? BiomeColors.getAverageFoliageColor(level, pos) + 0x222222 : AMARANTH;
            case "eucalyptus_leaves", "hopseed_leaves", "redwood_leaves" -> world ? BiomeColors.getAverageFoliageColor(level, pos) : LEAVES2;
            default -> NO_COLOR;
        };
    }

    @SubscribeEvent
    static void blockColors(RegisterColorHandlersEvent.Block event) {
        NaturaBlocks.LEAVES.forEach((id, block) -> event.register((BlockState state, BlockAndTintGetter level, BlockPos pos, int tint) ->
                leavesColor(id, level, pos), block.get()));
    }

    @SubscribeEvent
    static void itemColors(RegisterColorHandlersEvent.Item event) {
        NaturaBlocks.LEAVES.forEach((id, block) -> event.register((stack, tint) -> 0xFF000000 | leavesColor(id, null, null), block.get()));
    }
}

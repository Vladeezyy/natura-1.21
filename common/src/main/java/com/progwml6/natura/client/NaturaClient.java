package com.progwml6.natura.client;

import java.util.function.BiConsumer;

import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ItemLike;

/** Client registrations: the 1.12 LeavesColorizer tints and the bow properties; the loaders call these. */
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

    /** 0 topiary, 1 bluegrass, 2 autumnal, for the colored grass blocks, slabs and stairs. */
    private static int grassType(String id) {
        return id.startsWith("bluegrass") ? 1 : id.startsWith("autumnal") ? 2 : 0;
    }

    public static void blockColors(BiConsumer<BlockColor, Block> event) {
        NaturaBlocks.LEAVES.forEach((id, block) -> event.accept((BlockState state, BlockAndTintGetter level, BlockPos pos, int tint) ->
                leavesColor(id, level, pos), block.get()));
        NaturaBlocks.COLORED_GRASS.forEach((id, block) -> event.accept((BlockState state, BlockAndTintGetter level, BlockPos pos, int tint) ->
                level != null && pos != null ? GrassColorizer.colorForPos(level, pos, grassType(id)) : GrassColorizer.colorStatic(grassType(id)), block.get()));
    }

    public static void itemColors(BiConsumer<ItemColor, ItemLike> event) {
        NaturaBlocks.LEAVES.forEach((id, block) -> event.accept((stack, tint) -> 0xFF000000 | leavesColor(id, null, null), block.get()));
        NaturaBlocks.COLORED_GRASS.forEach((id, block) -> event.accept((stack, tint) -> 0xFF000000 | GrassColorizer.colorStatic(grassType(id)), block.get()));
    }

    /**
     * 1.12 ItemNaturaBow: "pulling" like the vanilla bow; its "pull" compared the drawn item with a field that was still
     * null when the bow was made, so it was always 0 and the bows only ever show their first pulling frame.
     */
    /** On the main thread after the registries (NeoForge FMLClientSetupEvent work, Fabric client init). */
    public static void setup() {
        for (String wood : new String[] {"ghostwood", "bloodwood", "darkwood", "fusewood"}) {
            var bow = com.progwml6.natura.tools.NaturaTools.TOOLS.get(wood + "_bow").get();
            net.minecraft.client.renderer.item.ItemProperties.register(bow, new net.minecraft.resources.ResourceLocation("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            net.minecraft.client.renderer.item.ItemProperties.register(bow, new net.minecraft.resources.ResourceLocation("pull"),
                    (stack, level, entity, seed) -> 0.0F);
        }
    }
}

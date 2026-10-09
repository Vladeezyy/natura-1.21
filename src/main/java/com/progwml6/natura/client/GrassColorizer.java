package com.progwml6.natura.client;

import java.io.IOException;

import com.progwml6.natura.Natura;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.biome.Biome;

/**
 * 1.12 GrassColorizer: topiary grass takes the biome grass colour, bluegrass and autumnal grass look up the Natura
 * colour maps (textures/colormap) with the biome temperature and rainfall, averaged over the 3x3 around the block.
 */
public final class GrassColorizer implements ResourceManagerReloadListener {
    private static int[] blue = new int[65536];
    private static int[] orange = new int[65536];
    private static final ResourceLocation BLUE = ResourceLocation.fromNamespaceAndPath(Natura.MODID, "textures/colormap/bluegrasscolor.png");
    private static final ResourceLocation ORANGE = ResourceLocation.fromNamespaceAndPath(Natura.MODID, "textures/colormap/orangegrasscolor.png");

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        try {
            blue = net.minecraft.client.resources.LegacyStuffWrapper.getPixels(manager, BLUE);
            orange = net.minecraft.client.resources.LegacyStuffWrapper.getPixels(manager, ORANGE);
        } catch (IOException e) {
            Natura.LOG.error("Could not load the grass colour maps", e);
        }
    }

    private static int lookup(int[] buffer, double temperature, double humidity) {
        humidity *= temperature;
        int i = (int) ((1.0D - temperature) * 255.0D);
        int j = (int) ((1.0D - humidity) * 255.0D);
        int index = j << 8 | i;
        return index >= 0 && index < buffer.length ? buffer[index] : 0xFFFFFF;
    }

    /** type: 0 topiary, 1 bluegrass, 2 autumnal. */
    public static int colorForPos(BlockAndTintGetter level, BlockPos pos, int type) {
        if (type == 0) {
            return BiomeColors.getAverageGrassColor(level, pos);
        }
        if (!(level instanceof net.minecraft.world.level.LevelReader reader)) {
            return colorStatic(type);
        }
        int r = 0, g = 0, b = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
            Biome biome = reader.getBiome(p).value();
            // 1.12 used the height-adjusted temperature of the centre position; 1.21 keeps that private: the biome's own
            double temp = Mth.clamp(biome.getBaseTemperature(), 0.0F, 1.0F);
            double rain = Mth.clamp(biome.getModifiedClimateSettings().downfall(), 0.0F, 1.0F);
            int color = type == 1 ? lookup(blue, temp, rain) : lookup(orange, temp, rain);
            r += (color & 0xFF0000) >> 16;
            g += (color & 0xFF00) >> 8;
            b += color & 0xFF;
        }
        return (r / 9 & 255) << 16 | (g / 9 & 255) << 8 | b / 9 & 255;
    }

    public static int colorStatic(int type) {
        return switch (type) {
            case 1 -> lookup(blue, 0.5D, 0.5D);
            case 2 -> lookup(orange, 1.0D, 1.0D);
            default -> GrassColor.get(0.5D, 1.0D);
        };
    }
}

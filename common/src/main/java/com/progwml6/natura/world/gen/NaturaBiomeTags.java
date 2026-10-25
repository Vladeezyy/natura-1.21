package com.progwml6.natura.world.gen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/** The conventional (c:) biome tags the 1.12 BiomeDictionary checks became; Forge and Fabric both fill them. */
public final class NaturaBiomeTags {
    public static final TagKey<Biome> IS_BEACH = tag("is_beach");
    public static final TagKey<Biome> IS_END = tag("is_end");
    public static final TagKey<Biome> IS_FOREST = tag("is_forest");
    public static final TagKey<Biome> IS_HILL = tag("is_hill");
    public static final TagKey<Biome> IS_JUNGLE = tag("is_jungle");
    public static final TagKey<Biome> IS_MOUNTAIN = tag("is_mountain");
    public static final TagKey<Biome> IS_NETHER = tag("is_nether");
    public static final TagKey<Biome> IS_PLAINS = tag("is_plains");
    public static final TagKey<Biome> IS_RIVER = tag("is_river");
    public static final TagKey<Biome> IS_SANDY = tag("is_sandy");
    public static final TagKey<Biome> IS_SWAMP = tag("is_swamp");

    private NaturaBiomeTags() {}

    private static TagKey<Biome> tag(String name) {
        return TagKey.create(Registries.BIOME, new ResourceLocation("c", name));
    }
}

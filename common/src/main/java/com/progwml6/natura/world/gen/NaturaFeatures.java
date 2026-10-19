package com.progwml6.natura.world.gen;


import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.progwml6.natura.platform.Entry;
import com.progwml6.natura.platform.Registration;

public final class NaturaFeatures {
    /** 26.3: the feature type is its codec; the features themselves are data (worldgen/feature). */
    public static final Entry<MapCodec<? extends Feature>, MapCodec<NaturaWorldFeature>> WORLD = Registration.register(Registries.FEATURE_TYPE, "world",
            () -> NaturaWorldFeature.CODEC);

    public static void init() {}

    private NaturaFeatures() {}
}

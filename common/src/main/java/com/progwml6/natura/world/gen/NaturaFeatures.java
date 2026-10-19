package com.progwml6.natura.world.gen;


import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.progwml6.natura.platform.Entry;
import com.progwml6.natura.platform.Registration;

public final class NaturaFeatures {
    public static final Entry<Feature<?>, NaturaWorldFeature> WORLD = Registration.register(Registries.FEATURE, "world", NaturaWorldFeature::new);

    public static void init() {}

    private NaturaFeatures() {}
}

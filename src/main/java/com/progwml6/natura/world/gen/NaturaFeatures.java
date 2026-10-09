package com.progwml6.natura.world.gen;

import com.progwml6.natura.Natura;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NaturaFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Natura.MODID);
    public static final DeferredHolder<Feature<?>, NaturaWorldFeature> WORLD = FEATURES.register("world", NaturaWorldFeature::new);

    private NaturaFeatures() {}
}

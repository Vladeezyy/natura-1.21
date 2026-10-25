package com.progwml6.natura.forge;

import com.progwml6.natura.common.NaturaConfig;
import com.progwml6.natura.platform.ConfigSpec;

import net.minecraftforge.common.ForgeConfigSpec;

/** config/natura-common.toml: a ForgeConfigSpec built from the common {@link NaturaConfig#SPEC}, its values bound to it. */
final class NaturaForgeConfig {
    private NaturaForgeConfig() {}

    static ForgeConfigSpec build() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        for (ConfigSpec.Value<?> value : NaturaConfig.SPEC.values()) {
            if (!value.comment().isEmpty()) {
                builder.comment(value.comment().toArray(String[]::new));
            }
            if (value instanceof ConfigSpec.BoolValue bool) {
                ForgeConfigSpec.BooleanValue spec = builder.define(value.path(), (boolean) bool.defaultValue());
                bool.bind(spec);
            } else if (value instanceof ConfigSpec.IntValue integer) {
                ForgeConfigSpec.IntValue spec = builder.defineInRange(value.path(), (int) integer.defaultValue(), integer.min(), integer.max());
                integer.bind(spec);
            }
        }
        return builder.build();
    }
}

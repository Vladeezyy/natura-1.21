package com.progwml6.natura.neoforge;

import com.progwml6.natura.common.NaturaConfig;
import com.progwml6.natura.platform.ConfigSpec;

import net.neoforged.neoforge.common.ModConfigSpec;

/** config/natura-common.toml: a ModConfigSpec built from the common {@link NaturaConfig#SPEC}, its values bound to it. */
final class NaturaNeoForgeConfig {
    private NaturaNeoForgeConfig() {}

    static ModConfigSpec build() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        for (ConfigSpec.Value<?> value : NaturaConfig.SPEC.values()) {
            if (!value.comment().isEmpty()) {
                builder.comment(value.comment().toArray(String[]::new));
            }
            if (value instanceof ConfigSpec.BoolValue bool) {
                ModConfigSpec.BooleanValue spec = builder.define(value.path(), (boolean) bool.defaultValue());
                bool.bind(spec);
            } else if (value instanceof ConfigSpec.IntValue integer) {
                ModConfigSpec.IntValue spec = builder.defineInRange(value.path(), (int) integer.defaultValue(), integer.min(), integer.max());
                integer.bind(spec);
            }
        }
        return builder.build();
    }
}

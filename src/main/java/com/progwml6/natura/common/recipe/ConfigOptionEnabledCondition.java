package com.progwml6.natura.common.recipe;

import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.progwml6.natura.common.NaturaConfig;

import net.neoforged.neoforge.common.conditions.ICondition;

/** 1.12 ConfigOptionEnabledConditionFactory: a recipe only there when a config option is on (natura:config_option_enabled). */
public record ConfigOptionEnabledCondition(String setting) implements ICondition {
    public static final MapCodec<ConfigOptionEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("config_setting").forGetter(ConfigOptionEnabledCondition::setting)).apply(i, ConfigOptionEnabledCondition::new));

    private static final Map<String, Supplier<Boolean>> SETTINGS = Map.of(
            "enableWheatRecipe", NaturaConfig.ENABLE_WHEAT_RECIPE::get);

    @Override
    public boolean test(IContext context) {
        Supplier<Boolean> value = SETTINGS.get(this.setting);
        return value != null && value.get();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}

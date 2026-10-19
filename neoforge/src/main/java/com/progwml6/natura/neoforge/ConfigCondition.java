package com.progwml6.natura.neoforge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.progwml6.natura.common.recipe.ConfigOptionEnabledCondition;

import net.neoforged.neoforge.common.conditions.ICondition;

/** natura:config_option_enabled in neoforge:conditions (recipes, biome modifiers). */
public record ConfigCondition(String setting) implements ICondition {
    public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf(ConfigOptionEnabledCondition.FIELD).forGetter(ConfigCondition::setting)).apply(i, ConfigCondition::new));

    @Override
    public boolean test(IContext context) {
        return ConfigOptionEnabledCondition.test(this.setting);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}

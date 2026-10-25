package com.progwml6.natura.common.recipe;

import java.util.Map;
import java.util.function.Supplier;

import com.progwml6.natura.common.NaturaConfig;

/**
 * 1.12 ConfigOptionEnabledConditionFactory: a recipe (or another data entry, like the heatscar spider spawns) only there
 * when a config option is on (natura:config_option_enabled, field "config_setting"). The loaders wrap it in their own
 * condition types (Forge: conditions / forge:conditions, Fabric: fabric:load_conditions).
 */
public final class ConfigOptionEnabledCondition {
    public static final String ID = "config_option_enabled";
    public static final String FIELD = "config_setting";

    private static final Map<String, Supplier<Boolean>> SETTINGS = Map.of(
            "enableWheatRecipe", NaturaConfig.ENABLE_WHEAT_RECIPE::get,
            "enableHeatscarSpider", NaturaConfig.ENABLE_HEATSCAR_SPIDER::get);

    private ConfigOptionEnabledCondition() {}

    public static boolean test(String setting) {
        Supplier<Boolean> value = SETTINGS.get(setting);
        return value != null && value.get();
    }
}

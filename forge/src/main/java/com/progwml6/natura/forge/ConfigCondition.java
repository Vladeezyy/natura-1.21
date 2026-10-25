package com.progwml6.natura.forge;

import com.google.gson.JsonObject;
import com.progwml6.natura.Natura;
import com.progwml6.natura.common.recipe.ConfigOptionEnabledCondition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/** natura:config_option_enabled in Forge "conditions" (recipes) and "forge:conditions" (biome modifiers). */
public record ConfigCondition(String setting) implements ICondition {
    private static final ResourceLocation ID = new ResourceLocation(Natura.MODID, ConfigOptionEnabledCondition.ID);

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return ConfigOptionEnabledCondition.test(this.setting);
    }

    public enum Serializer implements IConditionSerializer<ConfigCondition> {
        INSTANCE;

        @Override
        public void write(JsonObject json, ConfigCondition value) {
            json.addProperty(ConfigOptionEnabledCondition.FIELD, value.setting());
        }

        @Override
        public ConfigCondition read(JsonObject json) {
            return new ConfigCondition(GsonHelper.getAsString(json, ConfigOptionEnabledCondition.FIELD));
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}

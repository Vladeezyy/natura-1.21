package com.progwml6.natura.common.item;

import net.minecraft.world.item.consume_effects.ConsumeEffect;

import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import net.minecraft.world.item.component.Consumable;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 1.12 ItemNaturaEdible (Mantle ItemEdible): the Mantle tooltip, then each effect's name, red for harmful ones and blue
 * for the others (1.12 displayCustomEffectsTooltip).
 */
public class NaturaFoodItem extends NaturaItem {
    public NaturaFoodItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        Consumable consumable = stack.get(net.minecraft.core.component.DataComponents.CONSUMABLE);
        if (consumable != null) {
            for (ConsumeEffect effect : consumable.onConsumeEffects()) {
                if (effect instanceof ApplyStatusEffectsConsumeEffect apply) {
                    for (var instance : apply.effects()) {
                        boolean bad = instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL;
                        tooltip.accept(Component.translatable(instance.getDescriptionId()).withStyle(bad ? ChatFormatting.RED : ChatFormatting.BLUE));
                    }
                }
            }
        }
    }
}

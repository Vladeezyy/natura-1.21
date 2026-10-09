package com.progwml6.natura.common.item;

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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        FoodProperties food = stack.getFoodProperties(null);
        if (food != null) {
            for (FoodProperties.PossibleEffect effect : food.effects()) {
                var instance = effect.effect();
                boolean bad = instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL;
                tooltip.add(Component.translatable(instance.getDescriptionId()).withStyle(bad ? ChatFormatting.RED : ChatFormatting.BLUE));
            }
        }
    }
}

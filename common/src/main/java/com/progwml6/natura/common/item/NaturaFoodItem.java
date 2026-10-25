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
    /** 1.12 itemUseDuration (1.21: FoodProperties eatSeconds; 1.20.1 has only normal or fast food). */
    private final int eatTicks;

    public NaturaFoodItem(Properties properties, int eatTicks) {
        super(properties);
        this.eatTicks = eatTicks;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.eatTicks;
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.level.Level context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        FoodProperties food = this.getFoodProperties();
        if (food != null) {
            for (var effect : food.getEffects()) {
                var instance = effect.getFirst();
                boolean bad = instance.getEffect().getCategory() == MobEffectCategory.HARMFUL;
                tooltip.add(Component.translatable(instance.getDescriptionId()).withStyle(bad ? ChatFormatting.RED : ChatFormatting.BLUE));
            }
        }
    }
}

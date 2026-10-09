package com.progwml6.natura.common;

import com.progwml6.natura.common.item.NaturaFoodItem;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

/** Items that are not blocks (1.12 NaturaCommons materials, edibles, ...). */
public final class NaturaItems {
    /**
     * 1.12 edibles meta 10. Registered without an eating time, so Mantle's map gave 0 and it was eaten at once (on the
     * next tick); 1.21 needs a positive time, one tick here.
     */
    public static final DeferredItem<Item> POTASH_APPLE = food("potash_apple", new FoodProperties.Builder().nutrition(4).saturationModifier(0.4F)
            .fast().effect(() -> new MobEffectInstance(MobEffects.POISON, 2 * 25, 0), 1.0F));

    private NaturaItems() {}

    private static DeferredItem<Item> food(String id, FoodProperties.Builder food) {
        FoodProperties properties = food.build();
        FoodProperties oneTick = new FoodProperties(properties.nutrition(), properties.saturation(), properties.canAlwaysEat(), 0.05F,
                properties.usingConvertsTo(), properties.effects());
        DeferredItem<Item> item = NaturaBlocks.ITEMS.register(id, () -> new NaturaFoodItem(new Item.Properties().food(oneTick)));
        NaturaBlocks.TAB_GENERAL.add(item);
        return item;
    }

    public static void init() {}
}

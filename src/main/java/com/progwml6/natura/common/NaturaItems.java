package com.progwml6.natura.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.progwml6.natura.common.item.NaturaBoneBagItem;
import com.progwml6.natura.common.item.NaturaFoodItem;
import com.progwml6.natura.common.item.NaturaItem;
import com.progwml6.natura.common.item.NaturaSeedBagItem;
import com.progwml6.natura.common.item.NaturaSeedItem;
import com.progwml6.natura.common.item.NaturaSoupItem;
import com.progwml6.natura.common.item.SaguaroFruitItem;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Items that are not blocks: the 1.12 NaturaCommons materials, edibles, soups, bowls, bags and the overworld seeds.
 * Food: 1.12 nutrition, saturation and eating time (Mantle used its own times: 16 ticks for berries, 32 for meat and
 * stews), every effect always applied, never "always edible".
 */
public final class NaturaItems {
    // materials (1.12 materials metas 0-8)
    public static final DeferredItem<Item> BARLEY = item("barley");
    public static final DeferredItem<Item> BARLEY_FLOUR = item("barley_flour");
    public static final DeferredItem<Item> WHEAT_FLOUR = item("wheat_flour");
    public static final DeferredItem<Item> COTTON = item("cotton");
    public static final DeferredItem<Item> SULFUR_POWDER = item("sulfur_powder");
    public static final DeferredItem<Item> GHOSTWOOD_FLETCHING = item("ghostwood_fletching");
    public static final DeferredItem<Item> IMP_LEATHER = item("imp_leather");
    public static final DeferredItem<Item> FLAME_STRING = item("flame_string");
    public static final DeferredItem<Item> BLUE_DYE = item("blue_dye");

    // empty bowls
    public static final Map<String, DeferredItem<Item>> BOWLS = new LinkedHashMap<>();

    static {
        for (String wood : new String[] {"ghostwood", "bloodwood", "darkwood", "fusewood"}) {
            BOWLS.put(wood, item(wood + "_bowl"));
        }
    }

    // edibles (1.12 edibles metas 0-11)
    public static final DeferredItem<Item> IMPMEAT_RAW = food("impmeat_raw", 3, 0.2F, 32,
            effect(MobEffects.HUNGER, 8 * 20), effect(MobEffects.POISON, 5 * 20));
    public static final DeferredItem<Item> IMPMEAT_COOKED = food("impmeat_cooked", 8, 0.6F, 32,
            effect(MobEffects.FIRE_RESISTANCE, 15 * 20), effect(MobEffects.POISON, 5 * 20));
    public static final Map<String, DeferredItem<Item>> BERRIES = new LinkedHashMap<>();

    static {
        for (String berry : new String[] {"raspberry", "blueberry", "blackberry", "maloberry"}) {
            BERRIES.put(berry, food(berry, 1, 0.4F, 16));
        }
        BERRIES.put("blightberry", food("blightberry", 1, 0.4F, 16, effect(MobEffects.REGENERATION, 8 * 20), effect(MobEffects.POISON, 5 * 20),
                effect(MobEffects.WITHER, 5 * 20)));
        BERRIES.put("duskberry", food("duskberry", 1, 0.4F, 16, effect(MobEffects.NIGHT_VISION, 15 * 20), effect(MobEffects.BLINDNESS, 3 * 20)));
        BERRIES.put("skyberry", food("skyberry", 1, 0.4F, 16, effect(MobEffects.JUMP, 8 * 20), effect(MobEffects.MOVEMENT_SLOWDOWN, 3 * 20)));
        BERRIES.put("stingberry", food("stingberry", 1, 0.4F, 16, effect(MobEffects.DAMAGE_BOOST, 10 * 20), effect(MobEffects.DIG_SLOWDOWN, 10 * 20)));
    }

    /**
     * 1.12 edibles meta 10. Registered without an eating time, so Mantle's map gave 0 and it was eaten at once (on the
     * next tick); 1.21 needs a positive time, one tick here.
     */
    public static final DeferredItem<Item> POTASH_APPLE = food("potash_apple", 4, 0.4F, 1, effect(MobEffects.POISON, 2 * 25));
    public static final DeferredItem<Item> CACTUS_JUICE = food("cactus_juice", 1, 0.1F, 12);

    // soups (1.12 soups metas 0-9)
    public static final Map<String, DeferredItem<Item>> SOUPS = new LinkedHashMap<>();

    static {
        for (String wood : new String[] {"ghostwood", "bloodwood", "darkwood", "fusewood"}) {
            SOUPS.put(wood + "_mushroom_stew", soup(wood + "_mushroom_stew", 6, 0.6F, BOWLS.get(wood)));
        }
        SOUPS.put("glowshroom_stew", soup("glowshroom_stew", 6, 0.6F, () -> Items.BOWL, glowshroom()));
        SOUPS.put("ghostwood_glowshroom_stew", soup("ghostwood_glowshroom_stew", 6, 0.6F, BOWLS.get("ghostwood"), glowshroom()));
        // 1.12: the other wooden bowls of glowshroom stew feed less (3, 0.2)
        for (String wood : new String[] {"bloodwood", "darkwood", "fusewood"}) {
            SOUPS.put(wood + "_glowshroom_stew", soup(wood + "_glowshroom_stew", 3, 0.2F, BOWLS.get(wood), glowshroom()));
        }
        SOUPS.put("berry_medley", soup("berry_medley", 5, 1.4F, () -> Items.BOWL));
    }

    // bags and seeds
    public static final DeferredItem<Item> WHEAT_SEED_BAG = bag("wheat_seed_bag", () -> Blocks.WHEAT);
    public static final DeferredItem<Item> CARROTS_SEED_BAG = bag("carrots_seed_bag", () -> Blocks.CARROTS);
    public static final DeferredItem<Item> POTATOES_SEED_BAG = bag("potatoes_seed_bag", () -> Blocks.POTATOES);
    public static final DeferredItem<Item> NETHER_WART_SEED_BAG = bag("nether_wart_seed_bag", () -> Blocks.NETHER_WART);
    public static final DeferredItem<Item> BONEMEAL_BAG = register("bonemeal_bag", NaturaBoneBagItem::new);
    public static final DeferredItem<Item> BARLEY_SEEDS = register("barley_seeds", p -> new NaturaSeedItem(() -> NaturaBlocks.BARLEY_CROP.get(), p));
    public static final DeferredItem<Item> COTTON_SEEDS = register("cotton_seeds", p -> new NaturaSeedItem(() -> NaturaBlocks.COTTON_CROP.get(), p));
    public static final DeferredItem<Item> BARLEY_SEED_BAG = bag("barley_seed_bag", () -> NaturaBlocks.BARLEY_CROP.get());
    public static final DeferredItem<Item> COTTON_SEED_BAG = bag("cotton_seed_bag", () -> NaturaBlocks.COTTON_CROP.get());
    public static final DeferredItem<Item> SAGUARO_FRUIT_ITEM = register("saguaro_fruit_item", p -> new SaguaroFruitItem(() -> NaturaBlocks.SAGUARO_BABY.get(),
            p.food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build())));

    private NaturaItems() {}

    public static void init() {}

    private static DeferredItem<Item> register(String id, Function<Item.Properties, Item> item) {
        DeferredItem<Item> holder = NaturaBlocks.ITEMS.register(id, () -> item.apply(new Item.Properties()));
        NaturaBlocks.TAB_GENERAL.add(holder);
        return holder;
    }

    private static DeferredItem<Item> item(String id) {
        return register(id, NaturaItem::new);
    }

    private static DeferredItem<Item> bag(String id, Supplier<? extends net.minecraft.world.level.block.Block> crop) {
        return register(id, p -> new NaturaSeedBagItem(crop, p));
    }

    private record Effect(Holder<MobEffect> effect, int duration) {}

    private static Effect effect(Holder<MobEffect> effect, int duration) {
        return new Effect(effect, duration);
    }

    private static Effect[] glowshroom() {
        return new Effect[] {effect(MobEffects.NIGHT_VISION, 45 * 25), effect(MobEffects.POISON, 16 * 25), effect(MobEffects.DIG_SLOWDOWN, 8 * 25)};
    }

    private static FoodProperties foodProperties(int nutrition, float saturation, int ticks, Effect... effects) {
        FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation);
        for (Effect e : effects) {
            builder.effect(() -> new MobEffectInstance(e.effect(), e.duration(), 0), 1.0F);
        }
        FoodProperties food = builder.build();
        return new FoodProperties(food.nutrition(), food.saturation(), false, ticks / 20.0F, food.usingConvertsTo(), food.effects());
    }

    private static DeferredItem<Item> food(String id, int nutrition, float saturation, int ticks, Effect... effects) {
        return register(id, p -> new NaturaFoodItem(p.food(foodProperties(nutrition, saturation, ticks, effects))));
    }

    private static DeferredItem<Item> soup(String id, int nutrition, float saturation, Supplier<? extends ItemLike> bowl, Effect... effects) {
        return register(id, p -> new NaturaSoupItem(bowl, p.food(foodProperties(nutrition, saturation, 32, effects))));
    }
}

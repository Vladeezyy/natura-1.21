package com.progwml6.natura.world;

import com.progwml6.natura.common.NaturaItems;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 1.12 WorldEvents: cows and sheep follow a player holding any 1.12 "materials" item (barley, flour, cotton, sulfur,
 * ...), chickens one holding barley or cotton seeds, slowly (speed 0.25). The 1.12 barley breeding never worked (it
 * compared the held stack with a fixed stack by identity), so barley does not breed them here either.
 */
public final class WorldEvents {
    /** Made once: an Ingredient per joining cow, sheep or chicken was a new object on every chunk load. */
    private static final com.google.common.base.Supplier<Ingredient> MATERIALS = com.google.common.base.Suppliers.memoize(() -> Ingredient.of(
            NaturaItems.BARLEY.get(), NaturaItems.BARLEY_FLOUR.get(), NaturaItems.WHEAT_FLOUR.get(), NaturaItems.COTTON.get(),
            NaturaItems.SULFUR_POWDER.get(), NaturaItems.GHOSTWOOD_FLETCHING.get(), NaturaItems.IMP_LEATHER.get(), NaturaItems.FLAME_STRING.get(),
            NaturaItems.BLUE_DYE.get()));
    private static final com.google.common.base.Supplier<Ingredient> SEEDS = com.google.common.base.Suppliers.memoize(() -> Ingredient.of(
            NaturaItems.BARLEY_SEEDS.get(), NaturaItems.COTTON_SEEDS.get()));

    private WorldEvents() {}

    /** An entity joined a server level (NeoForge EntityJoinLevelEvent, Fabric ServerEntityEvents.ENTITY_LOAD). */
    public static void onJoin(Entity entity) {
        if (entity instanceof Cow || entity instanceof Sheep) {
            var animal = (net.minecraft.world.entity.animal.Animal) entity;
            animal.goalSelector.addGoal(3, new TemptGoal(animal, 0.25D, MATERIALS.get(), false));
        }
        if (entity instanceof Chicken chicken) {
            chicken.goalSelector.addGoal(3, new TemptGoal(chicken, 0.25D, SEEDS.get(), false));
        }
    }
}

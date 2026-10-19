package com.progwml6.natura.fabric;

import com.progwml6.natura.gametest.NaturaGameTests;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * The common game tests (gametest/NaturaGameTests) for Fabric's game test API, which finds tests by this annotation;
 * the trees run in environments of their own (data/natura/test_environment in the Fabric resources), so each is a batch
 * of its own like on 1.21.1.
 */
public class FabricGameTests {
    private static final String AREA = "natura:gametest_area";

    @GameTest(structure = AREA, maxTicks = 100) public void every_block_has_loot(GameTestHelper h) { NaturaGameTests.run("every_block_has_loot", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void strength(GameTestHelper h) { NaturaGameTests.run("strength", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void flammability(GameTestHelper h) { NaturaGameTests.run("flammability", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void leaves_sapling_chance(GameTestHelper h) { NaturaGameTests.run("leaves_sapling_chance", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void bookshelf_drops_books(GameTestHelper h) { NaturaGameTests.run("bookshelf_drops_books", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void redwood_leaves_decay(GameTestHelper h) { NaturaGameTests.run("redwood_leaves_decay", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void fusewood_explodes(GameTestHelper h) { NaturaGameTests.run("fusewood_explodes", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void recipes(GameTestHelper h) { NaturaGameTests.run("recipes", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void workbench_menu(GameTestHelper h) { NaturaGameTests.run("workbench_menu", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void crops(GameTestHelper h) { NaturaGameTests.run("crops", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void grown_crops_do_not_tick(GameTestHelper h) { NaturaGameTests.run("grown_crops_do_not_tick", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void berry_bushes(GameTestHelper h) { NaturaGameTests.run("berry_bushes", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void saguaro(GameTestHelper h) { NaturaGameTests.run("saguaro", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void food(GameTestHelper h) { NaturaGameTests.run("food", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void seed_bag(GameTestHelper h) { NaturaGameTests.run("seed_bag", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void plants_on_colored_grass(GameTestHelper h) { NaturaGameTests.run("plants_on_colored_grass", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void tempt(GameTestHelper h) { NaturaGameTests.run("tempt", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void heat_sand_and_glass(GameTestHelper h) { NaturaGameTests.run("heat_sand_and_glass", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void glowshrooms(GameTestHelper h) { NaturaGameTests.run("glowshrooms", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void respawn_obelisk(GameTestHelper h) { NaturaGameTests.run("respawn_obelisk", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void netherrack_furnace(GameTestHelper h) { NaturaGameTests.run("netherrack_furnace", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void blaze_rails_and_hopper(GameTestHelper h) { NaturaGameTests.run("blaze_rails_and_hopper", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void blaze_rail_carts(GameTestHelper h) { NaturaGameTests.run("blaze_rail_carts", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void clouds(GameTestHelper h) { NaturaGameTests.run("clouds", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void tools(GameTestHelper h) { NaturaGameTests.run("tools", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void kama(GameTestHelper h) { NaturaGameTests.run("kama", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void flint_and_blaze(GameTestHelper h) { NaturaGameTests.run("flint_and_blaze", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void imp_armor(GameTestHelper h) { NaturaGameTests.run("imp_armor", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void mob_stats(GameTestHelper h) { NaturaGameTests.run("mob_stats", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void heatscar_babies(GameTestHelper h) { NaturaGameTests.run("heatscar_babies", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void nitro_creeper(GameTestHelper h) { NaturaGameTests.run("nitro_creeper", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void imp_breeding(GameTestHelper h) { NaturaGameTests.run("imp_breeding", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void mob_spawns(GameTestHelper h) { NaturaGameTests.run("mob_spawns", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void worldgen_features(GameTestHelper h) { NaturaGameTests.run("worldgen_features", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void worldgen_veins(GameTestHelper h) { NaturaGameTests.run("worldgen_veins", h); }
    @GameTest(structure = AREA, maxTicks = 100) public void worldgen_berry_bushes(GameTestHelper h) { NaturaGameTests.run("worldgen_berry_bushes", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_maple", skyAccess = true) public void tree_maple(GameTestHelper h) { NaturaGameTests.run("tree_maple", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_silverbell", skyAccess = true) public void tree_silverbell(GameTestHelper h) { NaturaGameTests.run("tree_silverbell", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_amaranth", skyAccess = true) public void tree_amaranth(GameTestHelper h) { NaturaGameTests.run("tree_amaranth", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_tiger", skyAccess = true) public void tree_tiger(GameTestHelper h) { NaturaGameTests.run("tree_tiger", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_willow", skyAccess = true) public void tree_willow(GameTestHelper h) { NaturaGameTests.run("tree_willow", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_eucalyptus", skyAccess = true) public void tree_eucalyptus(GameTestHelper h) { NaturaGameTests.run("tree_eucalyptus", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_hopseed", skyAccess = true) public void tree_hopseed(GameTestHelper h) { NaturaGameTests.run("tree_hopseed", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_sakura", skyAccess = true) public void tree_sakura(GameTestHelper h) { NaturaGameTests.run("tree_sakura", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_redwood", skyAccess = true) public void tree_redwood(GameTestHelper h) { NaturaGameTests.run("tree_redwood", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_ghostwood", skyAccess = true) public void tree_ghostwood(GameTestHelper h) { NaturaGameTests.run("tree_ghostwood", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_fusewood", skyAccess = true) public void tree_fusewood(GameTestHelper h) { NaturaGameTests.run("tree_fusewood", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_darkwood", skyAccess = true) public void tree_darkwood(GameTestHelper h) { NaturaGameTests.run("tree_darkwood", h); }
    @GameTest(structure = AREA, maxTicks = 100, environment = "natura:tree_bloodwood", skyAccess = true) public void tree_bloodwood(GameTestHelper h) { NaturaGameTests.run("tree_bloodwood", h); }
}

package com.progwml6.natura.gametest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.common.block.RedwoodLeavesBlock;
import com.progwml6.natura.library.NaturaWood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * In-game checks of the port against the 1.12 behaviour (numbers from the 1.12 source), run headless with
 * {@code ./gradlew runGameTestServer}.
 */
public final class NaturaGameTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = new LinkedHashMap<>();
    private static final String STRUCTURE = Natura.MODID + ":gametest_area";

    static {
        TESTS.put("every_block_has_loot", NaturaGameTests::everyBlockHasLoot);
        TESTS.put("strength", NaturaGameTests::strength);
        TESTS.put("flammability", NaturaGameTests::flammability);
        TESTS.put("leaves_sapling_chance", NaturaGameTests::leavesSaplingChance);
        TESTS.put("bookshelf_drops_books", NaturaGameTests::bookshelfDrops);
        TESTS.put("redwood_leaves_decay", NaturaGameTests::redwoodLeavesDecay);
        TESTS.put("fusewood_explodes", NaturaGameTests::fusewoodExplodes);
        TESTS.put("recipes", NaturaGameTests::recipes);
        TESTS.put("workbench_menu", NaturaGameTests::workbenchMenu);
        TESTS.put("crops", NaturaGameTests::crops);
        TESTS.put("berry_bushes", NaturaGameTests::berryBushes);
        TESTS.put("saguaro", NaturaGameTests::saguaro);
        TESTS.put("food", NaturaGameTests::food);
        TESTS.put("seed_bag", NaturaGameTests::seedBag);
        TESTS.put("plants_on_colored_grass", NaturaGameTests::coloredGrass);
        TESTS.put("tempt", NaturaGameTests::tempt);
        TESTS.put("heat_sand_and_glass", NaturaGameTests::heatSandAndGlass);
        TESTS.put("glowshrooms", NaturaGameTests::glowshrooms);
        TESTS.put("respawn_obelisk", NaturaGameTests::obelisk);
        TESTS.put("netherrack_furnace", NaturaGameTests::furnace);
        TESTS.put("blaze_rails_and_hopper", NaturaGameTests::railsAndHopper);
        TESTS.put("clouds", NaturaGameTests::clouds);
    }

    private NaturaGameTests() {}

    @GameTestGenerator
    public static Collection<TestFunction> generate() {
        List<TestFunction> functions = new ArrayList<>();
        TESTS.forEach((name, body) -> functions.add(new TestFunction(Natura.MODID, Natura.MODID + "." + name, STRUCTURE, Rotation.NONE, 100, 0,
                true, false, 1, 1, false, body)));
        // every tree in a batch of its own: the bigger ones reach into the neighbouring test areas
        for (String tree : new String[] {"maple", "silverbell", "amaranth", "tiger", "willow", "eucalyptus", "hopseed", "sakura", "redwood",
                "ghostwood", "fusewood", "darkwood", "bloodwood"}) {
            functions.add(new TestFunction(Natura.MODID + "_tree_" + tree, Natura.MODID + ".tree_" + tree, STRUCTURE, Rotation.NONE, 100, 0,
                    true, false, 1, 1, true, helper -> saplingGrows(helper, tree)));
        }
        return functions;
    }

    // --- helpers --------------------------------------------------------------------------------------------------

    /** A survival mock player that can open menus. */
    static ServerPlayer player(GameTestHelper helper) {
        var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
                new com.mojang.authlib.GameProfile(UUID.randomUUID(), "test-player"), false);
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(connection);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        Vec3 pos = helper.absolutePos(new BlockPos(2, 1, 2)).getBottomCenter();
        player.moveTo(pos.x, pos.y, pos.z, 0, 0);
        return player;
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Natura.MODID, id));
    }

    private static List<ItemStack> drops(GameTestHelper helper, BlockState state, ItemStack tool) {
        LootParams.Builder params = new LootParams.Builder(helper.getLevel()).withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.TOOL, tool);
        return state.getDrops(params);
    }

    private static ItemStack enchanted(GameTestHelper helper, ItemStack stack, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key, int level) {
        var holder = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        stack.enchant(holder, level);
        return stack;
    }

    // --- tests ----------------------------------------------------------------------------------------------------

    private static void everyBlockHasLoot(GameTestHelper helper) {
        List<String> missing = new ArrayList<>();
        for (var holder : NaturaBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(block.getLootTable());
            if (table == LootTable.EMPTY) {
                missing.add(holder.getId().getPath());
            }
        }
        helper.assertTrue(missing.isEmpty(), "no loot table: " + missing);
        helper.succeed();
    }

    /** 1.12 effective numbers: hardness / explosion resistance. */
    private static void strength(GameTestHelper helper) {
        Object[][] expected = {
                {"maple_planks", 2.0F, 2.0F}, {"maple_slab", 2.0F, 2.0F}, {"maple_stairs", 2.0F, 2.0F}, {"maple_log", 1.5F, 3.0F},
                {"fusewood_log", 3.5F, 24.0F}, {"bloodwood_log", 8.0F, 8.0F}, {"redwood_heart", 2.0F, 2.0F}, {"maple_fence", 2.0F, 3.0F},
                {"maple_fence_gate", 2.0F, 3.0F}, {"maple_door", 3.0F, 3.0F}, {"maple_trapdoor", 3.0F, 3.0F}, {"maple_button", 0.5F, 0.5F},
                {"maple_pressure_plate", 0.5F, 0.5F}, {"maple_bookshelf", 1.5F, 1.5F}, {"maple_workbench", 2.5F, 2.5F}, {"maple_leaves", 0.2F, 0.2F}};
        for (Object[] e : expected) {
            Block block = block((String) e[0]);
            float hardness = block.defaultBlockState().getDestroySpeed(helper.getLevel(), BlockPos.ZERO);
            helper.assertTrue(hardness == (float) e[1] && block.getExplosionResistance() == (float) e[2],
                    e[0] + ": " + hardness + " / " + block.getExplosionResistance());
        }
        helper.assertTrue(block("bloodwood_log").defaultBlockState().getSoundType() == net.minecraft.world.level.block.SoundType.METAL, "bloodwood sounds");
        helper.succeed();
    }

    private static void flammability(GameTestHelper helper) {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        Object[][] expected = {{"maple_planks", 20}, {"maple_log", 20}, {"redwood_bark", 20}, {"maple_slab", 20}, {"ghostwood_slab", 20},
                {"maple_leaves", 60}, {"redwood_leaves", 20}, {"ghostwood_planks", 0}, {"ghostwood_log", 0}, {"bloodwood_leaves", 0},
                {"maple_stairs", 0}, {"maple_fence", 0}, {"maple_bookshelf", 0}, {"maple_door", 0}};
        for (Object[] e : expected) {
            int flammability = fire.getBurnOdds(block((String) e[0]).defaultBlockState());
            helper.assertTrue(flammability == (int) e[1], e[0] + " burns " + flammability);
        }
        helper.succeed();
    }

    /** 1 in 25 without fortune (maple), 1 in 20 for darkwood; fruit leaves drop potash apples instead; never apples or sticks. */
    private static void leavesSaplingChance(GameTestHelper helper) {
        int saplings = 0, other = 0;
        BlockState maple = block("maple_leaves").defaultBlockState();
        for (int i = 0; i < 10000; i++) {
            for (ItemStack stack : drops(helper, maple, ItemStack.EMPTY)) {
                if (stack.is(block("maple_sapling").asItem())) saplings++;
                else other++;
            }
        }
        helper.assertTrue(saplings > 320 && saplings < 480 && other == 0, "maple leaves: " + saplings + " saplings, " + other + " other");
        List<ItemStack> sheared = drops(helper, maple, new ItemStack(Items.SHEARS));
        helper.assertTrue(sheared.size() == 1 && sheared.get(0).is(maple.getBlock().asItem()), "shears give the leaves");
        int dark = 0;
        for (int i = 0; i < 10000; i++) {
            dark += drops(helper, block("darkwood_leaves").defaultBlockState(), ItemStack.EMPTY).size();
        }
        helper.assertTrue(dark > 420 && dark < 580, "darkwood leaves: " + dark + " saplings");
        helper.succeed();
    }

    private static void bookshelfDrops(GameTestHelper helper) {
        BlockState shelf = NaturaBlocks.WOODS.get(NaturaWood.MAPLE).bookshelf().get().defaultBlockState();
        for (ItemStack tool : new ItemStack[] {ItemStack.EMPTY, enchanted(helper, new ItemStack(Items.DIAMOND_AXE), Enchantments.SILK_TOUCH, 1)}) {
            List<ItemStack> drops = drops(helper, shelf, tool);
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(Items.BOOK) && drops.get(0).getCount() == 3, "bookshelf drops " + drops);
        }
        helper.assertTrue(shelf.is(net.minecraft.tags.BlockTags.ENCHANTMENT_POWER_PROVIDER), "bookshelf powers enchanting");
        helper.succeed();
    }

    /** A redwood leaf checks once a log nearby broke, and decays without a log in the 9x9x9 cube. */
    private static void redwoodLeavesDecay(GameTestHelper helper) {
        BlockPos leafPos = new BlockPos(3, 2, 3);
        BlockPos barkPos = new BlockPos(3, 1, 3);
        BlockState leaf = block("redwood_leaves").defaultBlockState();
        helper.setBlock(barkPos, block("redwood_bark"));
        helper.setBlock(new BlockPos(6, 1, 6), block("redwood_heart"));
        helper.setBlock(leafPos, leaf);
        helper.assertTrue(!helper.getBlockState(leafPos).getValue(RedwoodLeavesBlock.CHECK_DECAY), "no check before a break");
        helper.setBlock(barkPos, Blocks.AIR);
        BlockState after = helper.getBlockState(leafPos);
        helper.assertTrue(after.getValue(RedwoodLeavesBlock.CHECK_DECAY) && after.isRandomlyTicking(), "a broken log starts the check");
        // the heart 3 blocks away keeps it
        after.randomTick(helper.getLevel(), helper.absolutePos(leafPos), helper.getLevel().random);
        helper.assertTrue(helper.getBlockState(leafPos).is(leaf.getBlock()) && !helper.getBlockState(leafPos).getValue(RedwoodLeavesBlock.CHECK_DECAY),
                "a log in the cube keeps the leaf");
        helper.setBlock(new BlockPos(6, 1, 6), Blocks.AIR);
        BlockState checking = helper.getBlockState(leafPos);
        checking.randomTick(helper.getLevel(), helper.absolutePos(leafPos), helper.getLevel().random);
        helper.assertTrue(helper.getBlockState(leafPos).isAir(), "no log left: the leaf decays");
        BlockState persistent = leaf.setValue(BlockStateProperties.PERSISTENT, true).setValue(RedwoodLeavesBlock.CHECK_DECAY, true);
        helper.assertTrue(!persistent.isRandomlyTicking(), "placed leaves never decay");
        helper.succeed();
    }

    /** Harvesting fusewood: an explosion that hurts and breaks no blocks (Normal difficulty 1.75). */
    private static void fusewoodExplodes(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        Difficulty before = server.getWorldData().getDifficulty();
        server.setDifficulty(Difficulty.NORMAL, true);
        ServerPlayer player = player(helper);
        BlockPos log = new BlockPos(2, 1, 3);
        helper.setBlock(log, block("fusewood_log"));
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.DIRT);
        // a pig next to it takes the damage (the just joined test player is still spawn-invulnerable)
        var pig = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(2, 1, 4));
        float health = pig.getHealth();
        player.gameMode.destroyBlock(helper.absolutePos(log));
        server.setDifficulty(before, true);
        helper.assertTrue(helper.getBlockState(log).isAir(), "the log is harvested");
        helper.assertTrue(helper.getBlockState(new BlockPos(3, 1, 3)).is(Blocks.DIRT), "the explosion breaks no blocks");
        helper.assertTrue(pig.getHealth() < health, "the explosion hurts: " + health + " -> " + pig.getHealth());
        player.discard();
        helper.succeed();
    }

    private static ItemStack craft(GameTestHelper helper, int width, int height, ItemStack... grid) {
        CraftingInput input = CraftingInput.of(width, height, List.of(grid));
        var level = helper.getLevel();
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).map(r -> r.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    private static void recipes(GameTestHelper helper) {
        ItemStack log = new ItemStack(block("maple_log"));
        ItemStack planks = new ItemStack(block("maple_planks"));
        ItemStack out = craft(helper, 1, 1, log);
        helper.assertTrue(out.is(planks.getItem()) && out.getCount() == 4, "log -> 4 planks: " + out);
        out = craft(helper, 2, 2, planks, ItemStack.EMPTY, ItemStack.EMPTY, planks);
        helper.assertTrue(out.is(NaturaBlocks.WOODS.get(NaturaWood.MAPLE).stick().get()) && out.getCount() == 4, "diagonal planks -> 4 maple sticks: " + out);
        out = craft(helper, 2, 3, planks, planks, planks, planks, planks, planks);
        helper.assertTrue(out.is(block("maple_door").asItem()) && out.getCount() == 3, "6 planks -> 3 doors: " + out);
        var level = helper.getLevel();
        var smelt = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(new ItemStack(block("redwood_heart"))), level);
        helper.assertTrue(smelt.isPresent() && smelt.get().value().getResultItem(level.registryAccess()).is(Items.CHARCOAL), "redwood heart -> charcoal");
        // vanilla recipes take the Natura wood through the tags
        out = craft(helper, 1, 2, planks, planks);
        helper.assertTrue(out.is(Items.STICK), "two planks -> vanilla sticks: " + out);
        helper.succeed();
    }

    /**
     * The sapling grows its 1.12 tree: the log where the sapling stood (bloodwood: hanging from the netherrack above),
     * leaves with a vanilla distance below 7 so they stay. The redwood needs 40 redwood saplings in the 7x7 square.
     */
    private static void saplingGrows(GameTestHelper helper, String tree) {
        BlockPos pos = new BlockPos(4, 2, 4);
        boolean nether = tree.equals("ghostwood") || tree.equals("fusewood") || tree.equals("darkwood") || tree.equals("bloodwood");
        BlockState soil = nether ? Blocks.NETHERRACK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
        var sapling = (com.progwml6.natura.common.block.NaturaSaplingBlock) block(tree + "_sapling");
        BlockState grown = sapling.defaultBlockState().setValue(net.minecraft.world.level.block.SaplingBlock.STAGE, 1);
        if (tree.equals("bloodwood")) {
            helper.setBlock(pos.above(), soil);
        } else if (tree.equals("redwood")) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    helper.setBlock(pos.offset(x, -1, z), soil);
                    helper.setBlock(pos.offset(x, 0, z), grown);
                }
            }
            // 39 saplings are not enough
            helper.setBlock(pos.offset(3, 0, 3), Blocks.AIR);
            for (int i = 0; i < 9; i++) helper.setBlock(pos.offset(-3 + i % 7, 0, i < 7 ? -3 : -2), Blocks.AIR);
            sapling.advanceTree(helper.getLevel(), helper.absolutePos(pos), grown, helper.getLevel().random);
            helper.assertTrue(helper.getBlockState(pos).is(sapling) && helper.getBlockState(pos.offset(3, 0, 2)).is(sapling), "39 redwood saplings stay saplings");
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    helper.setBlock(pos.offset(x, 0, z), grown);
                }
            }
        } else {
            helper.setBlock(pos.below(), soil);
        }
        helper.setBlock(pos, grown);
        BlockState here = helper.getBlockState(pos);
        sapling.advanceTree(helper.getLevel(), helper.absolutePos(pos), here, helper.getLevel().random);
        String log = switch (tree) {
            case "redwood" -> "redwood_heart";
            case "bloodwood" -> "bloodwood_log";
            default -> tree + "_log";
        };
        helper.assertTrue(helper.getBlockState(pos).is(block(log)), tree + ": " + log + " where the sapling was, found " + helper.getBlockState(pos));
        int leaves = 0, decaying = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-16, -20, -16), pos.offset(16, 40, 16))) {
            BlockState state = helper.getBlockState(p);
            if (state.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock && !state.getValue(BlockStateProperties.PERSISTENT)) {
                leaves++;
                if (state.getValue(net.minecraft.world.level.block.LeavesBlock.DISTANCE) >= 7 && !tree.equals("redwood")) decaying++;
            }
        }
        helper.assertTrue(leaves > 0, tree + ": no leaves");
        helper.assertTrue(decaying * 10 <= leaves, tree + ": " + decaying + " of " + leaves + " leaves would decay");
        helper.succeed();
    }

    /** Barley: planted on farmland, bone meal adds 2-5 stages, picked when ripe (back to stage 1, one barley). */
    private static void crops(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos.below(), Blocks.FARMLAND);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(com.progwml6.natura.common.NaturaItems.BARLEY_SEEDS.get(), 2));
        var hit = new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos.below())), net.minecraft.core.Direction.UP,
                helper.absolutePos(pos.below()), false);
        player.getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
        var crop = (com.progwml6.natura.common.block.NaturaCropBlock) block("barley_crop");
        helper.assertTrue(helper.getBlockState(pos).is(crop) && player.getMainHandItem().getCount() == 1, "barley seeds plant barley on farmland");
        crop.performBonemeal(helper.getLevel(), helper.getLevel().random, helper.absolutePos(pos), helper.getBlockState(pos));
        int age = helper.getBlockState(pos).getValue(crop.getAgeProperty());
        helper.assertTrue(age >= 2, "bone meal grows at least two stages: " + age);
        helper.setBlock(pos, crop.defaultBlockState().setValue(crop.getAgeProperty(), 3));
        List<ItemStack> ripe = drops(helper, helper.getBlockState(pos), ItemStack.EMPTY);
        helper.assertTrue(ripe.get(0).is(com.progwml6.natura.common.NaturaItems.BARLEY.get()), "a ripe crop drops barley: " + ripe);
        helper.getBlockState(pos).useWithoutItem(helper.getLevel(), player, new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)),
                net.minecraft.core.Direction.UP, helper.absolutePos(pos), false));
        helper.assertTrue(helper.getBlockState(pos).getValue(crop.getAgeProperty()) == 1, "picked barley goes back to stage 1");
        helper.assertTrue(player.getInventory().countItem(com.progwml6.natura.common.NaturaItems.BARLEY.get()) == 1, "the picked barley is in the inventory");
        player.discard();
        helper.succeed();
    }

    private static void berryBushes(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        BlockPos pos = new BlockPos(2, 1, 3);
        var bush = (com.progwml6.natura.common.block.NaturaBerryBushBlock) block("raspberry_bush");
        helper.setBlock(pos, bush.defaultBlockState().setValue(com.progwml6.natura.common.block.NaturaBerryBushBlock.AGE, 2));
        for (int i = 0; i < 20; i++) {
            bush.performBonemeal(helper.getLevel(), helper.getLevel().random, helper.absolutePos(pos), helper.getBlockState(pos));
        }
        helper.assertTrue(helper.getBlockState(pos).getValue(com.progwml6.natura.common.block.NaturaBerryBushBlock.AGE) == 2, "bone meal never ripens a bush");
        helper.assertTrue(helper.getBlockState(pos.above()).is(bush), "bone meal on a grown bush starts one above");
        helper.setBlock(pos, bush.defaultBlockState().setValue(com.progwml6.natura.common.block.NaturaBerryBushBlock.AGE, 3));
        helper.getBlockState(pos).useWithoutItem(helper.getLevel(), player, new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)),
                net.minecraft.core.Direction.UP, helper.absolutePos(pos), false));
        helper.assertTrue(helper.getBlockState(pos).getValue(com.progwml6.natura.common.block.NaturaBerryBushBlock.AGE) == 2
                && player.getInventory().countItem(com.progwml6.natura.common.NaturaItems.BERRIES.get("raspberry").get()) == 1, "picking a ripe bush gives a raspberry");
        player.discard();
        helper.succeed();
    }

    /** A baby saguaro on sand grows into a connected saguaro; it hurts what touches it. */
    private static void saguaro(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 2, 4);
        for (BlockPos p : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(7, 1, 7))) {
            helper.setBlock(p, Blocks.SAND);
        }
        helper.setBlock(pos, block("saguaro_baby"));
        var gen = new com.progwml6.natura.world.tree.SaguaroGenerator(block("saguaro").defaultBlockState(), true, true);
        gen.generateSaguaro(new java.util.Random(1), new com.progwml6.natura.world.tree.TreeWorld(helper.getLevel()), helper.absolutePos(pos));
        BlockState trunk = helper.getBlockState(pos);
        helper.assertTrue(trunk.is(block("saguaro")), "the baby became a saguaro: " + trunk);
        helper.assertTrue(trunk.getValue(net.minecraft.world.level.block.PipeBlock.UP) && trunk.getValue(net.minecraft.world.level.block.PipeBlock.DOWN),
                "the trunk connects up and to the sand");
        helper.setBlock(pos.offset(3, 2, 0), Blocks.AIR);
        helper.setBlock(new BlockPos(1, 2, 1), block("saguaro_baby"));
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.GLASS);
        helper.assertTrue(helper.getBlockState(new BlockPos(1, 2, 1)).isAir(), "a baby saguaro without sand breaks");
        helper.succeed();
    }

    /** 1.12 food numbers; soups give their bowl back. */
    private static void food(GameTestHelper helper) {
        var items = com.progwml6.natura.common.NaturaItems.class;
        Object[][] expected = {{"raspberry", 1, 16}, {"impmeat_cooked", 8, 32}, {"potash_apple", 4, 1}, {"cactus_juice", 1, 12}, {"berry_medley", 5, 32},
                {"bloodwood_glowshroom_stew", 3, 32}, {"ghostwood_glowshroom_stew", 6, 32}};
        for (Object[] e : expected) {
            var item = BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Natura.MODID, (String) e[0]));
            var food = new ItemStack(item).getFoodProperties(null);
            helper.assertTrue(food != null && food.nutrition() == (int) e[1] && food.eatDurationTicks() == (int) e[2], e[0] + ": " + food);
        }
        ServerPlayer player = player(helper);
        player.getFoodData().setFoodLevel(10);
        ItemStack stew = new ItemStack(com.progwml6.natura.common.NaturaItems.SOUPS.get("ghostwood_mushroom_stew").get());
        helper.assertTrue(stew.getMaxStackSize() == 1, "stews stack to one");
        stew.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(player.getInventory().countItem(com.progwml6.natura.common.NaturaItems.BOWLS.get("ghostwood").get()) == 1, "the ghostwood bowl comes back");
        new ItemStack(com.progwml6.natura.common.NaturaItems.BERRIES.get("duskberry").get()).finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION) && player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS),
                "a duskberry gives night vision and blindness");
        player.discard();
        helper.succeed();
    }

    private static void seedBag(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        for (BlockPos p : BlockPos.betweenClosed(new BlockPos(1, 1, 1), new BlockPos(3, 1, 3))) {
            helper.setBlock(p, Blocks.FARMLAND);
        }
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.DIRT);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(com.progwml6.natura.common.NaturaItems.COTTON_SEED_BAG.get(), 2));
        BlockPos centre = helper.absolutePos(new BlockPos(2, 1, 2));
        player.getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(centre), net.minecraft.core.Direction.UP, centre, false)));
        int planted = 0;
        for (BlockPos p : BlockPos.betweenClosed(new BlockPos(1, 2, 1), new BlockPos(3, 2, 3))) {
            if (helper.getBlockState(p).is(block("cotton_crop"))) planted++;
        }
        helper.assertTrue(planted == 8 && player.getMainHandItem().getCount() == 1, "the bag plants the 8 farmland blocks and is used once: " + planted);
        player.discard();
        helper.succeed();
    }

    private static void coloredGrass(GameTestHelper helper) {
        for (String grass : new String[] {"topiary_grass", "bluegrass", "autumnal_grass"}) {
            helper.setBlock(new BlockPos(2, 1, 2), block(grass));
            helper.assertTrue(block("bluebells").defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2)))
                    && block("maple_sapling").defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2))),
                    "plains plants grow on " + grass);
        }
        helper.succeed();
    }

    private static void tempt(GameTestHelper helper) {
        var cow = helper.spawn(net.minecraft.world.entity.EntityType.COW, new BlockPos(2, 1, 2));
        var chicken = helper.spawn(net.minecraft.world.entity.EntityType.CHICKEN, new BlockPos(4, 1, 4));
        boolean cowTempted = cow.goalSelector.getAvailableGoals().stream().anyMatch(g -> g.getGoal() instanceof net.minecraft.world.entity.ai.goal.TemptGoal t
                && t.toString() != null);
        helper.assertTrue(cowTempted && chicken.goalSelector.getAvailableGoals().stream().filter(g -> g.getGoal() instanceof net.minecraft.world.entity.ai.goal.TemptGoal).count() >= 2,
                "cows and chickens get the Natura tempt goals");
        helper.succeed();
    }

    private static void heatSandAndGlass(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 1, 2), block("heat_sand"));
        var pig = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(2, 2, 2));
        float health = pig.getHealth();
        helper.getBlockState(new BlockPos(2, 1, 2)).entityInside(helper.getLevel(), helper.absolutePos(new BlockPos(2, 1, 2)), pig);
        helper.assertTrue(pig.getHealth() < health, "heat sand burns a pig");
        BlockState soul = block("soul_glass").defaultBlockState();
        helper.assertTrue(soul.getCollisionShape(helper.getLevel(), BlockPos.ZERO).isEmpty(), "soul glass has no collision");
        soul.entityInside(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 4)), pig);
        helper.assertTrue(pig.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN), "soul glass slows");
        helper.assertTrue(drops(helper, soul, ItemStack.EMPTY).isEmpty()
                && drops(helper, soul, enchanted(helper, new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.SILK_TOUCH, 1)).size() == 1, "glass only with silk touch");
        helper.assertTrue(block("heat_glass").getExplosionResistance() == 1800.0F, "nether glass resists 1800");
        helper.succeed();
    }

    private static void glowshrooms(GameTestHelper helper) {
        BlockState large = block("green_large_glowshroom").defaultBlockState();
        int[] counts = new int[3];
        for (int i = 0; i < 3000; i++) {
            List<ItemStack> d = drops(helper, large, ItemStack.EMPTY);
            int n = d.isEmpty() ? 0 : d.get(0).getCount();
            helper.assertTrue(n <= 2 && (d.isEmpty() || d.get(0).is(block("green_glowshroom").asItem())), "a huge glowshroom drops 0-2 small ones");
            counts[n]++;
        }
        helper.assertTrue(counts[1] > 200 && counts[1] < 400 && counts[2] > 200 && counts[2] < 400, "1 in 10 one, 1 in 10 two: " + java.util.Arrays.toString(counts));
        helper.assertTrue(large.getLightEmission() == 9 && large.getDestroySpeed(helper.getLevel(), BlockPos.ZERO) == 0.0F, "glows 9, breaks at once");
        BlockPos pos = new BlockPos(4, 2, 4);
        helper.setBlock(pos.below(), Blocks.NETHERRACK);
        var small = (com.progwml6.natura.common.block.NaturaGlowshroomBlock) block("blue_glowshroom");
        helper.setBlock(pos, small);
        small.performBonemeal(helper.getLevel(), helper.getLevel().random, helper.absolutePos(pos), helper.getBlockState(pos));
        helper.assertTrue(helper.getBlockState(pos).is(block("blue_large_glowshroom"))
                && helper.getBlockState(pos).getValue(com.progwml6.natura.common.block.LargeGlowshroomBlock.VARIANT) == com.progwml6.natura.common.block.LargeGlowshroomBlock.Variant.STEM,
                "bone meal grows a huge blue glowshroom: " + helper.getBlockState(pos));
        helper.succeed();
    }

    private static void obelisk(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        BlockPos pos = new BlockPos(2, 1, 3);
        helper.setBlock(pos, block("respawn_obelisk"));
        helper.getBlockState(pos).useWithoutItem(helper.getLevel(), player, new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)),
                net.minecraft.core.Direction.UP, helper.absolutePos(pos), false));
        BlockState active = helper.getBlockState(pos);
        helper.assertTrue(active.getValue(com.progwml6.natura.common.block.RespawnObeliskBlock.ACTIVE) && active.getLightEmission() == 7, "the obelisk lights up");
        helper.assertTrue(helper.absolutePos(pos).equals(player.getRespawnPosition()), "the respawn point is the obelisk");
        var spawn = active.getRespawnPosition(net.minecraft.world.entity.EntityType.PLAYER, helper.getLevel(), helper.absolutePos(pos), 0);
        helper.assertTrue(spawn.isPresent() && spawn.get().position().y > helper.absolutePos(pos).getY() + 1, "the player respawns on top");
        player.discard();
        helper.succeed();
    }

    private static void furnace(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, block("netherrack_furnace"));
        var be = (com.progwml6.natura.common.block.NetherrackFurnaceBlockEntity) helper.getBlockEntity(pos);
        be.setItem(0, new ItemStack(Items.RAW_IRON, 2));
        be.setItem(1, new ItemStack(Items.COAL));
        BlockState state = helper.getBlockState(pos);
        net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(pos), state, be);
        var data = new net.minecraft.nbt.CompoundTag();
        var saved = be.saveWithoutMetadata(helper.getLevel().registryAccess());
        helper.assertTrue(saved.getShort("BurnTime") >= 3199, "coal burns 3200 ticks (twice the vanilla 1600): " + saved.getShort("BurnTime"));
        helper.succeed();
    }

    private static void railsAndHopper(GameTestHelper helper) {
        var rail = (net.minecraft.world.level.block.BaseRailBlock) block("blaze_rail");
        helper.assertTrue(rail.getRailMaxSpeed(rail.defaultBlockState(), helper.getLevel(), BlockPos.ZERO, null) == 0.65F, "blaze rails allow 0.65");
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, block("blaze_hopper"));
        helper.assertTrue(helper.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.HopperBlockEntity, "the blaze hopper is a hopper");
        helper.succeed();
    }

    private static void clouds(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 3, 2);
        helper.setBlock(pos, block("white_cloud"));
        helper.assertTrue(helper.getBlockState(pos).getCollisionShape(helper.getLevel(), helper.absolutePos(pos)).max(net.minecraft.core.Direction.Axis.Y) <= 1 / 16.0 + 1e-6,
                "a cloud is solid only at the bottom");
        helper.setBlock(pos.above(), block("white_cloud"));
        helper.assertTrue(helper.getBlockState(pos.above()).getCollisionShape(helper.getLevel(), helper.absolutePos(pos.above())).isEmpty(), "a cloud on a cloud has no collision");
        var pig = helper.spawn(net.minecraft.world.entity.EntityType.PIG, pos);
        pig.fallDistance = 10;
        pig.setDeltaMovement(0, -1, 0);
        helper.getBlockState(pos).entityInside(helper.getLevel(), helper.absolutePos(pos), pig);
        helper.assertTrue(pig.fallDistance == 0 && pig.getDeltaMovement().y > -0.01, "clouds stop the fall");
        BlockPos sulfur = new BlockPos(5, 3, 5);
        helper.setBlock(sulfur, block("sulfur_cloud"));
        ServerPlayer player = player(helper);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
        helper.getBlockState(sulfur).useItemOn(player.getMainHandItem(), helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(helper.absolutePos(sulfur)), net.minecraft.core.Direction.UP, helper.absolutePos(sulfur), false));
        helper.assertTrue(helper.getBlockState(sulfur).isAir(), "flint and steel sets off a sulfur cloud");
        player.discard();
        helper.succeed();
    }

    private static void workbenchMenu(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        BlockPos pos = new BlockPos(2, 1, 3);
        helper.setBlock(pos, NaturaBlocks.WOODS.get(NaturaWood.REDWOOD).workbench().get());
        BlockState state = helper.getBlockState(pos);
        player.openMenu(state.getMenuProvider(helper.getLevel(), helper.absolutePos(pos)));
        helper.assertTrue(player.containerMenu instanceof net.minecraft.world.inventory.CraftingMenu && player.containerMenu.stillValid(player),
                "the workbench opens a crafting grid that stays open");
        player.closeContainer();
        player.discard();
        helper.succeed();
    }
}

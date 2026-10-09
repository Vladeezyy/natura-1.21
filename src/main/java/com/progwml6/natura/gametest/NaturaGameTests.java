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
    }

    private NaturaGameTests() {}

    @GameTestGenerator
    public static Collection<TestFunction> generate() {
        List<TestFunction> functions = new ArrayList<>();
        TESTS.forEach((name, body) -> functions.add(new TestFunction(Natura.MODID, Natura.MODID + "." + name, STRUCTURE, Rotation.NONE, 100, 0,
                true, false, 1, 1, false, body)));
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

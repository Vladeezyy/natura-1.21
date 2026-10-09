package com.progwml6.natura.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.common.NaturaTabs;
import com.progwml6.natura.library.NaturaWood;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * A scripted scene for development: {@code ./gradlew runScene} joins the {@code natura_scene} world, builds a gallery of
 * the Natura blocks, saves screenshots to {@code run/screenshots/scene_*.png} and quits. Inert unless the
 * {@code natura.scene} system property is set; {@code -Dnatura.sceneFrom=N} skips the steps before tick N.
 */
@EventBusSubscriber(modid = Natura.MODID, value = Dist.CLIENT)
public final class DevScene {
    private static final boolean ENABLED = System.getProperty("natura.scene") != null;
    private static final List<Step> STEPS = new ArrayList<>();
    private static int tick;
    private static BlockPos origin;
    private static BlockPos redwoodBase = BlockPos.ZERO;

    private record Step(int at, Consumer<Minecraft> action) {}

    static {
        at(40, mc -> server(mc, p -> {
            p.setGameMode(GameType.CREATIVE);
            p.getInventory().clearContent();
            ServerLevel level = p.serverLevel();
            origin = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, p.blockPosition());
        }));
        NaturaWood[] overworld = {NaturaWood.MAPLE, NaturaWood.SILVERBELL, NaturaWood.AMARANTH, NaturaWood.TIGER, NaturaWood.WILLOW,
                NaturaWood.EUCALYPTUS, NaturaWood.HOPSEED, NaturaWood.SAKURA, NaturaWood.REDWOOD};
        NaturaWood[] nether = {NaturaWood.GHOSTWOOD, NaturaWood.BLOODWOOD, NaturaWood.FUSEWOOD, NaturaWood.DARKWOOD};
        at(50, mc -> server(mc, p -> gallery(p, overworld)));
        at(52, mc -> mc.options.hideGui = true);
        at(90, mc -> shot(mc, "woods_overworld"));
        at(95, mc -> server(mc, p -> gallery(p, nether)));
        at(135, mc -> shot(mc, "woods_nether"));
        at(140, mc -> server(mc, DevScene::logsAndLeaves));
        at(180, mc -> shot(mc, "logs_leaves"));
        at(185, mc -> mc.options.hideGui = false);
        String[] tabNames = {"general", "world", "decorative"};
        for (int i = 0; i < 3; i++) {
            int t = 190 + i * 20, index = i;
            at(t, mc -> mc.setScreen(new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), true)));
            at(t + 4, mc -> selectTab(mc, index == 0 ? NaturaTabs.GENERAL.get() : index == 1 ? NaturaTabs.WORLD.get() : NaturaTabs.DECORATIVE.get()));
            at(t + 14, mc -> shot(mc, "tab_" + tabNames[index]));
            at(t + 16, mc -> mc.setScreen(null));
        }
        at(260, mc -> server(mc, p -> {
            BlockPos bench = origin.offset(0, 0, 8);
            p.serverLevel().setBlock(bench, NaturaBlocks.WOODS.get(NaturaWood.SAKURA).workbench().get().defaultBlockState(), 3);
            p.teleportTo(p.serverLevel(), bench.getX() + 0.5, bench.getY(), bench.getZ() + 2.5, 180.0F, 30.0F);
            p.openMenu(p.serverLevel().getBlockState(bench).getMenuProvider(p.serverLevel(), bench));
        }));
        at(280, mc -> shot(mc, "workbench_gui"));
        at(285, mc -> mc.player.closeContainer());
        at(290, mc -> mc.options.hideGui = true);
        at(300, mc -> server(mc, p -> trees(p, new String[] {"maple", "silverbell", "amaranth", "tiger", "willow", "eucalyptus", "hopseed", "sakura"}, false)));
        at(360, mc -> shot(mc, "trees_overworld"));
        at(370, mc -> server(mc, p -> trees(p, new String[] {"ghostwood", "fusewood", "darkwood", "bloodwood"}, true)));
        at(430, mc -> shot(mc, "trees_nether"));
        at(440, mc -> server(mc, DevScene::redwood));
        at(520, mc -> shot(mc, "tree_redwood"));
        at(525, mc -> server(mc, p -> p.teleportTo(p.serverLevel(), redwoodBase.getX() + 26.5, redwoodBase.getY() + 60, redwoodBase.getZ() + 26.5, 135.0F, 10.0F)));
        at(560, mc -> shot(mc, "tree_redwood_crown"));
        at(565, mc -> server(mc, p -> p.teleportTo(p.serverLevel(), redwoodBase.getX() + 14.5, redwoodBase.getY() + 2, redwoodBase.getZ() + 14.5, 135.0F, -35.0F)));
        at(600, mc -> shot(mc, "tree_redwood_base"));
        at(610, mc -> server(mc, DevScene::plants));
        at(650, mc -> shot(mc, "plants"));
        at(660, mc -> server(mc, DevScene::netherBlocks));
        at(700, mc -> shot(mc, "nether_blocks"));
        at(705, mc -> server(mc, DevScene::armorStand));
        at(740, mc -> shot(mc, "imp_armor"));
        at(745, mc -> mc.options.hideGui = false);
        at(750, mc -> mc.setScreen(new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), true)));
        at(754, mc -> selectTab(mc, NaturaTabs.GENERAL.get()));
        at(758, mc -> mc.screen.mouseScrolled(mc.getWindow().getGuiScaledWidth() / 2.0, mc.getWindow().getGuiScaledHeight() / 2.0, 0, -10));
        at(768, mc -> shot(mc, "tab_general_tools"));
        at(770, mc -> mc.setScreen(null));
        at(775, mc -> mc.options.hideGui = true);
        at(780, mc -> server(mc, DevScene::mobs));
        at(820, mc -> shot(mc, "mobs"));
        at(825, mc -> server(mc, DevScene::jadeCrop));
        at(828, mc -> mc.options.hideGui = false);
        at(860, mc -> shot(mc, "jade_crop"));
        at(870, Minecraft::stop);
    }

    private static void at(int t, Consumer<Minecraft> action) {
        STEPS.add(new Step(t, action));
    }

    @SubscribeEvent
    static void onTick(ClientTickEvent.Post event) {
        if (!ENABLED) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        // The window is usually unfocused while this runs: never pause, or the integrated server stops.
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) {
            mc.setScreen(null);
        }
        tick++;
        for (Step step : STEPS) {
            if (step.at() == tick) {
                Natura.LOG.info("[scene] tick {}", tick);
                step.action().accept(mc);
            }
        }
    }

    private static void server(Minecraft mc, Consumer<ServerPlayer> action) {
        MinecraftServer server = mc.getSingleplayerServer();
        server.execute(() -> action.accept(server.getPlayerList().getPlayer(mc.player.getUUID())));
    }

    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private static void clear(ServerLevel level) {
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-4, 0, -6), origin.offset(34, 8, 14))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), FLAGS);
        }
        // the flat world's animals wander into the pictures
        for (var entity : level.getEntities((net.minecraft.world.entity.Entity) null, new net.minecraft.world.phys.AABB(origin).inflate(64),
                e -> !(e instanceof ServerPlayer))) {
            entity.discard();
        }
    }

    /** Every block of the given woods: one column per wood, a tower of full blocks behind rows of the shapes. */
    private static void gallery(ServerPlayer player, NaturaWood[] woods) {
        ServerLevel level = player.serverLevel();
        clear(level);
        for (int i = 0; i < woods.length; i++) {
            NaturaWood wood = woods[i];
            NaturaBlocks.WoodSet set = NaturaBlocks.WOODS.get(wood);
            BlockPos base = origin.offset(i * 2, 0, 0);
            String name = wood.getName();
            put(level, base, log(name));
            put(level, base.above(), set.planks().get().defaultBlockState());
            put(level, base.above(2), set.bookshelf().get().defaultBlockState());
            put(level, base.above(3), set.workbench().get().defaultBlockState()
                    .rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_180));
            put(level, base.above(4), leavesState(leaves(name)));
            put(level, base.above().south(), set.button().get().defaultBlockState().setValue(ButtonBlock.FACE, AttachFace.WALL)
                    .setValue(ButtonBlock.FACING, Direction.SOUTH));
            put(level, base.south(2), set.stairs().get().defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
            put(level, base.south(3), set.slab().get().defaultBlockState());
            put(level, base.south(4), set.fence().get().defaultBlockState());
            put(level, base.south(5), set.fenceGate().get().defaultBlockState());
            var door = NaturaBlocks.DOORS.get(name + "_door");
            if (door != null) {
                put(level, base.south(6), door.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
                put(level, base.south(6).above(), door.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            }
            put(level, base.south(7), set.trapdoor().get().defaultBlockState());
            put(level, base.south(8), set.pressurePlate().get().defaultBlockState());
            put(level, base.south(9), NaturaBlocks.SAPLINGS.get(name + "_sapling").get().defaultBlockState());
        }
        look(player, woods.length);
    }

    /** The log pieces (axes, redwood, bloodwood quarters and bark) and every leaves block. */
    private static void logsAndLeaves(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        clear(level);
        int x = 0;
        for (var entry : NaturaBlocks.LOGS.entrySet()) {
            BlockState state = entry.getValue().get().defaultBlockState();
            put(level, origin.offset(x, 0, 0), state);
            if (state.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)) {
                put(level, origin.offset(x, 1, 0), state.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X));
                put(level, origin.offset(x, 2, 0), state.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z));
            }
            x += 2;
        }
        // a 2x2 bloodwood trunk from the four upright quarters, and one lying each way
        BlockState blood = NaturaBlocks.LOGS.get("bloodwood_log").get().defaultBlockState();
        for (int piece = 0; piece < 12; piece++) {
            int set = piece / 4, quarter = piece % 4;
            put(level, origin.offset(x + set * 3 + (quarter & 1), 0, -(quarter >> 1)), blood.setValue(
                    com.progwml6.natura.common.block.BloodwoodLogBlock.PIECE, piece));
        }
        x = 0;
        for (var entry : NaturaBlocks.LEAVES.values()) {
            put(level, origin.offset(x, 0, 4), entry.get().defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true));
            x += 2;
        }
        look(player, 16);
    }

    private static void box(ServerLevel level, BlockPos from, BlockPos to, BlockState state) {
        for (BlockPos p : BlockPos.betweenClosed(from, to)) {
            level.setBlock(p, state, FLAGS);
        }
    }

    /** One grown tree per sapling in a row, 12 blocks apart (nether ones on netherrack, bloodwood under a ceiling). */
    private static void trees(ServerPlayer player, String[] names, boolean nether) {
        ServerLevel level = player.serverLevel();
        BlockPos base = origin.offset(0, 0, 60);
        box(level, base.offset(-8, 0, -10), base.offset(names.length * 12 + 4, 40, 10), Blocks.AIR.defaultBlockState());
        box(level, base.offset(-8, -1, -10), base.offset(names.length * 12 + 4, -1, 10),
                (nether ? Blocks.NETHERRACK : Blocks.GRASS_BLOCK).defaultBlockState());
        for (int i = 0; i < names.length; i++) {
            BlockPos pos = base.offset(i * 12, 0, 0);
            var sapling = NaturaBlocks.SAPLINGS.get(names[i] + "_sapling").get();
            if (names[i].equals("bloodwood")) {
                box(level, pos.offset(-5, 20, -5), pos.offset(6, 20, 6), Blocks.NETHERRACK.defaultBlockState());
                pos = pos.above(19);
            }
            BlockState state = sapling.defaultBlockState().setValue(net.minecraft.world.level.block.SaplingBlock.STAGE, 1);
            level.setBlock(pos, state, 3);
            sapling.advanceTree(level, pos, state, level.random);
        }
        double cx = base.getX() + (names.length - 1) * 6 + 0.5;
        player.teleportTo(level, cx, base.getY() + 8, base.getZ() + 14 + names.length * 2, 180.0F, 12.0F);
    }

    private static void redwood(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos base = origin.offset(0, 0, 160);
        redwoodBase = base;
        box(level, base.offset(-30, 0, -30), base.offset(30, 150, 30), Blocks.AIR.defaultBlockState());
        box(level, base.offset(-30, -1, -30), base.offset(30, -1, 30), Blocks.GRASS_BLOCK.defaultBlockState());
        var sapling = NaturaBlocks.SAPLINGS.get("redwood_sapling").get();
        BlockState state = sapling.defaultBlockState().setValue(net.minecraft.world.level.block.SaplingBlock.STAGE, 1);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                level.setBlock(base.offset(x, 0, z), state, 3);
            }
        }
        sapling.advanceTree(level, base, state, level.random);
        player.teleportTo(level, base.getX() + 0.5, base.getY() + 40, base.getZ() + 95, 180.0F, -14.0F);
    }

    /** Crops at every stage, berry bushes at every stage, saguaro, colored grass, bluebells. */
    private static void plants(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos base = origin.offset(0, 0, -40);
        box(level, base.offset(-4, 0, -4), base.offset(24, 12, 16), Blocks.AIR.defaultBlockState());
        box(level, base.offset(-4, -1, -4), base.offset(24, -1, 16), Blocks.GRASS_BLOCK.defaultBlockState());
        int x = 0;
        for (var crop : new com.progwml6.natura.common.block.NaturaCropBlock[] {NaturaBlocks.BARLEY_CROP.get(), NaturaBlocks.COTTON_CROP.get()}) {
            for (int age = 0; age <= crop.getMaxAge(); age++) {
                put(level, base.offset(x, -1, 0), Blocks.FARMLAND.defaultBlockState());
                put(level, base.offset(x, 0, 0), crop.defaultBlockState().setValue(crop.getAgeProperty(), age));
                x++;
            }
            x++;
        }
        int z = 3;
        for (var bush : NaturaBlocks.BUSHES.values()) {
            for (int age = 0; age < 4; age++) {
                put(level, base.offset(age * 2, 0, z), bush.get().defaultBlockState().setValue(com.progwml6.natura.common.block.NaturaBerryBushBlock.AGE, age));
            }
            z += 2;
            if (z > 9) {
                z = 3;
                base = base.offset(9, 0, 0);
            }
        }
        base = origin.offset(0, 0, -40);
        BlockPos sand = base.offset(18, 0, 2);
        box(level, sand.offset(-3, -1, -3), sand.offset(3, -1, 3), Blocks.SAND.defaultBlockState());
        var gen = new com.progwml6.natura.world.tree.SaguaroGenerator(NaturaBlocks.SAGUARO.get().defaultBlockState(), false, false);
        gen.generateSaguaro(new java.util.Random(7), new com.progwml6.natura.world.tree.TreeWorld(level), sand);
        put(level, sand.offset(2, 0, 2), NaturaBlocks.SAGUARO_BABY.get().defaultBlockState());
        put(level, sand.offset(-2, 0, 2), NaturaBlocks.SAGUARO_FRUIT.get().defaultBlockState());
        int gx = 0;
        for (String grass : new String[] {"topiary_grass", "bluegrass", "autumnal_grass"}) {
            put(level, base.offset(gx, 0, 12), NaturaBlocks.COLORED_GRASS.get(grass).get().defaultBlockState());
            put(level, base.offset(gx + 1, 0, 12), NaturaBlocks.COLORED_GRASS.get(grass + "_slab").get().defaultBlockState());
            put(level, base.offset(gx + 2, 0, 12), NaturaBlocks.COLORED_GRASS.get(grass + "_stairs").get().defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH));
            put(level, base.offset(gx, 1, 12), NaturaBlocks.BLUEBELLS.get().defaultBlockState());
            gx += 4;
        }
        player.teleportTo(level, base.getX() + 10.5, base.getY() + 7, base.getZ() + 22, 180.0F, 28.0F);
    }

    /** Heat sand, tainted soil, glass, thorn vines, glowshrooms, obelisk, furnace, hopper, redstone parts, rails, clouds. */
    private static void netherBlocks(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos base = origin.offset(40, 0, 0);
        box(level, base.offset(-6, 0, -8), base.offset(28, 14, 14), Blocks.AIR.defaultBlockState());
        box(level, base.offset(-6, -1, -8), base.offset(28, -1, 14), Blocks.NETHERRACK.defaultBlockState());
        String[] row = {"heat_sand", "tainted_soil", "tainted_farmland_dry", "tainted_farmland_heated", "soul_glass", "heat_glass"};
        for (int i = 0; i < row.length; i++) {
            put(level, base.offset(i * 2, 0, 8), NaturaBlocks.NETHER.get(row[i]).get().defaultBlockState());
        }
        // thorn vines on a netherrack wall
        box(level, base.offset(13, 0, 7), base.offset(15, 2, 7), Blocks.NETHERRACK.defaultBlockState());
        for (int x = 13; x <= 15; x++) {
            for (int y = 0; y <= 2; y++) {
                put(level, base.offset(x, y, 8), NaturaBlocks.NETHER.get("thorn_vines").get().defaultBlockState()
                        .setValue(net.minecraft.world.level.block.VineBlock.NORTH, true));
            }
        }
        int x = 0;
        for (var shroom : NaturaBlocks.GLOWSHROOMS.values()) {
            put(level, base.offset(x, 0, 11), shroom.get().defaultBlockState());
            BlockPos huge = base.offset(x * 4 - 2, 0, -2);
            for (int attempt = 0; attempt < 20 && !level.getBlockState(huge).is(NaturaBlocks.LARGE_GLOWSHROOMS.get(shroomColor(shroom.getId().getPath())).get()); attempt++) {
                level.setBlock(huge, shroom.get().defaultBlockState(), 3);
                shroom.get().performBonemeal(level, level.random, huge, shroom.get().defaultBlockState());
            }
            x += 2;
        }
        x = 0;
        put(level, base.offset(7, 0, 4), NaturaBlocks.NETHER.get("respawn_obelisk").get().defaultBlockState());
        put(level, base.offset(8, 0, 4), NaturaBlocks.NETHER.get("respawn_obelisk").get().defaultBlockState()
                .setValue(com.progwml6.natura.common.block.RespawnObeliskBlock.ACTIVE, true));
        put(level, base.offset(10, 0, 4), NaturaBlocks.NETHERRACK_FURNACE.get().defaultBlockState().setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, Direction.SOUTH));
        put(level, base.offset(11, 0, 4), NaturaBlocks.NETHERRACK_FURNACE.get().defaultBlockState().setValue(net.minecraft.world.level.block.AbstractFurnaceBlock.FACING, Direction.SOUTH)
                .setValue(com.progwml6.natura.common.block.NetherrackFurnaceBlock.LIT, true));
        put(level, base.offset(13, 0, 4), NaturaBlocks.BLAZE_HOPPER.get().defaultBlockState());
        put(level, base.offset(15, 0, 4), NaturaBlocks.NETHER.get("nether_lever").get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.LeverBlock.FACE, AttachFace.FLOOR));
        put(level, base.offset(16, 0, 4), NaturaBlocks.NETHER.get("nether_pressure_plate").get().defaultBlockState());
        put(level, base.offset(17, 0, 4), NaturaBlocks.NETHER.get("nether_button").get().defaultBlockState()
                .setValue(ButtonBlock.FACE, AttachFace.FLOOR));
        String[] rails = {"blaze_rail", "blaze_powered_rail", "blaze_activator_rail", "blaze_detector_rail"};
        for (int i = 0; i < rails.length; i++) {
            for (int z = 0; z < 3; z++) {
                BlockState rail = NaturaBlocks.NETHER.get(rails[i]).get().defaultBlockState();
                if (z == 2 && rail.hasProperty(net.minecraft.world.level.block.PoweredRailBlock.POWERED)) {
                    rail = rail.setValue(net.minecraft.world.level.block.PoweredRailBlock.POWERED, true);
                }
                put(level, base.offset(19 + i * 2, 0, z), rail);
            }
        }
        int c = 0;
        for (var cloud : NaturaBlocks.CLOUDS.values()) {
            put(level, base.offset(19 + c * 2, 2, 9), cloud.get().defaultBlockState());
            put(level, base.offset(19 + c * 2, 3, 9), cloud.get().defaultBlockState());
            c++;
        }
        player.teleportTo(level, base.getX() + 12.5, base.getY() + 9, base.getZ() + 24, 180.0F, 25.0F);
    }

    /** An armor stand in imp armor holding a bloodwood kama, next to the nether gallery. */
    private static void armorStand(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = origin.offset(52, 0, 18);
        var stand = new net.minecraft.world.entity.decoration.ArmorStand(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        stand.setYRot(0);
        stand.setYBodyRot(0);
        stand.setYHeadRot(0);
        String[] pieces = {"imp_armor_boots", "imp_armor_leggings", "imp_armor_chestplate", "imp_armor_helmet"};
        net.minecraft.world.entity.EquipmentSlot[] slots = {net.minecraft.world.entity.EquipmentSlot.FEET, net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.HEAD};
        for (int i = 0; i < 4; i++) {
            stand.setItemSlot(slots[i], new net.minecraft.world.item.ItemStack(com.progwml6.natura.tools.NaturaTools.TOOLS.get(pieces[i]).get()));
        }
        level.addFreshEntity(stand);
        player.teleportTo(level, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 3.5, 180.0F, 5.0F);
    }

    /** The four mobs, frozen (no AI) on netherrack. */
    private static void mobs(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        // the scene world is on Peaceful, which removes the monsters at once
        level.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL, true);
        BlockPos base = origin.offset(70, 0, 0);
        box(level, base.offset(-6, 0, -6), base.offset(14, 8, 10), Blocks.AIR.defaultBlockState());
        box(level, base.offset(-6, -1, -6), base.offset(14, -1, 10), Blocks.NETHERRACK.defaultBlockState());
        var types = new net.minecraft.world.entity.EntityType<?>[] {com.progwml6.natura.entities.NaturaEntities.IMP.get(),
                com.progwml6.natura.entities.NaturaEntities.HEATSCAR_SPIDER.get(), com.progwml6.natura.entities.NaturaEntities.BABY_HEATSCAR_SPIDER.get(),
                com.progwml6.natura.entities.NaturaEntities.NITRO_CREEPER.get()};
        double[] xs = {0, 3.5, 7.5, 10};
        for (int i = 0; i < types.length; i++) {
            var entity = types[i].create(level);
            if (entity instanceof net.minecraft.world.entity.Mob mob) {
                mob.setNoAi(true);
                mob.setPersistenceRequired();
            }
            entity.moveTo(base.getX() + xs[i] + 0.5, base.getY(), base.getZ() + 0.5, 0.0F, 0.0F);
            entity.setYHeadRot(0.0F);
            level.addFreshEntity(entity);
        }
        player.teleportTo(level, base.getX() + 5.5, base.getY() + 2.5, base.getZ() + 9.5, 180.0F, 12.0F);
    }

    /** A half grown barley crop under the crosshair: Jade shows the barley icon (the 1.12 Waila plugin) and its growth. */
    private static void jadeCrop(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos base = origin.offset(100, 0, 0);
        box(level, base.offset(-3, 0, -3), base.offset(3, 4, 4), Blocks.AIR.defaultBlockState());
        box(level, base.offset(-3, -1, -3), base.offset(3, -1, 4), Blocks.GRASS_BLOCK.defaultBlockState());
        put(level, base.below(), Blocks.FARMLAND.defaultBlockState());
        put(level, base, NaturaBlocks.BARLEY_CROP.get().defaultBlockState().setValue(com.progwml6.natura.common.block.NaturaCropBlock.AGE_3, 2));
        player.teleportTo(level, base.getX() + 0.5, base.getY(), base.getZ() + 2.5, 180.0F, 36.0F);
    }

    private static String shroomColor(String id) {
        return id.substring(0, id.indexOf('_'));
    }

    private static void put(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, FLAGS);
    }

    private static BlockState leavesState(String id) {
        return NaturaBlocks.LEAVES.get(id).get().defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);
    }

    private static BlockState log(String wood) {
        return switch (wood) {
            case "redwood" -> NaturaBlocks.LOGS.get("redwood_bark").get().defaultBlockState();
            default -> NaturaBlocks.LOGS.get(wood + "_log").get().defaultBlockState();
        };
    }

    private static String leaves(String wood) {
        return wood.equals("darkwood") ? "darkwood_leaves" : wood + "_leaves";
    }

    /** Fly south of the gallery and above it, looking north at its middle (east is on the right). */
    private static void look(ServerPlayer player, int columns) {
        double cx = origin.getX() + columns - 0.5;
        double eyeY = origin.getY() + 7.5;
        double ez = origin.getZ() + 17.0;
        double ty = origin.getY() + 1.5, tz = origin.getZ() + 3.0;
        float pitch = (float) Math.toDegrees(Math.atan2(eyeY - ty, ez - tz));
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.teleportTo(player.serverLevel(), cx, eyeY - player.getEyeHeight(), ez, 180.0F, pitch);
    }

    private static void selectTab(Minecraft mc, CreativeModeTab tab) {
        if (!(mc.screen instanceof CreativeModeInventoryScreen screen)) return;
        try {
            var method = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
            method.setAccessible(true);
            method.invoke(screen, tab);
        } catch (ReflectiveOperationException e) {
            Natura.LOG.warn("[scene] could not select the creative tab", e);
        }
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "scene_" + name + ".png", mc.getMainRenderTarget(), msg -> Natura.LOG.info("[scene] {}", msg.getString()));
    }

    private DevScene() {}
}

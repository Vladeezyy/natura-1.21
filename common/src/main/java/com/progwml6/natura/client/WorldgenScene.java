package com.progwml6.natura.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;
import com.progwml6.natura.Natura;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Screenshots of the generated world for development: {@code ./gradlew runWorldgenScene} opens (or creates, seed 1234)
 * the normal world {@code natura_gen}, goes to biomes where Natura generates, looks at a generated Natura block and saves
 * {@code run/screenshots/worldgen_*.png}. Inert unless the {@code natura.worldgenScene} system property is set.
 */
public final class WorldgenScene {
    private static final boolean ENABLED = System.getProperty("natura.worldgenScene") != null;
    private static final String WORLD = "natura_gen";
    private static final List<Step> STEPS = new ArrayList<>();
    private static boolean opened;
    private static int tick;
    private static BlockPos target;

    private record Step(int at, Consumer<Minecraft> action) {}

    private record Shot(String name, ResourceKey<Level> dimension, String biome, Set<String> blocks) {}

    static {
        List<Shot> shots = List.of(
                new Shot("forest", Level.OVERWORLD, "minecraft:forest", Set.of("maple_log", "silverbell_log", "tiger_log", "sakura_log")),
                new Shot("jungle", Level.OVERWORLD, "minecraft:jungle", Set.of("amaranth_log")),
                new Shot("swamp", Level.OVERWORLD, "minecraft:swamp", Set.of("willow_log")),
                new Shot("hills", Level.OVERWORLD, "minecraft:windswept_hills", Set.of("hopseed_log", "eucalyptus_log")),
                new Shot("desert", Level.OVERWORLD, "minecraft:desert", Set.of("saguaro")),
                new Shot("bushes", Level.OVERWORLD, "minecraft:plains", Set.of("raspberry_bush", "blueberry_bush", "blackberry_bush", "maloberry_bush",
                        "barley_crop", "cotton_crop", "bluebells")),
                new Shot("nether_trees", Level.NETHER, "minecraft:nether_wastes", Set.of("darkwood_log", "ghostwood_log", "fusewood_log")),
                new Shot("nether_bloodwood", Level.NETHER, "minecraft:nether_wastes", Set.of("bloodwood_log", "bloodwood_bark")),
                new Shot("nether_plants", Level.NETHER, "minecraft:nether_wastes", Set.of("green_large_glowshroom", "blue_large_glowshroom",
                        "purple_large_glowshroom", "green_glowshroom", "thorn_vines", "blightberry_bush", "duskberry_bush", "skyberry_bush", "stingberry_bush")),
                new Shot("nether_soil", Level.NETHER, "minecraft:soul_sand_valley", Set.of("heat_sand", "tainted_soil")));
        at(40, mc -> server(mc, p -> {
            p.setGameMode(GameType.CREATIVE);
            p.getAbilities().flying = true;
            p.onUpdateAbilities();
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 1000000, 0, false, false));
            MinecraftServer server = p.getServer();
            server.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
            server.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
            server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
            server.overworld().setDayTime(6000);
            server.overworld().setWeatherParameters(6000, 0, false, false);
        }));
        at(45, mc -> mc.options.hideGui = true);
        int t = 50;
        for (Shot shot : shots) {
            at(t, mc -> server(mc, p -> go(p, shot)));
            at(t + 300, mc -> server(mc, p -> look(p, shot)));
            at(t + 380, mc -> shot(mc, shot.name()));
            t += 400;
        }
        at(t + 10, Minecraft::stop);
    }

    private static void at(int t, Consumer<Minecraft> action) {
        STEPS.add(new Step(t, action));
    }

    /** Called by the loader at the end of every client tick. */
    public static void onTick() {
        if (!ENABLED) return;
        Minecraft mc = Minecraft.getInstance();
        if (!opened && mc.screen instanceof TitleScreen) {
            opened = true;
            if (mc.getLevelSource().levelExists(WORLD)) {
                mc.createWorldOpenFlows().openWorld(WORLD, () -> mc.setScreen(new TitleScreen()));
            } else {
                LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(),
                        WorldDataConfiguration.DEFAULT);
                mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(1234L, true, false), WorldPresets::createNormalWorldDimensions,
                        new TitleScreen());
            }
            return;
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) {
            mc.setScreen(null);
        }
        tick++;
        for (Step step : STEPS) {
            if (step.at() == tick) {
                Natura.LOG.info("[worldgen scene] tick {}", tick);
                step.action().accept(mc);
            }
        }
    }

    private static void server(Minecraft mc, Consumer<ServerPlayer> action) {
        MinecraftServer server = mc.getSingleplayerServer();
        server.execute(() -> action.accept(server.getPlayerList().getPlayer(mc.player.getUUID())));
    }

    /** Goes to the closest biome of the shot (the chunks around generate while the next steps wait). */
    private static void go(ServerPlayer player, Shot shot) {
        ServerLevel level = player.getServer().getLevel(shot.dimension());
        ResourceKey<Biome> biome = ResourceKey.create(Registries.BIOME, ResourceLocation.parse(shot.biome()));
        BlockPos from = player.level() == level ? player.blockPosition() : new BlockPos(0, 64, 0);
        Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(h -> h.is(biome), from.offset(600, 0, 0), 6400, 32, 64);
        target = found == null ? from : found.getFirst();
        int y = shot.dimension() == Level.NETHER ? 70 : 200;
        player.teleportTo(level, target.getX() + 0.5, y, target.getZ() + 0.5, 0.0F, 30.0F);
        Natura.LOG.info("[worldgen scene] {}: {} at {}", shot.name(), shot.biome(), target);
    }

    /** Finds the generated Natura block of the shot closest to the biome spot and looks at it from a free spot. */
    private static void look(ServerPlayer player, Shot shot) {
        ServerLevel level = player.serverLevel();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean nether = shot.dimension() == Level.NETHER;
        int r = shot.name().equals("desert") ? 180 : 80;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                int cx = target.getX() + x, cz = target.getZ() + z;
                if (!level.hasChunk(cx >> 4, cz >> 4)) continue;
                int top = nether ? 127 : level.getHeight(Heightmap.Types.WORLD_SURFACE, cx, cz);
                int bottom = nether ? 1 : top - 40;
                for (int y = top; y >= bottom; y--) {
                    ResourceLocation id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos.set(cx, y, cz)).getBlock());
                    if (id.getNamespace().equals(Natura.MODID) && shot.blocks().contains(id.getPath())) {
                        double d = x * x + z * z + (nether ? (y - 64) * (y - 64) : 0);
                        if (d < bestDistance) {
                            bestDistance = d;
                            best = pos.immutable();
                        }
                        break;
                    }
                }
            }
        }
        if (best == null) {
            Natura.LOG.info("[worldgen scene] {}: nothing found around {}", shot.name(), target);
            player.teleportTo(level, target.getX() + 0.5, nether ? 70 : level.getHeight(Heightmap.Types.MOTION_BLOCKING, target.getX(), target.getZ()) + 20,
                    target.getZ() + 0.5, 0.0F, 40.0F);
            return;
        }
        Natura.LOG.info("[worldgen scene] {}: {} at {}", shot.name(), BuiltInRegistries.BLOCK.getKey(level.getBlockState(best).getBlock()), best);
        Vec3 aim = Vec3.atCenterOf(best);
        Vec3 eye = null;
        int[] distances = nether ? new int[] {7, 5, 9, 4, 12} : new int[] {16, 22, 12};
        outer:
        for (int d : distances) {
            for (int up : nether ? new int[] {2, 4, 0} : new int[] {8, 12, 4}) {
                for (int angle = 0; angle < 360; angle += 20) {
                    Vec3 candidate = aim.add(Mth.sin(angle * Mth.DEG_TO_RAD) * d, up, Mth.cos(angle * Mth.DEG_TO_RAD) * d);
                    if (!free(level, BlockPos.containing(candidate))) continue;
                    BlockHitResult hit = level.clip(new ClipContext(candidate, aim, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                    if (hit.getType() == HitResult.Type.MISS || hit.getBlockPos().distSqr(best) <= (nether ? 4 : 16)) {
                        eye = candidate;
                        break outer;
                    }
                }
            }
        }
        if (eye == null) {
            eye = aim.add(0, nether ? 3 : 12, 10);
        }
        Vec3 dir = aim.subtract(eye);
        float yaw = (float) (Mth.atan2(-dir.x, dir.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (-Mth.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)) * Mth.RAD_TO_DEG);
        player.teleportTo(level, eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
    }

    /** Air all around the camera, so no leaves or rock fill the view. */
    private static boolean free(ServerLevel level, BlockPos at) {
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-1, -2, -1), at.offset(1, 1, 1))) {
            if (!level.getBlockState(p).isAir()) return false;
        }
        return true;
    }

    private static void shot(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, "worldgen_" + name + ".png", mc.getMainRenderTarget(), msg -> Natura.LOG.info("[worldgen scene] {}", msg.getString()));
    }

    private WorldgenScene() {}
}

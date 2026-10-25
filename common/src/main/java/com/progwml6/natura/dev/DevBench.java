package com.progwml6.natura.dev;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.common.block.NaturaCropBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;

/**
 * Dev-only measurements, inert unless {@code -Dnatura.bench=gen|crops} (tools/bench.sh):
 * <ul>
 * <li>{@code gen}: forces a square of {@code natura.bench.size} chunks far from spawn in the overworld and in the nether
 * and logs the chunks per second until all are fully generated (run with and without the Natura world generation);</li>
 * <li>{@code crops}: a field of mature crops (Natura barley or vanilla wheat, {@code natura.bench.crop}) with random
 * ticks sped up, logs the average server tick time.</li>
 * </ul>
 * Results are "[bench]" log lines; the server stops itself.
 */
public final class DevBench {
    private static final String MODE = System.getProperty("natura.bench", "");
    private static final int SIZE = Integer.getInteger("natura.bench.size", 24);
    private static int stage;
    private static long started;
    private static int ticks;
    private static long tickNanos;
    private static long tickStart;

    private DevBench() {}

    /** Whether a bench runs: the loader then calls {@link #start}, {@link #tickStart} and {@link #tick}. */
    public static boolean active() {
        return !MODE.isEmpty();
    }

    public static void tickStart() {
        tickStart = System.nanoTime();
    }

    public static void start(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        }
        if (MODE.equals("gen")) {
            stage = 1;
            // setChunkForced loads (generates) the chunk at once, so the time includes it
            started = System.nanoTime();
            force(server.overworld(), true);
        } else if (MODE.equals("crops")) {
            buildField(server.overworld());
            server.overworld().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(300, server);
            stage = 10;
        } else if (MODE.equals("carts")) {
            buildCarts(server.overworld());
            stage = 10;
        }
    }

    private static void force(ServerLevel level, boolean on) {
        int base = 20000 >> 4;
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                level.setChunkForced(base + x, base + z, on);
            }
        }
    }

    private static boolean generated(ServerLevel level) {
        int base = 20000 >> 4;
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                if (level.getChunkSource().getChunkNow(base + x, base + z) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void tick(MinecraftServer server) {
        if (stage == 1 || stage == 2) {
            ServerLevel level = stage == 1 ? server.overworld() : server.getLevel(Level.NETHER);
            if (generated(level)) {
                double seconds = (System.nanoTime() - started) / 1e9;
                Natura.LOG.info("[bench] gen {} {}x{} chunks in {} s: {} chunks/s", stage == 1 ? "overworld" : "nether", SIZE, SIZE,
                        String.format("%.2f", seconds), String.format("%.1f", SIZE * SIZE / seconds));
                force(level, false);
                if (stage == 1) {
                    stage = 2;
                    started = System.nanoTime();
                    force(server.getLevel(Level.NETHER), true);
                } else {
                    stage = 99;
                    server.halt(false);
                }
            }
        } else if (stage == 10) {
            ticks++;
            if (ticks > 100) {
                tickNanos += System.nanoTime() - tickStart;
            }
            if (ticks == 100 + 1200) {
                var items = new java.util.TreeMap<String, Integer>();
                for (var e : server.overworld().getAllEntities()) {
                    if (e instanceof net.minecraft.world.entity.item.ItemEntity item) {
                        items.merge(item.getItem().getItem().toString(), item.getItem().getCount(), Integer::sum);
                    }
                }
                Natura.LOG.info("[bench] item entities: {}", items);
                if (MODE.equals("carts")) {
                    Natura.LOG.info("[bench] carts {} {} carts: {} ms per tick (average of 1200 ticks)",
                            System.getProperty("natura.bench.rail", "blaze"), CARTS * CARTS, String.format("%.3f", tickNanos / 1200 / 1e6));
                } else {
                    Natura.LOG.info("[bench] crops {} {} plants: {} ms per tick (average of 1200 ticks, random tick speed 300)",
                            System.getProperty("natura.bench.crop", "natura"), 128 * 128, String.format("%.3f", tickNanos / 1200 / 1e6));
                }
                stage = 99;
                server.halt(false);
            }
        }
    }

    /** 128x128 mature crops on moist farmland under the sky, chunks kept loaded and ticking */
    private static void buildField(ServerLevel level) {
        boolean vanilla = System.getProperty("natura.bench.crop", "natura").equals("wheat");
        BlockState crop = vanilla ? Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7) : NaturaBlocks.BARLEY_CROP.get().defaultBlockState();
        if (!vanilla && crop.getBlock() instanceof NaturaCropBlock natura) {
            crop = natura.withAge(natura.getMaxAge());
        }
        BlockPos origin = new BlockPos(5000, 200, 5000);
        for (int cx = 0; cx < 8; cx++) {
            for (int cz = 0; cz < 8; cz++) {
                level.setChunkForced((origin.getX() >> 4) + cx, (origin.getZ() >> 4) + cz, true);
            }
        }
        BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                BlockPos pos = origin.offset(x, 0, z);
                level.setBlock(pos.below(), (x % 9 == 4 && z % 9 == 4) ? Blocks.WATER.defaultBlockState() : farmland, Block.UPDATE_CLIENTS);
                level.setBlock(pos, (x % 9 == 4 && z % 9 == 4) ? Blocks.AIR.defaultBlockState() : crop, Block.UPDATE_CLIENTS);
            }
        }
    }

    private static final int CARTS = 32;

    /** 32 straight tracks of 32 rails (Natura blaze rails or vanilla rails, natura.bench.rail), a minecart on every rail. */
    private static void buildCarts(ServerLevel level) {
        boolean vanilla = System.getProperty("natura.bench.rail", "blaze").equals("vanilla");
        Block rail = vanilla ? Blocks.RAIL : NaturaBlocks.NETHER.get("blaze_rail").get();
        BlockPos origin = new BlockPos(5000, 200, 5000);
        for (int cx = 0; cx < 4; cx++) {
            for (int cz = 0; cz < 6; cz++) {
                level.setChunkForced((origin.getX() >> 4) + cx, (origin.getZ() >> 4) + cz, true);
            }
        }
        for (int row = 0; row < CARTS; row++) {
            for (int i = 0; i < CARTS; i++) {
                BlockPos pos = origin.offset(i, 0, row * 3);
                level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(pos, rail.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        for (int row = 0; row < CARTS; row++) {
            for (int i = 0; i < CARTS; i++) {
                var cart = net.minecraft.world.entity.EntityType.MINECART.create(level);
                if (cart != null) {
                    BlockPos pos = origin.offset(i, 0, row * 3);
                    cart.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
                    level.addFreshEntity(cart);
                }
            }
        }
    }
}

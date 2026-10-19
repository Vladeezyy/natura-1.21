package com.progwml6.natura.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import com.progwml6.natura.common.block.BloodwoodLogBlock;
import com.progwml6.natura.common.block.NaturaSaplingBlock;
import com.progwml6.natura.world.tree.BloodwoodTreeGenerator;
import com.progwml6.natura.world.tree.DarkwoodTreeGenerator;
import com.progwml6.natura.world.tree.EucalyptusTreeGenerator;
import com.progwml6.natura.world.tree.FusewoodTreeGenerator;
import com.progwml6.natura.world.tree.GhostwoodTreeGenerator;
import com.progwml6.natura.world.tree.HopseedTreeGenerator;
import com.progwml6.natura.world.tree.NaturaTreeGenerator;
import com.progwml6.natura.world.tree.OverworldTreeGenerator;
import com.progwml6.natura.world.tree.RedwoodTreeGenerator;
import com.progwml6.natura.world.tree.SakuraTreeGenerator;
import com.progwml6.natura.world.tree.TreeWorld;
import com.progwml6.natura.world.tree.WillowTreeGenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Sapling growth: the 1.12 generator of each sapling (BlockOverworldSapling.generateTree and friends). */
public final class NaturaTrees {
    private NaturaTrees() {}

    public static BlockState log(String id) {
        return NaturaBlocks.LOGS.get(id).get().defaultBlockState();
    }

    public static BlockState leaves(String id) {
        return NaturaBlocks.LEAVES.get(id).get().defaultBlockState();
    }

    public static BlockState bloodwood(int piece) {
        return log("bloodwood_log").setValue(BloodwoodLogBlock.PIECE, piece);
    }

    /** The generator a sapling grows (1.12: a new one for every growth). */
    public static NaturaTreeGenerator saplingGenerator(String name) {
        return switch (name) {
            case "maple", "silverbell" -> new OverworldTreeGenerator(4, 2, log(name + "_log"), leaves(name + "_leaves"), true, true);
            case "amaranth" -> new OverworldTreeGenerator(9, 8, log("amaranth_log"), leaves("amaranth_leaves"), true, true);
            case "tiger" -> new OverworldTreeGenerator(6, 4, log("tiger_log"), leaves("tiger_leaves"), true, true);
            case "willow" -> new WillowTreeGenerator(4, 5, log("willow_log"), leaves("willow_leaves"), true, true);
            case "eucalyptus" -> new EucalyptusTreeGenerator(6, 3, log("eucalyptus_log"), leaves("eucalyptus_leaves"), true);
            case "hopseed" -> new HopseedTreeGenerator(2, 3, log("hopseed_log"), leaves("hopseed_leaves"), true, true);
            case "sakura" -> new SakuraTreeGenerator(log("sakura_log"), leaves("sakura_leaves"), false, true);
            case "redwood" -> new RedwoodTreeGenerator(log("redwood_bark"), log("redwood_heart"), log("redwood_root"), leaves("redwood_leaves"), true, true);
            case "ghostwood" -> new GhostwoodTreeGenerator(log("ghostwood_log"), leaves("ghostwood_leaves"), false);
            case "fusewood" -> new FusewoodTreeGenerator(3, log("fusewood_log"), leaves("fusewood_leaves"), false);
            case "darkwood" -> new DarkwoodTreeGenerator(3, log("darkwood_log"), leaves("darkwood_leaves"), leaves("darkwood_flowering_leaves"),
                    leaves("darkwood_fruit_leaves"), true);
            case "bloodwood" -> new BloodwoodTreeGenerator(log("bloodwood_bark"), bloodwood(0), bloodwood(1), bloodwood(2), bloodwood(3),
                    leaves("bloodwood_leaves"));
            default -> throw new IllegalArgumentException(name);
        };
    }

    public static NaturaSaplingBlock.Tree forSapling(String name) {
        if (name.equals("redwood")) {
            return NaturaTrees::growRedwood;
        }
        return (level, pos, sapling, random) -> grow(level, pos, sapling, random, () -> saplingGenerator(name));
    }

    /** 1.12: the sapling is removed, the tree grown, and the sapling put back when nothing grew where it stood. */
    private static boolean grow(ServerLevel level, BlockPos pos, BlockState sapling, RandomSource random, Supplier<NaturaTreeGenerator> generator) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_INVISIBLE);
        TreeWorld world = new TreeWorld(level);
        generator.get().generateTree(new Random(random.nextLong()), world, pos);
        world.finish();
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, sapling, Block.UPDATE_INVISIBLE);
            return false;
        }
        return true;
    }

    /**
     * 1.12 BlockRedwoodSapling: only with at least 40 redwood saplings in the 7x7 square around it; all of them are
     * removed for the tree, and put back where it left air if nothing grew where the sapling stood.
     */
    private static boolean growRedwood(ServerLevel level, BlockPos pos, BlockState sapling, RandomSource random) {
        List<BlockPos> saplings = new ArrayList<>();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                if (level.getBlockState(pos.offset(x, 0, z)).is(sapling.getBlock())) {
                    saplings.add(pos.offset(x, 0, z));
                }
            }
        }
        NaturaTreeGenerator generator = saplings.size() >= 40 ? saplingGenerator("redwood") : null;
        for (BlockPos p : saplings) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        TreeWorld world = new TreeWorld(level);
        Random rand = new Random(random.nextLong());
        if (generator != null) {
            generator.generateTree(rand, world, pos);
            world.finish();
        }
        if (level.isEmptyBlock(pos)) {
            for (BlockPos p : saplings) {
                if (level.isEmptyBlock(p)) {
                    level.setBlock(p, sapling, Block.UPDATE_CLIENTS);
                }
            }
            level.setBlock(pos, sapling, Block.UPDATE_CLIENTS);
            return false;
        }
        return true;
    }
}

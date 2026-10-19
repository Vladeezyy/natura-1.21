package com.progwml6.natura.world.tree;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The 1.12 World calls the Natura tree generators used, on a 1.21 level. Leaves placed through it get their vanilla
 * distance once the tree is done ({@link #finish()}), like vanilla trees do, or they would decay at once.
 *
 * <p>1.12 world limits (y 0 and 256) are the 1.21 world's bottom and top; the other heights in the generators (sea
 * level 64, the willow limit 128, the eucalyptus search from 96 to 32, the nether ceiling 120) are kept as they were.
 */
public final class TreeWorld {
    private final LevelAccessor level;
    private final Map<BlockPos, BlockState> leaves = new HashMap<>();

    public TreeWorld(LevelAccessor level) {
        this.level = level;
    }

    public LevelAccessor level() {
        return this.level;
    }

    /**
     * During world generation only the chunks around the decorated one can be read (and only the 3x3 written): 1.12
     * generators reached further (the redwood crowns), so outside that this reads air and does not write.
     */
    private boolean readable(BlockPos pos) {
        if (pos.getY() < this.level.getMinY() || pos.getY() >= this.level.getMaxY() + 1) {
            return false;
        }
        return !(this.level instanceof net.minecraft.server.level.WorldGenRegion region)
                || region.hasChunk(net.minecraft.core.SectionPos.blockToSectionCoord(pos.getX()), net.minecraft.core.SectionPos.blockToSectionCoord(pos.getZ()));
    }

    private boolean writable(BlockPos pos) {
        if (!this.readable(pos)) {
            return false;
        }
        if (this.level instanceof net.minecraft.server.level.WorldGenRegion region) {
            var center = region.getCenter();
            return Math.abs(center.x() - net.minecraft.core.SectionPos.blockToSectionCoord(pos.getX())) <= 1
                    && Math.abs(center.z() - net.minecraft.core.SectionPos.blockToSectionCoord(pos.getZ())) <= 1;
        }
        return true;
    }

    public BlockState getBlockState(BlockPos pos) {
        return this.readable(pos) ? this.level.getBlockState(pos) : Blocks.AIR.defaultBlockState();
    }

    /** 1.12 setBlockState(pos, state, 2 / 0): sent to clients, no neighbour updates. */
    public void setBlockState(BlockPos pos, BlockState state) {
        if (!this.writable(pos)) {
            return;
        }
        pos = pos.immutable();
        this.level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        if (state.getBlock() instanceof LeavesBlock) {
            this.leaves.put(pos, state);
        } else {
            this.leaves.remove(pos);
        }
    }

    public boolean isAirBlock(BlockPos pos) {
        return this.getBlockState(pos).isAir();
    }

    public static boolean isAir(BlockState state) {
        return state.isAir();
    }

    public static boolean isLeaves(BlockState state) {
        return state.is(BlockTags.LEAVES);
    }

    /** 1.12 Block.isWood: the logs. */
    public static boolean isWood(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    /** 1.12 Block.isReplaceable (the material's replaceable flag: air, water, lava, grass, snow layers, vines, ...). */
    public static boolean isReplaceable(BlockState state) {
        return state.canBeReplaced();
    }

    /** 1.12 Block.canPlaceBlockAt of the block already there: whether it can be replaced. */
    public static boolean canPlaceBlockAt(BlockState state) {
        return state.canBeReplaced();
    }

    /** 1.12 Block.canBeReplacedByLeaves: not an opaque cube. */
    public boolean canBeReplacedByLeaves(BlockState state, BlockPos pos) {
        return !state.isSolidRender();
    }

    /** 1.12 IBlockState.isFullBlock: an opaque full cube. */
    public boolean isFullBlock(BlockState state, BlockPos pos) {
        return state.isSolidRender();
    }

    /** 1.12 Blocks.DIRT (dirt, coarse dirt, podzol) or Blocks.GRASS. */
    public static boolean isDirtOrGrass(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.GRASS_BLOCK);
    }

    /** 1.12 canSustainPlant for a plains plant (the overworld saplings): grass, dirt, farmland. */
    public static boolean isPlainsSoil(BlockState state) {
        return isDirtOrGrass(state) || state.is(Blocks.FARMLAND);
    }

    /** 1.12 nether soil of the generators: netherrack, soul sand, tainted soil. */
    public static boolean isNetherSoil(BlockState state) {
        return state.is(NaturaBlocks.NETHER_SAPLING_SOIL);
    }

    /** 1.12 Block.onPlantGrow: grass and farmland under a growing tree become dirt. */
    public void onPlantGrow(BlockPos pos) {
        BlockState state = this.getBlockState(pos);
        if ((state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.FARMLAND)) && this.writable(pos)) {
            this.level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** 1.12 y 0. */
    public int bottom() {
        return this.level.getMinY();
    }

    /** 1.12 y 256 (world.getActualHeight() in the overworld). */
    public int top() {
        return this.level.getMaxY() + 1;
    }

    /** 1.12 world.getWorldType() == WorldType.FLAT. */
    public boolean isFlat() {
        if (this.level instanceof ServerLevel server) {
            return server.isFlat();
        }
        return this.level instanceof WorldGenLevel gen && gen.getLevel().isFlat();
    }

    /**
     * The vanilla leaves distance (TreeFeature.updateLeaves) of every leaf this tree placed, at most 6: 1.12 leaves only
     * checked for decay once something next to them changed (CHECK_DECAY), so the far ones of a grown tree (willow
     * curtains, the hopseed crown edge) stayed until disturbed. A vanilla leaf recomputes its distance on a neighbour
     * update, so these decay then, as in 1.12.
     */
    public void finish() {
        Map<BlockPos, Integer> distance = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos pos : this.leaves.keySet()) {
            for (Direction dir : Direction.values()) {
                if (this.getBlockState(pos.relative(dir)).is(BlockTags.LOGS)) {
                    distance.put(pos, 1);
                    queue.add(pos);
                    break;
                }
            }
        }
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            int d = distance.get(pos);
            if (d >= 7) continue;
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (this.leaves.containsKey(next) && !distance.containsKey(next)) {
                    distance.put(next, d + 1);
                    queue.add(next);
                }
            }
        }
        Set<BlockPos> done = new HashSet<>();
        for (var entry : this.leaves.entrySet()) {
            BlockState current = this.getBlockState(entry.getKey());
            if (current.getBlock() instanceof LeavesBlock && current.hasProperty(LeavesBlock.DISTANCE) && done.add(entry.getKey())) {
                int d = Math.min(6, distance.getOrDefault(entry.getKey(), 7));
                this.level.setBlock(entry.getKey(), current.setValue(LeavesBlock.DISTANCE, d), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }
}

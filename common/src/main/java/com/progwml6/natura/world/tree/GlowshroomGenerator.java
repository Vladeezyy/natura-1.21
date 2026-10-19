package com.progwml6.natura.world.tree;

import java.util.Random;

import com.progwml6.natura.common.block.LargeGlowshroomBlock;
import com.progwml6.natura.common.block.LargeGlowshroomBlock.Variant;
import com.progwml6.natura.common.block.TaintedSoilBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The 1.12 huge glowshroom generators: green (a flat cap 7 wide on a 4-6 stem), blue (a cap like the vanilla brown
 * mushroom's, stepped), purple (a 1-4 stem under a rounded cap). They need clear space and mycelium, netherrack, soul
 * sand or tainted soil below.
 */
public class GlowshroomGenerator {
    private final String color;
    private final BlockState glowshroom;

    public GlowshroomGenerator(String color, BlockState glowshroom) {
        this.color = color;
        this.glowshroom = glowshroom;
    }

    private BlockState variant(Variant v) {
        return this.glowshroom.setValue(LargeGlowshroomBlock.VARIANT, v);
    }

    public void generateShroom(Random random, TreeWorld world, BlockPos pos) {
        int height = this.color.equals("purple") ? random.nextInt(4) + 1 : random.nextInt(3) + 4;
        boolean flag = true;
        if (pos.getY() < world.bottom() + 1 || pos.getY() + height + 1 >= world.top()) {
            return;
        }
        for (int j = pos.getY(); j <= pos.getY() + 1 + height; ++j) {
            int k = 3;
            if (j <= pos.getY() + 3) {
                k = 0;
            }
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (int l = pos.getX() - k; l <= pos.getX() + k && flag; ++l) {
                for (int i1 = pos.getZ() - k; i1 <= pos.getZ() + k && flag; ++i1) {
                    if (j >= world.bottom() && j < world.top()) {
                        BlockState state = world.getBlockState(mutable.set(l, j, i1));
                        if (!TreeWorld.isAir(state) && !TreeWorld.isLeaves(state)) {
                            flag = false;
                        }
                    } else {
                        flag = false;
                    }
                }
            }
        }
        if (!flag) {
            return;
        }
        BlockState below = world.getBlockState(pos.below());
        if (!below.is(Blocks.MYCELIUM) && !below.is(Blocks.NETHERRACK) && !below.is(Blocks.SOUL_SAND) && !(below.getBlock() instanceof TaintedSoilBlock)) {
            return;
        }
        switch (this.color) {
            case "green" -> this.green(world, pos, height);
            case "blue" -> this.blue(world, pos, height);
            default -> this.purple(world, pos, height);
        }
        for (int i3 = 0; i3 < height; ++i3) {
            this.place(world, pos.above(i3), Variant.STEM);
        }
    }

    private void place(TreeWorld world, BlockPos pos, Variant v) {
        if (world.canBeReplacedByLeaves(world.getBlockState(pos), pos)) {
            world.setBlockState(pos, this.variant(v));
        }
    }

    private static int capMeta(int l1, int i2, int k3, int l3, int j1, int k1) {
        int j2 = 5;
        if (l1 == k3) {
            --j2;
        } else if (l1 == l3) {
            ++j2;
        }
        if (i2 == j1) {
            j2 -= 3;
        } else if (i2 == k1) {
            j2 += 3;
        }
        return j2;
    }

    private void blue(TreeWorld world, BlockPos pos, int height) {
        int k2 = pos.getY() + height - 3;
        for (int l2 = k2; l2 <= pos.getY() + height; ++l2) {
            int j3 = 1;
            if (l2 < pos.getY() + height) {
                ++j3;
            }
            int k3 = pos.getX() - j3, l3 = pos.getX() + j3, j1 = pos.getZ() - j3, k1 = pos.getZ() + j3;
            for (int l1 = k3; l1 <= l3; ++l1) {
                for (int i2 = j1; i2 <= k1; ++i2) {
                    Variant v = Variant.byMetadata(capMeta(l1, i2, k3, l3, j1, k1));
                    if (v == Variant.CENTER && l2 < pos.getY() + height) {
                        v = Variant.ALL_INSIDE;
                    }
                    if (pos.getY() >= pos.getY() + height - 1 || v != Variant.ALL_INSIDE) {
                        this.place(world, new BlockPos(l1, l2, i2), v);
                    }
                }
            }
        }
    }

    private void green(TreeWorld world, BlockPos pos, int height) {
        int k2 = pos.getY() + height;
        for (int l2 = k2; l2 <= pos.getY() + height; ++l2) {
            int j3 = 3;
            int k3 = pos.getX() - j3, l3 = pos.getX() + j3, j1 = pos.getZ() - j3, k1 = pos.getZ() + j3;
            for (int l1 = k3; l1 <= l3; ++l1) {
                for (int i2 = j1; i2 <= k1; ++i2) {
                    Variant v = Variant.byMetadata(capMeta(l1, i2, k3, l3, j1, k1));
                    if (l2 < pos.getY() + height) {
                        if ((l1 == k3 || l1 == l3) && (i2 == j1 || i2 == k1)) {
                            continue;
                        }
                        v = corner(v, l1, i2, pos, j3, 1, k3, l3, j1, k1);
                    }
                    if (v == Variant.CENTER && l2 < pos.getY() + height) {
                        v = Variant.ALL_INSIDE;
                    }
                    if (pos.getY() >= pos.getY() + height - 1 || v != Variant.ALL_INSIDE) {
                        this.place(world, new BlockPos(l1, l2, i2), v);
                    }
                }
            }
        }
    }

    private void purple(TreeWorld world, BlockPos pos, int height) {
        int k2 = pos.getY() + height - 1;
        for (int l2 = k2; l2 <= pos.getY() + height; ++l2) {
            int j3 = 2;
            if (l2 < pos.getY() + height) {
                ++j3;
            }
            int k3 = pos.getX() - j3, l3 = pos.getX() + j3, j1 = pos.getZ() - j3, k1 = pos.getZ() + j3;
            for (int l1 = k3; l1 <= l3; ++l1) {
                for (int i2 = j1; i2 <= k1; ++i2) {
                    Variant v = Variant.byMetadata(capMeta(l1, i2, k3, l3, j1, k1));
                    if (l2 < pos.getY() + height) {
                        int swap = 2;
                        if ((l1 == k3 || l1 == l3) && (i2 == j1 || i2 == k1)) {
                            continue;
                        }
                        if ((l1 == k3 + 1 || l1 == l3 - 1) && (i2 == j1 + 1 || i2 == k1 - 1)) {
                            continue;
                        }
                        v = corner(v, l1, i2, pos, j3, swap, k3, l3, j1, k1);
                    }
                    if (v == Variant.CENTER && l2 < pos.getY() + height) {
                        v = Variant.ALL_INSIDE;
                    }
                    if (pos.getY() >= pos.getY() + height - 1 || v != Variant.ALL_INSIDE) {
                        this.place(world, new BlockPos(l1, l2, i2), v);
                    }
                }
            }
        }
        this.place(world, pos.offset(-2, height - 1, -2), Variant.NORTH_WEST);
        this.place(world, pos.offset(2, height - 1, -2), Variant.NORTH_EAST);
        this.place(world, pos.offset(-2, height - 1, 2), Variant.SOUTH_WEST);
        this.place(world, pos.offset(2, height - 1, 2), Variant.SOUTH_EAST);
    }

    /** The cap corner pieces of the lower ring (1.12: the same eight checks in green and purple). */
    private static Variant corner(Variant v, int l1, int i2, BlockPos pos, int j3, int swap, int k3, int l3, int j1, int k1) {
        if (l1 == pos.getX() - (j3 - swap) && i2 == j1) v = Variant.NORTH_WEST;
        if (l1 == k3 && i2 == pos.getZ() - (j3 - swap)) v = Variant.NORTH_WEST;
        if (l1 == pos.getX() + (j3 - swap) && i2 == j1) v = Variant.NORTH_EAST;
        if (l1 == l3 && i2 == pos.getZ() - (j3 - swap)) v = Variant.NORTH_EAST;
        if (l1 == pos.getX() - (j3 - swap) && i2 == k1) v = Variant.SOUTH_WEST;
        if (l1 == k3 && i2 == pos.getZ() + (j3 - swap)) v = Variant.SOUTH_WEST;
        if (l1 == pos.getX() + (j3 - swap) && i2 == k1) v = Variant.SOUTH_EAST;
        if (l1 == l3 && i2 == pos.getZ() + (j3 - swap)) v = Variant.SOUTH_EAST;
        return v;
    }
}

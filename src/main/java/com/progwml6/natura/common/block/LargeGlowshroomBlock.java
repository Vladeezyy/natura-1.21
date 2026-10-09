package com.progwml6.natura.common.block;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 1.12 BlockNetherLargeGlowshroom: a huge glowshroom piece (the 1.12 mushroom-block variants). Breaks instantly (1.12 set
 * no hardness), glows (9), only its top 1/16 is solid when nothing glowshroom is above, and it slows what falls into it
 * (fall speed x0.25, fall distance -0.25, sideways x0.25 when not falling) and gives mobs (not players) nausea. Drops 0-2
 * small glowshrooms, or itself with Silk Touch.
 */
public class LargeGlowshroomBlock extends Block {
    public enum Variant implements StringRepresentable {
        NORTH_WEST, NORTH, NORTH_EAST, WEST, CENTER, EAST, SOUTH_WEST, SOUTH, SOUTH_EAST, STEM, ALL_INSIDE, ALL_OUTSIDE, ALL_STEM;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.US);
        }

        /** 1.12 BlockHugeMushroom.EnumType.byMetadata: 1-9 the cap pieces, 10 stem, 0 all inside, 14 all outside, 15 all stem. */
        public static Variant byMetadata(int meta) {
            return switch (meta) {
                case 1 -> NORTH_WEST;
                case 2 -> NORTH;
                case 3 -> NORTH_EAST;
                case 4 -> WEST;
                case 5 -> CENTER;
                case 6 -> EAST;
                case 7 -> SOUTH_WEST;
                case 8 -> SOUTH;
                case 9 -> SOUTH_EAST;
                case 10 -> STEM;
                case 14 -> ALL_OUTSIDE;
                case 15 -> ALL_STEM;
                default -> ALL_INSIDE;
            };
        }
    }

    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);
    private static final VoxelShape TOP = Block.box(0, 15, 0, 16, 16, 16);
    private final Supplier<? extends ItemLike> small;

    public LargeGlowshroomBlock(Supplier<? extends ItemLike> small, Properties properties) {
        super(properties);
        this.small = small;
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, Variant.ALL_INSIDE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return level.getBlockState(pos.above()).getBlock() instanceof LargeGlowshroomBlock ? Shapes.empty() : TOP;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, net.minecraft.core.Direction direction) {
        // 1.12: blue and purple pieces hide the faces next to them
        String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(adjacent.getBlock()).getPath();
        return id.equals("blue_large_glowshroom") || id.equals("purple_large_glowshroom") || super.skipRendering(state, adjacent, direction);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y < 0) {
            motion = new Vec3(motion.x, motion.y * 0.25F, motion.z);
        }
        entity.fallDistance -= 0.25F;
        if (motion.y == 0.0F) {
            motion = new Vec3(motion.x * 0.25F, motion.y, motion.z * 0.25F);
        }
        entity.setDeltaMovement(motion);
        if (entity instanceof Mob mob) {
            mob.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(this.small.get());
    }
}

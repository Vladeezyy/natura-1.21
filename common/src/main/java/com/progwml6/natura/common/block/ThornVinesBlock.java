package com.progwml6.natura.common.block;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 BlockNetherThornVines: vines that prick (1 damage, cactus or lava) what moves through them, 1 in 30 rolled with a
 * Random made from the same fixed seed every time, so the roll always comes out the same; kept as in 1.12.
 */
public class ThornVinesBlock extends VineBlock {
    /** The 1.12 roll, the same every time: computed once instead of a new Random per entity and tick. */
    private static final boolean PRICKS;
    private static final boolean CACTUS;

    static {
        Random random = new Random();
        random.setSeed(2 ^ 16 + 2 ^ 8 + (4 * 3 * 271));
        PRICKS = random.nextInt(30) == 0;
        CACTUS = PRICKS && random.nextBoolean();
    }

    public ThornVinesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (PRICKS && !(entity instanceof ItemEntity) && !(entity instanceof Ghast)) {
            DamageSource source = CACTUS ? level.damageSources().cactus() : level.damageSources().lava();
            entity.hurt(source, 1);
        }
    }
}

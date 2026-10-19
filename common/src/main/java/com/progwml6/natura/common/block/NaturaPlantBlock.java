package com.progwml6.natura.common.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.block.BushBlock;

/** 1.12 BlockBush without anything else (bluebells): plains soil, no collision. */
public class NaturaPlantBlock extends BushBlock {
    public NaturaPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        throw new UnsupportedOperationException();
    }
}

package com.progwml6.natura.forge;

import com.progwml6.natura.common.block.TaintedSoilBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

/** Tainted soil on Forge: it sustains the nether plants through IForgeBlock.canSustainPlant. */
public class ForgeTaintedSoilBlock extends TaintedSoilBlock {
    public ForgeTaintedSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, IPlantable plantable) {
        BlockState plant = plantable.getPlant(level, pos.relative(facing));
        return sustains(plant) || super.canSustainPlant(state, level, pos, facing, plantable);
    }
}

package com.progwml6.natura.common.block;

import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 TileEntityNetherrackFurnace: the vanilla furnace with every fuel burning twice as long (getItemBurnTime x2). */
public class NetherrackFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public NetherrackFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(NaturaBlocks.NETHERRACK_FURNACE_BE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.furnace");
    }

    @Override
    protected int getBurnDuration(ItemStack fuel) {
        return super.getBurnDuration(fuel) * 2;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory player) {
        return new FurnaceMenu(id, player, this, this.dataAccess);
    }
}

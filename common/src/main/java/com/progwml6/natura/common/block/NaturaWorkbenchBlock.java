package com.progwml6.natura.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 BlockOverworldWorkbenches / BlockNetherWorkbenches: a crafting table of Natura wood (the vanilla 3x3 crafting GUI). */
public class NaturaWorkbenchBlock extends CraftingTableBlock {
    private static final Component TITLE = Component.translatable("container.crafting");

    public NaturaWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
        return new SimpleMenuProvider((id, inventory, player) -> new CraftingMenu(id, inventory, access) {
            @Override
            public boolean stillValid(Player p) {
                return stillValid(access, p, NaturaWorkbenchBlock.this);
            }
        }, TITLE);
    }
}

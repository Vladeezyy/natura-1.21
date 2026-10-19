package com.progwml6.natura.common.item;

import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** Block item with the Mantle ItemBlockMeta tooltip: the grey "&lt;key&gt;.tooltip" lines when the language has them. */
public class NaturaBlockItem extends BlockItem {
    public NaturaBlockItem(Block block, Properties properties) {
        super(block, properties.useBlockDescriptionPrefix());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        NaturaItem.addTooltip(this.getDescriptionId(), tooltip);
    }
}

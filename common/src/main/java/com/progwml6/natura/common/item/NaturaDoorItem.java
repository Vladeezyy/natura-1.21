package com.progwml6.natura.common.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** The door item (1.12 overworld_doors / nether_doors metas), with the Mantle tooltip. */
public class NaturaDoorItem extends DoubleHighBlockItem {
    public NaturaDoorItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        NaturaItem.addTooltip(this.getDescriptionId(), tooltip);
    }
}

package com.progwml6.natura.common.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Item with the Mantle tooltip ("&lt;key&gt;.tooltip", lines split on \n, grey). */
public class NaturaItem extends Item {
    public NaturaItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.level.Level context, List<Component> tooltip, TooltipFlag flag) {
        addTooltip(this.getDescriptionId(), tooltip);
    }

    public static void addTooltip(String descriptionId, List<Component> tooltip) {
        String key = descriptionId + ".tooltip";
        if (Language.getInstance().has(key)) {
            for (String line : Language.getInstance().getOrDefault(key).split("\n")) {
                tooltip.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}

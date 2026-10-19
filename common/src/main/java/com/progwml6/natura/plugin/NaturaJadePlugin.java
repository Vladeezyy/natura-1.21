package com.progwml6.natura.plugin;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.block.NaturaCropBlock;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

/** The 1.12 Waila plugin: the barley and cotton crops show the barley and cotton items and their growth. */
@WailaPlugin
public class NaturaJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockIcon(Crop.INSTANCE, NaturaCropBlock.class);
        registration.registerBlockComponent(Crop.INSTANCE, NaturaCropBlock.class);
    }

    public enum Crop implements IBlockComponentProvider {
        INSTANCE;

        private static final Identifier UID = Identifier.fromNamespaceAndPath(Natura.MODID, "crop");

        @Override
        public Element getIcon(BlockAccessor accessor, IPluginConfig config, Element currentIcon) {
            if (accessor.getBlock() instanceof NaturaCropBlock crop) {
                return JadeUI.item(new ItemStack(crop.crop()));
            }
            return null;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlock() instanceof NaturaCropBlock crop)) {
                return;
            }
            tooltip.replace(JadeIds.CORE_OBJECT_NAME, IThemeHelper.get().title(new ItemStack(crop.crop()).getHoverName()));
            float growth = crop.getAge(accessor.getBlockState()) / (float) crop.getMaxAge() * 100.0F;
            if (growth < 100.0F) {
                tooltip.add(Component.translatable("tooltip.jade.crop_growth", IThemeHelper.get().info(String.format("%.0f%%", growth))));
            } else {
                tooltip.add(Component.translatable("tooltip.jade.crop_growth", IThemeHelper.get().success(Component.translatable("tooltip.jade.crop_mature"))));
            }
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}

package com.progwml6.natura;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.common.NaturaItems;
import com.progwml6.natura.common.NaturaTabs;

import com.progwml6.natura.gametest.NaturaGameTests;

import com.progwml6.natura.common.NaturaConfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@Mod(Natura.MODID)
public class Natura {
    public static final String MODID = "natura";
    public static final Logger LOG = LogUtils.getLogger();

    public Natura(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, NaturaConfig.SPEC);
        NaturaBlocks.init();
        NaturaItems.init();
        NaturaBlocks.BLOCKS.register(modBus);
        NaturaBlocks.ITEMS.register(modBus);
        NaturaTabs.TABS.register(modBus);
        modBus.addListener(this::setup);
        modBus.addListener(RegisterGameTestsEvent.class, event -> event.register(NaturaGameTests.class));
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(NaturaBlocks::registerFlammability);
    }
}

package com.progwml6.natura;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.progwml6.natura.common.recipe.ConfigOptionEnabledCondition;
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
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(Natura.MODID)
public class Natura {
    public static final String MODID = "natura";
    public static final Logger LOG = LogUtils.getLogger();
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MODID);

    public Natura(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, NaturaConfig.SPEC);
        NaturaBlocks.init();
        NaturaItems.init();
        com.progwml6.natura.tools.NaturaTools.init();
        com.progwml6.natura.tools.NaturaTools.ARMOR.register(modBus);
        com.progwml6.natura.entities.NaturaEntities.register(modBus);
        NaturaBlocks.BLOCKS.register(modBus);
        NaturaBlocks.ITEMS.register(modBus);
        NaturaBlocks.BLOCK_ENTITIES.register(modBus);
        com.progwml6.natura.world.gen.NaturaFeatures.FEATURES.register(modBus);
        modBus.addListener(net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent.class,
                event -> event.modify(net.minecraft.world.level.block.entity.BlockEntityType.HOPPER, NaturaBlocks.BLAZE_HOPPER.get()));
        NaturaTabs.TABS.register(modBus);
        CONDITIONS.register("config_option_enabled", () -> ConfigOptionEnabledCondition.CODEC);
        CONDITIONS.register(modBus);
        modBus.addListener(this::setup);
        modBus.addListener(RegisterGameTestsEvent.class, event -> event.register(NaturaGameTests.class));
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(NaturaBlocks::registerFlammability);
    }
}

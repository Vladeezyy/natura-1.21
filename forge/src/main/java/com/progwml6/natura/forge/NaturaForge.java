package com.progwml6.natura.forge;

import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.dev.DevBench;
import com.progwml6.natura.entities.NaturaEntities;
import com.progwml6.natura.gametest.NaturaGameTests;
import com.progwml6.natura.world.WorldEvents;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** The Forge entry point: the common registrations through deferred registers, the events wired to the common code. */
@Mod(Natura.MODID)
public class NaturaForge {
    public NaturaForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, NaturaForgeConfig.build());
        ForgePlatform.init(modBus);
        Natura.init();
        CraftingHelper.register(ConfigCondition.Serializer.INSTANCE);

        // the blaze hopper is a hopper block entity (NeoForge 1.21: BlockEntityTypeAddBlocksEvent; here as on Fabric)
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            NaturaBlocks.registerFlammability();
            ForgePlatform.addHopperBlock();
        }));
        modBus.addListener((EntityAttributeCreationEvent event) -> NaturaEntities.attributes(event::put));
        modBus.addListener((SpawnPlacementRegisterEvent event) -> NaturaEntities.placements(new NaturaEntities.PlacementRegistrar() {
            @Override
            public <T extends Mob> void register(net.minecraft.world.entity.EntityType<T> type, net.minecraft.world.entity.SpawnPlacements.Type placement,
                                                 net.minecraft.world.level.levelgen.Heightmap.Types heightmap,
                                                 net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> predicate) {
                event.register(type, placement, heightmap, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
            }
        }));
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
                NaturaEntities.spawnEggs().forEach(egg -> event.accept(egg.get()));
            }
        });
        modBus.addListener((RegisterGameTestsEvent event) -> event.register(NaturaGameTests.class));

        MinecraftForge.EVENT_BUS.addListener((EntityJoinLevelEvent event) -> {
            if (!event.getLevel().isClientSide) {
                WorldEvents.onJoin(event.getEntity());
            }
        });
        if (DevBench.active()) {
            MinecraftForge.EVENT_BUS.addListener((ServerStartedEvent event) -> DevBench.start(event.getServer()));
            MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
                if (event.phase == TickEvent.Phase.START) {
                    DevBench.tickStart();
                } else {
                    DevBench.tick(event.getServer());
                }
            });
        }
        if (FMLEnvironment.dist.isClient()) {
            NaturaForgeClient.init(modBus);
        }
    }
}

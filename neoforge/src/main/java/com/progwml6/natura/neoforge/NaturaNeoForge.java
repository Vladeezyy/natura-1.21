package com.progwml6.natura.neoforge;

import com.mojang.serialization.MapCodec;
import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.dev.DevBench;
import com.progwml6.natura.entities.NaturaEntities;
import com.progwml6.natura.gametest.NaturaGameTests;
import com.progwml6.natura.world.WorldEvents;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** The NeoForge entry point: the common registrations through deferred registers, the events wired to the common code. */
@Mod(Natura.MODID)
public class NaturaNeoForge {
    public NaturaNeoForge(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, NaturaNeoForgeConfig.build());
        NeoForgePlatform.init(modBus);
        Natura.init();
        NeoForgePlatform.deferred(NeoForgeRegistries.Keys.CONDITION_CODECS).register(
                com.progwml6.natura.common.recipe.ConfigOptionEnabledCondition.ID, () -> (MapCodec<? extends ICondition>) ConfigCondition.CODEC);

        modBus.addListener(BlockEntityTypeAddBlocksEvent.class, event -> event.modify(BlockEntityType.HOPPER, NaturaBlocks.BLAZE_HOPPER.get()));
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(NaturaBlocks::registerFlammability));
        modBus.addListener(EntityAttributeCreationEvent.class, event -> NaturaEntities.attributes(event::put));
        modBus.addListener(RegisterSpawnPlacementsEvent.class, event -> NaturaEntities.placements(new NaturaEntities.PlacementRegistrar() {
            @Override
            public <T extends Mob> void register(net.minecraft.world.entity.EntityType<T> type, net.minecraft.world.entity.SpawnPlacementType placement,
                                                 net.minecraft.world.level.levelgen.Heightmap.Types heightmap,
                                                 net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> predicate) {
                event.register(type, placement, heightmap, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        }));
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
                NaturaEntities.spawnEggs().forEach(egg -> event.accept(egg.get()));
            }
        });
        modBus.addListener(RegisterGameTestsEvent.class, event -> event.register(NaturaGameTests.class));

        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, event -> {
            if (!event.getLevel().isClientSide) {
                WorldEvents.onJoin(event.getEntity());
            }
        });
        if (DevBench.active()) {
            NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> DevBench.start(event.getServer()));
            NeoForge.EVENT_BUS.addListener(ServerTickEvent.Pre.class, event -> DevBench.tickStart());
            NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> DevBench.tick(event.getServer()));
        }
        if (FMLEnvironment.dist.isClient()) {
            NaturaNeoForgeClient.init(modBus);
        }
    }
}

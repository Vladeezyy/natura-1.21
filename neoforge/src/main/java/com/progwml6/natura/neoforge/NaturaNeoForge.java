package com.progwml6.natura.neoforge;

import net.minecraft.world.level.block.Rotation;

import net.minecraft.resources.ResourceKey;

import net.minecraft.resources.Identifier;

import net.minecraft.gametest.framework.TestEnvironmentDefinition;

import net.minecraft.gametest.framework.TestData;

import net.minecraft.gametest.framework.GameTestHelper;

import net.minecraft.gametest.framework.FunctionGameTestInstance;

import net.minecraft.core.registries.Registries;

import net.minecraft.core.Holder;

import java.util.function.Consumer;

import java.util.Map;

import java.util.List;

import java.util.HashMap;

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

        modBus.addListener(BlockEntityTypeAddBlocksEvent.class, event -> event.modify(net.minecraft.world.level.block.entity.BlockEntityTypes.HOPPER, NaturaBlocks.BLAZE_HOPPER.get()));
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
        NaturaGameTests.tests().forEach((name, body) -> NeoForgePlatform.<Consumer<GameTestHelper>>deferred(Registries.TEST_FUNCTION).register(name, () -> body));
        modBus.addListener(RegisterGameTestsEvent.class, NaturaNeoForge::registerTests);

        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, event -> {
            if (!event.getLevel().isClientSide()) {
                WorldEvents.onJoin(event.getEntity());
            }
        });
        if (DevBench.active()) {
            NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> DevBench.start(event.getServer()));
            NeoForge.EVENT_BUS.addListener(ServerTickEvent.Pre.class, event -> DevBench.tickStart());
            NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> DevBench.tick(event.getServer()));
        }
        if (FMLEnvironment.getDist().isClient()) {
            NaturaNeoForgeClient.init(modBus);
        }
    }

    /** 26.x game tests: a test instance per common test; the trees each in their own environment (their own batch). */
    private static void registerTests(RegisterGameTestsEvent event) {
        Map<String, Holder<TestEnvironmentDefinition<?>>> environments = new HashMap<>();
        NaturaGameTests.tests().keySet().forEach(name -> {
            String batch = NaturaGameTests.batch(name);
            var environment = environments.computeIfAbsent(batch, b -> event.registerEnvironment(id(b), new TestEnvironmentDefinition.AllOf(List.of())));
            event.registerTest(id(name), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                    new TestData<>(environment, NaturaGameTests.structure(), 100, 0, true, Rotation.NONE, false, 1, 1, !batch.equals("default"), 0)));
        });
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(Natura.MODID, name);
    }
}

package com.progwml6.natura.fabric;

import net.minecraft.resources.RegistryOps;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;
import com.progwml6.natura.Natura;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.common.recipe.ConfigOptionEnabledCondition;
import com.progwml6.natura.dev.DevBench;
import com.progwml6.natura.entities.NaturaEntities;
import com.progwml6.natura.fabric.mixin.SpawnPlacementsInvoker;
import com.progwml6.natura.world.WorldEvents;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;


/** The Fabric entry point: the common registrations (right away) and the Fabric API events wired to the common code. */
public class NaturaFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricConfig.load();
        Natura.init();
        ResourceConditions.register(ConfigCondition.TYPE);
        NaturaBlocks.registerFlammability();
        // the blaze hopper uses the vanilla hopper block entity (NeoForge: BlockEntityTypeAddBlocksEvent)
        ((FabricBlockEntityType) net.minecraft.world.level.block.entity.BlockEntityTypes.HOPPER).addValidBlock(NaturaBlocks.BLAZE_HOPPER.get());
        NaturaEntities.attributes(FabricDefaultAttributeRegistry::register);
        NaturaEntities.placements(new NaturaEntities.PlacementRegistrar() {
            @Override
            public <T extends Mob> void register(net.minecraft.world.entity.EntityType<T> type, net.minecraft.world.entity.SpawnPlacementType placement,
                                                 net.minecraft.world.level.levelgen.Heightmap.Types heightmap,
                                                 net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> predicate) {
                SpawnPlacementsInvoker.invokeRegister(type, placement, heightmap, predicate);
            }
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> NaturaEntities.spawnEggs().forEach(egg -> output.accept(egg.get())));
        FabricBiomeModifiers.register();
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> WorldEvents.onJoin(entity));
        if (DevBench.active()) {
            ServerLifecycleEvents.SERVER_STARTED.register(DevBench::start);
            ServerTickEvents.START_SERVER_TICK.register(server -> DevBench.tickStart());
            ServerTickEvents.END_SERVER_TICK.register(DevBench::tick);
        }
    }

    /** natura:config_option_enabled in fabric:load_conditions (recipes). */
    record ConfigCondition(String setting) implements ResourceCondition {
        static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf(ConfigOptionEnabledCondition.FIELD).forGetter(ConfigCondition::setting)).apply(i, ConfigCondition::new));
        static final ResourceConditionType<ConfigCondition> TYPE = ResourceConditionType.create(
                Identifier.fromNamespaceAndPath(Natura.MODID, ConfigOptionEnabledCondition.ID), CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(RegistryOps.RegistryInfoLookup registryLookup) {
            return ConfigOptionEnabledCondition.test(this.setting);
        }
    }
}

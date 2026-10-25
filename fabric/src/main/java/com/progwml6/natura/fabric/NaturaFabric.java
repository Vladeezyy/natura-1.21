package com.progwml6.natura.fabric;

import com.google.common.collect.ImmutableSet;
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
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;


/** The Fabric entry point: the common registrations (right away) and the Fabric API events wired to the common code. */
public class NaturaFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricConfig.load();
        Natura.init();
        // natura:config_option_enabled in fabric:load_conditions (recipes)
        ResourceConditions.register(new ResourceLocation(Natura.MODID, ConfigOptionEnabledCondition.ID),
                json -> ConfigOptionEnabledCondition.test(GsonHelper.getAsString(json, ConfigOptionEnabledCondition.FIELD)));
        NaturaBlocks.registerFlammability();
        // the blaze hopper uses the vanilla hopper block entity (NeoForge: BlockEntityTypeAddBlocksEvent)
        BlockEntityType.HOPPER.validBlocks = ImmutableSet.<Block>builder().addAll(BlockEntityType.HOPPER.validBlocks)
                .add(NaturaBlocks.BLAZE_HOPPER.get()).build();
        NaturaEntities.attributes(FabricDefaultAttributeRegistry::register);
        NaturaEntities.placements(new NaturaEntities.PlacementRegistrar() {
            @Override
            public <T extends Mob> void register(net.minecraft.world.entity.EntityType<T> type, net.minecraft.world.entity.SpawnPlacements.Type placement,
                                                 net.minecraft.world.level.levelgen.Heightmap.Types heightmap,
                                                 net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> predicate) {
                SpawnPlacementsInvoker.invokeRegister(type, placement, heightmap, predicate);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> NaturaEntities.spawnEggs().forEach(egg -> entries.accept(egg.get())));
        FabricBiomeModifiers.register();
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> WorldEvents.onJoin(entity));
        if (DevBench.active()) {
            ServerLifecycleEvents.SERVER_STARTED.register(DevBench::start);
            ServerTickEvents.START_SERVER_TICK.register(server -> DevBench.tickStart());
            ServerTickEvents.END_SERVER_TICK.register(DevBench::tick);
        }
    }
}

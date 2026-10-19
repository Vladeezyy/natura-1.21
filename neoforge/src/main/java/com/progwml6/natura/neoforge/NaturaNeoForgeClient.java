package com.progwml6.natura.neoforge;

import com.progwml6.natura.Natura;

import net.minecraft.resources.Identifier;

import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

import com.progwml6.natura.client.DevScene;
import com.progwml6.natura.client.GrassColorizer;
import com.progwml6.natura.client.NaturaClient;
import com.progwml6.natura.client.WorldgenScene;
import com.progwml6.natura.client.entity.NaturaEntityRenderers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

/** The client events on NeoForge, wired to the common client code. */
final class NaturaNeoForgeClient {
    private NaturaNeoForgeClient() {}

    static void init(IEventBus modBus) {
        modBus.addListener(RegisterColorHandlersEvent.BlockTintSources.class, event -> NaturaClient.blockColors((tint, block) -> event.register(java.util.List.of(tint), block)));
        modBus.addListener(AddClientReloadListenersEvent.class, event -> event.addListener(Identifier.fromNamespaceAndPath(Natura.MODID, "grass_colorizer"), new GrassColorizer()));
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class,
                event -> NaturaEntityRenderers.layers((layer, definition) -> event.registerLayerDefinition(layer, definition)));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> NaturaEntityRenderers.renderers(new NaturaEntityRenderers.Renderers() {
            @Override
            public <E extends Entity> void register(EntityType<? extends E> type, EntityRendererProvider<E> provider) {
                event.registerEntityRenderer(type, provider);
            }
        }));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            DevScene.onTick();
            WorldgenScene.onTick();
        });
    }
}

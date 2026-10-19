package com.progwml6.natura.neoforge;

import com.progwml6.natura.client.DevScene;
import com.progwml6.natura.client.GrassColorizer;
import com.progwml6.natura.client.NaturaClient;
import com.progwml6.natura.client.WorldgenScene;
import com.progwml6.natura.client.entity.NaturaEntityRenderers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

/** The client events on NeoForge, wired to the common client code. */
final class NaturaNeoForgeClient {
    private NaturaNeoForgeClient() {}

    static void init(IEventBus modBus) {
        modBus.addListener(RegisterColorHandlersEvent.Block.class, event -> NaturaClient.blockColors((color, block) -> event.register(color, block)));
        modBus.addListener(RegisterColorHandlersEvent.Item.class, event -> NaturaClient.itemColors((color, item) -> event.register(color, item)));
        modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(NaturaClient::setup));
        modBus.addListener(RegisterClientReloadListenersEvent.class, event -> event.registerReloadListener(new GrassColorizer()));
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

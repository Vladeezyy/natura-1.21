package com.progwml6.natura.forge;

import com.progwml6.natura.client.DevScene;
import com.progwml6.natura.client.GrassColorizer;
import com.progwml6.natura.client.NaturaClient;
import com.progwml6.natura.client.WorldgenScene;
import com.progwml6.natura.client.entity.NaturaEntityRenderers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** The client events on Forge, wired to the common client code. */
final class NaturaForgeClient {
    private NaturaForgeClient() {}

    static void init(IEventBus modBus) {
        modBus.addListener((RegisterColorHandlersEvent.Block event) -> NaturaClient.blockColors((color, block) -> event.register(color, block)));
        modBus.addListener((RegisterColorHandlersEvent.Item event) -> NaturaClient.itemColors((color, item) -> event.register(color, item)));
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(NaturaClient::setup));
        modBus.addListener((RegisterClientReloadListenersEvent event) -> event.registerReloadListener(new GrassColorizer()));
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) ->
                NaturaEntityRenderers.layers((layer, definition) -> event.registerLayerDefinition(layer, definition)));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> NaturaEntityRenderers.renderers(new NaturaEntityRenderers.Renderers() {
            @Override
            public <E extends Entity> void register(EntityType<? extends E> type, EntityRendererProvider<E> provider) {
                event.registerEntityRenderer(type, provider);
            }
        }));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                DevScene.onTick();
                WorldgenScene.onTick();
            }
        });
    }
}

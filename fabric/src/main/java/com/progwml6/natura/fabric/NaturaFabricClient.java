package com.progwml6.natura.fabric;

import java.util.List;

import com.progwml6.natura.Natura;
import com.progwml6.natura.client.DevScene;
import com.progwml6.natura.client.GrassColorizer;
import com.progwml6.natura.client.NaturaClient;
import com.progwml6.natura.client.WorldgenScene;
import com.progwml6.natura.client.entity.NaturaEntityRenderers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/** The client side on Fabric: the common client registrations (26.x: block render layers come from the textures). */
public class NaturaFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NaturaClient.blockColors((tint, block) -> BlockColorRegistry.register(List.of(tint), block));
        NaturaEntityRenderers.layers((layer, definition) -> ModelLayerRegistry.registerModelLayer(layer, definition::get));
        NaturaEntityRenderers.renderers(new NaturaEntityRenderers.Renderers() {
            @Override
            public <E extends Entity> void register(EntityType<? extends E> type, EntityRendererProvider<E> provider) {
                EntityRendererRegistry.register(type, provider);
            }
        });
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Identifier.fromNamespaceAndPath(Natura.MODID, "grass_colorizer"), new GrassColorizer());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DevScene.onTick();
            WorldgenScene.onTick();
        });
    }
}

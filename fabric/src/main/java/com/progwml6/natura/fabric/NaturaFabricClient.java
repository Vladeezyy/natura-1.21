package com.progwml6.natura.fabric;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.progwml6.natura.Natura;
import com.progwml6.natura.client.DevScene;
import com.progwml6.natura.client.GrassColorizer;
import com.progwml6.natura.client.NaturaClient;
import com.progwml6.natura.client.WorldgenScene;
import com.progwml6.natura.client.entity.NaturaEntityRenderers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

/** The client side on Fabric: the common client registrations, and the block render layers NeoForge reads from the models. */
public class NaturaFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NaturaClient.blockColors((color, block) -> ColorProviderRegistry.BLOCK.register(color, block));
        NaturaClient.itemColors((color, item) -> ColorProviderRegistry.ITEM.register(color, item));
        NaturaClient.setup();
        NaturaEntityRenderers.layers((layer, definition) -> EntityModelLayerRegistry.registerModelLayer(layer, definition::get));
        NaturaEntityRenderers.renderers(new NaturaEntityRenderers.Renderers() {
            @Override
            public <E extends Entity> void register(EntityType<? extends E> type, EntityRendererProvider<E> provider) {
                EntityRendererRegistry.register(type, provider);
            }
        });
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new GrassColorizerListener());
        renderLayers();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DevScene.onTick();
            WorldgenScene.onTick();
        });
    }

    /**
     * The "render_type" of the Natura block models (NeoForge reads it, Fabric does not): every block whose blockstate's
     * first model asks for cutout, cutout_mipped or translucent is put in that layer.
     */
    private static void renderLayers() {
        var mod = FabricLoader.getInstance().getModContainer(Natura.MODID).orElseThrow();
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().equals(Natura.MODID)) {
                continue;
            }
            Optional<JsonObject> states = read(mod.findPath("assets/" + Natura.MODID + "/blockstates/" + id.getPath() + ".json"));
            if (states.isEmpty()) {
                continue;
            }
            List<String> models = new ArrayList<>();
            collectModels(states.get(), models);
            for (String model : models) {
                String type = renderType(mod, ResourceLocation.parse(model), 0);
                if (type != null) {
                    RenderType layer = switch (type) {
                        case "minecraft:cutout", "cutout" -> RenderType.cutout();
                        case "minecraft:cutout_mipped", "cutout_mipped" -> RenderType.cutoutMipped();
                        case "minecraft:translucent", "translucent" -> RenderType.translucent();
                        default -> null;
                    };
                    if (layer != null) {
                        BlockRenderLayerMap.INSTANCE.putBlock(block, layer);
                    }
                    break;
                }
            }
        }
    }

    private static void collectModels(JsonElement element, List<String> models) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("model") && object.get("model").isJsonPrimitive()) {
                models.add(object.get("model").getAsString());
            }
            object.entrySet().forEach(e -> collectModels(e.getValue(), models));
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(e -> collectModels(e, models));
        }
    }

    /** The model's render_type, or its parent's (Natura models only). */
    private static String renderType(net.fabricmc.loader.api.ModContainer mod, ResourceLocation model, int depth) {
        if (!model.getNamespace().equals(Natura.MODID) || depth > 8) {
            return null;
        }
        Optional<JsonObject> json = read(mod.findPath("assets/" + Natura.MODID + "/models/" + model.getPath() + ".json"));
        if (json.isEmpty()) {
            return null;
        }
        if (json.get().has("render_type")) {
            return json.get().get("render_type").getAsString();
        }
        return json.get().has("parent") ? renderType(mod, ResourceLocation.parse(json.get().get("parent").getAsString()), depth + 1) : null;
    }

    private static Optional<JsonObject> read(Optional<Path> path) {
        if (path.isEmpty() || !Files.exists(path.get())) {
            return Optional.empty();
        }
        try (Reader reader = Files.newBufferedReader(path.get())) {
            return Optional.of(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (IOException | RuntimeException e) {
            Natura.LOG.warn("Could not read {}", path.get(), e);
            return Optional.empty();
        }
    }

    /** The common GrassColorizer as a Fabric reload listener (it reads the Natura grass colour maps). */
    private static final class GrassColorizerListener implements IdentifiableResourceReloadListener {
        private final GrassColorizer colorizer = new GrassColorizer();

        @Override
        public ResourceLocation getFabricId() {
            return ResourceLocation.fromNamespaceAndPath(Natura.MODID, "grass_colorizer");
        }

        @Override
        public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepareProfiler,
                                              ProfilerFiller applyProfiler, Executor background, Executor game) {
            return this.colorizer.reload(barrier, manager, prepareProfiler, applyProfiler, background, game);
        }
    }
}

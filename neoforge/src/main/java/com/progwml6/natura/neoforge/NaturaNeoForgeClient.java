package com.progwml6.natura.neoforge;

import com.progwml6.natura.client.DevScene;
import com.progwml6.natura.client.GrassColorizer;
import com.progwml6.natura.client.NaturaClient;
import com.progwml6.natura.client.WorldgenScene;
import com.progwml6.natura.client.entity.NaturaEntityRenderers;

import com.progwml6.natura.common.NaturaBlocks;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
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
        modBus.addListener(ModelEvent.ModifyBakingResult.class, NaturaNeoForgeClient::bushRenderTypes);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            DevScene.onTick();
            WorldgenScene.onTick();
        });
    }

    /**
     * 1.12 BlockEnumBerryBush.getBlockLayer: CUTOUT_MIPPED with fancy graphics, SOLID with fast ones, like the leaves.
     * Vanilla only does that for a LeavesBlock, so the bush models in the world take the leaves' layer; the items keep
     * the cutout_mipped of the model.
     */
    private static void bushRenderTypes(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();
        NaturaBlocks.BUSHES.values().forEach(bush -> bush.get().getStateDefinition().getPossibleStates().forEach(state ->
                models.computeIfPresent(BlockModelShaper.stateToModelLocation(state), (location, model) -> new BakedModelWrapper<>(model) {
                    @Override
                    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
                        return ItemBlockRenderTypes.getRenderLayers(Blocks.OAK_LEAVES.defaultBlockState());
                    }
                })));
    }
}

package com.progwml6.natura.forge;

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
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
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
        modBus.addListener(NaturaForgeClient::bushRenderTypes);
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                DevScene.onTick();
                WorldgenScene.onTick();
            }
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
